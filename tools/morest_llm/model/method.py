import uuid
from typing import Any, Dict, List, Optional, Tuple, Union

import loguru

from constant.api import MethodRequestType
from constant.parameter import ParameterLocation, RequestBodyContent
from model.parameter import Parameter

logger = loguru.logger


class Method:
    def __init__(self, method_type: str, api_path: str, method_raw_body: dict):
        self.method_path: str = api_path
        self.operation_id: str = method_raw_body.get("operationId", None)
        self.summary: str = method_raw_body.get("summary", None)
        self.description: str = method_raw_body.get("description", None)

        # request and response data type
        self.consumes: List[str] = method_raw_body.get("consumes", None)
        self.produces: List[str] = method_raw_body.get("produces", None)
        self.tags: List[str] = method_raw_body.get("tags", None)

        # request parameter
        self.request_parameter: Dict[str, Parameter] = {}

        # response parameter
        self.response_parameter: Dict[str, Parameter] = {}

        self.method_type: MethodRequestType = MethodRequestType(method_type)
        self.method_raw_body: dict = method_raw_body

        # method id
        self.method_id: str = f"{uuid.uuid4()}"

    def parse_parameters(self):
        logger.info(f"parse method {self.signature}")

        # parse request parameters
        if self.method_raw_body.__contains__("parameters"):
            raw_request_parameters = self.method_raw_body["parameters"]
            # if no parameters found
            if raw_request_parameters is None:
                print("No raw request parameters found.")
                return
            for raw_request_parameter in raw_request_parameters:
                parameter_location: ParameterLocation = ParameterLocation(
                    raw_request_parameter["in"]
                )
                parameter_name: str = (
                    raw_request_parameter["name"]
                    if parameter_location != ParameterLocation.BODY
                    else ""
                )
                parameter: Parameter = Parameter(
                    name=parameter_name,
                    parameter_location=parameter_location,
                    parameter_raw_body=raw_request_parameter,
                )
                parameter.parse_parameter()

                description: str = raw_request_parameter.get("description", None)
                parameter.description = description
                parameter.method = self

                if ParameterLocation.BODY == parameter_location:
                    parameter.request_body_content = RequestBodyContent.JSON

                self.request_parameter[parameter.name] = parameter
        if self.method_raw_body.__contains__("requestBody"):
            raw_request_parameters = self.method_raw_body["requestBody"]
            parameter_location: ParameterLocation = ParameterLocation.BODY

            for request_body_type in raw_request_parameters["content"]:
                if (
                    "json" not in request_body_type
                    and "octet-stream" not in request_body_type
                ):
                    logger.error(
                        f"request body type {request_body_type} is not supported"
                    )
                    continue
                parameter_name: str = f"{request_body_type}_body"
                body_schema = raw_request_parameters["content"][request_body_type][
                    "schema"
                ]
                parameter: Parameter = Parameter(
                    name=parameter_name,
                    parameter_location=parameter_location,
                    parameter_raw_body=body_schema,
                )
                required: bool = raw_request_parameters.get("required", False)
                parameter.required = required
                parameter.request_body_content = RequestBodyContent.JSON
                parameter.parse_parameter()

                description = body_schema.get("description", None)
                parameter.description = description
                parameter.method = self
                self.request_parameter[parameter.name] = parameter

        # parse response parameters
        if self.method_raw_body.__contains__("responses"):
            raw_response_parameters = self.method_raw_body["responses"]
            for raw_response_parameter in raw_response_parameters:
                parameter: Parameter = Parameter(
                    name=raw_response_parameter,
                    parameter_location=ParameterLocation.RESPONSE,
                    parameter_raw_body=raw_response_parameters[raw_response_parameter],
                )
                parameter.parse_parameter()
                description: str = raw_response_parameters[raw_response_parameter].get(
                    "description", None
                )
                parameter.description = description
                parameter.method = self
                self.response_parameter[parameter.name] = parameter

    @property
    def signature(self):
        return f"{self.method_type.value}_{self.operation_id}_{self.method_path}"

    @property
    def full_description(self):
        return f"{self.signature}, {self.summary}, {self.description}"

    def __repr__(self):
        return self.signature

    def __hash__(self):
        return hash(self.signature)

    def __eq__(self, other):
        if isinstance(other, Method):
            return self.signature == other.signature
        return False
