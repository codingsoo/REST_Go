import copy
import dataclasses
import uuid
from typing import Dict, List, Tuple

from model.method import Method
from model.parameter import Parameter, ParameterAttribute
from model.parameter_dependency import (InContextAttributeDependency,
                                        InContextParameterDependency,
                                        ParameterDependency)


@dataclasses.dataclass
class Sequence:
    method_sequence: List[Method] = dataclasses.field(default_factory=list)
    parameter_dependency_list: List[InContextParameterDependency] = dataclasses.field(
        default_factory=list
    )
    consumer_index_to_dependency_map: Dict[
        int, List[InContextAttributeDependency]
    ] = dataclasses.field(default_factory=dict)
    producer_index_to_dependency_map: Dict[
        int, List[InContextAttributeDependency]
    ] = dataclasses.field(default_factory=dict)
    is_from_chatgpt: bool = False
    sequence_id: str = dataclasses.field(default_factory=lambda: str(uuid.uuid4()))

    def add_method(self, method: Method):
        self.method_sequence.append(method)

    def add_parameter_dependency(
            self, parameter_dependency: InContextParameterDependency
    ):
        consumer_index = parameter_dependency.consumer_index
        producer_index = parameter_dependency.producer_index
        if consumer_index not in self.consumer_index_to_dependency_map:
            self.consumer_index_to_dependency_map[consumer_index] = []
        if producer_index not in self.producer_index_to_dependency_map:
            self.producer_index_to_dependency_map[producer_index] = []
        for dependency in parameter_dependency.parameter_dependency_list:
            parameter_dependency: InContextAttributeDependency = (
                InContextAttributeDependency()
            )
            parameter_dependency.consumer_index = consumer_index
            parameter_dependency.producer_index = producer_index
            parameter_dependency.parameter_dependency = dependency
            self.consumer_index_to_dependency_map[consumer_index].append(
                parameter_dependency
            )
            self.producer_index_to_dependency_map[producer_index].append(
                parameter_dependency
            )

        # add to parameter dependency list
        self.parameter_dependency_list.append(parameter_dependency)

    def copy(self):
        seq = Sequence()
        seq.method_sequence = [method for method in self.method_sequence]
        seq.parameter_dependency_list = [dep for dep in self.parameter_dependency_list]
        seq.consumer_index_to_dependency_map = {key: value for key, value in
                                                self.consumer_index_to_dependency_map.items()}
        seq.producer_index_to_dependency_map = {key: value for key, value in
                                                self.producer_index_to_dependency_map.items()}
        return seq
