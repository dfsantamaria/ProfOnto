#
#
#

import sys
from nltk.corpus import wordnet

sys.path.insert(0, "./lib")

from profeta.Types import *
from profeta.Main import *
from profeta.Lib import *

import telegram
import spacy


class language(Belief): pass

class synset_verb(Belief): pass

class intention(Reactor): pass

class int_with_cond(Reactor): pass

class verb(Belief): pass

class obj(Belief): pass

class conditions(ActiveBelief):
    def evaluate(self, cond):
        return cond() == cond()

class message(Reactor): pass

class go(Procedure): pass

BOT = None

class Fabulos(Sensor):

    def on_start(self):
        global BOT
        BOT = telegram.Bot("761251160:AAFI63ErogZxLFeS8X8ur6O1TxFjCjv1530")
        self.update_id = None
        self.msgs = [ ]

    def sense(self):
        global BOT
        while True:
            if self.msgs == []:
                for m in BOT.get_updates(offset=self.update_id): #, timeout=10):
                    if self.update_id is None:
                        self.update_id = m.update_id
                    self.update_id = self.update_id + 1
                    if m.message:
                        self.msgs.append(m)
            if self.msgs == []:
                continue
            m = self.msgs[0]
            del self.msgs[0]
            print(m.message.text)
            if m.message.text is None:
                continue

            message_data = m.message.text.lower().split()
            message_data.insert(0, m.message.chat.id)
            print(message_data)

            self.assert_belief(message(m.message.chat.id, m.message.text))


class Reply(Action):

    def execute(self, *args):
        m = []
        sender = args[0]()
        for v in args[1:]:
            m.append(v())
        message = " ".join(m)
        BOT.sendMessage(sender, message)


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


class SpaCyItParser:
    def __init__(self, message):

        self.message = message

        self.intentions = []
        self.modificators = []
        self.temp_modificators = []
        self.conditionals = []

        actual_subj = ""

        mark_open = False

        actual_cond_property = ""
        actual_gov_nsubj = ""
        actual_dep_nsubj = ""
        prev_tag = ""

        nlp = spacy.load('it')
        doc = nlp(message)

        for token in doc:
            print("{0} --{1}--> {2}".format(token.head.text, token.dep_, token.text))
            if mark_open is False:

                if token.dep_ == "mark":
                    mark_open = True

                if token.dep_ == "obj":
                    if actual_subj == token.head.text:
                        self.modificators.append((token.head.text, token.text))
                    else:
                        self.intentions.append((token.head.text, token.text))
                        actual_subj = token.head.text

                if token.dep_ == "nsubj:pass":
                    actual_subj = token.head.text
                    self.intentions.append((token.head.text, token.text))

                if token.dep_ == "advmod":
                    self.temp_modificators.append((actual_subj, token.text))

                if token.dep_ == "nsubj":
                    self.modificators.append((actual_subj, token.text))

                if token.dep_ == "obl" or token.dep_ == "nmod" or token.dep_ == "xcomp":
                        self.modificators.append((token.head.text, token.text))

                if token.dep_ == "amod":
                    if token.dep_ == prev_tag:
                        self.modificators.append((actual_subj, token.text))

                prev_tag = token.dep_

            else:

                if token.dep_ == "nsubj":
                    actual_dep_nsubj = token.text
                    actual_gov_nsubj = token.head.text
                    actual_cond_property = token.head.text

                if token.dep_ == "case":
                    if token.head.text == actual_gov_nsubj:
                        actual_cond_property = token.text+" "+actual_cond_property

                if token.dep_ == "advmod":
                        actual_cond_property = token.text+" "+actual_cond_property

                if token.dep_ == "nummod" or token.dep_ == "obl":
                    actual_cond_property = actual_cond_property+" "+token.text

                if token.dep_ == "mark" or token.dep_ == "cc":
                    if actual_dep_nsubj != "" and actual_cond_property != "":
                        self.conditionals.append((actual_dep_nsubj, actual_cond_property))
                    actual_cond_property = ""
                    actual_dep_nsubj = ""
                    actual_gov_nsubj = ""

        if actual_dep_nsubj != "" and actual_cond_property != "":
            self.conditionals.append((actual_dep_nsubj, actual_cond_property))

    def getIntentions(self):
        return self.intentions

    def getModificators(self):
        return self.modificators

    def getTemp_modificators(self):
        return self.temp_modificators

    def getConditionals(self):
        return self.conditionals




