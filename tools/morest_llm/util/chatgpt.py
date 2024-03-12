# -*- coding: utf-8 -*-

import dataclasses
import json
import re
import time
from typing import Any, Dict, List, Tuple
from uuid import uuid1

import loguru
import openai
import requests

logger = loguru.logger

config = json.load(open("./config.json", "r"))

openai.api_key = config["api_key"]


def chatgpt_completion(history: List) -> str:
    response = openai.ChatCompletion.create(
        model="gpt-4",
        messages=history,
    )
    return response["choices"][0]["message"]["content"]


class ChatGPT:
    def send_message(self, message):
        history = [{"role": "user", "content": message}]
        response = chatgpt_completion(history)
        return response
