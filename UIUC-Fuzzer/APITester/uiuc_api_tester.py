import openai
import os
import json
import requests
import re
import traceback
import collections
from functools import (partial,
                       singledispatch)
from itertools import chain
from typing import (Dict,
                    List,
                    TypeVar)

Serializable = TypeVar('Serializable', None, int, bool, float, str,
                       dict, list, tuple)
Array = List[Serializable]
Object = Dict[str, Serializable]


def set_default(obj):
    if isinstance(obj, set):
        return list(obj)
    raise TypeError


def flatten(object_: Object,
            *,
            path_separator: str = '.') -> Array[Object]:
    keys = set(object_)
    result = [dict(object_)]
    while keys:
        key = keys.pop()
        new_result = []
        for index, record in enumerate(result):
            try:
                value = record[key]
            except KeyError:
                new_result.append(record)
            else:
                if isinstance(value, dict):
                    del record[key]
                    new_value = flatten_nested_objects(
                        value,
                        prefix=key + path_separator,
                        path_separator=path_separator
                    )
                    keys.update(new_value.keys())
                    new_result.append({**new_value, **record})
                elif isinstance(value, list):
                    del record[key]
                    new_records = [
                        flatten_nested_objects(sub_value,
                                               prefix=key + path_separator,
                                               path_separator=path_separator)
                        for sub_value in value
                    ]
                    keys.update(chain.from_iterable(map(dict.keys,
                                                        new_records)))
                    if new_records:
                        new_result.extend({**new_record, **record}
                                          for new_record in new_records)
                    else:
                        new_result.append(record)
                else:
                    new_result.append(record)
        result = new_result
    return result


@singledispatch
def flatten_nested_objects(object_: Serializable,
                           *,
                           prefix: str = '',
                           path_separator: str) -> Object:
    return {prefix[:-len(path_separator)]: object_}


@flatten_nested_objects.register(dict)
def _(object_: Object,
      *,
      prefix: str = '',
      path_separator: str) -> Object:
    result = dict(object_)
    for key in list(result):
        result.update(flatten_nested_objects(result.pop(key),
                                             prefix=(prefix + key
                                                     + path_separator),
                                             path_separator=path_separator))
    return result


@flatten_nested_objects.register(list)
def _(object_: Array,
      *,
      prefix: str = '',
      path_separator: str) -> Object:
    return {prefix[:-len(path_separator)]: list(map(partial(
            flatten_nested_objects,
            path_separator=path_separator),
        object_))}


def string_helper(json_dict):
    string_sequence = json_dict['sequence']
    string_sequence = string_sequence.replace("[","",1)
    string_sequence = string_sequence[::-1].replace("]","",1)[::-1]
    string_sequence = string_sequence.split('], [')
    string_sequence[0] = string_sequence[0].lstrip('[')
    string_sequence[-1] = string_sequence[-1].rstrip(']')

    return string_sequence


