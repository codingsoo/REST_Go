import dataclasses
import enum
import json


class ChatGPTCommandType(enum.Enum):
    INITIALIZE = "initialize"
    GENERATE_SEQUENCE = "generate_sequence"
    GENERATE_PLAIN_INSTANCE = "generate_plain_instance"
