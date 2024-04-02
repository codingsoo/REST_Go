import enum


class ParameterType(enum.Enum):
    INTEGER = "integer"
    NUMBER = "number"
    STRING = "string"
    BOOLEAN = "boolean"
    ARRAY = "array"
    FILE = "file"
    OBJECT = "object"
    NULL = "null"


class ParameterLocation(enum.Enum):
    QUERY = "query"
    HEADER = "header"
    PATH = "path"
    BODY = "body"
    FORM_DATA = "formData"
    RESPONSE = "response"


class RequestBodyContent(enum.Enum):
    JSON = "application/json"