def getBodyForUrl(urlToFind, previousResponse, GPTcontent):
    try:
        print(urlToFind)
        for ms in microservices:
            host = ms['host']
            methodToRequestMap = ms['methodToRequestMap']
            for key in methodToRequestMap:
                if (key == "POST"):
                    requestList = methodToRequestMap[key]
                    for ele in requestList:
                        url = host + ele['url']
                        if (urlToFind == url):
                            if  not previousResponse:
                                print("previousResponse")
                                print(previousResponse)
                                response = openai.ChatCompletion.create(
                                    model="gpt-3.5-turbo",
                                    messages=[
                                        {"role": "system",
                                            "content": "You are a helpful assistant that provides sample json data for HTTP POST requests. These are a sequence of HTTP requests so please use the same context in subsequent requests"},
                                        {"role": "user", "content": "using the same context provide one json data that follows the key value information without other things: {0}.Use {1} as reference to substitute for values in required places. Populate usa related values for non referenced fields.".format(
                                            ele['example'], ele['prompt'])},
                                        {"role": "user", "content": "For values that could not be found from above context, use {0} for the same. For dates use the format: yyyy-MM-dd'T'HH:mm:ss".format(GPTcontent)},    
                                        {"role": "user", "content": "using above context, populate with USA related data for each field"}
                                    ]
                                )
                                print("prompt: {}".format(ele['prompt']))
                                content = response['choices'][0]['message']['content']
                                content_json = content.split("{", 1)[1]
                                content_json = "{" + \
                                    content_json.rsplit("}", 1)[0] + "}"
                                content_json = json.loads(content_json)
                                print("Generated Json")
                                print(content_json)
                                return content_json
                            else:
                                print("previousResponse")
                                print(previousResponse)
                                response = openai.ChatCompletion.create(
                                    model="gpt-3.5-turbo",
                                    messages=[
                                        {"role": "system",
                                            "content": "You are a helpful assistant that provides sample json data for HTTP POST requests. These are a sequence of HTTP requests so please use the same context in subsequent requests"},
                                        {"role": "user", "content": "The previous POST request returned the json: {0} and Some fields needs to be populated with values in {1}".format(
                                            previousResponse, ele['prompt'])},
                                        {"role": "user", "content": "using the same context and reusing the attribute values from all the previous responses provide one json data that follows the json structure: {0}.".format(
                                            ele['example']) },
                                        {"role": "user", "content": "For values that could not be found from above context, use {} for the same.For dates use the format: yyyy-MM-dd'T'HH:mm:ss".format(GPTcontent)},    
                                        {"role": "user", "content": "using above context, populate with USA related data for each field"}
                                    ]
                                )
                                print("prompt: {}".format(ele['prompt']))
                                content = response['choices'][0]['message']['content']
                                content_json = content.split("{", 1)[1]
                                content_json = "{" + \
                                    content_json.rsplit("}", 1)[0] + "}"
                                content_json = json.loads(content_json)
                                print("Generated Json")
                                print(content_json)
                                return content_json
    except Exception as e:
        print(e)
        print(traceback.format_exc())
    return ""


def getParamFromAlreadyGeneratedValues(allJsonKeyValues, param):
    paramSet = set()
    for i in allJsonKeyValues:
        for j in i:
            for json in j:
                if param.lower() in json.lower() or json.lower() in param.lower():
                    # print(param + " matches with " + json)
                    paramSet.add(j[json])
    return paramSet


def processPostID(allJsonKeyValues, postUrl, postUrlIDVariation, logger_helper):
    if "{" not in postUrl:
        postUrlIDVariation.append(postUrl)
        logger_helper[postUrlIDVariation[-1]] = postUrl
    else:
        allParams = re.findall('\{.*?\}', postUrl)
        print(allParams)
        for param in allParams:
            paramValues = getParamFromAlreadyGeneratedValues(allJsonKeyValues, param)
            if len(paramValues) == 0:
                tmp = postUrl
                if "id" in param.lower():
                    postUrlIDVariation.append(tmp.replace(param, "1"))
                else:
                    postUrlIDVariation.append(tmp.replace(param, ""))
                logger_helper[postUrlIDVariation[-1]] = postUrl
            else:
                for p in paramValues:
                    tmp = postUrl
                    stringVal = str(p)
                    postUrlIDVariation.append(tmp.replace(param, stringVal))
                    logger_helper[postUrlIDVariation[-1]] = postUrl



def processGetRequests(allJsonKeyValues, getUrl, getUrlsProcessed, allIdFields, logg_helper):
    if "{" not in getUrl:
        getUrlsProcessed.append(getUrl)
        logg_helper[getUrlsProcessed[-1]] = getUrl
    else:
        allParams = re.findall('\{.*?\}', getUrl)
        print(allParams)
        for param in allParams:
            paramValues = getParamFromAlreadyGeneratedValues(
                allJsonKeyValues, param)
            print(paramValues)
            for p in paramValues:
                tmp = getUrl
                stringVal = str(p)
                getUrlsProcessed.append(tmp.replace(param, stringVal))
                logg_helper[getUrlsProcessed[-1]] = getUrl
                paramOnly = param.replace("{", "").replace("}", "")
                print("paramOnly: " + paramOnly)
                if paramOnly not in allIdFields:
                    allIdFields[paramOnly] = paramValues
                else:
                    allIdFields[paramOnly].update(paramValues)

            # response = openai.ChatCompletion.create(
            #     model="gpt-3.5-turbo",
            #     messages=[
            #         {"role": "system",
            #          "content": "You are generating HTTP GET requests"},
            #         {"role": "user", "content": "Using the same context as the POST requests and reusing the attribute values from the previous responses replace the params between { } in the url {}. Provide only the url as a response without any other text information".format(
            #                                     getUrl)}
            #     ]
            # )
            # print(response['choices'][0]['message']['content'])


