from typing import List

from model.operation_dependency_graph import OperationDependencyGraph
from model.request_response import Request, Response
from model.sequence import Sequence


class Analysis:
    name: str = "base_analysis"
    fuzzer: "Fuzzer" = None

    def on_init(self, odg_graph: OperationDependencyGraph):
        pass

    def on_request_response(
        self, sequence: Sequence, request: Request, response: Response
    ):
        pass

    def on_sequence_end(
        self,
        sequence: Sequence,
        request_list: List[Request],
        response_list: List[Response],
    ):
        pass

    def on_iteration_end(self):
        pass

    def on_end(self):
        pass
