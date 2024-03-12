import datetime
import os
import random
import string
import uuid
from typing import Any, Dict, List, Tuple

import numpy as np
import rstr
import re

from algo.rl_algorithm import rl_algorithm
from algo.runtime_dictionary import ReferenceValueResult, RuntimeDictionary
from constant.data_generation_config import DataGenerationConfig
from constant.parameter import ParameterType
from model.method import Method
from model.parameter import Parameter, ParameterAttribute, ParameterType
from model.parameter_dependency import (InContextAttributeDependency,
                                        InContextParameterDependency,
                                        ParameterDependency,
                                        ReferenceValueResult)
from model.request_response import Response
from model.sequence import Sequence


class DataGenerator:
    """Data generator class."""

    SKIP_SYMBOL: str = "SKIP_SYMBOL"

    def __init__(
        self,
        sequence_converter: "SequenceConverter",
        method_index: int,
        method: Method,
        sequence: Sequence,
        last_response: Response,
        response_list: List[Response],
    ):
        self.sequence_converter: "SequenceConverter" = sequence_converter
        self.fuzzer: "Fuzzer" = sequence_converter.fuzzer
        self.runtime_dictionary: RuntimeDictionary = (
            sequence_converter.runtime_dictionary
        )
        self.method_index: int = method_index
        self.method: Method = method
        self.last_response: Response = last_response
        self.response_list: List[Response] = response_list
        self.sequence: Sequence = sequence
        self.valid_dependency_map: Dict[
            ParameterAttribute, List[InContextAttributeDependency]
        ] = {}
        self.value_generator: Dict[ParameterType, Any] = {
            ParameterType.STRING: self.generate_string_value,
            ParameterType.INTEGER: self.generate_integer_value,
            ParameterType.NUMBER: self.generate_number_value,
            ParameterType.BOOLEAN: self.generate_boolean_value,
            ParameterType.ARRAY: self.generate_array_value,
            ParameterType.OBJECT: self.generate_object_value,
            ParameterType.FILE: self.generate_file_value,
        }
        self.config: DataGenerationConfig = self.fuzzer.data_generation_config
        self.reference_value_result_list: List[ReferenceValueResult] = []

    def _should_skip(self, parameter_attribute: ParameterAttribute) -> bool:
        if parameter_attribute.required:
            return False
        if (
            parameter_attribute in self.valid_dependency_map
            and np.random.random() > self.config.dependency_skip_probability
        ):
            return False
        if (
            parameter_attribute.parent_parameter_attribute is None
            and np.random.random() > self.config.parent_parameter_skip_probability
        ):
            return False
        if np.random.random() > self.config.child_parameter_skip_probability:
            return False
        return True

    def _should_use_dependency(self, parameter_attribute: ParameterAttribute) -> bool:
        if (
            parameter_attribute in self.valid_dependency_map
            and np.random.random() > self.config.dependency_skip_probability
        ):
            return True
        return False

    def _should_use_example(self, parameter_attribute: ParameterAttribute) -> bool:
        if (
            parameter_attribute.schema_info.has_example
            and np.random.random() > self.config.example_skip_probability
        ):
            return True
        return False

    def _fetch_dependency_value(
        self, parameter_attribute: ParameterAttribute
    ) -> InContextAttributeDependency:
        in_context_dependency_list = self.valid_dependency_map[parameter_attribute]
        dependency_list = [
            dependency.parameter_dependency for dependency in in_context_dependency_list
        ]
        index = rl_algorithm(dependency_list)
        dependency: InContextAttributeDependency = in_context_dependency_list[index]
        response_attribute: ParameterAttribute = self.response_list[
            dependency.producer_index
        ].response_body_value_map[
            dependency.parameter_dependency.producer_parameter.attribute_path
        ]
        parameter_value_list = response_attribute.parameter_value_list
        value = parameter_value_list[np.random.randint(0, len(parameter_value_list))]
        refer_value_result = ReferenceValueResult()
        refer_value_result.value = value
        refer_value_result.dependency = dependency.parameter_dependency
        refer_value_result.should_use = True
        refer_value_result.attribute = parameter_attribute
        self.reference_value_result_list.append(refer_value_result)
        return value

    def generate_string_value(self, parameter_attribute: ParameterAttribute) -> str:
        # use example
        if self._should_use_example(parameter_attribute):
            return parameter_attribute.schema_info.example

        # use dependency
        if self._should_use_dependency(parameter_attribute):
            return self._fetch_dependency_value(parameter_attribute)
        # concrete implementation
        min_len = 0
        max_len = 32
        if (
            parameter_attribute.schema_info.has_enum
            and np.random.random() > self.config.violation_enum_probability
        ):
            enum = np.random.choice(parameter_attribute.schema_info.enum)
            return enum

        # use runtime dictionary
        runtime_value_result = self.runtime_dictionary.fetch_value(
            self, parameter_attribute
        )
        if runtime_value_result.should_use:
            self.reference_value_result_list.append(runtime_value_result)
            return runtime_value_result.value

        if (
            parameter_attribute.schema_info.has_max_length
            and parameter_attribute.schema_info.has_min_length
        ):
            # min_len = parameter_attribute.schema_info.min_length
            # max_len = parameter_attribute.schema_info.max_length
            if hasattr(parameter_attribute.schema_info, 'min_length') and hasattr(parameter_attribute.schema_info,
                                                                                  'max_length'):
                min_len = parameter_attribute.schema_info.min_length
                max_len = parameter_attribute.schema_info.max_length
            else:
                # Handle the case where min_length or max_length is not present
                min_len = 0
                max_len = 32

        # if (
        #     parameter_attribute.schema_info.has_min_length
        #     and np.random.random() < self.config.violation_string_probability
        # ):
        #     max_len = parameter_attribute.schema_info.min_length
        if (
            parameter_attribute.schema_info.has_min_length
            and np.random.random() < self.config.violation_string_probability
        ):
            if hasattr(parameter_attribute.schema_info, 'min_length'):
                max_len = parameter_attribute.schema_info.min_length

        # elif (
        #     parameter_attribute.schema_info.has_max_length
        #     and np.random.random() < self.config.violation_string_probability
        # ):
        #     min_len = parameter_attribute.schema_info.max_length
        elif (
            parameter_attribute.schema_info.has_max_length
            and np.random.random() < self.config.violation_string_probability
        ):
            if hasattr(parameter_attribute.schema_info, 'max_length'):
                min_len = parameter_attribute.schema_info.max_length

        if parameter_attribute.schema_info.has_format:
            string_format = parameter_attribute.schema_info.format
            if string_format == "date-time":
                res = datetime.datetime.now().isoformat("T")
                return res
            elif string_format == "uuid":
                res = uuid.uuid4().__str__()
                return res
            elif string_format == "password":
                res = "testpassword"
                return res
            elif string_format == "binary":
                with open("./assets/smallest.jpg", "rb") as file:
                    return file.read()
                # return open("./assets/smallest.jpg", "rb").read()
            elif string_format == "date":  # Handle 'date' format
                res = datetime.datetime.now().strftime("%Y-%m-%d")
                return res
            else:
                raise Exception("unknown string format", string_format)

        if parameter_attribute.schema_info.has_pattern:
            pattern = parameter_attribute.schema_info.pattern
            try:
                res = rstr.xeger(pattern)
            except re.error as e:
                # Handle the regex error
                print(f"Regex error: {e}")
                res = ""

            return res

        # avoid body size too large
        max_len = min(max_len, 100)
        if max_len <= min_len:
            str_len = max_len
        else:
            str_len = np.random.randint(min_len, max_len + 1)
        res = "".join(random.choices(string.ascii_uppercase + string.digits, k=str_len))
        return res

    # write signature for all value generators
    def generate_integer_value(self, parameter_attribute: ParameterAttribute) -> int:
        # use example
        if self._should_use_example(parameter_attribute):
            return parameter_attribute.schema_info.example

        # use dependency
        if self._should_use_dependency(parameter_attribute):
            return self._fetch_dependency_value(parameter_attribute)

        # concrete implementation
        if (
            parameter_attribute.schema_info.has_enum
            and np.random.random() > self.config.violation_enum_probability
        ):
            enum = np.random.choice(parameter_attribute.schema_info.enum)
            return enum

        # use runtime dictionary
        runtime_value_result = self.runtime_dictionary.fetch_value(
            self, parameter_attribute
        )
        if runtime_value_result.should_use:
            self.reference_value_result_list.append(runtime_value_result)
            return runtime_value_result.value
        # bypass for enum
        if np.random.random() < self.config.enum_number_value_probability:
            res = np.random.randint(0, 2)
            return res

        if (
            parameter_attribute.schema_info.has_minimum
            and parameter_attribute.schema_info.has_maximum
        ):
            if np.random.random() < self.config.min_max_value_probability:
                res = np.random.randint(
                    parameter_attribute.schema_info.minimum,
                    parameter_attribute.schema_info.maximum,
                    dtype=np.int64,
                )
            else:
                res = np.random.choice(
                    [
                        parameter_attribute.schema_info.minimum,
                        parameter_attribute.schema_info.maximum,
                    ]
                )
            return res

        elif parameter_attribute.schema_info.has_minimum:
            if np.random.random() < self.config.min_value_probability:
                res = parameter_attribute.schema_info.minimum
            else:
                res = np.random.randint(0, 999999)
            return res
        elif parameter_attribute.schema_info.has_maximum:
            if np.random.random() < self.config.max_value_probability:
                res = parameter_attribute.schema_info.maximum
            else:
                res = np.random.randint(0, 999999)
            return res
        else:
            res = np.random.randint(0, 999999)
            return res

    def generate_number_value(self, parameter_attribute: ParameterAttribute) -> float:
        return float(self.generate_integer_value(parameter_attribute))

    def generate_boolean_value(self, parameter_attribute: ParameterAttribute) -> bool:
        # use example
        if self._should_use_example(parameter_attribute):
            return parameter_attribute.schema_info.example

        # use dependency
        if self._should_use_dependency(parameter_attribute):
            value = self._fetch_dependency_value(parameter_attribute)
            if value == True:
                return "true"
            elif value == False:
                return "false"
            else:
                return value
        res = np.random.choice(["true", "false"])
        return res

    def generate_array_value(
        self, parameter_attribute: ParameterAttribute
    ) -> List[Any]:
        # use example
        if self._should_use_example(parameter_attribute):
            return parameter_attribute.schema_info.example

        # use dependency
        if self._should_use_dependency(parameter_attribute):
            return self._fetch_dependency_value(parameter_attribute)

        # use runtime dictionary
        runtime_value_result = self.runtime_dictionary.fetch_value(
            self, parameter_attribute
        )
        if runtime_value_result.should_use:
            self.reference_value_result_list.append(runtime_value_result)
            return runtime_value_result.value

        result = []

        item_num = np.random.randint(0, 3)

        for _ in range(item_num):
            for child_parameter in parameter_attribute.child_parameter_attribute_list:
                generated_value = self.generate_value(child_parameter)
                if generated_value == self.SKIP_SYMBOL:
                    continue
                result.append(generated_value)

        return result

    def generate_object_value(
        self, parameter_attribute: ParameterAttribute
    ) -> Dict[str, Any]:
        # use example
        if self._should_use_example(parameter_attribute):
            return parameter_attribute.schema_info.example

        # use dependency
        if self._should_use_dependency(parameter_attribute):
            return self._fetch_dependency_value(parameter_attribute)

        # use runtime dictionary
        runtime_value_result = self.runtime_dictionary.fetch_value(
            self, parameter_attribute
        )
        if runtime_value_result.should_use:
            self.reference_value_result_list.append(runtime_value_result)
            return runtime_value_result.value

        result = {}

        for child_parameter in parameter_attribute.child_parameter_attribute_list:
            generated_value = self.generate_value(child_parameter)
            if generated_value == self.SKIP_SYMBOL:
                continue
            result[child_parameter.attribute_name] = generated_value

        return result

    def generate_file_value(self, parameter_attribute: ParameterAttribute) -> Any:
        # use example
        if self._should_use_example(parameter_attribute):
            return parameter_attribute.schema_info.example

        # use dependency
        if self._should_use_dependency(parameter_attribute):
            return self._fetch_dependency_value(parameter_attribute)

        # img_path = os.path.join("./assets/smallest.jpg")
        # file = open(img_path, "rb")
        # return file
        img_path = os.path.join("./assets/smallest.jpg")
        with open(img_path, "rb") as file:
            return file.read()

    def generate_value(self, parameter_attribute: ParameterAttribute) -> Any:
        # use example
        if self._should_use_example(parameter_attribute):
            return parameter_attribute.schema_info.example

        # use dependency
        if self._should_use_dependency(parameter_attribute):
            return self._fetch_dependency_value(parameter_attribute)

        if self._should_skip(parameter_attribute):
            return self.SKIP_SYMBOL
        parameter_type: ParameterType = parameter_attribute.parameter_type
        value_generator: Any = self.value_generator[parameter_type]
        value = value_generator(parameter_attribute)
        return value
