import socket, time
from threading import *
from pathlib import Path
import rdflib
from rdflib import *
import os




class Agent(Thread):
    def __init__(self, path, templates, configuration):
        Thread.__init__(self)
        #declare class members
        self.oasis = 'http://www.dmi.unict.it/oasis.owl#'
        self.oasisabox = 'http://www.dmi.unict.it/oasis-abox.owl#'
        self.graphTemplates = ''
        self.graphAgent = ''
        self.graphAgentConfiguration = ''
        self.host = ''
        self.port = 0
        #end declare
        #set agent graphs
        self.setAgentTemplates(templates)
        self.setAgentOntology(path)
        self.setAgentConfiguration(configuration)
        # end set
        #set connection
        self.setAgentConnectionInfo(self.graphAgentConfiguration)
        #end set
        self.start()


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

    def getGraph(self, value):
        g = rdflib.Graph()
        g.parse(data=value)
        return g

    def setAgentTemplates(self, templates):
        self.graphTemplates=rdflib.Graph()
        for tem in templates:
            self.graphTemplates+= self.getGraph(self.readOntoFile(tem))
        return

    def setAgentOntology(self, path):
        self.graphAgent = self.getGraph(self.readOntoFile(path))
        return

    def setAgentConfiguration(self, path):
        self.graphAgentConfiguration = self.getGraph(self.readOntoFile(path))
        return

    def setAgentConnectionInfo(self, graph):
        for agent, connection in graph.subject_objects(predicate=URIRef(self.oasis + "hasConnection")):
           for property, data in graph.predicate_objects(subject=connection):
              if property == URIRef(self.oasis+"hasIPAddress"):
                  self.host=data
              elif property== URIRef(self.oasis+"hasPortNumber"):
                  self.port=data
        return

    def printGraph (self, graph):
        print("--- printing raw triples ---")
        for s, p, o in graph:
           print((s, p, o))

    def run(self):
      serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      serversocket.bind((self.host, int(self.port)))
      serversocket.listen(5)
      print("Client started:", self.host, "port ", self.port)
      #self.printGraph(self.graphAgent)
      #self.printGraph(self.graphTemplates)
      #self.printGraph(self.graphAgentConfiguration)
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
     Agent("ontologies/test/rasb/rasb-lightagent.owl", {"ontologies/test/rasb/lightagent-from-template.owl"} ,"ontologies/test/rasb/rasb-lightagent-config.owl")


if __name__ == '__main__':
    main()