def replaceAdditionalParams(processedUrls, logger_helper):
    try:
        remove = []
        add = []
        for url in processedUrls:
            if "{" in url:
                response = openai.ChatCompletion.create(
                    model="gpt-3.5-turbo",
                    messages=[
                        {"role": "system",
                        "content": "You are generating HTTP GET requests"},
                        {"role": "user", "content": "Using the same context as the POST requests and reusing the attribute values from all the previous responses replace the params between braces in the url {} with one value. Provide only the url as a response without any other text information".format(
                                                    url)}
                    ]
                )
                remove.append(url)
                k = logger_helper.pop(url)
                add.append(response['choices'][0]['message']['content'])
                logger_helper[response['choices'][0]['message']['content']] = k
        for j in remove:
            processedUrls.remove(j)
        for j in add:
            processedUrls.append(j)
    
    except Exception as e:
        print(e)
        print(traceback.format_exc())


def getPutValuesForJson(jsonStr, idJsonLoad):

    try:
        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system",
                "content": "You are a helpful assistant that provides sample json data for HTTP PUT requests using the same context as the previous POST and GET requests."},
                {"role": "user", "content": "using the same context and reusing the id fields from the json {} provide one json data that follows the json structure: {}".format(
                                            idJsonLoad, jsonStr)},
                {"role": "user", "content": "using above context, populate with USA related data for each field"}
            ]
        )
        content = response['choices'][0]['message']['content']
        print(content)
        content_json = content.split("{", 1)[1]
        content_json = "{" + \
            content_json.rsplit("}", 1)[0] + "}"
        content_json = json.loads(content_json)
        print("Generated Json")
        print(content_json)
    except Exception as e:
        print(e)
        print(traceback.format_exc())
    return content_json


def pre_run(microservices, logger):
    # # authenticate using  username and password
    # authentication = {
    #     "email": "admin@example.com",
    #     "password": "1password"
    # }

    # #  get the auth token and customer id
    # login_url = "http://localhost:8080/auth"
    # resp = requests.post(login_url, json=authentication)
    token = "" #resp.json()['token']
    print(token)
    allJsonKeyValues = []
    prevRespJson = []
    GPTcontent = []
    run(microservices, token, allJsonKeyValues, prevRespJson, GPTcontent, logger)

