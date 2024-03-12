from typing import List

from model.api import API
from model.method import Method


def wrap_methods_from_open_api_document(open_api_doc: dict) -> List[API]:
    apis: List[API] = []

    for path in open_api_doc["paths"]:
        api = API(path, open_api_doc["paths"][path])
        apis.append(api)

        for method_type in open_api_doc["paths"][path]:
            method_raw_data = open_api_doc["paths"][path][method_type]
            method = Method(method_type, path, method_raw_data)
            method.parse_parameters()
            api.add_method(method)
    return apis
