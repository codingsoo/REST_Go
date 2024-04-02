import datetime
import pathlib
import sys
import time
from typing import Dict, List, Set, Tuple

import loguru

from algo.chatgpt_agent import ChatGPTAgent
from algo.sequence_converter import SequenceConverter
from analysis.base_analysis import Analysis
from analysis.result_writer_analysis import ResultWriterAnalysis
from analysis.statistic_analysis import StatisticAnalysis
from constant.data_generation_config import DataGenerationConfig
from constant.fuzzer_config import FuzzerConfig
from model.method import Method
from model.operation_dependency_graph import OperationDependencyGraph
from model.request_response import Request, Response
from model.sequence import Sequence

logger = loguru.logger

ANALYSIS = [StatisticAnalysis, ResultWriterAnalysis]


class Fuzzer:
    def __init__(self, graph: OperationDependencyGraph, config: FuzzerConfig):
        self.begin_time: float = time.time()
        self.start_time_str: str = datetime.datetime.now().strftime("%Y-%m-%d-%H-%M-%S")
        self.output_dir: pathlib.Path = (
                pathlib.Path(config.output_dir) / self.start_time_str
        )
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.graph: OperationDependencyGraph = graph
        self.config: FuzzerConfig = config
        self.time_budget: float = config.time_budget
        self.chatgpt_agent: ChatGPTAgent = ChatGPTAgent(self)
        self.sequence_list: List[Sequence] = []
        self.sequence_converter: SequenceConverter = SequenceConverter(self)
        self.data_generation_config: DataGenerationConfig = DataGenerationConfig()
        self.analysis_list: List[Analysis] = []
        self.success_method_set: Set[Method] = set()
        self.failed_method_set: Set[Method] = set()
        self.never_success_method_set: Set[Method] = set()
        self.pending_request_list: List[Request] = []
        self.operation_id_to_method_map: Dict[str, Method] = {}
        self.chatgpt_operation_id_to_method_map: Dict[str, Method] = {}
        self.pending_sequence_list: List[Sequence] = []
        self.single_method_sequence_list: List[Sequence] = []

    def setup(self):
        logger.info("Fuzzer setup")
        self._init_analysis()
        self.single_method_sequence_list = self.graph._generate_single_method_sequence()
        self.sequence_list = (
                self.single_method_sequence_list + self.graph.generate_sequence()
        )
        self.pending_sequence_list = self.sequence_list.copy()

        for method in self.graph.method_list:
            self.operation_id_to_method_map[method.operation_id] = method
            self.chatgpt_operation_id_to_method_map[f'{method.method_type.value.upper()}{method.method_path}'] = method

        logger.info(f"generated {len(self.sequence_list)} sequences")

    def _init_analysis(self):
        for analysis in ANALYSIS:
            analyzer = analysis()
            analyzer.on_init(self)
            self.analysis_list.append(analyzer)

    def _on_iteration_end(self):
        for analysis in self.analysis_list:
            analysis.on_iteration_end()

    def _on_request_response(
            self, sequence: Sequence, request: Request, response: Response
    ):
        for analysis in self.analysis_list:
            analysis.on_request_response(sequence, request, response)

    def _on_sequence_end(
            self,
            sequence: Sequence,
            request_list: List[Request],
            response_list: List[Response],
    ):
        for analysis in self.analysis_list:
            analysis.on_sequence_end(sequence, request_list, response_list)

    def _on_end(self):
        for analysis in self.analysis_list:
            analysis.on_end()

    def warm_up(self):
        logger.info("warmup")
        self.never_success_method_set = set(self.operation_id_to_method_map.values())
        # convert sequence to request
        for _ in range(self.config.warm_up_times):
            for sequence in self.single_method_sequence_list:
                self.sequence_converter.convert(sequence)
            self._on_iteration_end()

    def fuzz(self):
        converter = self.sequence_converter

        while self.begin_time + self.time_budget > time.time():
            # handle the case that all methods are never success
            if self.config.enable_chatgpt and len(self.never_success_method_set) > 0:
                for method in self.never_success_method_set:
                    self.chatgpt_agent.task_queue.put(method)

            # convert sequence to request
            for sequence in self.sequence_list:
                converter.convert(sequence)

            # handlers for each iteration
            self._on_iteration_end()

            # update sequence list
            if self.pending_sequence_list == 0:
                continue
            self.sequence_list += self.pending_sequence_list
            self.pending_sequence_list.clear()

        self._on_end()
        sys.exit(0)
