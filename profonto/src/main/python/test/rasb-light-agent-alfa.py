import socket, time
from threading import *
from pathlib import Path
import rdflib
from rdflib import *
import os




class Agent(Thread):
    def __init__(self, path, configuration):
        Thread.__init__(self)
        self.oasis = 'http://www.dmi.unict.it/oasis.owl#'
        self.oasisabox = 'http://www.dmi.unict.it/oasis-abox.owl#'
        self.ontologyAgent = ''
        self.ontologyConfiguration = ''
        self.setAgentOntology(path)
        self.setAgentConfiguration(configuration)
        self.start()
        self.host = 'localhost'
        self.port = 8087

    class ServerManager(Thread):
        def __init__(self, socket, address):
            Thread.__init__(self)
            self.sock = socket
            self.addr = address
            self.start()

        def performRequest(self, request):
            print(request)
            return

        def run(self):
            request = ''
            while 1:
                data = self.sock.recv(2048).decode()
                if not data:
                    break
                request += data
                self.performRequest(request)
                return

    def readOntoFile(self, file):
        f = open(file, "r")
        return f.read()

    def getGraph(value):
        g = rdflib.Graph()
        g.parse(data=value)
        return g

    def setAgentOntology(self, path):
        self.ontologyAgent = self.readOntoFile(path)

    def setAgentConfiguration(self, path):
        self.agentConfiguration = self.readOntoFile(path)

    def run(self):
      serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      serversocket.bind((self.host, int(self.port)))
      serversocket.listen(5)
      print("Client started:", self.host, "port ", self.port)
      #print(self.ontologyAgent)
      #print(self.agentConfiguration)
      while 1:
          clientsocket, address = serversocket.accept()
          ServerManager(clientsocket, address)
      return




##############################################################################################

def setTestPath(self):
 p = Path(__file__).parents[2]
 os.chdir(p)
 return

def main():
     setTestPath('')
     Agent("ontologies/test/rasb/rasb-lightagent.owl", "ontologies/test/rasb/rasb-lightagent-config.owl")


if __name__ == '__main__':
    main()

