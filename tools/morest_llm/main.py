import argparse
import glob
import shutil
from typing import List

import loguru
import prance

from constant.task_config import TaskConfig
from algo.fuzzer import Fuzzer
from constant.fuzzer_config import FuzzerConfig
from model.api import API
from model.operation_dependency_graph import OperationDependencyGraph
from util.api_document_warpper import wrap_methods_from_open_api_document
from prance.util.url import absurl
from prance.util.fs import abspath
from prance.util.resolver import RefResolver
import yaml
import json
import os

yaml_path = "specifications/openapi/scout-api/openapi.yaml"
parser = argparse.ArgumentParser()
parser.add_argument("--yaml_path", type=str, default=yaml_path)
parser.add_argument("--time_budget", type=float, default=600)
parser.add_argument("--warm_up_times", type=int, default=5)
parser.add_argument("--url", type=str, default="https://restcountries.com")
parser.add_argument("--chatgpt", type=bool, default=False)
parser.add_argument("--output_dir", type=str, default="output")
parser.add_argument("--rl", type=bool, default=True)
args = parser.parse_args()

logger = loguru.logger
logger.add("log/{time}.log")


def default_reclimit_handler(limit, parsed_url, recursions=()):
    """Raise prance.util.url.ResolutionError."""
    return {
        "type": "object",
    }


def load_specification(file_path: str):
    if file_path.endswith(".yaml"):
        return yaml.load(open(file_path, "r"), Loader=yaml.FullLoader)
    elif file_path.endswith(".json"):
        return json.load(open(file_path, "r"))
    else:
        raise Exception(f"unknown file type {file_path}")


def parsing(api_document_path: str) -> List[API]:
    url = absurl(api_document_path, abspath(os.getcwd()))
    specification = load_specification(api_document_path)
    resolver = RefResolver(specification, url, default_reclimit_handler=default_reclimit_handler)
    resolver.resolve_references()
    # parser = prance.ResolvingParser(
    #     api_document_path,
    #     backend="openapi-spec-validator",
    #     recursion_limit_handler=default_reclimit_handler,
    #     strict=False,
    # )
    apis = wrap_methods_from_open_api_document(resolver.specs)
    for api in apis:
        for method in api.method_dict.values():
            print("operationId:", method.operation_id)
            print("summary:", method.summary)
            # go through request parameters
            print("request parameters:")
            for parameter in method.request_parameter.values():
                for attribute in parameter.attribute_dict:
                    print(attribute)
            # go through response parameters
            print("response parameters:")
            for parameter in method.response_parameter.values():
                for attribute in parameter.attribute_dict:
                    print(attribute)
    return apis


def main(task_config: TaskConfig):
    apis = parsing(task_config.yaml_path)

    # build odg
    odg = OperationDependencyGraph(apis)
    odg.build()
    # graph = odg.generate_graph()

    # init fuzzer
    config = FuzzerConfig()
    config.time_budget = task_config.time_budget
    config.warm_up_times = task_config.warm_up_times
    config.url = task_config.url
    config.enable_chatgpt = task_config.chatgpt
    config.output_dir = task_config.output_dir
    config.enable_reinforcement_learning = task_config.rl
    fuzzer = Fuzzer(odg, config)

    # setup fuzzer
    fuzzer.setup()

    # warm up
    fuzzer.warm_up()

    # start fuzzing
    fuzzer.fuzz()


def list_folder_extract_yaml_files(folder_path: str):
    yaml_file_absolute_path_list = glob.glob(folder_path + "/*.json") + glob.glob(
        folder_path + "/*.yaml"
    )
    count = 0
    error_count = 0
    valid_doc_count = 0
    yaml_summary_list = []
    for yaml_file_absolute_path in yaml_file_absolute_path_list:
        try:
            loguru.logger.info(f"parsing {yaml_file_absolute_path}")
            # apis = parsing(yaml_file_absolute_path)
            # count += len(apis)
            specification = prance.ResolvingParser(
                yaml_file_absolute_path,
                backend="openapi-spec-validator",
                recursion_limit_handler=default_reclimit_handler,
                strict=False,
            )
            valid_doc_count += 1
            url = specification.specification["servers"][0]["url"]
            yaml_file_name = yaml_file_absolute_path.split("/")[-1].split(".")[0]
            yaml_summary = {
                "name": yaml_file_name,
                "path": yaml_file_absolute_path,
                "url": url
            }
            yaml_summary_list.append(yaml_summary)
            # copy valid doc to another folder
            # shutil.copy(
            #     yaml_file_absolute_path,
            #     "./valid_chatgpt_plugin_spec/" + yaml_file_absolute_path.split("/")[-1],
            # )
        except Exception as e:
            error_count += 1
            loguru.logger.error(f"error: {e}")
    loguru.logger.info(f"total apis: {count}")
    loguru.logger.info(f"error apis: {error_count}")
    loguru.logger.info(f"valid doc count: {valid_doc_count}")
    print(yaml_summary_list)


if __name__ == "__main__":
    # list_folder_extract_yaml_files("./specifications/openapi")
    main(args)
