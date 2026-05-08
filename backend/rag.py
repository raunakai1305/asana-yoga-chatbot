from dotenv import load_dotenv
from langchain import hub
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_chroma import Chroma
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.messages import HumanMessage, AIMessage
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain.chains import create_retrieval_chain

load_dotenv()

CHROMA_PATH = "../chroma_db"

# Base: rlm/rag-prompt (32M downloads) — handles context retrieval structure
# Extended: Asana CRISPE persona layered on top
ASANA_EXTENSION = """
You are Asana, a certified and highly experienced yoga teacher. \
You have deep expertise in anatomy, alignment, pose modifications, and safe yoga practice.

You are speaking with yoga students — from beginners to intermediate practitioners. \
Guide them through yoga poses accurately, safely, and clearly.

Always structure your response like this:
- Pose Name (English / Sanskrit)
- Step-by-step instructions
- Key benefits
- Common mistakes to avoid
- Beginner modification
- How long to hold

If the answer is not in the context, say: "I don't have information on that pose yet. \
Please ask your instructor for guidance."
If the question is not about yoga poses, say: "I specialize in yoga poses. \
I'd be happy to help you with any pose-related questions!"
If the user writes in Hindi or Hinglish, understand it fully but always respond in English.

Speak with authority and precision. Always prioritize student safety.
"""

def build_rag_chain():
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
    db = Chroma(persist_directory=CHROMA_PATH, embedding_function=embeddings)
    retriever = db.as_retriever(search_kwargs={"k": 4})

    llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash-lite", temperature=0.3)

    # Pull rlm/rag-prompt from LangChain Hub and extend with Asana persona
    # Replace {question} with {input} to match create_retrieval_chain's variable name
    base_prompt = hub.pull("rlm/rag-prompt")
    base_template = base_prompt.messages[0].prompt.template
    base_template = base_template.replace("Question: {question}", "").strip()

    combined_system = base_template.replace(
        "You are an assistant for question-answering tasks.",
        "You are an assistant for question-answering tasks." + ASANA_EXTENSION
    )

    prompt = ChatPromptTemplate.from_messages([
        ("system", combined_system),
        MessagesPlaceholder("chat_history"),
        ("human", "{input}"),
    ])

    question_answer_chain = create_stuff_documents_chain(llm, prompt)
    chain = create_retrieval_chain(retriever, question_answer_chain)
    return chain
