from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from langchain_core.messages import HumanMessage, AIMessage
from rag import build_rag_chain

app = FastAPI(title="Asana - Yoga Pose Chatbot")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

chain = build_rag_chain()
chat_histories = {}

class ChatRequest(BaseModel):
    session_id: str
    message: str

class ChatResponse(BaseModel):
    reply: str

@app.get("/")
def root():
    return {"status": "Asana is running"}

@app.post("/chat", response_model=ChatResponse)
def chat(request: ChatRequest):
    history = chat_histories.get(request.session_id, [])

    result = chain.invoke({
        "input": request.message,
        "chat_history": history,
    })

    reply = result["answer"]
    history.append(HumanMessage(content=request.message))
    history.append(AIMessage(content=reply))
    chat_histories[request.session_id] = history

    return ChatResponse(reply=reply)

@app.delete("/chat/{session_id}")
def clear_history(session_id: str):
    chat_histories.pop(session_id, None)
    return {"status": "cleared"}
