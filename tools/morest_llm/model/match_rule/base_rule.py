from model.method import Method


class Rule:
    name: str = "base_rule"

    @staticmethod
    def has_parameter_dependency(from_method: Method, to_method: Method) -> bool:
        pass

    @staticmethod
    def build_parameter_dependency(from_method: Method, to_method: Method):
        pass
