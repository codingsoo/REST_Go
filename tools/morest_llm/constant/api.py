import enum


class MethodRequestType(enum.Enum):
    GET = "get"
    POST = "post"
    PUT = "put"
    DELETE = "delete"
    PATCH = "patch"
    HEAD = "head"
    OPTIONS = "options"


class ResponseCustomizedStatusCode(enum.Enum):
    TIMEOUT = 1000
    EXCEPTION = 1001
