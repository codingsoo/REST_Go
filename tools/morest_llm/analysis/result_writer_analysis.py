import base64
import json
import pathlib
import time
from typing import Dict, List, Tuple

import loguru

from analysis.base_analysis import Analysis
from model.method import Method
from model.operation_dependency_graph import OperationDependencyGraph
from model.request_response import Request, Response
from model.sequence import Sequence

logger = loguru.logger


class BytesEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, bytes):
            return "bytes_data"
        return super().default(obj)


class ResultWriterAnalysis(Analysis):
    name = "result_writer_analysis"

    def on_init(self, fuzzer: "Fuzzer"):
        self.begin_time: float = time.time()
        self.fuzzer: "Fuzzer" = fuzzer
        self.method_list: List[Method] = list(fuzzer.graph.method_list)
        self.method_request_count: Dict[Method, int] = {
            method: 0 for method in self.method_list
        }
        self.status_code_count: Dict[int, int] = {}
        self.total_success_method_set: set = set()
        self.total_failed_method_set: set = set()
        self.total_success_count: int = 0
        self.total_request_count: int = 0
        self.total_method_count: int = len(self.method_list)
        self.sequence_list: List[List[Dict]] = []

    def on_request_response(self, sequence, request, response):
        status_code = response.status_code
        if status_code not in self.status_code_count:
            self.status_code_count[status_code] = 0
        self.status_code_count[status_code] += 1

        if 200 <= response.status_code < 300:
            self.total_success_count += 1
            self.total_success_method_set.add(request.method)

        if 600 > response.status_code >= 500:
            self.total_failed_method_set.add(request.method)

        self.total_request_count += 1

    def on_sequence_end(
        self,
        sequence: Sequence,
        request_list: List[Request],
        response_list: List[Response],
    ):
        sequence_dict_list = []
        for request, response in zip(request_list, response_list):
            sequence_dict_list.append(
                {
                    "request": request.to_dict(),
                    "response": response.to_dict(),
                }
            )
        self.sequence_list.append(sequence_dict_list)

    def on_end(self):
        # write result
        result_folder = self.fuzzer.output_dir
        current_time = time.strftime("%Y-%m-%d-%H-%M-%S", time.localtime())
        result_file = result_folder / f"{current_time}.json"
        result = {
            "sequence_list": self.sequence_list,
            "total_success_count": self.total_success_count,
            "total_request_count": self.total_request_count,
            "total_method_count": self.total_method_count,
            "success_method_count": len(self.total_success_method_set),
            "failed_method_count": len(self.total_failed_method_set),
        }
        with open(result_file, "w") as f:
            json.dump(result, f, cls=BytesEncoder, indent=4)
