import dataclasses
from typing import Any, Dict, List

import loguru

from constant.parameter import (ParameterLocation, ParameterType,
                                RequestBodyContent)

logger = loguru.logger
ARRAY_NOTATION = "[0]"


@dataclasses.dataclass
class ParameterAttributeSchemaInfo:
    raw_schema: dict = None
    enum: List[str] = None
    format: str = None
    pattern: str = None
    example: Any = None
    maximum: Any = None
    minimum: Any = None
    maxLength: int = None
    minLength: int = None

    @property
    def has_enum(self):
        return self.enum is not None and len(self.enum) > 0

    @property
    def has_example(self):
        return self.example is not None

    @property
    def has_format(self):
        return self.format is not None

    @property
    def has_maximum(self):
        return self.maximum is not None

    @property
    def has_minimum(self):
        return self.minimum is not None

    @property
    def has_pattern(self):
        return self.pattern is not None

    @property
    def has_max_length(self):
        return self.maxLength is not None

    @property
    def has_min_length(self):
        return self.minLength is not None


class ParameterAttribute:
    schema_info: ParameterAttributeSchemaInfo = None
    parameter: "Parameter" = None
    parameter_value_list: List[Any] = None

    def __init__(
            self,
            attribute_name: str,
            attribute_path: str,
            parameter: "Parameter",
            parameter_attribute_raw_body: dict,
    ):
        # root parameter
        self.parameter: Parameter = parameter

        # parameter attribute structure
        self.parent_parameter_attribute: ParameterAttribute = None
        self.sibling_parameter_attribute_list: List[ParameterAttribute] = []
        self.child_parameter_attribute_list: List[ParameterAttribute] = []

        # parameter attribute data
        self.parameter_attribute_raw_body: dict = parameter_attribute_raw_body
        self.attribute_path: str = attribute_path
        self.attribute_name: str = attribute_name
        self.description: str = parameter_attribute_raw_body.get("description", None)
        self.required: bool = False
        self.global_required: bool = False

        # if parameter is None, this is a runtime parameter
        self.parameter_value_list = []

        if self.parameter is None:
            return

        # check if the parameter attribute is a schema
        if (
                parameter_attribute_raw_body.__contains__("schema")
                and parameter_attribute_raw_body["schema"].__contains__("properties")
        ) or parameter_attribute_raw_body.__contains__("properties"):
            self.parameter_type: ParameterType = ParameterType.OBJECT
        else:
            if parameter_attribute_raw_body.__contains__("schema"):
                self.parameter_type: ParameterType = ParameterType(
                    parameter_attribute_raw_body["schema"]["type"]
                )
            elif parameter_attribute_raw_body.__contains__("anyOf"):
                # temp support for anyOf
                self.parameter_type: ParameterType = ParameterType(
                    parameter_attribute_raw_body["anyOf"][0]["type"]
                )
            elif "type" not in parameter_attribute_raw_body:
                # temp support for typeless
                self.parameter_type: ParameterType = ParameterType.STRING
            else:
                self.parameter_type: ParameterType = ParameterType(
                    parameter_attribute_raw_body["type"]
                )

        # update schema info
        self.schema_info = ParameterAttributeSchemaInfo()
        if parameter_attribute_raw_body.__contains__("schema"):
            parameter_attribute_raw_body = parameter_attribute_raw_body["schema"]
        self.schema_info.raw_schema = parameter_attribute_raw_body
        self.schema_info.enum = parameter_attribute_raw_body.get("enum", None)
        self.schema_info.example = parameter_attribute_raw_body.get("example", None)
        self.schema_info.format = parameter_attribute_raw_body.get("format", None)
        self.schema_info.maximum = parameter_attribute_raw_body.get("maximum", None)
        self.schema_info.minimum = parameter_attribute_raw_body.get("minimum", None)
        self.schema_info.pattern = parameter_attribute_raw_body.get("pattern", None)
        self.schema_info.maxLength = parameter_attribute_raw_body.get("maxLength", None)
        self.schema_info.minLength = parameter_attribute_raw_body.get("minLength", None)

    def set_parent_parameter_attribute(self, parent_parameter_attribute):
        self.parent_parameter_attribute = parent_parameter_attribute

    def add_sibling_parameter_attribute(self, sibling_parameter_attribute):
        self.sibling_parameter_attribute_list.append(sibling_parameter_attribute)

    def add_child_parameter_attribute(self, child_parameter_attribute):
        self.child_parameter_attribute_list.append(child_parameter_attribute)

    @property
    def signature(self):
        return f"type:({self.parameter_type.value})_path({self.attribute_path})"

    def __repr__(self):
        if self.parameter is not None:
            return f"{self.parameter.signature}_{self.signature}"
        return f"{self.signature}"

    def __eq__(self, other):
        return self.attribute_path == other.attribute_path

    def __hash__(self):
        return hash(self.attribute_path)

    def add_parameter_value(self, parameter_value):
        self.parameter_value_list.append(parameter_value)

    def get_parameter_value(self):
        for value in self.parameter_value_list:
            yield value


# remember to support requestBody