class DomoReply(Action):

    def execute(self, *args):
        m = []
        sender = args[0]()
        language = args[1]()

        for v in args[2:]:
            m.append(v())
        message = " ".join(m)

        if message[5:14] == "synset_vb":
            syns = wordnet.synsets(message[15:], pos=wordnet.VERB, lang=language)
            for synset in syns:
                BOT.sendMessage(sender, str(synset.name())+"\n"+
                                        str(synset.lemmas())+"\n"+
                                        str(synset.definition())+"\n"+
                                        str(synset.examples())+"\n"+
                                        "-----------------------")
        elif message[5:14] == "synset_nn":
            syns = wordnet.synsets(message[15:], pos=wordnet.NOUN, lang=language)
            for synset in syns:
                BOT.sendMessage(sender, str(synset.name()) + "\n" +
                                        str(synset.lemmas()) + "\n" +
                                        str(synset.definition()) + "\n" +
                                        str(synset.examples()) + "\n" +
                                        "-----------------------")
        elif message[5:7] == "dp":
            if language == "eng":
                nlp = spacy.load('en')
            else:
                nlp = spacy.load('it')
            doc = nlp(message[8:])
            for token in doc:
                BOT.sendMessage(sender, "{1}({0}, {2})".format(token.head.text, token.dep_, token.text))
        else:

            if language == "eng":
                dep = SpaCyEnParser(message)
            elif language == "ita":
                dep = SpaCyItParser(message)

            intentions = dep.getIntentions()
            modificators = dep.getModificators()
            temp_modificators = dep.getTemp_modificators()
            conditionals = dep.getConditionals()

            if len(conditionals) > 0:
                for x in intentions:
                    syns_verb = wordnet.synsets(x[0], lang=language, pos=wordnet.VERB)
                    mod = "none"
                    temp_mod = "today"

                    for z in modificators:
                        if x[0] == z[0] or x[1] == z[0]:
                            if mod == "none":
                                mod = z[1]
                            else:
                                mod = mod+", "+z[1]

                    for t in temp_modificators:
                        if x[0] == t[0]:
                            temp_mod = t[1]
                    for synset_verb in syns_verb:
                        syns_obj = wordnet.synsets(x[1], lang=language, pos=wordnet.NOUN)
                        for synset_obj in syns_obj:
                            self.assert_belief(int_with_cond(sender, synset_verb.name(), synset_obj.name(), mod, temp_mod, conditionals))
            else:
                for x in intentions:
                    syns_verb = wordnet.synsets(x[0], lang=language, pos=wordnet.VERB)
                    mod = "none"
                    temp_mod = "today"

                    for z in modificators:
                        if x[0] == z[0] or x[1] == z[0]:
                            if mod == "none":
                                mod = z[1]
                            else:
                                mod = mod+", "+z[1]

                    for t in temp_modificators:
                        if x[0] == t[0]:
                            temp_mod = t[1]
                    for synset_verb in syns_verb:
                        syns_obj = wordnet.synsets(x[1], lang=language, pos=wordnet.NOUN)
                        for synset_obj in syns_obj:
                            self.assert_belief(intention(sender, synset_verb.name(), synset_obj.name(), mod, temp_mod))

            BOT.sendMessage(sender, "INTENTIONS\n"+
                                    str(intentions)+"\n"+
                                    "MODIFICATORS\n"+
                                    str(modificators)+"\n"+
                                    "TEMPORAL MODIFICATORS\n"+
                                    str(temp_modificators)+"\n"+
                                    "CONDITIONALS\n"+
                                    str(conditionals)
                            )


