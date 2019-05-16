#
# profetaqi/Lib.py
#

import qi
import math
import time

from profeta.Types import *
from profeta.Main import *

class humanTracked(Reactor): pass
class faces(SingletonBelief): pass

class qiSession(object):

    session = None
    motion = None
    posture = None
    memory = None
    tts = None
    face_detection = None
    face_characteristics = None
    leds = None

    face_detection_event = None
    face_detection_sensor = None

    @classmethod
    def connect(cls, url):
        cls.session = qi.Session()
        cls.session.connect(url)
        cls.memory = cls.session.service("ALMemory")
        cls.motion = cls.session.service("ALMotion")
        cls.posture = cls.session.service("ALRobotPosture")
        cls.tts = cls.session.service("ALTextToSpeech")
        cls.leds = cls.session.service("ALLeds")
        cls.face_detection = cls.session.service("ALFaceDetection")
        cls.face_characteristics = cls.session.service("ALFaceCharacteristics")

        cls.face_detection_subscriber = cls.memory.subscriber("FaceDetected")

        cls.face_data = None

    @classmethod
    def getActuatorValue(cls, name):
        return cls.memory.getData("Device/SubDeviceList/" + name + "/Position/Actuator/Value")

    @classmethod
    def getMemoryData(cls, name):
        try:
            return cls.memory.getData(name)
        except:
            return None

    @classmethod
    def setTrackingEnabled(cls, uValue, uSensor = None):
        if uValue:
            cls.face_detection_subscriber.signal.connect(cls.on_human_tracked)
            cls.face_detection_sensor = uSensor
            cls.face_detection.subscribe(uSensor)
            cls.face_detection.setTrackingEnabled(uValue)
        else:
            if cls.face_detection_sensor is not None:
                cls.face_detection.unsubscribe(cls.face_detection_sensor)
                cls.face_detection_sensor = None
            cls.face_detection.setTrackingEnabled(uValue)

    @classmethod
    def on_human_tracked(cls, face_values):
        #print(face_values)
        if cls.face_detection_sensor is not None:
            if face_values != []:
                timeStamp = face_values[0]
                faceInfoArray = face_values[1]
                cls.face_data = faceInfoArray[:-1]
                #PROFETA.assert_belief(humanTracked(), cls.face_detection_agent_event)
                faceTracking.instance.assert_belief(humanTracked())


# ------------------------------------------------
# MOTION ACTIONS
# ------------------------------------------------
class wakeUp(Action):
    def execute(self):
        qiSession.motion.wakeUp()

class rest(Action):
    def execute(self):
        qiSession.motion.rest()

class posture(Action):
    def execute(self, Name, Speed = None):
        if Speed is None:
            qiSession.posture.goToPosture(Name(), 0.5)
        else:
            qiSession.posture.goToPosture(Name(), float(Speed()))

class stiffness(Action):
    def execute(self, J, V):
        qiSession.motion.setStiffnesses( J(), V() )

class moveHead(Action):
    def execute(self, Yaw, Pitch, Tim):
        t = Tim()
        qiSession.motion.angleInterpolation( [ "HeadYaw", "HeadPitch" ],
                                             [ Yaw(), Pitch() ],
                                             [ t, t ],
                                             True)
class moveHead_relative(Action):
    def execute(self, Yaw, Pitch, Tim):
        t = Tim()
        qiSession.motion.angleInterpolation( [ "HeadYaw", "HeadPitch" ],
                                             [ Yaw(), Pitch() ],
                                             [ t, t ],
                                             False)


# ------------------------------------------------
# VISION AND TRACKING SENSORS AND ACTIONS
# ------------------------------------------------
# Sensors
class faceTracking(AsyncSensor):

    instance = None

    def on_start(self):
        qiSession.setTrackingEnabled(True, self.current_agent)
        faceTracking.instance = self

    def on_stop(self):
        qiSession.setTrackingEnabled(False)


# ------------------------------------------------
# Actions
class faceRecognition(Action):
    def execute(self):
        qiSession.face_detection.setRecognitionEnabled(V())

class faces(Action):
    def execute(self, N):
        if qiSession.face_data is None:
            N(0)
        else:
            N(len(qiSession.face_data))

class facePosition(Action):
    def execute(self, N, A, B):
        if not(N.bound()):
            N(1)
            idx = 1
        else:
            idx = N()
        if idx > len(qiSession.face_data):
            return False
        [_, a, b, x, y] = qiSession.face_data[idx-1][0]
        A(a)
        B(b)



# class faceMood(ActiveBelief):
#     def evaluate(self, N, M):
#         if not(N.bound()):
#             N(1)
#             idx = 1
#         else:
#             idx = N()
#         ids = qiSession.getMemoryData("PeoplePerception/PeopleList")
#         if idx > len(ids):
#             return False
#         face_id = ids[idx - 1];
#         #print(face_id)
#         v = qiSession.face_characteristics.analyzeFaceCharacteristics(face_id)
#         if not(v):
#             return False
#         expressions = qiSession.getMemoryData("PeoplePerception/Person/" + str(face_id) + "/ExpressionProperties")
#         if expressions is None:
#             return False
#         else:
#             print(expressions)
#             mood = max(expressions)
#             if expressions[0] == mood:
#                 M('neutral')
#             elif expressions[1] == mood:
#                 M('happy')
#             elif expressions[2] == mood:
#                 M('surprised')
#             elif expressions[3] == mood:
#                 M('sad')
#             else:
#                 M('')
#             #print(expressions)
#             return True


class showPeople(Action):

    def execute(self):
        ids = qiSession.getMemoryData("PeoplePerception/PeopleList")
        print(ids)

# ------------------------------------------------
# SPEECH ACTIONS
# ------------------------------------------------
class say(Action):
    def execute(self, T):
        qiSession.tts.say(T())

class asay(Action):
    def execute(self, T):
        qiSession.tts.say(T(), _async = True)


# ------------------------------------------------
# LED ACTIONS
# ------------------------------------------------
class ledsOff(Action):
    def execute(self, X):
        qiSession.leds.off(X())

class ledsOn(Action):
    def execute(self, X):
        qiSession.leds.on(X())

class rotateEyes(Action):
    def execute(self, R, G, B, TimeRot, Dur):
        rgb = B() | (G() << 8) | (R() << 16)
        qiSession.leds.rotateEyes(rgb, TimeRot(), Dur())

# ------------------------------------------------
# ACTUATOR VALUES
# ------------------------------------------------
class ActuatorValue(ActiveBelief):
    def evaluate(self, P):
        value = qiSession.getActuatorValue(self.__class__.__name__)
        P(value)
        return True


class FrontSonar(ActuatorValue):  pass
class BackSonar(ActuatorValue):  pass

class HeadPitch(ActuatorValue):  pass
class HeadYaw(ActuatorValue):  pass

class LShoulderPitch(ActuatorValue):  pass
class LShoulderRoll(ActuatorValue):  pass
class LElbowYaw(ActuatorValue):  pass
class LElbowRoll(ActuatorValue):  pass
class LWirstYaw(ActuatorValue):  pass
class LHand(ActuatorValue):  pass

class RShoulderPitch(ActuatorValue):  pass
class RShoulderRoll(ActuatorValue):  pass
class RElbowYaw(ActuatorValue):  pass
class RElbowRoll(ActuatorValue):  pass
class RHand(ActuatorValue):  pass
