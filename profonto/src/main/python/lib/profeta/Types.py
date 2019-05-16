#
# Types.py
#

import types
import threading

from profeta.Runtime import *
from profeta.Exceptions import *
from functools import reduce

__all__ = [ 'Belief', 'Reactor', 'SingletonBelief',
            'Action', 'Procedure', 'ActiveBelief',
            'def_vars', 'def_actor',
            'AddBeliefEvent', 'DeleteBeliefEvent',
            'Sensor', 'AsyncSensor',
            'DATA_TYPE_REACTOR' ]

CONSTANT = 0
VARIABLE = 1

DATA_TYPE_BELIEF = 1
DATA_TYPE_REACTOR = 2
DATA_TYPE_SINGLETON_BELIEF = 3

# -----------------------------------------------
class Constant(object):

    def __init__(self, uTerm):
        self.type = CONSTANT
        self.value = uTerm

    def __repr__(self):
        return repr(self.value)

    def __call__(self, *args):
        return self.value

    def bound(self):
        return True


# -----------------------------------------------
class Variable(object):

    def __init__(self, uTerm):
        self.type = VARIABLE
        self.name = uTerm
        self.value = None

    def __repr__(self):
        if self.value is None:
            return self.name
        else:
            return repr(self.name) + "(" + repr(self.value) + ")"

    def __call__(self, *args):
        if len(args) == 1:
            self.value = args[0]
        return self.value

    def bound(self):
        return self.value is not None


#
# -----------------------------------------------
def def_vars(*vlist):
    for v in vlist:
        globals()['__builtins__'][v] = Variable(v)

def def_actor(a):
    Runtime.agent(a)

# -----------------------------------------------
class AtomicFormula(object):

    def __init__(self, *args, **kwargs):
        self.__make_terms(args)
        self.data_type = None
        self.tag = None
        self.__bindings = {}

    def __repr__(self):
        if len(self.__terms) == 0:
            repr_string = self.__class__.__name__ + "()"
        else:
            repr_string = self.__class__.__name__ + "(" + \
                reduce (lambda x,y : x + ", " + y,
                        map (lambda x: repr(x), self.__terms)) + \
                        ")"
        return repr_string

    def name(self):
        return self.__class__.__name__

    def terms(self):
        return self.__terms

    def bind(self, b):
        self.__bindings = b

    def __make_terms(self, args):
        self.__terms = []
        for t in args:
            if isinstance(t, Variable):
                v = t
            else:
                v = Constant(t)
            self.__terms.append(v)

    def __eq__(self, uT):
        return self.match_constants(uT)

    def match_constants(self, uT):
        l = len(self.__terms)
        uTerms = uT.terms()
        if l != len(uTerms):
            return False
        for i in range(0, l):
            Lhs = self.__terms[i]
            Rhs = uTerms[i]
            if (Lhs.type == VARIABLE)or(Rhs.type == VARIABLE):
                return True
            if Lhs.value != Rhs.value:
                return False
        return True


    # self --> belief with constant terms
    # uT ----> belief with variables
    def match(self, uT):
        l = len(self.__terms)
        uTerms = uT.terms()
        if l != len(uTerms):
            return False
        for i in range(0, l):
            Lhs = self.__terms[i]
            Rhs = uTerms[i]
            if Lhs.type == VARIABLE:
                raise NotAGroundTermException()
            if Rhs.type == VARIABLE:
                if Rhs.name in self.__bindings:
                    value = self.__bindings[Rhs.name]
                else:
                    value = Lhs.value # var unbound, get the same value of Lhs
                    self.__bindings[Rhs.name] = value
            else:
                value = Rhs.value
            if Lhs.value != value:
                return False
        return True

    def assign(self, ctx):
        for i in range(len(self.__terms)):
            t = self.__terms[i]
            if t.type == VARIABLE:
                if t.name in ctx:
                    self.__terms[i] = Constant(ctx[t.name])
                else:
                    raise UnboundVariableException()


# -------------------------------------------------
class AddDelBeliefEvent:

    ADD = 1
    DEL = 2

    def __init__(self, uBel):
        self.__belief = uBel
        self.event_type = 0

    def __repr__(self):
        return self.sign() + repr(self.__belief)

    def __eq__(self, other):
        return self.__belief == other.get_belief

    def name(self):
        return self.sign() + self.__belief.name()

    def get_belief(self):
        return self.__belief

    def __rshift__(self, actionList):
        if isinstance(actionList, list):
            p = Plan(self, None, actionList)
            Runtime.add_plan(p)
            return p
        else:
            raise NotAnActionListException()

    def __truediv__(self, uTerm):
        return self.__div_operator(uTerm)

    def __div__(self, uTerm):
        return self.__div_operator(uTerm)

    def __div_operator(self, uTerm):
        if isinstance(uTerm, Belief):
            p = Plan(self, ContextCondition(uTerm))
            Runtime.add_plan(p)
            return p
        elif isinstance(uTerm, ActiveBelief):
            p = Plan(self, ContextCondition(uTerm))
            Runtime.add_plan(p)
            return p
        elif isinstance(uTerm, ContextCondition):
            p = Plan(self, uTerm)
            Runtime.add_plan(p)
            return p
        else:
            raise InvalidContextException()



