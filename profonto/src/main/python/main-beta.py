import sys
sys.path.insert(0, "./lib")

from py4j.java_gateway import JavaGateway
import subprocess
import os
import time
import socket
import rdflib
from rdflib import *
from pathlib import Path
from threading import *
from phidias.Types  import *
from phidias.Main import *
from phidias.Lib import *


profonto = ''
oasis = 'http://www.dmi.unict.it/oasis.owl#'
oasisabox = 'http://www.dmi.unict.it/oasis-abox.owl#'
assistant=''

#################################################PHIDIAS PART ##############################

class welcome(Procedure): pass
class decide(Procedure): pass


def computesDependencies(graph, executions):
      for first, second in graph.subject_objects(predicate=URIRef(oasis + "dependsOn")):
          index=0
          while index < len(executions):
              key,value = executions[index]
              if second == key :
                  executions[index]= (key,value+1)
              index += 1

def getOntologyFile(graph, execution):
    file=None
    for t in graph.objects(execution, URIRef(oasis + "hasTaskParameter")): # retrieving source
       for s in graph.objects(t, URIRef(oasis + "descriptionProvidedByURL")):
           if (s is not None):
             file = readOntoFile(s)
    return file


def device_engage(graph,execution):
    #for s,p,o in graph.triples( (None,None,None) ):
    #    print(s,p,o)
    taskObject = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
    taskOperator = next(graph.objects(execution, URIRef(oasis + "hasTaskOperator")))
    performer = next(graph.subjects(URIRef(oasis + "performs"), execution))
    devip=next(graph.objects(subject=None, predicate=URIRef(oasis + "hasIPAddress")))
    devport=next(graph.objects(subject=None, predicate=URIRef(oasis + "hasPortNumber")))
    print("To engage:", performer, taskObject, taskOperator, devip, devport)

# Actions that the assistant performs
def profhome_decide(graph, execution):
    requester = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
    for actions in graph.objects(execution, URIRef(oasis + "hasTaskOperator")):
        if actions == URIRef(oasisabox + "install"):
            file = getOntologyFile(graph, execution)
            value = profonto.addDevice(file)  # read the device data
            print("Device", value, "added.")
            break
        elif actions == URIRef(oasisabox + "uninstall"):  # uninstallation task
            #requester = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
            value = profonto.removeDevice(retrieveEntityName(requester))  # read the device data
            print("Device", retrieveEntityName(requester), "removed with exit code", value,".")
            break
        elif actions == URIRef(oasisabox + "add") or actions == URIRef(oasisabox + "remove"):  # add user task
             for thetype in graph.objects(requester, URIRef(oasis + "hasType")):
                 if thetype== URIRef(oasisabox + "user_type"): #adding or removing user
                     if actions == URIRef(oasisabox + "add"):
                         file = getOntologyFile(graph, execution)
                         value= profonto.addUser(file)
                         print("User", value, "added.")
                     elif actions == URIRef(oasisabox + "remove"):
                          value=profonto.removeUser(retrieveEntityName(requester))
                          print("User", retrieveEntityName(requester), "removed with exit code", value, ".")
                 elif thetype == URIRef(oasisabox + "user_configuration_type"):  # adding or removing user
                     if actions == URIRef(oasisabox + "add"):
                         file = getOntologyFile(graph, execution)
                         value= profonto.addConfiguration(file)
                         print("Configuration added:", value,".")
                     elif actions == URIRef(oasisabox + "remove"):
                         value = profonto.removeConfiguration(retrieveEntityName(requester))
                         print("Configuration", retrieveEntityName(requester), "removed.")
                 elif  thetype == URIRef(oasisabox + "belief_description_object_type"):
                      if actions == URIRef(oasisabox + "add"):
                         file = getOntologyFile(graph, execution)
                         value = profonto.addDataBelief(file)
                         print("Belief  added with exit code", value)
                      elif actions == URIRef(oasisabox + "remove"):
                         file = getOntologyFile(graph, execution)
                         value = profonto.removeDataBelief(file)
                         print("Belief removed with exit code", value)
                 break
        elif actions == URIRef(oasisabox + "parse"):
            for thetype in graph.objects(requester, URIRef(oasis + "hasType")):
                if thetype == URIRef(oasisabox + "generalUtterance"):
                    print("General utterances parser is being developed... stay tuned!")
                    break
        elif actions == URIRef(oasisabox + "retrieve"):
            for thetype in graph.objects(requester, URIRef(oasis + "hasType")):
                if thetype == URIRef(oasisabox + "belief_description_object_type"):
                    file = getOntologyFile(graph, execution)
                    value = profonto.retrieveDataBelief(file)
                    print("Belief retrieved:\n", value)

        else:
            print("Action", actions, "not supported yet")
            break

