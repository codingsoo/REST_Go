import ast
import copy
import dataclasses
import json
import re

import requests
import yaml
import queue
import re
import threading
from typing import Any, Callable, Dict, List, Set, Tuple
import sys
import loguru
import time

from constant.chatgpt_config import ChatGPTCommandType
from model.method import Method
from model.request_response import Request, Response
from util.chatgpt import ChatGPT
import concurrent.futures

logger = loguru.logger


class APIRequester:
    def __init__(self, fuzzer: "Fuzzer", target_method: Method):
        self.fuzzer: "Fuzzer" = fuzzer
        self.target_method: Method = target_method
        self.chatgpt: ChatGPT = ChatGPT()

    def _assemble_method_list(self, method_list: List[Method]) -> str:
        method_str_list: List[str] = [f"- {method.method_type.value.upper()} {method.method_path} {method.description}"
                                      for method in method_list]
        return "".join(method_str_list)

    def extract_api_info(self, text):
        # Define the regular expression pattern to match the API method and path
        pattern = r'(?P<method>GET|POST|PUT|DELETE|PATCH｜OPTIONS|HEAD)\s+(?P<path>/\S+)'

        # Use re.findall() to find all occurrences of the pattern in the input text
        matches = re.findall(pattern, text)

        # Extract API method and path from each match
        api_info_list = [(match[0], match[1]) for match in matches]

        return api_info_list

    def _api_planner(self):
        prompt = f"""You are a planner that plans a sequence of API calls to request a target API.

You should:
1) if yes, generate a plan of API calls and say what they are doing step by step.

You should only use API endpoints documented below ("Endpoints you can use:").
Sometimes the target API can be resolved in a single API call, but some will require several API calls.
The plan will be passed to an API controller that can format it into web requests and return the responses.

----

Here are some examples:

Fake endpoints for examples:
GET /user to get information about the current user
GET /products/search search across products
POST /users/{"{id}"}/cart to add products to a user's cart

Usery query: I need to find the right API calls to call the api: POST /users/{"{id}"}/cart
Plan: 1. GET /products/search to search for couches
2. GET /user to find the user's id
3. POST /users/{"{id}"}/cart to add a couch to the user's cart

----

Here are endpoints you can use. Do not reference any of the endpoints above.

{self._assemble_method_list(list(self.fuzzer.operation_id_to_method_map.values()))}

----

User query: I need to find the right API calls to call the api: {f"{self.target_method.method_type.value.upper()} {self.target_method.method_path}"}
Plan:
        """
        raw_response = self.chatgpt.send_message(prompt)
        logger.info(
            f'{"=" * 20} api_planner: {self.target_method.method_type.value} {self.target_method.method_path} {"=" * 20}')
        logger.info(f"raw response: {raw_response}")
        api_sequence: List[Tuple[str, str]] = self.extract_api_info(raw_response)
        logger.info(f'extract api sequence from response: {api_sequence}')
        method_sequence: List[Method] = [self.fuzzer.chatgpt_operation_id_to_method_map[f'{method[0]}{method[1]}'] for
                                         method in api_sequence]
        return method_sequence, raw_response

    def _get_method_list_description(self, method_list: List[Method]):
        method_doc_description_str = ""
        for method in method_list:
            method_doc_dict = {
                "description": method.description,
                "parameters": method.method_raw_body.get("parameters", method.method_raw_body.get("requestBody", [])),
                "responses": method.method_raw_body.get("responses", {}).get("200", [])
            }
            method_doc_description_str += f"""
== Docs for {method.method_type.value.upper()} {method.method_path} ==
{yaml.dump(method_doc_dict)}
        """
        return method_doc_description_str

    def _generate_parameter_value(self, api_doc: str, plan: str, thought: str):
        prompt = f"""You are an agent that gets a sequence of API calls and given their documentation, should execute them and return the final response. Do not generate observations and only generate one Action and one Action Input.
If you cannot complete them and run into issues, you should explain the issue. If you're able to resolve an API call, you can retry the API call. When interacting with API objects, you should extract ids for inputs to other API calls but ids and names for outputs returned to the User.

Here is documentation on the API:
Base url: {self.fuzzer.config.url}
Endpoints:
{api_doc}


Here are tools to execute requests against the API: requests_get_head_options: Use this to GET/HEAD/OPTIONS content from a website.
Input to the tool should be a json string with 2 keys: "url" and "output_instructions".
The value of "url" should be a string. The value of "output_instructions" should be instructions on what information to extract from the response, for example the id(s) for a resource(s) that the GET request fetches.

requests_post_put_patch: Use this when you want to POST/PUT/PATCH to a website.
Input to the tool should be a json string with 3 keys: "url", "data", and "output_instructions".
The value of "url" should be a string.
The value of "data" should be a dictionary of key-value pairs you want to POST/PUT/PATCH to the url.
The value of "summary_instructions" should be instructions on what information to extract from the response, for example the id(s) for a resource(s) that the  POST/PUT/PATCH request creates.
Always use double quotes for strings in the json string.
Always make the json string use a json code fragment.


Starting below, you should follow this format:

Plan: the plan of API calls to execute
Thought: you should always think about what to do
Action: the action to take, should be one of the tools [requests_get_head_options, requests_post_put_patch]
Action Input: the input to the action
Observation: the output of the action
... (this Thought/Action/Action Input/Observation can repeat N times)
Thought: I am finished executing the plan (or, I cannot finish executing the plan without knowing some other information.)
Final Answer: the final output from executing the plan or missing information I'd need to re-plan correctly.


Begin!

Plan: {plan}
Thought: {thought}
"""
        raw_response = self.chatgpt.send_message(prompt)
        logger.info(
            f'{"=" * 20} generate_parameter_value: {self.target_method.method_type.value} {self.target_method.method_path} {"=" * 20}')
        logger.info(f"raw response: {raw_response}")
        return raw_response, prompt

    def parsing_parameter_value(self, raw_response: str):

        # Regular expression to match the action
        action_pattern = r"Action:\s*(\w+)"
        # Regular expression to match the action input
        input_pattern = r"Action Input:\s*(\{.*\})"

        # Find the action
        action_match = re.search(action_pattern, raw_response)
        action = action_match.group(1) if action_match else None

        # Find the action input
        input_match = re.search(input_pattern, raw_response)
        action_input = json.loads(input_match.group(1)) if input_match else None

        return action, action_input

    def _parse_response(self, response: Response, instructions: str):
        prompt = f"""Here is an API response and headers: ==Response==\n\n{response.text}\n\n ==Headers==\n\n{"{}"}\n\n====
Your task is to extract some information according to these instructions: {instructions}
When working with API objects, you should usually use ids over names. Do not return any ids or names that are not in the response.
If the response indicates an error, you should instead output a summary of the error.

Output:"""
        raw_response = self.chatgpt.send_message(prompt)
        return raw_response

    def run(self):
        # check if target method is success
        if self.target_method not in self.fuzzer.never_success_method_set:
            return

        method_sequence, plan = self._api_planner()
        doc_description = self._get_method_list_description(method_sequence)
        thought = ""
        session = requests.Session()
        for method in method_sequence:
            # check if target method is success
            if self.target_method not in self.fuzzer.never_success_method_set:
                return

            # generate parameter value
            raw_response, prompt = self._generate_parameter_value(doc_description, plan, thought)

            # get action and action input
            action, action_input = self.parsing_parameter_value(raw_response)

            # if failed to parsing then return
            if not action or not action_input:
                return

            # execute action
            request_url: str = action_input.get("url")
            request_data: Dict[str, Any] = action_input.get("data")
            response: Response = self.fuzzer.sequence_converter.request_chatgpt_single_instance(method, request_url,
                                                                                                request_data, session)
            # if not success then return
            if response.status_code > 300:
                return

            if method == self.target_method:
                logger.info(f"target method: {method.signature} response: {response.text}")
                return

            # get instruction
            instruction = action_input.get("output_instructions", None) or action_input.get("summary_instructions",
                                                                                            None)

            # if no instruction then return
            if instruction is None:
                return

            # parse response
            parsed_response = self._parse_response(response, instruction)
            thought += f"{raw_response}\nObservation: {parsed_response}\nThought: "


