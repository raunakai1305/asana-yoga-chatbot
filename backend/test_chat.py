"""
Usage:
  python test_chat.py "How do I do Warrior 1?"
  python test_chat.py "Your message" --url https://your-prod-url
"""

import argparse
import httpx

parser = argparse.ArgumentParser()
parser.add_argument("message", help="Message to send to the chatbot")
parser.add_argument("--url", default="http://localhost:8000")
args = parser.parse_args()

resp = httpx.post(f"{args.url}/chat", json={"message": args.message}, timeout=30)
resp.raise_for_status()
data = resp.json()

print(f"INPUT : {args.message}")
print(f"OUTPUT: {data['reply']}")
