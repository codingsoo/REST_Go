import sys
import stanza

text = sys.argv[1]
args = sys.argv[2]

nlp = stanza.Pipeline(lang='en', processors='tokenize,mwt,pos,lemma,depparse')
rels = ["case", "nsubj", "dep"]
noMeaningRels = ["cc", "det"]
pos = ["NOUN", "PROPN", "NUM"]

doc = nlp(text)

examples = []

for sentence in doc.sentences:
    startPoint = -1
    foundWord = ""
    isConjunction = False
    for word in sentence.words:
        if word.deprel in rels:
            startPoint = word.head
        if word.id >= startPoint > -1:
            if word.deprel in noMeaningRels:
                pass
            elif word.upos == "PUNCT":
                if foundWord != "":
                    examples.append(foundWord)
                    foundWord = ""
            elif isConjunction:
                foundWord = foundWord + word.text
                isConjunction = False
            elif word.deprel == "conj":
                foundWord = foundWord + word.text
                isConjunction = True
            elif word.upos in pos:
                examples.append(word.text)
            else:
                foundWord = word.text

for example in list(set(examples)):
    flag = True
    for arg in args.replace(" ", "").split(','):
        if arg in example:
            print("InterParamDep: " + example)
            flag = False
            break
    if flag:
        print("Example Value: " + example)