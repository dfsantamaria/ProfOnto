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
profhome = URIRef("http://www.dmi.unict.it/profonto-home.owl#profonto-home")

#################################################PHIDIAS PART ##############################

class welcome(Procedure): pass
class decide(Procedure): pass


def getOntologyFile(graph, execution):
    file=None
    for t in graph.objects(execution, URIRef(oasis + "hasTaskParameter")): # retrieving source
       for s in graph.objects(t, URIRef(oasis + "descriptionProvidedByURL")):
           if (s is not None):
             file = readOntoFile(s)
    return file

# Actions that the assistant performs
def profhome_decide(graph, execution):
    device = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
    for actions in graph.objects(execution, URIRef(oasis + "hasTaskOperator")):
        if actions == URIRef(oasisabox + "install"):  # installation task
            file = getOntologyFile(graph, execution)
            value = profonto.addDevice(file, retrieveEntityName(device))  # read the device data
            print("Device", device, "added with exit code:", value)

        elif actions == URIRef(oasisabox + "uninstall"):  # uninstallation task
            device = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
            value = profonto.removeDevice(retrieveEntityName(device))  # read the device data
            print("Device", device, "removed with exit code:", value)

        elif actions == URIRef(oasisabox + "add") or actions == URIRef(oasisabox + "remove"):  # add user task
             for thetype in graph.objects(device, URIRef(oasis + "hasType")):
                 if thetype== URIRef(oasisabox + "user_type"): #adding or removing user
                     if actions == URIRef(oasisabox + "add"):
                         file = getOntologyFile(graph, execution)
                         value= profonto.addUser(file, retrieveEntityName(device) )
                         print("User", device, "added with exit code:", value)
                     elif actions == URIRef(oasisabox + "remove"):
                          value=profonto.removeUser(retrieveEntityName(device))
                          print("User", device, "removed with exit code:", value)
                 break



#Decide which decision has to be taken
class Decide_Action(Action):
    def execute(self, rdf_source):
       value = profonto.parseRequest(rdf_source())
       #print("Client send request:", value)
       g = rdflib.Graph()
       g.parse(data=value)
       for execution in g.subjects(RDF.type, URIRef(oasis+"TaskExecution")):
          for executer in g.subjects( URIRef(oasis+"performs"), execution):
              if( executer == profhome ) :
                profhome_decide(g,execution)



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

#Adding HomeAssistant
home=readOntoFile("ontologies/test/homeassistant.owl")
value = profonto.addDevice(home, "ProfHomeAssistant")  #read the device data
print("Home assistant added with exit code:", value)
###

init_server()

