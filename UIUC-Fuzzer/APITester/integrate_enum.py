import json

# get swagger json
f = open("../output/output.json")
data = json.load(f)

# get enum 
f = open("../input/enum-props/output_enum_message_struct.json")
enum = json.load(f)



def recursivePrompt(prompt, enum, jsnData):
    print(jsnData)
    for key in jsnData.keys():
        if type(jsnData[key]) == list:
            for value in jsnData[key]:
                recursivePrompt(prompt, enum, value)
        elif isinstance(jsnData[key], dict):
            recursivePrompt(prompt, enum, jsnData[key])
        
        for enum_keys in enum.keys():
            if key.lower() in enum_keys.lower() or  enum_keys.lower() in key.lower():
                 prompt.append('{} should be filled with one among {}'.format(key, enum[enum_keys]))
                  
     
    return prompt

target = 'example'
for jsonsArray in data:
    httpjson = jsonsArray['methodToRequestMap']
    for methods in httpjson:
            httpcontent = httpjson[methods]
            for content in httpcontent:
                  prompt = []
                  if target in content.keys():
                        customData =  content[target]
                        jsnData = json.loads(customData)
                        content['prompt'] = []
                        prompt = recursivePrompt(prompt, enum, jsnData)
                        if prompt:
                            for val in prompt:
                                content['prompt'].append(val)
                            
                            content['prompt'].append("For other relevant values, use the following json as reference: {}".format(enum))
                        


with open('../output/uiuc-api-tester.json', 'w') as f:
    json.dump(data, f)