# -------------------------------------------------
class AddBeliefEvent(AddDelBeliefEvent):

    def __init__(self, uBel):
        AddDelBeliefEvent.__init__(self, uBel)
        self.event_type = AddDelBeliefEvent.ADD

    def sign(self):
        return '+'

# -------------------------------------------------
class DeleteBeliefEvent(AddDelBeliefEvent):

    def __init__(self, uBel):
        AddDelBeliefEvent.__init__(self, uBel)
        self.event_type = AddDelBeliefEvent.DEL

    def sign(self):
        return '-'

# -----------------------------------------------
class Belief(AtomicFormula):

    def __init__(self, *args, **kwargs):
        AtomicFormula.__init__(self, *args, **kwargs)
        self.data_type = DATA_TYPE_BELIEF
        self.dest = None
        self.source = None
        self.source_agent = None

    def __repr__(self):
        s = AtomicFormula.__repr__(self)
        modifs = ""
        if self.dest is not None:
            modifs = "'to':" + repr(self.dest) + ","
        if self.source is not None:
            modifs = "'from':" + repr(self.source) + ","
        if modifs != "":
            modifs = "[{" + modifs[:-1] + "}]"
        return s + modifs

    def name(self):
        return self.__class__.__name__

    def __and__(self, rhs):
        return ContextCondition(self, rhs)

    # +bel generates a Plan that is stored in plan database
    def __pos__(self):
        return AddBeliefEvent(self)

    # -bel generates a Plan that is stored in plan database
    def __neg__(self):
        return DeleteBeliefEvent(self)

    def __getitem__(self, uModifiers):
        if 'to' in uModifiers:
            self.dest = uModifiers['to']
        if 'from' in uModifiers:
            self.source = uModifiers['from']
        return self


# -----------------------------------------------
class Reactor(Belief):

    def __init__(self, *args, **kwargs):
        Belief.__init__(self, *args, **kwargs)
        self.data_type = DATA_TYPE_REACTOR


# -----------------------------------------------
class SingletonBelief(Belief):

    def __init__(self, *args, **kwargs):
        Belief.__init__(self, *args, **kwargs)
        self.data_type = DATA_TYPE_SINGLETON_BELIEF


# -----------------------------------------------
class ActiveBelief(AtomicFormula):

    def __and__(self, rhs):
        return ContextCondition(self, rhs)

    def do_evaluate(self, ctx):
        args = []
        for t in self.terms():
            if t.type == CONSTANT:
                args.append(t) #.value)
            elif t.type == VARIABLE:
                if t.name in ctx:
                    t.value = ctx[t.name]
                args.append(t)
        if self.evaluate(*args):
            for t in args:
                if t.type == VARIABLE:
                    if t.value is not None:
                        ctx[t.name] = t.value
            return True
        else:
            return False


    def evaluate(self, *args):
        return False

# -----------------------------------------------
class Action(AtomicFormula):

    def __init__(self, *args, **kwargs):
        AtomicFormula.__init__(self, *args, **kwargs)
        self.current_agent = Runtime.currentAgent
        self.engine = Runtime.get_engine(self.current_agent)
        self.method = None


    def __getattr__(self, uAttrName):
        self.method = getattr(self.__class__,  'on_' + uAttrName )
        return self

    def assert_belief(self, uBel):
        self.engine.add_belief(uBel)

    def retract_belief(self, uBel):
        self.engine.remove_belief(uBel)

    def do_execute(self, ctx):
        args = []
        for t in self.terms():
            if t.type == CONSTANT:
                args.append(t)
            elif t.type == VARIABLE:
                if t.name in ctx:
                    t.value = ctx[t.name]
                args.append(t)
        if self.method is None:
            self.execute(*args)
        else:
            #args.insert(0, self)
            self.method(*args)


    def execute(self, *args):
        raise MethodNotOverriddenException()


