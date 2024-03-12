import dataclasses
from typing import Dict, List, Tuple

import loguru
from graphviz import Digraph

from algo.chatgpt_agent import ChatGPTAgent
from model.api import API
from model.match_rule.base_rule import Rule
from model.match_rule.substr_rule import SubStringRule
from model.method import Method
from model.parameter import ParameterAttribute
from model.parameter_dependency import (InContextParameterDependency,
                                        ParameterDependency)
from model.sequence import Sequence

logger = loguru.logger


@dataclasses.dataclass
class Edge:
    producer: Method = None
    consumer: Method = None
    parameter_dependency_list: List[ParameterDependency] = dataclasses.field(
        default_factory=list
    )

    def graphviz_label(self):
        label = "Params: "
        for parameter_dependency in self.parameter_dependency_list:
            label += f"{parameter_dependency.producer_parameter.attribute_path} -> {parameter_dependency.consumer_parameter.attribute_path},"
        return label


class OperationDependencyGraph:
    def __init__(self, apis: List[API]):
        self.api_list: List[API] = apis
        self.method_list: List[Method] = []
        self.edge_list: List[Edge] = []
        self.rule_list: List[Rule] = [SubStringRule]
        self.sequence_length: int = 2
        self.producer_consumer_map: Dict[Method, List[Method]] = {}
        self.consumer_producer_map: Dict[Method, List[Method]] = {}
        self.producer_consumer_edge_map: Dict[Method, List[Edge]] = {}
        self.consumer_producer_edge_map: Dict[Method, List[Edge]] = {}
        self.producer_consumer_to_edge_map: Dict[Tuple[Method, Method], Edge] = {}
        self.producer_and_parameter_attribute_to_edge_map: Dict[
            Tuple[Method, ParameterAttribute], List[ParameterDependency]
        ] = {}
        self.consumer_and_parameter_attribute_to_edge_map: Dict[
            Tuple[Method, ParameterAttribute], List[ParameterDependency]
        ] = {}
        self.graph: Digraph = Digraph(comment="Operation Dependency Graph")

    def build(self):
        # extract methods from apis
        for api in self.api_list:
            for method in api.method_dict.values():
                self.method_list.append(method)

        # build producer-consumer map
        for producer in self.method_list:
            for consumer in self.method_list:
                if producer == consumer:
                    continue
                for rule in self.rule_list:
                    if (
                        producer.operation_id == "addPet"
                        and consumer.operation_id == "getPetById"
                    ):
                        a = 1
                    if rule.has_parameter_dependency(producer, consumer):
                        parameter_dependency_list = rule.build_parameter_dependency(
                            producer, consumer
                        )
                        if producer not in self.producer_consumer_map:
                            self.producer_consumer_map[producer] = []
                        self.producer_consumer_map[producer].append(consumer)
                        if consumer not in self.consumer_producer_map:
                            self.consumer_producer_map[consumer] = []
                        self.consumer_producer_map[consumer].append(producer)
                        edge = Edge(producer, consumer, parameter_dependency_list)
                        self.edge_list.append(edge)
                        if producer not in self.producer_consumer_edge_map:
                            self.producer_consumer_edge_map[producer] = []
                        self.producer_consumer_edge_map[producer].append(edge)
                        if consumer not in self.consumer_producer_edge_map:
                            self.consumer_producer_edge_map[consumer] = []
                        self.consumer_producer_edge_map[consumer].append(edge)
                        self.producer_consumer_to_edge_map[(producer, consumer)] = edge

                        for parameter_dependency in parameter_dependency_list:
                            producer_tuple = (
                                producer,
                                parameter_dependency.producer_parameter,
                            )
                            if (
                                producer_tuple
                                not in self.producer_and_parameter_attribute_to_edge_map
                            ):
                                self.producer_and_parameter_attribute_to_edge_map[
                                    producer_tuple
                                ] = []
                            self.producer_and_parameter_attribute_to_edge_map[
                                producer_tuple
                            ].append(parameter_dependency)
                            consumer_tuple = (
                                consumer,
                                parameter_dependency.consumer_parameter,
                            )
                            if (
                                consumer_tuple
                                not in self.consumer_and_parameter_attribute_to_edge_map
                            ):
                                self.consumer_and_parameter_attribute_to_edge_map[
                                    consumer_tuple
                                ] = []
                            self.consumer_and_parameter_attribute_to_edge_map[
                                consumer_tuple
                            ].append(parameter_dependency)

                        self._add_edge(edge)
                        break

    def generate_sequence(self) -> List[Sequence]:
        """
        Recursive generate sequence by producer-consumer map

        :return: List[Sequence]
        """
        sequence_list = []
        for producer in self.producer_consumer_map:
            sequence_list += self._generate_sequence(producer, Sequence())
        return sequence_list

    def generate_sequence_by_chatgpt(
        self, test_sequence_list: List[List[str]]
    ) -> List[Sequence]:
        """
        Generate sequence by chatgpt
        :param test_sequence_list: List[List[str]]
        :return: List[Sequence]
        """
        sequence_list = []
        for test_sequence_line in test_sequence_list:
            sequence = Sequence()
            for test_sequence in test_sequence_line:
                consumer = self._find_method_by_name(test_sequence)
                if consumer is None:
                    continue
                sequence.add_method(consumer)
                # check has dependency
                for producer_index, producer in enumerate(
                    sequence.method_sequence[:-1]
                ):
                    if (producer, consumer) in self.producer_consumer_to_edge_map:
                        dependency = InContextParameterDependency()
                        dependency.producer = producer
                        dependency.consumer = consumer
                        dependency.producer_index = producer_index
                        dependency.consumer_index = len(sequence.method_sequence) - 1
                        for parameter_dependency in self.producer_consumer_to_edge_map[
                            (producer, consumer)
                        ].parameter_dependency_list:
                            dependency.add_parameter_dependency(parameter_dependency)
                        sequence.add_parameter_dependency(dependency)
            if len(sequence.method_sequence) == 0:
                continue
            sequence.is_from_chatgpt = True
            sequence_list.append(sequence)

        return sequence_list

    def _generate_single_method_sequence(self) -> List[Sequence]:
        """
        Generate sequence by single method
        :return: List[Sequence]
        """
        sequence_list = []
        for method in self.method_list:
            sequence = Sequence()
            sequence.add_method(method)
            sequence_list.append(sequence)
        return sequence_list

    def _find_method_by_name(self, method_name: str) -> Method:
        for method in self.method_list:
            if method.operation_id == method_name:
                return method
        return None

    def _generate_sequence(
        self, producer: Method, sequence: Sequence
    ) -> List[Sequence]:
        """
        Recursive generate sequence by producer-consumer map

        :param producer: Method
        :param sequence: Sequence
        :return: List[Sequence]
        """
        sequence_list = []

        sequence.add_method(producer)

        # check the length of sequence
        if len(sequence.method_sequence) >= self.sequence_length:
            return [sequence.copy()]

        # it is a leaf node
        if producer not in self.producer_consumer_map:
            return [sequence.copy()]
        producer_index = sequence.method_sequence.index(producer)

        for consumer in self.producer_consumer_map[producer]:
            if consumer in sequence.method_sequence:
                sequence_list.append(sequence.copy())
                continue

            seq = sequence.copy()
            dependency: InContextParameterDependency = InContextParameterDependency(
                producer=producer, consumer=consumer
            )
            for parameter_dependency in self.producer_consumer_to_edge_map[
                (producer, consumer)
            ].parameter_dependency_list:
                dependency.add_parameter_dependency(parameter_dependency)

            dependency.producer_index = producer_index
            dependency.consumer_index = producer_index + 1
            seq.add_parameter_dependency(dependency)
            sequence_list += self._generate_sequence(consumer, seq.copy())
        return sequence_list

    def _add_edge(self, edge: Edge) -> str:
        """
        Add edge to graph

        :param edge:
        :return: None
        """
        self.graph.node(edge.producer.operation_id)
        self.graph.node(edge.consumer.operation_id)
        self.graph.edge(edge.consumer.operation_id, edge.producer.operation_id)

    def generate_graph(self) -> Digraph:
        """
        Generate graph

        :return: str
        """
        self.graph.render("odg")
        return self.graph.source
