package io.resttestgen.nominaltester.testers;

import io.resttestgen.nominaltester.models.ResponseDictionary;

public interface DictionaryBasedTester {
    ResponseDictionary getResponseDictionary();
    void setResponseDictionary(ResponseDictionary responseDictionary);
}