class exec_plan(Action):

    def execute(self, *args):
        sender = args[0]()
        intention = args[1]()
        object = args[2]()
        mod = args[3]()
        temp_mod = args[4]()

        BOT.sendMessage(sender, "<<<< EXECUTING PLAN...>>>>\n"+
                                "intention: "+intention+"\n"+
                                "object: "+object+"\n"+
                                "mod: "+mod+"\n"+
                                "temp_mod: " + temp_mod)

def strategy():

    def_vars("C", "X", "Y", "Z", "T", "W")
    go() >> [show_line("starting"), Fabulos().start, +language("EN")]

    +message(C, "hello fabulos") / language("EN") >> [Reply(C, "Hello!")]
    +message(C, "ciao fabulos") / language("IT") >> [Reply(C, "Ciao a te!")]

    +message(C, "fabulos") / language("EN") >> [Reply(C, "Yes, give me a command")]
    +message(C, "fabulos") / language("IT") >> [Reply(C, "Eccomi, dammi un comando")]

    +message(C, "switch ita") >> [Reply(C, "italian language set (usare verbo forma infinito)"), +language("IT"), -language("EN")]
    +message(C, "switch eng") >> [Reply(C, "english language set"), +language("EN"), -language("IT")]

    +message(C, "language?") / language("IT") >> [Reply(C, "italian")]
    +message(C, "language?") / language("EN") >> [Reply(C, "english")]

    +message(C, "?") / language("EN") >> [Reply(C, "language?: show current set language\n"
                                                   "show dp [sentence]: show dependency parsing of [sentence]\n"
                                                   "show synset_vb [verb]: show synonimous sets of [verb]\n"
                                                   "show synset_nn [noun]: show synonimous sets of [noun]\n"
                                                   "switch [language] (language=ita/eng): change current language")]
    +message(C, "?") / language("IT") >> [Reply(C, "language?: mostra il linguaggio corrente\n"
                                                   "show dp [frase]: mostra il parsing di dipendenza di [frase]\n"
                                                   "show synset_vb [verbo]: mostra i set dei sinonimi di [verb]\n"
                                                   "show synset_nn [sostantivo]: mostra i set dei sinonimi di [sostantivo]\n"
                                                   "switch [language] (language=ita/eng): cambia il linguaggio corrente")]

    +message(C, X) / language("IT") >> [DomoReply(C, "ita", X)]
    +message(C, X) / language("EN") >> [DomoReply(C, "eng", X)]

    # SIMPLE RULES
    +intention(C, "switch_on.v.01", "light.n.02", Z, T) >> [exec_plan(C, "turn_on", "light", Z, T)]
    +intention(C, "switch_off.v.01", "light.n.02", Z, T) >> [exec_plan(C, "turn_off", "light", Z, T)]
    +intention(C, "switch_on.v.01", "air_conditioner.n.01", Z, T) >> [exec_plan(C, "turn_on", "air_cond", Z, T)]
    +intention(C, "switch_off.v.01", "air_conditioner.n.01", Z, T) >> [exec_plan(C, "turn_off", "air_cond", Z, T)]
    +intention(C, "specify.v.02", "air_conditioner.n.01", Z, T) >> [exec_plan(C, "set", "air_cond", Z, T)]
    +intention(C, "play.v.06", "music.n.01", Z, T) >> [exec_plan(C, "play", "music", Z, T)]
    +intention(C, "discontinue.v.01", "music.n.01", Z, T) >> [exec_plan(C, "stop", "music", Z, T)]
    +intention(C, "open.v.01", "shutter.n.02", Z, T) >> [exec_plan(C, "open", "shutter", Z, T)]
    +intention(C, "close.v.01", "shutter.n.02", Z, T) >> [exec_plan(C, "close", "shutter", Z, T)]
    +intention(C, "clean.v.01", "floor.n.01", Z, T) >> [exec_plan(C, "clean", "floor", Z, T)]

    # RULE WITH CONDITION
    # C=sender, X=verb, Y=object, Z=modificator, T=day, W=conditions
    +int_with_cond(C, X, Y, Z, T, W) / conditions(W) >> [+intention(C, X, Y, Z, T)]

if __name__ == "__main__":
    PROFETA.run()
    strategy()
    PROFETA.shell(globals())

