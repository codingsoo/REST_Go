import sys
from nltk.corpus import wordnet

args1 = sys.argv[1]
args2 = sys.argv[2]

requestParams = list(set(args1.replace(" ", "").split(',')))
responseParams = list(set(args2.replace(" ", "").split(',')))

score = {}
reqRes = {}

for word1 in requestParams:
    score[word1] = [0, 0, 0]
    reqRes[word1] = ["", "", ""]
    for word2 in responseParams:
        wordFromList1 = wordnet.synsets(word1)
        wordFromList2 = wordnet.synsets(word2)
        if len(wordFromList1) > 0 and len(wordFromList2) > 0:
            s = wordFromList1[0].wup_similarity(wordFromList2[0])
            if s > score[word1][2]:
                score[word1][2] = s
                reqRes[word1][2] = word2
            if s > score[word1][1]:
                score[word1][1], score[word1][2] = score[word1][2], score[word1][1]
                reqRes[word1][1], reqRes[word1][2] = reqRes[word1][2], reqRes[word1][1]
            if s > score[word1][0]:
                score[word1][0], score[word1][1] = score[word1][1], score[word1][0]
                reqRes[word1][0], reqRes[word1][1] = reqRes[word1][1], reqRes[word1][0]

print(reqRes)