class Parameter:
    def __init__(
            self, name: str, parameter_location: ParameterLocation, parameter_raw_body: dict
    ):
        self.parameter_raw_body: dict = parameter_raw_body
        self.name: str = name
        self.description: str = None
        self.required: bool = parameter_raw_body.get("required", False)
        # required for path
        if parameter_location == ParameterLocation.PATH:
            self.required = True
        self.attribute_dict: Dict[str, ParameterAttribute] = {}
        self.parameter: ParameterAttribute = None
        self.location: ParameterLocation = parameter_location
        self.request_body_content: RequestBodyContent = None
        self.method: "Method" = None

    def parse_parameter(self):
        parameter_body = self.parameter_raw_body
        if self.location == ParameterLocation.BODY:
            if self.request_body_content:
                self.recursive_parse_parameter(
                    parameter_name="",
                    parameter_body=parameter_body,
                    parent_path="",
                    parent_attribute=None,
                    parent_required=self.required,
                )
            else:
                self.recursive_parse_parameter(
                    parameter_name="",
                    parameter_body=parameter_body["schema"],
                    parent_path="",
                    parent_attribute=None,
                    parent_required=parameter_body.get("required", False),
                )
        elif self.location == ParameterLocation.RESPONSE:
            if parameter_body.__contains__("schema"):
                self.recursive_parse_parameter(
                    parameter_name="",
                    parameter_body=parameter_body["schema"],
                    parent_path="",
                    parent_attribute=None,
                    parent_required=parameter_body.get("required", False),
                )

            elif parameter_body.__contains__("content"):
                content: dict = parameter_body["content"]
                for content_type in content:
                    if "json" not in content_type and "*/*" not in content_type:
                        logger.error(f"not support content type: {content_type}")
                        continue
                    self.recursive_parse_parameter(
                        parameter_name="",
                        parameter_body=content[content_type]["schema"],
                        parent_path="",
                        parent_attribute=None,
                        parent_required=parameter_body.get("required", False),
                    )

                    # TODO: support other content type
                    break

        else:
            parameter_name: str = parameter_body["name"]
            self.recursive_parse_parameter(
                parameter_name=parameter_name,
                parameter_body=parameter_body,
                parent_path="",
                parent_attribute=None,
                parent_required=parameter_body.get("required", False),
            )

    def recursive_parse_parameter(
            self,
            parameter_name: str,
            parameter_body: dict,
            parent_path: str,
            parent_attribute: ParameterAttribute,
            parent_required: bool,
    ):
        parameter_name: str = parameter_name
        parameter_path: str = (
            f"{parent_path}.{parameter_name}" if parent_path else parameter_name
        )

        # check array notation
        if parameter_name == ARRAY_NOTATION:
            parameter_path = f"{parent_path}{parameter_name}"
            if parent_attribute.parameter_type == ParameterType.ARRAY:
                parameter_name = f"{parent_attribute.attribute_name}{parameter_name}"

        parameter_attribute = ParameterAttribute(
            attribute_name=parameter_name,
            attribute_path=parameter_path,
            parameter=self,
            parameter_attribute_raw_body=parameter_body,
        )

        # root parameter
        if parent_attribute is None:
            self.parameter = parameter_attribute
            self.required = parent_required
            parameter_attribute.required = parent_required
            parameter_attribute.global_required = parent_required
        else:
            parameter_attribute.required = parameter_body.get("required", False)
            parameter_attribute.global_required = (
                    parent_required and parameter_body.get("required", False)
            )
            parent_attribute.add_child_parameter_attribute(parameter_attribute)
            parameter_attribute.set_parent_parameter_attribute(parent_attribute)

        if parameter_attribute.parameter_type == ParameterType.ARRAY:
            parameter_body_items: dict = (
                parameter_body["items"]
                if parameter_body.__contains__("items")
                else parameter_body["schema"]["items"]
            )
            child_parameter = self.recursive_parse_parameter(
                parameter_name=ARRAY_NOTATION,
                parameter_body=parameter_body_items,
                parent_path=f"{parameter_path}",
                parent_attribute=parameter_attribute,
                parent_required=parameter_attribute.global_required,
            )
        elif parameter_attribute.parameter_type == ParameterType.OBJECT:
            required_list: List[str] = parameter_body.get("required", [])
            properties: dict = parameter_body.get("properties", {})
            if parameter_body.__contains__("allOf"):
                all_of: List[dict] = parameter_body.get("allOf", [])
                for all_of_item in all_of:
                    if all_of_item.__contains__("properties"):
                        properties.update(all_of_item.get("properties", {}))
                    if all_of_item.__contains__("required"):
                        required_list.extend(all_of_item.get("required", []))
            children: List[ParameterAttribute] = []

            # iterate property
            for property_name in properties:
                property_body: dict = properties[property_name]
                is_required = property_name in required_list
                child_attribute = self.recursive_parse_parameter(
                    parameter_name=property_name,
                    parameter_body=property_body,
                    parent_path=parameter_path,
                    parent_attribute=parameter_attribute,
                    parent_required=parameter_attribute.global_required,
                )
                child_attribute.required = is_required
                child_attribute.global_required = (
                        parameter_attribute.global_required and is_required
                )
                children.append(child_attribute)

            # add sibling
            for child in children:
                for sibling in children:
                    if child != sibling:
                        child.add_sibling_parameter_attribute(sibling)

        elif parameter_attribute.parameter_type in (
                ParameterType.STRING,
                ParameterType.BOOLEAN,
                ParameterType.INTEGER,
                ParameterType.NUMBER,
                ParameterType.FILE,
        ):
            pass
        else:
            raise NotImplementedError(
                f"parameter type {parameter_attribute.parameter_type} is not supported yet"
            )

        self.add_attribute_attribute(parameter_attribute)
        return parameter_attribute

    def add_attribute_attribute(self, attribute_attribute: ParameterAttribute):
        if attribute_attribute.attribute_path in self.attribute_dict:
            raise Exception(
                f"attribute path {attribute_attribute.attribute_path} already exists"
            )
        if attribute_attribute.attribute_path == "":
            return
        logger.info(
            f"add attribute location: {self.location}, attribute: {attribute_attribute.signature}"
        )
        self.attribute_dict[attribute_attribute.attribute_path] = attribute_attribute

    @property
    def signature(self):
        return f"method({self.method.signature})_name({self.name})_location({self.location.value})"

    def __repr__(self):
        return f"{self.signature}"
