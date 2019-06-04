import sys

sys.path.insert(0, "./lib")

from py4j.java_gateway import JavaGateway
import subprocess
import os
import time
from lib.profeta.Types import *
from lib.profeta.Main import *
from lib.profeta.Lib import *
from pathlib import Path


class init(Goal): pass

class init_gateway(Action):
  def execute(self):
      print("init here")



init() >> [init_gateway()]





PROFETA.run()

PROFETA.shell(globals())


# p = Path(__file__).parents[1]
# os.chdir(p)
# folder = 'ontologies/devices'
# jar="java -jar Prof-Onto-1.0-SNAPSHOT.jar"
# process = subprocess.Popen(jar, shell=True, stdout = subprocess.PIPE)
# time.sleep(10)
# profontoGateWay = JavaGateway()                   # connect to the JVM
# profonto = profontoGateWay .entry_point
# profontoGateWay.shutdown() #Shutdown the gateway