# ------------------------------------------------
class ContextCondition(object):

    def __init__(self, lhs, rhs = None):
        if rhs is None:
            self.__condition_terms = [ lhs ]
        else:
            self.__condition_terms = [ lhs, rhs ]

    def __repr__(self):
        repr_string = "(" + \
            reduce (lambda x,y : x + " & " + y,
                    map (lambda x: repr(x), self.__condition_terms)) + \
                    ")"
        return repr_string

    def __and__(self, rhs):
        if isinstance(rhs, Belief) or isinstance(rhs, ActiveBelief) or type(rhs) == types.LambdaType:
            self.__condition_terms.append(rhs)
            return self
        else:
            raise InvalidContextConditionException()

    def terms(self):
        return self.__condition_terms


# -------------------------------------------------
class Procedure(AtomicFormula):

    PROC = 3
    PROC_CANCEL = 4

    def __init__(self, *args, **kwargs):
        super(Procedure, self).__init__(*args, **kwargs)
        self.event_type = Procedure.PROC
        self.__additional_event = None

    def additional_event(self):
        return self.__additional_event

    def name(self):
        if self.event_type == Procedure.PROC_CANCEL:
            s = "-"
        else:
            s = ""
        return s + super(Procedure, self).name()

    def basename(self):
        return super(Procedure, self).name()

    def __neg__(self):
        self.event_type = Procedure.PROC_CANCEL
        return self

    def __repr__(self):
        if self.event_type == Procedure.PROC_CANCEL:
            s = "-"
        else:
            s = ""
        if self.__additional_event is None:
            return s + AtomicFormula.__repr__(self)
        else:
            return s + AtomicFormula.__repr__(self) + " / " + repr(self.__additional_event)

    def __rshift__(self, actionList):
        if isinstance(actionList, list):
            p = Plan(self, None, actionList)
            Runtime.add_plan(p)
            return p
        else:
            raise NotAnActionListException()

    def __truediv__(self, uTerm):
        return self.__div_operator(uTerm)

    def __div__(self, uTerm):
        return self.__div_operator(uTerm)

    def __div_operator(self, uTerm):
        if isinstance(uTerm, AddBeliefEvent):
            if self.event_type == Procedure.PROC_CANCEL:
                raise CannotSuspendACancelPlanException()
            self.__additional_event = uTerm
            return self
        elif isinstance(uTerm, Belief):
            p = Plan(self, ContextCondition(uTerm))
            Runtime.add_plan(p)
            return p
        elif isinstance(uTerm, ActiveBelief):
            p = Plan(self, ContextCondition(uTerm))
            Runtime.add_plan(p)
            return p
        elif isinstance(uTerm, ContextCondition):
            p = Plan(self, uTerm)
            Runtime.add_plan(p)
            return p
        else:
            raise InvalidContextException()


# -----------------------------------------------
# BASIC SENSOR
# This is the basic sensor class.
# It has a *blocking* semantics, it is executed,
# by the runtine, within a separate thread
# -----------------------------------------------
class Sensor(Action):

    METHODS = [ 'bind', 'unbind', 'start', 'stop' ]

    def __init__(self, *args, **kwargs):
        Action.__init__(self, *args, **kwargs)
        self.thread = None
        self.stopped = False

    def __getattr__(self, uAttrName):
        if uAttrName in Sensor.METHODS:
            self.method = getattr(self.__class__,  'on_sense_' + uAttrName )
            return self
        else:
            return getattr(self.__class__, uAttrName)

    def __setattr__(self, uAttrName, uValue):
        setattr(self.__class__, uAttrName, uValue)

    def on_sense_bind(self, *args):
        pass

    def on_sense_unbind(self, *args):
        pass

    def on_sense_start(self, *args):
        if self.engine.get_sensor(self) is None:
            self.engine.add_sensor(self)
            self.stopped = False
            self.on_start(*args)
            t = threading.Thread(target = self.sense)
            t.daemon = True
            t.start()
            self.thread = t
        else:
            self.on_restart(*args)

    def on_sense_stop(self, *args):
        self.stopped = True
        self.engine.del_sensor(self)
        self.on_stop(*args)

    def execute(self, *args):
        pass

    # callback
    def on_start(self, *args):
        pass

    # callback
    def on_restart(self, *args):
        pass

    # callback
    def on_stop(self, *args):
        pass

    # main method: sense is intended to be blocking
    def sense(self):
        raise MethodNotOverriddenException()


# -----------------------------------------------
# PERIODIC SENSOR
# -----------------------------------------------
class PeriodicSensor(Sensor):

    # sense is intended to be non-blocking
    def sense(self):
        raise MethodNotOverriddenException()


# -----------------------------------------------
# ASYNC SENSOR
# -----------------------------------------------
class AsyncSensor(Sensor):

    # sense is intended to be non-executed
    def sense(self):
        pass
