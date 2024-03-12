from typing import Any, Dict, List, Tuple

from constant.parameter import ParameterLocation, ParameterType
from model.method import Method
from model.parameter import Parameter, ParameterAttribute
from model.request_response import Request


def build_request(method: Method, parameters: List[Tuple[Parameter, Any]]) -> Request:
    params = {}
    data = {}
    url = method.method_path
    headers = {}
    files = {}
    form_data = {}
    for parameter_pair in parameters:
        parameter, val = parameter_pair
        parameter_location = parameter.location
        if parameter_location == ParameterLocation.HEADER:
            # headers[parameter.name] = val
            # Check if the parameter is 'Authorization'
            if parameter.name.lower() == 'authorization':
                # Ensure the value is a string or bytes
                headers[parameter.name] = str(val)
            else:
                headers[parameter.name] = val
        elif parameter_location == ParameterLocation.QUERY:
            params[parameter.name] = val
        elif parameter_location == ParameterLocation.PATH:
            url = str(url)
            url = url.replace("{" + str(parameter.name) + "}", str(val))
        elif parameter_location == ParameterLocation.FORM_DATA:
            if parameter.parameter.parameter_type == ParameterType.FILE:
                files[parameter.name] = ("test.jpg", val)
                continue
            form_data[parameter.name] = val
        elif parameter_location == ParameterLocation.BODY:
            data = val
        else:
            raise Exception("Unrecognized type", parameter.parameter_raw_body)
    request = Request()
    request.method = method
    request.url = url
    request.params = params
    request.data = data
    request.headers = headers
    request.files = files
    request.form_data = form_data
    return request
