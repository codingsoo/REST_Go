import os.path
from typing import Any, Dict, List, Tuple
from urllib.parse import urljoin

import loguru
import requests

from algo.data_generator import DataGenerator
from algo.runtime_dictionary import ReferenceValueResult, RuntimeDictionary
from constant.api import ResponseCustomizedStatusCode
from model.method import Method
from model.parameter import Parameter, ParameterAttribute
from model.parameter_dependency import (InContextAttributeDependency,
                                        InContextParameterDependency,
                                        ParameterDependency)
from model.request_response import Request, Response
from model.sequence import Sequence
from util.request_builder import build_request

logger = loguru.logger


class SequenceConverter:
    def __init__(self, fuzzer: "Fuzzer"):
        self.fuzzer: "Fuzzer" = fuzzer
        self.runtime_dictionary: RuntimeDictionary = RuntimeDictionary(fuzzer)

        # initialize session
        self.request_session: requests.Session = None
        self._new_session()

    def _new_session(self):
        self.request_session = requests.Session()

    def _generate_random_data(
            self,
            method_index: int,
            method: Method,
            sequence: Sequence,
            response_list: List[Response],
            last_response: Response,
    ) -> Any:
        generated_value_tuple_list: List[Tuple[Parameter, Any]] = []
        reference_result_list: List[ReferenceValueResult] = []
        valid_dependency_map: Dict[
            ParameterAttribute, List[InContextAttributeDependency]
        ] = {}
        if method_index in sequence.consumer_index_to_dependency_map:
            origin_dependency_list: List[
                InContextAttributeDependency
            ] = sequence.consumer_index_to_dependency_map[method_index]

            for dependency in origin_dependency_list:
                dependency: InContextAttributeDependency
                producer_response: Response = response_list[dependency.producer_index]

                # invalid response
                if 300 <= producer_response.status_code:
                    continue
                producer_attribute: ParameterAttribute = (
                    dependency.parameter_dependency.producer_parameter
                )
                if (
                        producer_attribute.attribute_path
                        not in producer_response.response_body_value_map
                ):
                    continue

                consumer_attribute: ParameterAttribute = (
                    dependency.parameter_dependency.consumer_parameter
                )
                if consumer_attribute not in valid_dependency_map:
                    valid_dependency_map[consumer_attribute] = []
                valid_dependency_map[consumer_attribute].append(dependency)

        for parameter_name in method.request_parameter:
            parameter: Parameter = method.request_parameter[parameter_name]
            # initialize data generator
            data_generator: DataGenerator = DataGenerator(
                self, method_index, method, sequence, last_response, response_list
            )

            # add reference value
            data_generator.valid_dependency_map = valid_dependency_map

            # generate value
            value = data_generator.generate_value(parameter.parameter)

            # if value is SKIP_SYMBOL, skip this parameter
            if value == DataGenerator.SKIP_SYMBOL:
                continue

            generated_value_tuple_list.append((parameter, value))
            reference_result_list.extend(data_generator.reference_value_result_list)

        return generated_value_tuple_list, reference_result_list

    def _do_request(self, method: Method, request: Request) -> Response:
        request_actor = getattr(self.request_session, method.method_type.value)
        url = self.fuzzer.config.url + request.url
        response: Response = Response()
        response.request = request
        response.method = method

        # do request
        try:
            if isinstance(request.data, bytes):
                request.headers["Content-Type"] = "application/octet-stream"
                raw_response: requests.Response = request_actor(
                    url,
                    params=request.params,
                    data=request.data,
                    headers=request.headers,
                    files=request.files,
                    allow_redirects=False,
                    timeout=30,
                )
            else:
                raw_response: requests.Response = request_actor(
                    url,
                    params=request.params,
                    data=request.form_data,
                    json=request.data,
                    headers=request.headers,
                    files=request.files,
                    allow_redirects=False,
                    timeout=30,
                )
        except requests.exceptions.ReadTimeout as err:
            logger.error(err)
            response.status_code = ResponseCustomizedStatusCode.TIMEOUT.value
        except Exception as e:  # probably an encoding error
            raise e
        else:
            response.text = raw_response.text
            response.headers = raw_response.headers
            if method in self.fuzzer.never_success_method_set:
                a = 1
            try:
                response.parse_response(raw_response)
            except Exception as e:  # returned value format not correct
                # logger.error(f"Error when parsing response: {e}, {raw_response.text}")
                pass
        return response

    def convert(self, sequence: Sequence) -> Sequence:
        # renew session
        self._new_session()

        last_response: Response = None
        request_list: List[Request] = []
        response_list: List[Response] = []

        # generate value for each parameter in the sequence methods' parameters
        for method_index, method in enumerate(sequence.method_sequence):
            # generate random data
            generated_value, reference_result_list = self._generate_random_data(
                method_index, method, sequence, response_list, last_response
            )

            # assemble data
            request: Request = build_request(method, generated_value)

            # do response
            response = self._do_request(method, request)

            if method.operation_id == "getUserByName":
                a = 1

            # add to runtime dictionary
            self.runtime_dictionary.add_response(response)

            # add to response list
            response_list.append(response)

            # add to request list
            request_list.append(request)

            # update dependency success count
            for reference_result in reference_result_list:
                if 200 <= response.status_code < 300:
                    reference_result.dependency.update(5)
                else:
                    reference_result.dependency.update(-1)

            # call analysis function
            self.fuzzer._on_request_response(sequence, request, response)
        self.fuzzer._on_sequence_end(sequence, request_list, response_list)
        return sequence

    def _generate_value_for_method_by_chatgpt(self, method: Method):
        generated_value_dict: Dict[str, Any] = {}
        result = (
            self.fuzzer.chatgpt_agent.generate_request_instance_by_openapi_document(
                method.method_raw_body
            )
        )
        return result

    def _do_chatgpt_request(self, method: Method, request: Request, session: requests.Session) -> Response:
        request_actor = getattr(session, method.method_type.value)
        url = request.url
        response: Response = Response()
        response.request = request
        response.method = method

        # do request
        try:
            if isinstance(request.data, bytes):
                request.headers["Content-Type"] = "application/octet-stream"
                raw_response: requests.Response = request_actor(
                    url,
                    params=request.params,
                    data=request.data,
                    headers=request.headers,
                    files=request.files,
                    allow_redirects=False,
                    timeout=30,
                )
            else:
                raw_response: requests.Response = request_actor(
                    url,
                    params=request.params,
                    data=request.form_data,
                    json=request.data,
                    headers=request.headers,
                    files=request.files,
                    allow_redirects=False,
                    timeout=30,
                )
        except requests.exceptions.ReadTimeout as err:
            logger.error(err)
            response.status_code = ResponseCustomizedStatusCode.TIMEOUT.value
        except Exception as e:  # probably an encoding error
            raise e
        else:
            response.text = raw_response.text
            response.headers = raw_response.headers
            if method in self.fuzzer.never_success_method_set:
                a = 1
            try:
                response.parse_response(raw_response)
            except Exception as e:  # returned value format not correct
                # logger.error(f"Error when parsing response: {e}, {raw_response.text}")
                pass
        return response

    def request_chatgpt_single_instance(self, method: Method, request_url: str, request_data: Dict[str, Any],
                                        session: requests.Session) -> Response:
        request: Request = Request()
        request.method = method
        request.url = request_url
        request.data = request_data
        try:
            response = self._do_chatgpt_request(request.method, request, session)
        except Exception as e:
            logger.error(f"Error when requesting ChatGPT instance: {e}, {request}")
            return

        if 200 <= response.status_code < 300:
            logger.info(f"ChatGPT instance request success: {request.method.signature}")

        self.fuzzer._on_request_response(None, request, response)
        self.fuzzer._on_sequence_end(None, [request], [response])
        return response
