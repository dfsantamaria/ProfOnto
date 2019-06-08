import spacy
import sys
from nltk.corpus import wordnet


class SpaCyEnParser:
    def __init__(self, message):

        self.message = message

        self.intentions = []
        self.modificators = []
        self.temp_modificators = []
        self.conditionals = []

        gov_prt = ""
        actual_gov_dobj = ""

        dep_compound = ""
        gov_compound = ""

        root = ""

        comp_cond_dep = ""

        cond_open = False
        cond_gov = ""

        nlp = spacy.load('en')
        doc = nlp(message)

        for token in doc:
            print("{0} --{1}--> {2}".format(token.head.text, token.dep_, token.text))

            if token.dep_ == "ROOT":
                root = token.head.text
                actual_gov_dobj = token.head.text

            if token.dep_ == "mark" or token.dep_ == "advmod":
                cond_open = True

            if cond_open is False:

                if token.dep_ == "prt":
                    gov_prt = token.head.text
                    prt = token.text

                if token.dep_ == "dobj":

                    if gov_compound == token.text:
                        actual_dep_dobj = dep_compound+" "+token.text
                    else:
                        actual_dep_dobj = token.text

                    if token.head.text == gov_prt:
                        actual_gov_dobj = token.head.text+"_"+prt
                        self.intentions.append((actual_gov_dobj, actual_dep_dobj))
                    else:
                        actual_gov_dobj = token.head.text
                        self.intentions.append((actual_gov_dobj, actual_dep_dobj))

                if token.dep_ == "compound":
                    gov_compound = token.head.text
                    dep_compound = token.text

                if token.dep_ == "pobj":
                    if token.text == gov_compound:
                        self.modificators.append((actual_gov_dobj, (dep_compound+" "+token.text)))
                        gov_compound = ""
                        dep_compound = ""
                    else:
                        self.modificators.append((actual_gov_dobj, token.text))

                if token.dep_ == "npadvmod":
                    self.temp_modificators.append((actual_gov_dobj, token.text))

            else:

                if token.dep_ == "compound":
                    comp_cond_dep = token.text

                if token.dep_ == "nsubj":
                    if comp_cond_dep != "":
                        cond_gov = comp_cond_dep+" "+token.text
                        comp_cond_dep = ""
                    else:
                        cond_gov = token.text

                if token.dep_ == "pobj":
                    self.conditionals.append((cond_gov, token.head.text+" "+token.text))
                    cond_gov = ""

                if token.dep_ == "advmod" or token.dep_ == "quantmod":
                    if cond_gov != "":
                        self.conditionals.append((cond_gov, token.text+" "+token.head.text))
                        cond_gov = ""

                if token.dep_ == "acomp" or token.dep_ == "attr":
                    if cond_gov != "":
                        self.conditionals.append((cond_gov, token.text))
                        cond_gov = ""
        if len(self.intentions) == 0:
            self.intentions.append((root, root))


    def getIntentions(self):
            return self.intentions

    def getModificators(self):
            return self.modificators

    def getTemp_modificators(self):
            return self.temp_modificators

    def getConditionals(self):
            return self.conditionals


dep = SpaCyEnParser('turn off the light')
intentions = dep.getIntentions()
print(intensions)
modificators = dep.getModificators()
print(modificators)
temp_modificators = dep.getTemp_modificators()
print(temp_modificators)
conditionals = dep.getConditionals()
print(conditionals)