EXECUTOR_NUMBER = 2


class ChatGPTAgent:
    def __init__(self, fuzzer: "Fuzzer"):
        self.fuzzer: "Fuzzer" = fuzzer
        self.task_queue: queue.Queue = queue.Queue()
        self.consumers: List[Callable] = [self._execute_api_requester] * EXECUTOR_NUMBER
        self.executor: concurrent.futures.ThreadPoolExecutor = concurrent.futures.ThreadPoolExecutor(
            max_workers=EXECUTOR_NUMBER)
        self.future_list: List[concurrent.futures.Future] = []
        threading.Thread(target=self.execute).start()

    def consumer(self, task):
        return
        # disable chatgpt
        if not self.fuzzer.config.enable_chatgpt:
            logger.info("chatgpt disabled")
            return

        while True:
            # check if time budget is reached
            if self.fuzzer.begin_time + self.fuzzer.time_budget < time.time():
                logger.info(f"time budget reached: {self.fuzzer.time_budget}s")
                return

            if not self.task_queue.empty():
                data = self.task_queue.get()
                try:
                    task(data)
                except Exception as e:
                    logger.error(e)

    def check_exception(self) -> bool:
        for future in self.futures:
            if future.exception():
                logger.info(f"An exception occurred: {future.exception()}")
                sys.exit(1)

    def _execute_api_requester(self, target_method: Method):
        logger.info(f"execute api requester for {target_method.signature}")
        requester = APIRequester(self.fuzzer, target_method)
        requester.run()

    def execute(self):
        for task in self.consumers:
            future = self.executor.submit(self.consumer, task)
            self.future_list.append(future)
        # add exception checker
        # self.future_list.append(self.executor.submit(self.check_exception))