def run(microservices, token, allJsonKeyValues, prevRespJson, GPTcontent, logger):
    login_url = "" #"http://localhost:8080/auth"
    finalReqs = {}
    finalReqs['POST'] = {}
    finalReqs['GET'] = {}
    finalReqs['PUT'] = {}
    finalReqs['DELETE'] = {}
    finalReqs['PATCH'] = {}

    for ms in microservices:
        host = ms['host']
        methodToRequestMap = ms['methodToRequestMap']
        for key in methodToRequestMap:
            if (key == "POST"):
                requestList = methodToRequestMap[key]
                for ele in requestList:
                    if 'body' in ele:
                        url = host + ele['url']
                        logger['POST'][url] = collections.defaultdict(int)
                        finalReqs['POST'][url] = ""
            elif (key == "GET"):
                requestList = methodToRequestMap[key]
                for ele in requestList:
                    url = host + ele['url']
                    logger['GET'][url] = collections.defaultdict(int)
                    finalReqs['GET'][url] = ""
            elif (key == "PUT"):
                requestList = methodToRequestMap[key]
                for ele in requestList:
                    if 'body' in ele:
                        url = host + ele['url']
                        logger['PUT'][url] = collections.defaultdict(int)
                        finalReqs['PUT'][url] = ele['example']
            elif (key == "DELETE"):
                requestList = methodToRequestMap[key]
                for ele in requestList:
                    url = host + ele['url']
                    logger['DELETE'][url] = collections.defaultdict(int)
                    finalReqs['DELETE'][url] = ""
            elif (key == "PATCH"):
                requestList = methodToRequestMap[key]
                for ele in requestList:
                    if 'body' in ele:
                        url = host + ele['url']
                        logger['PATCH'][url] = collections.defaultdict(int)
                        finalReqs['PATCH'][url] = ele['example']

    
    urls = ",".join(finalReqs['POST'].keys())
    print(urls)
    if urls:
        urlList = urls.split(",")
        if len(urlList)>2:
            response2 = openai.ChatCompletion.create(
                model="gpt-3.5-turbo",
                messages=[
                    {"role": "system",
                    "content": "You are working with HTTP POST request URLs"},
                    {"role": "user", "content": "Can you logically order these POST URLs without any additional information as a comma separated line {}".format(
                        urls)}
                ]
            )

            content2 = response2['choices'][0]['message']['content']
            urlList = [x.strip() for x in content2.split(',')]

        if login_url in urlList:
            urlList.remove(login_url)
        print(urlList)

        logger_helper = {}
        for url in urlList:
            if url.endswith('.'):
                url = url[:-1]
            body = getBodyForUrl(url, prevRespJson, GPTcontent)
            allJsonKeyValues.append(flatten(body))
            postUrlIDVariation = []
            processPostID(allJsonKeyValues, url, postUrlIDVariation, logger_helper)
            # print("URL : " + url)
            # print(body)
            for postUrl in postUrlIDVariation:
                try:
                    resp = requests.post(postUrl, json=body, headers={
                                        'X-Auth-Token': token})
                    print(resp.status_code)
                    logger['POST'][logger_helper[postUrl]][resp.status_code] += 1
                    if resp.status_code == 200:
                        GPTcontent.append(body)
                        id_gen = url.split("/")[-1]
                        id_gen = id_gen[:-1]
                        resp_json = {}
                        if resp != "":
                            resp = resp.json()
                            for key in resp:
                                if key == 'id':
                                    resp_json[id_gen + key] = resp[key]
                                else:
                                    resp_json[key] = resp[key]

                            prevRespJson.append(str(resp_json))
                            allJsonKeyValues.append(flatten(resp_json))
                except Exception as e:
                    print(e)
                    print(traceback.format_exc())
                    print(resp.status_code)
            
            postUrlIDVariation = []

    # str1 = "[[{\"email\":\"usauser@example.com\",\"password\":\"usapassword\"}],[{\"email\":\"usauser@example.com\"}],[{\"email\":\"usauser@example.com\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"birthday\":\"1980-10-20T15:00:00-07:00\",\"city\":\"Los Angeles\",\"streetAddress\":\"123 Main St\",\"postalCode\":\"90001\",\"phoneNumber\":\"+1 (555) 123-4567\"}],[{\"_links.self.href\":\"http://localhost:8080/customers/b2rdal6fiw\",\"_links.address.change.href\":\"http://localhost:8080/customers/b2rdal6fiw/address\",\"customerId\":\"b2rdal6fiw\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"birthday\":\"1980-10-20T00:00:00.000+00:00\",\"streetAddress\":\"123 Main St\",\"postalCode\":\"90001\",\"city\":\"Los Angeles\",\"email\":\"admin@example.com\",\"phoneNumber\":\"(555) 123-4567\"}],[{\"insuranceQuote.expirationDate\":\"2022-02-28T23:59:59-07:00\",\"insuranceQuote.insurancePremium.amount\":2000,\"insuranceQuote.insurancePremium.currency\":\"USD\",\"insuranceQuote.policyLimit.amount\":50000,\"insuranceQuote.policyLimit.currency\":\"USD\",\"customerInfo.customerId\":\"b2rdal6fiw\",\"customerInfo.firstname\":\"John\",\"customerInfo.lastname\":\"Doe\",\"customerInfo.contactAddress.streetAddress\":\"456 7th St\",\"customerInfo.contactAddress.postalCode\":\"90001\",\"customerInfo.contactAddress.city\":\"Los Angeles\",\"customerInfo.billingAddress.streetAddress\":\"456 7th St\",\"customerInfo.billingAddress.postalCode\":\"90001\",\"customerInfo.billingAddress.city\":\"Los Angeles\",\"statusHistory.date\":\"2021-08-25T14:30:00-07:00\",\"statusHistory.status\":\"pending\",\"insuranceOptions.startDate\":\"2021-09-01T00:00:00-07:00\",\"insuranceOptions.insuranceType\":\"auto\",\"insuranceOptions.deductible.amount\":500,\"insuranceOptions.deductible.currency\":\"USD\",\"id\":0,\"date\":\"2021-08-25T14:30:00-07:00\",\"policyId\":\"ABC123\"}],[{\"customerInfo.customerId\":\"b2rdal6fiw\",\"customerInfo.firstname\":\"John\",\"customerInfo.lastname\":\"Doe\",\"customerInfo.contactAddress.streetAddress\":\"456 7th St\",\"customerInfo.contactAddress.postalCode\":\"90001\",\"customerInfo.contactAddress.city\":\"Los Angeles\",\"customerInfo.billingAddress.streetAddress\":\"456 7th St\",\"customerInfo.billingAddress.postalCode\":\"90001\",\"customerInfo.billingAddress.city\":\"Los Angeles\",\"statusHistory.date\":\"2023-03-29T16:17:28.364+00:00\",\"statusHistory.status\":\"REQUEST_SUBMITTED\",\"insuranceOptions.startDate\":\"2021-09-01\",\"insuranceOptions.insuranceType\":\"auto\",\"insuranceOptions.deductible.amount\":500,\"insuranceOptions.deductible.currency\":\"USD\",\"id\":104,\"date\":\"2023-03-29T16:17:28.364+00:00\"}],[{\"insuringAgreement.agreementItems.title\":\"Liability Coverage\",\"insuringAgreement.agreementItems.description\":\"Coverage for Bodily Injury and Property Damage caused to others in an accident\",\"insurancePremium.amount\":750,\"insurancePremium.currency\":\"USD\",\"policyPeriod.startDate\":\"2021-09-01T00:00:00-06:00\",\"policyPeriod.endDate\":\"2022-09-01T00:00:00-06:00\",\"deductible.amount\":500,\"deductible.currency\":\"USD\",\"policyLimit.amount\":100000,\"policyLimit.currency\":\"USD\",\"customerId\":\"b2rdal6fiw\",\"policyType\":\"Auto Insurance\"},{\"insuringAgreement.agreementItems.title\":\"Comprehensive Coverage\",\"insuringAgreement.agreementItems.description\":\"Coverage for theft, fire, vandalism, and natural disasters\",\"insurancePremium.amount\":750,\"insurancePremium.currency\":\"USD\",\"policyPeriod.startDate\":\"2021-09-01T00:00:00-06:00\",\"policyPeriod.endDate\":\"2022-09-01T00:00:00-06:00\",\"deductible.amount\":500,\"deductible.currency\":\"USD\",\"policyLimit.amount\":100000,\"policyLimit.currency\":\"USD\",\"customerId\":\"b2rdal6fiw\",\"policyType\":\"Auto Insurance\"},{\"insuringAgreement.agreementItems.title\":\"Collision Coverage\",\"insuringAgreement.agreementItems.description\":\"Coverage for damage to your own car in an accident\",\"insurancePremium.amount\":750,\"insurancePremium.currency\":\"USD\",\"policyPeriod.startDate\":\"2021-09-01T00:00:00-06:00\",\"policyPeriod.endDate\":\"2022-09-01T00:00:00-06:00\",\"deductible.amount\":500,\"deductible.currency\":\"USD\",\"policyLimit.amount\":100000,\"policyLimit.currency\":\"USD\",\"customerId\":\"b2rdal6fiw\",\"policyType\":\"Auto Insurance\"}],[{\"policyPeriod.startDate\":\"2021-09-01T06:00:00.000+00:00\",\"policyPeriod.endDate\":\"2022-09-01T06:00:00.000+00:00\",\"insuringAgreement.agreementItems.title\":\"Liability Coverage\",\"insuringAgreement.agreementItems.description\":\"Coverage for Bodily Injury and Property Damage caused to others in an accident\",\"policyLimit.amount\":100000,\"policyLimit.currency\":\"USD\",\"deductible.amount\":500,\"deductible.currency\":\"USD\",\"_expandable\":\"customer\",\"insurancePremium.amount\":750,\"insurancePremium.currency\":\"USD\",\"policyId\":\"ycjdrcigyq\",\"customer\":\"b2rdal6fiw\",\"creationDate\":\"2023-03-29T16:17:51.722+00:00\",\"policyType\":\"Auto Insurance\"},{\"policyPeriod.startDate\":\"2021-09-01T06:00:00.000+00:00\",\"policyPeriod.endDate\":\"2022-09-01T06:00:00.000+00:00\",\"insuringAgreement.agreementItems.title\":\"Comprehensive Coverage\",\"insuringAgreement.agreementItems.description\":\"Coverage for theft, fire, vandalism, and natural disasters\",\"policyLimit.amount\":100000,\"policyLimit.currency\":\"USD\",\"deductible.amount\":500,\"deductible.currency\":\"USD\",\"_expandable\":\"customer\",\"insurancePremium.amount\":750,\"insurancePremium.currency\":\"USD\",\"policyId\":\"ycjdrcigyq\",\"customer\":\"b2rdal6fiw\",\"creationDate\":\"2023-03-29T16:17:51.722+00:00\",\"policyType\":\"Auto Insurance\"},{\"policyPeriod.startDate\":\"2021-09-01T06:00:00.000+00:00\",\"policyPeriod.endDate\":\"2022-09-01T06:00:00.000+00:00\",\"insuringAgreement.agreementItems.title\":\"Collision Coverage\",\"insuringAgreement.agreementItems.description\":\"Coverage for damage to your own car in an accident\",\"policyLimit.amount\":100000,\"policyLimit.currency\":\"USD\",\"deductible.amount\":500,\"deductible.currency\":\"USD\",\"_expandable\":\"customer\",\"insurancePremium.amount\":750,\"insurancePremium.currency\":\"USD\",\"policyId\":\"ycjdrcigyq\",\"customer\":\"b2rdal6fiw\",\"creationDate\":\"2023-03-29T16:17:51.722+00:00\",\"policyType\":\"Auto Insurance\"}],[{\"birthday\":\"1983-10-04T00:00:00-07:00\",\"postalCode\":\"94117\"}],[{\"riskFactor\":25}]]"
    # allJsonKeyValues = json.loads(str1)

    print("ALL VALUES ")
    print(logger)


    
    print(allJsonKeyValues)
    allIdFields = {}
    logger_helper = {}
    print("GET REQUESTS")
    getUrlsProcessed = []
    for k in finalReqs['GET'].keys():
        processGetRequests(allJsonKeyValues, k, getUrlsProcessed, allIdFields, logger_helper)
    for i in getUrlsProcessed:
        print(i)

    for k in allIdFields:
        print(k)
        print(allIdFields[k])

    print("Processed")
    replaceAdditionalParams(getUrlsProcessed, logger_helper)
    for i in getUrlsProcessed:
        print(i)
        try:
            resp = requests.get(
                i, headers={'X-Auth-Token': token, 'accept': '*/*'})
            print(resp.status_code)
            logger['GET'][logger_helper[i]][resp.status_code] += 1
            if resp.status_code == 200:
                prevRespJson.append(str(resp.json()))
        except Exception as e:
            print(e)
            print(traceback.format_exc())
            print(resp.status_code)

    print("PUT REQUESTS")
    finalProcessedPutReqs = {}
    logger_helper = {}
    for k in finalReqs['PUT'].keys():
        putUrlsProcessed = []
        processGetRequests(allJsonKeyValues, k, putUrlsProcessed, allIdFields, logger_helper)
        replaceAdditionalParams(putUrlsProcessed,logger_helper)
        for j in putUrlsProcessed:
            finalProcessedPutReqs[j] = finalReqs['PUT'][k]

    idJsonDump = json.dumps(allIdFields, default=set_default)
    idJsonLoad = json.loads(idJsonDump)
    print(idJsonLoad)

    for i in finalProcessedPutReqs:
        print(i)
        body = getPutValuesForJson(finalProcessedPutReqs[i], idJsonLoad)
        try:
            resp = requests.put(i, json=body, headers={
                                'X-Auth-Token': token, 'accept': '*/*'})
            print(resp.status_code)
            logger['PUT'][logger_helper[i]][resp.status_code] += 1
            if resp.status_code == 200:
                prevRespJson.append(str(resp.json()))
        except Exception as e:
            print(traceback.format_exc())
            print(resp.status_code)

    print("PATCH REQUESTS")
    finalProcessedPatchReqs = {}
    logger_helper = {}
    for k in finalReqs['PATCH'].keys():
        putUrlsProcessed = []
        processGetRequests(allJsonKeyValues, k, putUrlsProcessed, allIdFields, logger_helper)
        replaceAdditionalParams(putUrlsProcessed,logger_helper)
        for j in putUrlsProcessed:
            finalProcessedPatchReqs[j] = finalReqs['PATCH'][k]

    idJsonDump = json.dumps(allIdFields, default=set_default)
    idJsonLoad = json.loads(idJsonDump)
    print(idJsonLoad)

    for i in finalProcessedPatchReqs:
        print(i)
        body = getPutValuesForJson(finalProcessedPatchReqs[i], idJsonLoad)
        print(body)
        try:
            resp = requests.patch(i, json=body, headers={
                'X-Auth-Token': token, 'accept': '*/*'})
            print(resp.status_code)
            logger['PATCH'][logger_helper[i]][resp.status_code] += 1
            if resp.status_code == 200:
                prevRespJson.append(str(resp.json()))
        except:
            print(traceback.format_exc())
            print(resp.status_code)

    print("DELETE REQUESTS")

    deleteUrlsProcessed = []
    logger_helper = {}
    for k in finalReqs['DELETE'].keys():
        processGetRequests(allJsonKeyValues, k,
                        deleteUrlsProcessed, allIdFields,logger_helper)
        replaceAdditionalParams(deleteUrlsProcessed,logger_helper)
        logger_helper[deleteUrlsProcessed[-1]] = k
    for j in deleteUrlsProcessed:
        print(j)

    for i in deleteUrlsProcessed:
        print(i)
        try:
            resp = requests.delete(i, headers={
                'X-Auth-Token': token, 'accept': '*/*'})
            print(resp.status_code)
            logger['DELETE'][logger_helper[i]][resp.status_code] += 1
            if resp.status_code == 200:
                prevRespJson.append(str(resp.json()))
        except:
            print(traceback.format_exc())
            print(resp.status_code)



