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
        self.iriSet=['http://www.dmi.unict.it/oasis.owl#','http://www.dmi.unict.it/oasis-abox.owl#', ''] #2->agent
        self.graphSet=['','',''] #0->Agent, 1-> agent config, 2->Templates
        self.host = ''
        self.port = -1
        #end declare
        #set agent graphs
        self.setAgentTemplates(templates)
        self.setAgentOntology(path)
        self.setAgentConfiguration(configuration)
        self.setAgentIRI(self.graphSet[0])
        # end set
        #set connection
        self.setAgentConnectionInfo(self.graphSet[1])
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
        self.graphSet[2]=rdflib.Graph()
        for tem in templates:
            self.graphSet[2]+= self.getGraph(self.readOntoFile(tem))
        return

    def setAgentOntology(self, path):
        self.graphSet[0] = self.getGraph(self.readOntoFile(path))
        return

    def setAgentConfiguration(self, path):
        self.graphSet[1] = self.getGraph(self.readOntoFile(path))
        return

    def setAgentConnectionInfo(self, graph):
        for agent, connection in graph.subject_objects(predicate=URIRef(self.iriSet[0] + "hasConnection")):
           for property, data in graph.predicate_objects(subject=connection):
              if property == URIRef(self.iriSet[0]+"hasIPAddress"):
                  self.host=data
              elif property== URIRef(self.iriSet[0]+"hasPortNumber"):
                  self.port=data
        return

    def setAgentIRI(self, graph):
        for agent in graph.subjects(predicate=RDF.type,  object=URIRef(self.iriSet[0]+'Device')):
            self.iriSet[2]= agent
            break
        return

    def printGraph (self, graph):
        print("--- printing raw triples ---")
        for s, p, o in graph:
           print((s, p, o))

    def run(self):
      serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      serversocket.bind((self.host, int(self.port)))
      serversocket.listen(5)
      print("Client started at", self.host, "port", self.port)
      #print(self.iriSet[0],' ', self.iriSet[1], ' ', self.iriSet[2])
      #self.printGraph(self.graphSet[0])
      #self.printGraph(self.graphSet[1])
      #self.printGraph(self.graphSet[2])
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

