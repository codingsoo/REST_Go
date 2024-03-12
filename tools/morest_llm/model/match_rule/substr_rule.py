from typing import List

from model.match_rule.base_rule import Rule
from model.match_rule.black_list import BLACK_LIST
from model.method import Method
from model.parameter_dependency import ParameterDependency
from model.util.type_reasoner import reason_type


class SubStringRule(Rule):
    name = "substr_rule"

    @staticmethod
    def has_parameter_dependency(producer_method: Method, consumer_method: Method):
        for response in producer_method.response_parameter.values():
            for request in consumer_method.request_parameter.values():
                if (
                    producer_method.operation_id == "addPet"
                    and consumer_method.operation_id == "updatePet"
                ):
                    a = 1
                for producer_parameter_attribute in response.attribute_dict.values():
                    for consumer_parameter_attribute in request.attribute_dict.values():
                        if (
                            producer_parameter_attribute.attribute_name.lower().find(
                                consumer_parameter_attribute.attribute_name.lower()
                            )
                            == -1
                            and consumer_parameter_attribute.attribute_name.lower().find(
                                producer_parameter_attribute.attribute_name.lower()
                            )
                            == -1
                        ):
                            continue
                        # perform reasoning
                        if reason_type(
                            producer_parameter_attribute, consumer_parameter_attribute
                        ):
                            return True
        return False

    @staticmethod
    def build_parameter_dependency(
        producer_method: Method, consumer_method: Method
    ) -> List[ParameterDependency]:
        parameter_dependency_list: List[ParameterDependency] = []
        for response in producer_method.response_parameter.values():
            for request in consumer_method.request_parameter.values():
                for producer_parameter_attribute in response.attribute_dict.values():
                    for consumer_parameter_attribute in request.attribute_dict.values():
                        if (
                            producer_parameter_attribute.attribute_name.lower().find(
                                consumer_parameter_attribute.attribute_name.lower()
                            )
                            == -1
                            and consumer_parameter_attribute.attribute_name.lower().find(
                                producer_parameter_attribute.attribute_name.lower()
                            )
                            == -1
                        ):
                            continue
                        # perform reasoning
                        if reason_type(
                            producer_parameter_attribute, consumer_parameter_attribute
                        ):
                            parameter_dependency: ParameterDependency = (
                                ParameterDependency()
                            )
                            parameter_dependency.producer = producer_method
                            parameter_dependency.consumer = consumer_method
                            parameter_dependency.producer_parameter = (
                                producer_parameter_attribute
                            )
                            parameter_dependency.consumer_parameter = (
                                consumer_parameter_attribute
                            )
                            parameter_dependency.match_rule = SubStringRule.name
                            parameter_dependency_list.append(parameter_dependency)

        return parameter_dependency_list
