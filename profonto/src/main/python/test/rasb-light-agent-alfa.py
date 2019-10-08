import socket, time
from threading import *
from pathlib import Path
import rdflib
from rdflib import *
import os




class Agent(Thread):
    def __init__(self, path, templates, configuration):
        Thread.__init__(self)
        self.alive=True
        #declare class members
        self.iriSet=['http://www.dmi.unict.it/oasis.owl#','http://www.dmi.unict.it/oasis-abox.owl#', ''] #2->agent
        self.graphSet=['','',''] #0->Agent, 1-> agent config, 2->Templates
        self.agentInfo=['','',''] #0->name, 1->host, 2->port
        #end declare
        #set agent graphs
        self.setAgentTemplates(templates)
        self.setAgentOntology(path)
        self.setAgentConfiguration(configuration)
        self.setAgentIRI(self.graphSet[0])
        self.agentInfo[0]=self.retrieveEntityName(self.iriSet[2])
        # end set
        #set connection
        self.setAgentConnectionInfo(self.graphSet[1])
        #end set
        self.start()

    def stop(self):
        self.alive=False
        print("Server is closing. Wait.")
        return

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
                  self.agentInfo[1]=data
              elif property== URIRef(self.iriSet[0]+"hasPortNumber"):
                  self.agentInfo[2]=data
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

    def retrieveEntityName(self, iri):
        start=iri.rfind('#')
        return iri[start+1:]

    def run(self):
      serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      serversocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
      serversocket.bind((self.agentInfo[1], int(self.agentInfo[2])))
      serversocket.listen(5)
      print("Client started at", self.agentInfo[1], "port", self.agentInfo[2])
      #print(self.iriSet[0],' ', self.iriSet[1], ' ', self.iriSet[2])
      #self.printGraph(self.graphSet[0])
      #self.printGraph(self.graphSet[1])
      #self.printGraph(self.graphSet[2])
      while self.alive:
          clientsocket, address = serversocket.accept()
          ServerManager(clientsocket, address)

      print("Server stopped. Goodbye.")
      serversocket.close()
      return




##############################################################################################

def setTestPath(self):
 p = Path(__file__).parents[2]
 os.chdir(p)
 return


class Console(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.start()
        return

    def start_command(self):
        return Agent("ontologies/test/rasb/rasb-lightagent.owl", {"ontologies/test/rasb/lightagent-from-template.owl"},
                     "ontologies/test/rasb/rasb-lightagent-config.owl")

    def stop_command(self, agent):
        agent.stop()
        return True

    def exit_command(self, agent):
        self.stop_command(agent)
        print("Console closing. Goodbye.")
        return False

    def status_command(self, agent):
        return agent.alive

    def run(self):
        agent = ''
        exec_status = True
        while (exec_status):
            print("Enter a command:")
            command = input(" ---> ")
            if command == "start":
                agent = self.start_command()
            elif command == "stop":
                self.stop_command(agent)
            elif command == "exit":
                exec_status = self.exit_command(agent)
            elif command == "status":
                status=self.status_command(agent)
                print("The server is ", end = "")
                if (not status):
                    print ("not ", end= "")
                print("active.")
            else:
                print("Unrecognized command")
                print("Use start | stop | status")
            time.sleep(1)
        return

def main():
     setTestPath('')
     Console()

if __name__ == '__main__':
    main()

