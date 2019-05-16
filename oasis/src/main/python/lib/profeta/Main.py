#
#
#

from __future__ import print_function

import threading
import sys
import readline

from profeta.Types  import *
from profeta.Runtime  import *

__all__ = [ 'PROFETA' ]

# ------------------------------------------------
class PROFETA:

    @classmethod
    def set_debug(cls, debug):
        pass

    @classmethod
    def run_agent(cls, agent = DEFAULT_AGENT):
        Runtime.run_agent(agent)

    @classmethod
    def run(cls, main = None, agent = DEFAULT_AGENT):
        Runtime.run_agents()
        if main is not None:
            PROFETA.achieve(main, agent)

    @classmethod
    def stop_agent(cls, agent = DEFAULT_AGENT):
        Runtime.stop_agent(agent)

    @classmethod
    def stop(cls):
        for a in Runtime.agents():
            Runtime.stop_agent(a)

    @classmethod
    def shell(cls, g, agent = None):
        sh = SHELL(g, agent)
        sh.run()

    @classmethod
    def kb(cls, agent = DEFAULT_AGENT):
        return Runtime.get_engine(agent).knowledge()

    @classmethod
    def agents(cls):
        return Runtime.agents()

    @classmethod
    def engine(cls, agent = DEFAULT_AGENT):
        return Runtime.get_engine(agent)

    @classmethod
    def assert_belief(cls, b, agent = DEFAULT_AGENT):
        if isinstance(b, Belief):
            Runtime.get_engine(agent).add_belief(b)
        else:
            raise NotABeliefException()

    @classmethod
    def retract_belief(cls, b, agent = DEFAULT_AGENT):
        if isinstance(b, Belief):
            Runtime.get_engine(agent).remove_belief(b)
        else:
            raise NotABeliefException()

    @classmethod
    def achieve(cls, g, agent = DEFAULT_AGENT):
        if isinstance(g, Procedure):
            Runtime.get_engine(agent).achieve(g)
        else:
            raise NotAProcedureException()

    @classmethod
    def add_sensor(cls, sensor):
        cls.__engine.add_event_poller(sensor)

    @classmethod
    def set_current_episode(cls, ep):
        cls.__engine.set_current_episode(ep)



# ------------------------------------------------
class Completer:
    def __init__(self, words):
        self.words = words
        self.prefix = None
    def complete(self, prefix, index):
        if prefix != self.prefix:
            # we have a new prefix!
            # find all words that start with this prefix
            self.matching_words = [
                w for w in self.words if w.startswith(prefix)
                ]
            self.prefix = prefix
        try:
            return self.matching_words[index]
        except IndexError:
            return None

# ------------------------------------------------
class SHELL:

    def __init__(self, g, a):
        self.__globals = g
        self.__current_agent = DEFAULT_AGENT
        if a is not None:
            if isinstance(a,str):
                self.__current_agent = a
            else:
                self.__current_agent = a.__name__

    def run(self):
        c = Completer([ "help", "assert", "+", "retract", "-", "agents", "agent",
                        "plans", "kb", "waiting", "quit" ])
        readline.set_completer(c.complete)
        readline.parse_and_bind('tab: complete')
        readline.parse_and_bind('set editing-mode vi')
        print("PROFETA Release 2.0.0-alpha. Autonomous Robotic Lab @ Uversity of Catania (santoro@dmi.unict.it)")
        print("")
        while True:
            if sys.version_info[0] == 2:
                s = raw_input("ProfetaShell: {0} > ".format(self.__current_agent)).strip()
            else:
                s = input("ProfetaShell: {0} > ".format(self.__current_agent)).strip()
            if s == "":
                continue
            if s[0] == '+':
                s = "assert " + s[1:]
            if s[0] == '-':
                s = "retract " + s[1:]
            if s[0] == '~':
                s = "achieve " + s[1:]
            args = s.split()
            cmd = "C_" + args[0]
            if not(hasattr(self,cmd)):
                self.C_achieve([s])
                #print ("Invalid command")
                #continue
            else:
                getattr(self, cmd)(args[1:])


    def C_help(self, args):
        print ("")
        print ("+B                 asserts a belief")
        print ("-B                 retract a belief")
        print ("G                  achieves a goal")
        print ("plans              shows plans of current agent")
        print ("agents             shows defined agents")
        print ("agent a            set current agent")
        print ("kb                 prints the knowledge base")
        #print ("verbose on|off     sets the verbosity on or off")
        print ("help               shows help")
        print ("quit               quits PROFETA")
        print ("")


    def C_kb(self, args):
        PROFETA.kb(self.__current_agent).show()


    def C_agents(self, args):
        print("\nCurrent defined agents:")
        for a in PROFETA.agents():
            print("\t" + a)
        print("")


    def C_agent(self, args):
        if len(args) == 0:
            print(self.__current_agent)
        else:
            self.__current_agent = args[0]


    def C_plans(self, args):
        e = PROFETA.engine(self.__current_agent)
        pl = e.plans().list_all_plans()
        print("\nPlans of agent " + self.__current_agent + ":\n")
        for p in pl:
            print("\t" + repr(p))
        print("")

    def C_waiting(self, args):
        e = PROFETA.engine(self.__current_agent)
        (_, pl) = e.waiting_plans().all()
        print("\nWaiting plans of agent " + self.__current_agent + ":\n")
        for n in pl:
            print("Event " + n + ":")
            for p in pl[n]:
                print("\t" + repr(p))
            print("")


    def C_quit(self, args):
        PROFETA.stop()
        sys.exit(0)


    def C_assert(self, args):
        if len(args) == 0:
            print ("assert: missing belief")
            return
        try:
            B = eval(args[0], self.__globals)
            PROFETA.assert_belief(B, self.__current_agent)
        except:
            print ("Unexpected error in ASSERT:", sys.exc_info()[0])


    def C_retract(self, args):
        if len(args) == 0:
            print ("retract: missing belief")
            return
        try:
            B = eval(args[0], self.__globals)
            PROFETA.retract_belief(B, self.__current_agent)
        except:
            print ("Unexpected error in RETRACT:", sys.exc_info()[0])


    def C_achieve(self, args):
        if len(args) == 0:
            print ("achieve: missing goal")
            return
        try:
            G = eval(args[0], self.__globals)
            PROFETA.achieve(G, self.__current_agent)
        except NameError:
            print ("achieve: undefined goal")
        except :
            print ("Unexpected error in ACHIEVE:", sys.exc_info()[0])


    def C_verbose(self, args):
        if len(args) == 0:
            print ("verbose: missing parameter")
            return
        if args[0] == "on":
            PROFETA.set_debug(True)
        elif args[0] == "off":
            PROFETA.set_debug(False)
        else:
            print ("verbose: invalid parameter")

