class Specification:
    def __init__(self, open_api_spec: dict):
        self.name = name
        self.description = description
        self.parameters = parameters
        self.inputs = inputs
        self.outputs = outputs

    def __repr__(self):
        return f"Specification({self.name}, {self.description}, {self.parameters}, {self.inputs}, {self.outputs})"
