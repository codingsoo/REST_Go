import collections
from typing import Any, Dict, List, Optional, Set, Tuple, Union

import loguru
import numpy as np

from algo.rl_algorithm import rl_algorithm
from constant.data_generation_config import DataGenerationConfig
from constant.parameter import ParameterLocation, ParameterType
from model.method import Method
from model.parameter import Parameter, ParameterAttribute
from model.parameter_dependency import (ParameterDependency,
                                        ReferenceValueResult)
from model.request_response import Request, Response

logger = loguru.logger


class RuntimeDictionary:
    """
    This class is used to store the runtime values of the parameters.
    """

    def __init__(self, fuzzer: "Fuzzer"):
        self.fuzzer: "Fuzzer" = fuzzer
        self.method_set: Set[Method] = set()
        self.method_to_parameter_attribute_map: Dict[
            Method, Set[ParameterAttribute]
        ] = {}
        self.method_to_response_list_map: Dict[Method, List[Response]] = {}
        self.method_parameter_attribute_to_value_map: Dict[
            Tuple[Method, ParameterAttribute], List[Any]
        ] = {}
        # initilize parameter type to method parameter attribute map
        self.parameter_type_to_method_parameter_attribute_map: Dict[
            ParameterType, List[Tuple[Method, ParameterAttribute]]
        ] = {parameter_type: [] for parameter_type in ParameterType}
        self.fifo_length: int = 20
        self.consumer_method_parameter_to_dependency_map: Dict[
            Tuple[Method, ParameterAttribute], Set[ParameterDependency]
        ] = {}

    def _choose_dependency(
        self, dependency_list: List[ParameterDependency]
    ) -> ParameterDependency:
        if self.fuzzer.config.enable_reinforcement_learning:
            index = rl_algorithm(dependency_list)
            return dependency_list[index]
        else:
            return dependency_list[np.random.randint(0, len(dependency_list))]

    def fetch_value(
        self,
        data_generator: "DataGenerator",
        consumer_parameter_attribute: ParameterAttribute,
    ) -> ReferenceValueResult:
        result = ReferenceValueResult()
        parameter_tuple = (data_generator.method, consumer_parameter_attribute)

        # skip runtime dictionary
        if data_generator.config.no_dictionary_value_probability > np.random.random():
            return result

        # no value
        if (
            self.parameter_type_to_method_parameter_attribute_map[
                consumer_parameter_attribute.parameter_type
            ]
            == []
        ):
            return result

        result.should_use = True
        result.attribute = consumer_parameter_attribute

        # use odg data
        if (
            data_generator.config.no_odg_value_probability < np.random.random()
            and parameter_tuple
            in data_generator.fuzzer.graph.consumer_and_parameter_attribute_to_edge_map
        ):
            valid_parameter_dependency_list = []
            for (
                parameter_dependency
            ) in data_generator.fuzzer.graph.consumer_and_parameter_attribute_to_edge_map[
                parameter_tuple
            ]:
                parameter_dependency: ParameterDependency

                runtime_tuple = (
                    parameter_dependency.producer,
                    parameter_dependency.producer_parameter,
                )
                if runtime_tuple in self.method_parameter_attribute_to_value_map:
                    valid_parameter_dependency_list.append(parameter_dependency)

            if len(valid_parameter_dependency_list) > 0:
                parameter_dependency = self._choose_dependency(
                    valid_parameter_dependency_list
                )
                result.dependency = parameter_dependency
                runtime_tuple = (
                    parameter_dependency.producer,
                    parameter_dependency.producer_parameter,
                )
                value_list = self.method_parameter_attribute_to_value_map[runtime_tuple]
                result.value = value_list[np.random.randint(0, len(value_list))]
                return result

        # use random value
        if parameter_tuple not in self.consumer_method_parameter_to_dependency_map:
            self.consumer_method_parameter_to_dependency_map[parameter_tuple] = set()

        if (
            len(self.consumer_method_parameter_to_dependency_map[parameter_tuple]) > 0
            and data_generator.config.random_runtime_dictionary_value_probability
            > np.random.random()
        ):
            producer_parameter_dependency_list = list(
                self.consumer_method_parameter_to_dependency_map[parameter_tuple]
            )
            parameter_dependency = self._choose_dependency(
                producer_parameter_dependency_list
            )
            result.dependency = parameter_dependency
            value_list = self.method_parameter_attribute_to_value_map[
                (parameter_dependency.producer, parameter_dependency.producer_parameter)
            ]
            result.value = value_list[np.random.randint(0, len(value_list))]
            return result

        # use runtime data
        random_index = np.random.randint(
            0,
            len(
                self.parameter_type_to_method_parameter_attribute_map[
                    consumer_parameter_attribute.parameter_type
                ]
            ),
        )
        (
            producer_method,
            parameter_attribute,
        ) = self.parameter_type_to_method_parameter_attribute_map[
            consumer_parameter_attribute.parameter_type
        ][
            random_index
        ]

        parameter_dependency = ParameterDependency()
        parameter_dependency.producer = producer_method
        parameter_dependency.producer_parameter = parameter_attribute
        parameter_dependency.consumer = data_generator.method
        parameter_dependency.consumer_parameter = consumer_parameter_attribute
        self.consumer_method_parameter_to_dependency_map[parameter_tuple].add(
            parameter_dependency
        )
        result.dependency = parameter_dependency

        value_list = self.method_parameter_attribute_to_value_map[
            (producer_method, parameter_attribute)
        ]
        result.value = value_list[np.random.randint(0, len(value_list))]

        return result

    def add_response(self, response: Response):
        if response.status_code >= 300:
            return

        method: Method = response.method
        self.method_set.add(method)

        # add response to response list
        if method not in self.method_to_response_list_map:
            self.method_to_response_list_map[method] = collections.deque(
                maxlen=self.fifo_length
            )
        self.method_to_response_list_map[method].append(response)

        # add parameter attribute to parameter attribute set
        if method not in self.method_to_parameter_attribute_map:
            self.method_to_parameter_attribute_map[method] = set()
        for parameter_attribute in response.response_body_value_map.values():
            self.method_to_parameter_attribute_map[method].add(parameter_attribute)

        # add parameter attribute to value map
        for parameter_attribute in response.response_body_value_map.values():
            method_parameter_tuple = (method, parameter_attribute)
            if (
                method_parameter_tuple
                not in self.method_parameter_attribute_to_value_map
            ):
                self.method_parameter_attribute_to_value_map[
                    method_parameter_tuple
                ] = collections.deque(maxlen=self.fifo_length)
                self.parameter_type_to_method_parameter_attribute_map[
                    parameter_attribute.parameter_type
                ].append(method_parameter_tuple)
                logger.info(
                    f"Found new parameter attribute: {parameter_attribute} on {method}"
                )
            for value in parameter_attribute.get_parameter_value():
                self.method_parameter_attribute_to_value_map[
                    method_parameter_tuple
                ].append(value)