#Decide which decision has to be taken
class Decide_Action(Action):
    def execute(self, rdf_source):
       value = profonto.parseRequest(rdf_source())
       #print("Client send request:", value)
       g = rdflib.Graph()
       g.parse(data=value)
       executions=[]
       for execution in g.subjects(RDF.type, URIRef(oasis+"TaskExecution")):
           executions.append((execution, 0))

       if len(executions) > 1 :
          computesDependencies(g,executions)
          executions=sorted(executions, key = lambda x: x[1])


       for execution, val in executions:
           for executer in g.subjects( URIRef(oasis+"performs"), execution):
              if( retrieveEntityName(executer) == assistant ) :
                profhome_decide(g,execution)
              else:
                device_engage(g,execution)

def_vars("rdf_source")
welcome() >> [ show_line("Phidias has been started. Wait for Prof-Onto to start") ]
decide(rdf_source) >> [ Decide_Action(rdf_source) ]


################################################ END PHIDIAS PART ##########################




def retrieveEntityName(string):
    out=string.split("#", 1)[1]
    return out

def readOntoFile(file):
 f=open(file,"r")
 return f.read()

def getProcessOut(process):
  message=''
  while True:
    out = process.stdout.read(1)
    if out != '\n':
        message += out
    else:
        break
  return message

class client(Thread):
    def __init__(self, socket, address):
        Thread.__init__(self)
        self.sock = socket
        self.addr = address
        self.start()

    def run(self):
        request=''
        while 1:
          data=self.sock.recv(1024).decode()
          if not data:
             break
          request+=data
        PHIDIAS.achieve(decide(request))

def init_gateway():
    global profonto
    p = Path(__file__).parents[1]
    os.chdir(p)
    folder = 'ontologies/devices'
    for the_file in os.listdir(folder):
        file_path = os.path.join(folder, the_file)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            # elif os.path.isdir(file_path): shutil.rmtree(file_path)
        except Exception as e:
            print(e)
    jar = "java -jar Prof-Onto-1.0-SNAPSHOT.jar"
    process = subprocess.Popen(jar, universal_newlines=True, stdout=subprocess.PIPE)
    # stdout, stderr = process.communicate()
    print(getProcessOut(process))
    profontoGateWay = JavaGateway()  # connect to the JVM
    profonto = profontoGateWay.entry_point



def init_server():
      serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      host = 'localhost'
      port = 8000
      serversocket.bind((host, port))
      serversocket.listen(5)
      print("Prof-Onto Assistant has been started, send requests to:", host, "port ", port)
      while 1:
          clientsocket, address = serversocket.accept()
          client(clientsocket, address)
      return


PHIDIAS.run()
PHIDIAS.achieve(welcome())
init_gateway()
print(open("amens/logo.txt", "r").read())
#Adding HomeAssistant
home=readOntoFile("ontologies/test/homeassistant.owl")
assistant = profonto.addDevice(home)  #read the device data
print("Home assistant added:", assistant)
###

init_server()