# chat GPT code to get data suggestions
openai.api_key = "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"
openai.organization = os.getenv("OPENAI_ORGANIZATION")

# read the pojo with the required data type ( please input the file location of struct.json)
# please input the unified swagger json
f = open('../output/uiuc-api-tester.json')
microservices = json.load(f)
logger_write = []

# track 1
for i in range(1, 5):
    try:
        logger = {}
        logger['POST'] = {}
        logger['POST'] = {}
        logger['GET'] = {}
        logger['PUT'] = {}
        logger['DELETE'] = {}
        logger['PATCH'] = {}    
        pre_run(microservices, logger)
        
    except Exception as e:
        print("An exception occurred")
        print(traceback.format_exc())
        print(e)
    
    data = {}
    data["Run "+str(i)] = logger
    logger_write.append(data)

with open("IndividualRecord.json", 'a+') as f:
    json.dump(logger_write, f)

print(" track 1 done")

#track 2
# dependency_file = open('dependency.json')
# json_dict = json.load(dependency_file)

# if json_dict:
#     string_sequence = string_helper(json_dict)
#     string_list = [x.split(",") for x in string_sequence]

#     for sequence in string_list:
#         for i in range(1,3):
#             # authenticate using  username and password
#             authentication = {
#                 "email": "admin@example.com",
#                 "password": "1password"
#             }

#             #  get the auth token and customer id
#             login_url = "http://localhost:8080/auth"
#             resp = requests.post(login_url, json=authentication)
#             token = resp.json()['token']
#             allJsonKeyValues = []
#             prevRespJson = []
#             for service in sequence: 
#                 print(service)
#                 for swagger_service in microservices:
#                     if swagger_service['microservice'] in service.strip() or service.strip() in swagger_service['microservice']:
#                         formatted_json = []
#                         formatted_json.append(swagger_service)
#                         try:
#                             run(formatted_json, token, allJsonKeyValues, prevRespJson)
#                         except Exception as e:
#                             print("An exception occurred in track 2")
#                             print(traceback.format_exc())
#                             print(e)
