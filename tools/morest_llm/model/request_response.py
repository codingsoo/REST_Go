import dataclasses
import enum
from typing import Any, Dict, List, Tuple

import requests

from constant.parameter import ParameterLocation, ParameterType
from model.method import Method
from model.parameter import Parameter, ParameterAttribute


@dataclasses.dataclass
class Request:
    method: Method = None
    request_attribute_value_map: Dict[ParameterAttribute, Any] = dataclasses.field(
        default_factory=dict
    )
    params: Dict[str, Any] = dataclasses.field(default_factory=dict)
    data: Dict[str, Any] = dataclasses.field(default_factory=dict)
    url: str = None
    headers: Dict[str, Any] = dataclasses.field(default_factory=dict)
    files: Dict[str, Any] = dataclasses.field(default_factory=dict)
    form_data: Dict[str, Any] = dataclasses.field(default_factory=dict)

    def to_dict(self):
        return {
            "method": self.method.method_path,
            "url": self.url,
            "params": self.params,
            "data": self.data,
            "headers": self.headers,
            "files": self.files,
            "form_data": self.form_data,
        }


@dataclasses.dataclass
class Response:
    status_code: int = None
    method: Method = None
    request: Request = None
    text: str = None
    headers: Dict[str, Any] = dataclasses.field(default_factory=dict)
    response_header_value_map: Dict[str, ParameterAttribute] = dataclasses.field(
        default_factory=dict
    )
    response_body_value_map: Dict[str, ParameterAttribute] = dataclasses.field(
        default_factory=dict
    )

    def _parse_json_value(
        self, attribute_path: str, json_item: Any, parameter_location: ParameterLocation
    ):
        attribute_name = attribute_path.split(".")[-1]

        # handle parameter attribute
        parameter_attribute = ParameterAttribute(
            attribute_name, attribute_path, None, {}
        )
        if parameter_location == ParameterLocation.HEADER:
            parameter_attribute = self.response_header_value_map.get(
                attribute_path, parameter_attribute
            )
        elif parameter_location == ParameterLocation.BODY:
            parameter_attribute = self.response_body_value_map.get(
                attribute_path, parameter_attribute
            )

        if isinstance(json_item, str):
            parameter_attribute.parameter_type = ParameterType.STRING
        elif isinstance(json_item, int):
            parameter_attribute.parameter_type = ParameterType.INTEGER
        elif isinstance(json_item, float):
            parameter_attribute.parameter_type = ParameterType.NUMBER
        elif json_item is None:
            parameter_attribute.parameter_type = ParameterType.NULL
        elif isinstance(json_item, dict):
            parameter_attribute.parameter_type = ParameterType.OBJECT
            prefix = ""
            if len(attribute_path) > 0:
                prefix = attribute_path + "."
            for key in json_item.keys():
                item = json_item[key]
                path = prefix + key
                self._parse_json_value(path, item, parameter_location)
        elif isinstance(json_item, list):
            parameter_attribute.parameter_type = ParameterType.ARRAY
            for i, val in enumerate(json_item):
                self._parse_json_value(attribute_path + "[0]", val, parameter_location)
        else:
            raise Exception(f"Unknown parameter {attribute_path} {json_item}")

        # add value
        parameter_attribute.add_parameter_value(json_item)

        # update value
        if parameter_location == ParameterLocation.HEADER:
            self.response_header_value_map[attribute_path] = parameter_attribute
        elif parameter_location == ParameterLocation.BODY:
            self.response_body_value_map[attribute_path] = parameter_attribute

    def parse_response(self, response: requests.Response):
        self.status_code = response.status_code
        for header_key in response.headers.keys():
            self._parse_json_value(
                header_key, response.headers[header_key], ParameterLocation.HEADER
            )
        self._parse_json_value("", response.json(), ParameterLocation.BODY)

    def to_dict(self):
        return {"status_code": self.status_code, "text": self.text}
