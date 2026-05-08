import asyncio
import os
import sqlite3
import uuid
from contextlib import contextmanager
from pathlib import Path
from typing import Optional

import httpx
from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from langchain_chroma import Chroma
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_huggingface import HuggingFaceEmbeddings
from pydantic import BaseModel

load_dotenv()

app = FastAPI(title="Asana - Yoga Pose Chatbot")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

CHROMA_PATH = "../chroma_db"
DB_PATH = "sessions.db"
IMAGES_DIR = Path("static/images")
IMAGES_DIR.mkdir(parents=True, exist_ok=True)
app.mount("/images", StaticFiles(directory=str(IMAGES_DIR)), name="images")

HOST_URL = os.getenv("HOST_URL", "http://10.0.2.2:8000")
SSLIP_BASE_URL = os.getenv("SSLIP_BASE_URL", "https://35-207-202-131.sslip.io")
SSLIP_USER_ID = os.getenv("SSLIP_USER_ID", "")

SYSTEM_PROMPT = """You are Asana, a certified and highly experienced yoga teacher.
You have deep expertise in anatomy, alignment, pose modifications, and safe yoga practice.
You are speaking with yoga students — from beginners to intermediate practitioners.

When a student asks about a yoga pose for the first time, always structure your response exactly like this:

Pose Name in English
Step-by-step instructions and How long to hold

Use the provided context if it is relevant. If the context does not contain the specific pose, draw on your own extensive yoga knowledge to answer fully and accurately. Never say you don't have information about a pose.

When a student asks a follow-up question about a pose already discussed (e.g. "how long should I hold it?", "what are the benefits?", "any modifications?"), answer only the specific question concisely. Do not repeat the full pose breakdown.

If the question is not about yoga poses, say: "I specialize in yoga poses. I'd be happy to help you with any pose-related questions!"
If the user writes in Hindi or Hinglish, understand it fully but always respond in English."""

embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
db = Chroma(persist_directory=CHROMA_PATH, embedding_function=embeddings)

_api_keys = [k.strip() for k in os.getenv("GOOGLE_API_KEYS", os.getenv("GOOGLE_API_KEY", "")).split(",") if k.strip()]
_key_index = 0


def get_llm() -> ChatGoogleGenerativeAI:
    return ChatGoogleGenerativeAI(
        model="gemini-flash-latest",
        google_api_key=_api_keys[_key_index],
        temperature=0.3,
    )


def invoke_with_rotation(messages: list):
    global _key_index
    for attempt in range(len(_api_keys)):
        try:
            return get_llm().invoke(messages)
        except Exception as e:
            if "ResourceExhausted" in type(e).__name__ or "429" in str(e) or "quota" in str(e).lower():
                _key_index = (_key_index + 1) % len(_api_keys)
                if attempt < len(_api_keys) - 1:
                    continue
            raise
    raise RuntimeError("All API keys exhausted")


async def generate_image_for_pose(query: str) -> Optional[str]:
    if not SSLIP_USER_ID:
        return None
    try:
        async with httpx.AsyncClient(timeout=60) as client:
            # Generate image
            gen_resp = await client.post(
                f"{SSLIP_BASE_URL}/api/generate/image",
                headers={"X-User-Id": SSLIP_USER_ID, "Content-Type": "application/json"},
                json={"prompt": f"A person demonstrating {query} yoga pose. Clean white background, instructional illustration style, full body visible, clear alignment."}
            )
            if not gen_resp.is_success:
                return None
            file_url = gen_resp.json().get("file_url")
            if not file_url:
                return None

            # Download the SVG (requires auth header)
            dl_resp = await client.get(
                f"{SSLIP_BASE_URL}{file_url}",
                headers={"X-User-Id": SSLIP_USER_ID}
            )
            if not dl_resp.is_success:
                return None

        # Save locally and serve from our static folder
        filename = f"{uuid.uuid4().hex}.svg"
        (IMAGES_DIR / filename).write_bytes(dl_resp.content)
        return f"{HOST_URL}/images/{filename}"
    except Exception:
        pass
    return None


# --- SQLite session store ---

def init_db():
    con = sqlite3.connect(DB_PATH)
    con.execute("""
        CREATE TABLE IF NOT EXISTS messages (
            id        INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id TEXT NOT NULL,
            role      TEXT NOT NULL,
            content   TEXT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    """)
    con.commit()
    con.close()

init_db()

@contextmanager
def get_db():
    con = sqlite3.connect(DB_PATH)
    con.row_factory = sqlite3.Row
    try:
        yield con
    finally:
        con.close()

def load_history(session_id: str) -> list:
    with get_db() as con:
        rows = con.execute(
            "SELECT role, content FROM messages WHERE session_id = ? ORDER BY id",
            (session_id,)
        ).fetchall()
    return [
        HumanMessage(content=r["content"]) if r["role"] == "user"
        else AIMessage(content=r["content"])
        for r in rows
    ]

def save_messages(session_id: str, user_text: str, ai_text: str):
    with get_db() as con:
        con.execute(
            "INSERT INTO messages (session_id, role, content) VALUES (?, ?, ?)",
            (session_id, "user", user_text)
        )
        con.execute(
            "INSERT INTO messages (session_id, role, content) VALUES (?, ?, ?)",
            (session_id, "model", ai_text)
        )
        con.commit()


# --- API models ---

class ChatRequest(BaseModel):
    session_id: Optional[str] = None
    message: str

class ChatResponse(BaseModel):
    reply: str
    session_id: str
    image_url: Optional[str] = None

class HistoryMessage(BaseModel):
    role: str
    content: str

class HistoryResponse(BaseModel):
    messages: list[HistoryMessage]


# --- Routes ---

@app.get("/")
def root():
    return {"status": "Asana is running"}

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    docs = db.similarity_search(request.message, k=4)
    context = "\n\n".join([d.page_content for d in docs])

    augmented_user_msg = f"Reference context (may be partial or unrelated — always answer from your own yoga expertise if the context is insufficient):\n{context}\n\nStudent question: {request.message}"
    session_id = request.session_id or uuid.uuid4().hex[:8]
    history = load_history(session_id)

    messages = (
        [SystemMessage(content=SYSTEM_PROMPT)]
        + history
        + [HumanMessage(content=augmented_user_msg)]
    )

    loop = asyncio.get_event_loop()
    text_response = await loop.run_in_executor(None, invoke_with_rotation, messages)
    reply = text_response.content

    save_messages(session_id, request.message, reply)
    return ChatResponse(reply=reply, session_id=session_id, image_url=None)

class ImageRequest(BaseModel):
    message: str

class ImageResponse(BaseModel):
    image_url: Optional[str] = None

@app.post("/image", response_model=ImageResponse)
async def image(request: ImageRequest):
    image_url = await generate_image_for_pose(request.message)
    return ImageResponse(image_url=image_url)

@app.get("/history/{session_id}", response_model=HistoryResponse)
def get_history(session_id: str):
    with get_db() as con:
        rows = con.execute(
            "SELECT role, content FROM messages WHERE session_id = ? ORDER BY id",
            (session_id,)
        ).fetchall()
    return HistoryResponse(messages=[
        HistoryMessage(role=r["role"], content=r["content"]) for r in rows
    ])
