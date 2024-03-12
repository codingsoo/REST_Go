from typing import Dict, List

from constant.api import MethodRequestType
from model.method import Method


class API:
    def __init__(self, path: str, api_raw_data: dict):
        self.path: str = path
        self.api_raw_data: dict = api_raw_data
        self.method_dict: Dict[MethodRequestType, Method] = {}

    def add_method(self, method: Method):
        self.method_dict[method.method_type] = method
