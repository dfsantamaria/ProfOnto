import socket, time
from threading import *
from pathlib import Path
import rdflib
from rdflib import *
import os
from datetime import datetime
import re
# current date and time


class Utils():
    def recvall(self, sock):
        BUFF_SIZE = 1024  # 1 KiB
        data = b''
        while True:
            part = sock.recv(BUFF_SIZE)
            data += part
            if len(part) < BUFF_SIZE:
                # either 0 or end of data
                break
        return data


class Agent(Thread,Utils):
    def __init__(self, path, templates, iriAgent, iriTemplate):
        Thread.__init__(self)
        self.alive=True
        #declare class members
        self.iriSet=['http://www.dmi.unict.it/oasis.owl','http://www.dmi.unict.it/oasis-abox.owl', iriAgent , iriTemplate] #2->agent 3->template
        self.graphSet=['','',''] #0->Agent,  1->Templates
        self.agentInfo=['','',''] #0->short name, 1->host, 2->port
        self.hubInfo=['',''] #0->address, 1-> port

        self.owlobj=URIRef("http://www.w3.org/2002/07/owl#ObjectProperty")
        self.owldat=URIRef("http://www.w3.org/2002/07/owl#DatatypeProperty")
        #end declare
        #set agent graphs
        self.setAgentTemplates(templates)
        self.setAgentOntology(path)
        #self.setAgentConfiguration(configuration)
        self.setAgentIRIs(self.graphSet[0])
        # end set
        #set connection
        self.setAgentConnectionInfo(self.graphSet[0])
        #end set
        self.start()

    def stop(self):
        self.alive=False
        print("Server is closing. Wait.")
        return

    class ServerManager(Thread, Utils):
        def __init__(self, socket, address):
            Thread.__init__(self)
            self.sock = socket
            self.addr = address
            self.start()

        def performRequest(self, request):
            g = rdflib.Graph()
            g.parse(data=request)
            execution = next(g.subjects(RDF.type, URIRef("http://www.dmi.unict.it/oasis.owl" + "#TaskExecution")))
            taskObject = next(g.objects(execution, URIRef("http://www.dmi.unict.it/oasis.owl" + "#hasTaskObject")))
            taskOperator = next(g.objects(execution, URIRef("http://www.dmi.unict.it/oasis.owl" + "#hasTaskOperator")))
            print("\n Action ", taskOperator, "on ", taskObject)
            print("---> ", end='')
            return 1

        def run(self):
            request = self.recvall(self.sock)
            self.performRequest(request)
            return

    def readOntoFile(self, file):
        f = open(file, "r")
        return f.read()

    def getGraph(self, value, iri):
        g = rdflib.Graph()
        g.parse(data=value)
        #g.bind('xml:base', iri, override=True)
        return g

    def setAgentTemplates(self, templates):
        self.graphSet[1]=None
        for tem in templates:
            if(self.graphSet[1]==None):
               self.graphSet[1]=self.getGraph(self.readOntoFile(tem), self.iriSet[3])
            else:
               self.graphSet[1]+= self.getGraph(self.readOntoFile(tem),self.iriSet[3])
        return

    def setAgentOntology(self, path):
        self.graphSet[0] = self.getGraph(self.readOntoFile(path),self.iriSet[2])
        return

   # def setAgentConfiguration(self, path):
   #     self.graphSet[1] = self.getGraph(self.readOntoFile(path),self.iriSet[4])
   #     return

    def setAgentConnectionInfo(self, graph):
        for agent, connection in graph.subject_objects(predicate=URIRef(self.iriSet[0] + "#hasConnection")):
           for property, data in graph.predicate_objects(subject=connection):
              if property == URIRef(self.iriSet[0]+"#hasIPAddress"):
                  self.agentInfo[1]=data
              elif property== URIRef(self.iriSet[0]+"#hasPortNumber"):
                  self.agentInfo[2]=data
        return

    def setAgentIRIs(self, graph):
        for agent in graph.subjects(predicate=RDF.type,  object=URIRef(self.iriSet[0]+'#Device')):
            names= str(agent).split('#')
            self.agentInfo[0]=names[1]
            break
        return

    def getTimeStamp(self):
        return  (str(datetime.timestamp(datetime.now()))).replace(".", "-")

    def generateRequest(self, reqGraph, iri):

        request = URIRef(iri+"#request")             #the request
        reqGraph.add(( request, RDF.type, URIRef(self.iriSet[0]+"#PlanDescription")))  # request type

        goal = URIRef(iri + "#goal")  # the goal
        reqGraph.add((goal, RDF.type, URIRef(self.iriSet[0] + "#GoalDescription")))  # goal type

        task = URIRef(iri + "#task")  # the task
        reqGraph.add((task, RDF.type, URIRef(self.iriSet[0] + "#TaskDescription")))  # task type

        reqGraph.add((URIRef(self.iriSet[0] + "#consistsOfGoalDescription"), RDF.type, self.owlobj))
        reqGraph.add((request, URIRef(self.iriSet[0] + "#consistsOfGoalDescription"), goal))  # has goal

        reqGraph.add((URIRef(self.iriSet[0] + "#consistsOfTaskDescription"), RDF.type, self.owlobj))
        reqGraph.add((goal, URIRef(self.iriSet[0] + "#consistsOfTaskDescription"), task))  # has goal

        return


    def replacenth(self, string, sub, wanted, n):
        where = [m.start() for m in re.finditer(sub, string)][n - 1]
        before = string[:where]
        after = string[where:]
        after = after.replace(sub, wanted, 1)
        newString = before + after
        return newString

    def libbug(self, timestamp, graph, iri):
        tosend=graph.serialize(format='pretty-xml').decode()  # transmits template
        replace = "  xml:base=\"" + iri + "\"> \n"
        tosend = self.replacenth(tosend, ">", replace, 2)
        return tosend

    def install_device(self):
        if(self.hubInfo[0]=='' or self.hubInfo[1]==''):
           return 0
        timestamp = self.getTimeStamp()

        tosend=self.libbug(timestamp, self.graphSet[1], self.iriSet[3])  # transmits template solving the rdflib bug of xml:base
        self.transmit(tosend.encode(), False)

        tosend = self.libbug(timestamp, self.graphSet[0], self.iriSet[2])  # transmits behavior solving the rdflib bug of xml:base
        self.transmit(tosend.encode(), False)

        reqGraph = rdflib.Graph()

        iri= str(self.iriSet[2]).rsplit('.',1)[0]+"-request"+timestamp+".owl"
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[1])))
        #if(self.iriSet[4] != ''):
        #    reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[4])))

        self.generateRequest(reqGraph, iri)

        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add((agent, RDF.type, URIRef(self.iriSet[0] + "#Device")))  # has request

        request = URIRef(iri + "#request")  # the request
        reqGraph.add((URIRef(self.iriSet[0] + "#requests"), RDF.type, self.owlobj))
        reqGraph.add((agent, URIRef(self.iriSet[0] + "#requests"), request))  # has request

        task = URIRef(iri+ "#task")  # the task
        reqGraph.add((URIRef(self.iriSet[0] + "#hasTaskOperator"), RDF.type, self.owlobj))
        reqGraph.add((task, URIRef(self.iriSet[0] + "#hasTaskOperator"), URIRef(self.iriSet[1] + "#install")))  # task operator

        reqGraph.add((URIRef(self.iriSet[0] + "#hasTaskObject"), RDF.type, self.owlobj))
        reqGraph.add((task, URIRef(self.iriSet[0] + "#hasTaskObject"), agent ))  # task object
        reqGraph.add((agent, URIRef(self.iriSet[0] + "#hasType"), URIRef(self.iriSet[1] + "#device_type") ))  # task object

        parameter = URIRef(iri +"#parameter")  # the parameter
        reqGraph.add((parameter, RDF.type, URIRef(self.iriSet[0] + "#TaskInputParameter")))
        reqGraph.add((parameter, RDF.type, URIRef(self.iriSet[0] + "#OntologyDescriptionObject")))

        reqGraph.add((URIRef(self.iriSet[0] + "#hasInformationObjectType"), RDF.type, self.owlobj))
        reqGraph.add((parameter, URIRef(self.iriSet[0] + "#hasInformationObjectType"), URIRef(self.iriSet[1] + "#ontology_description_object_type")))

        reqGraph.add((URIRef(self.iriSet[0] + "#descriptionProvidedByIRI"), RDF.type, self.owldat))
        reqGraph.add((parameter, URIRef(self.iriSet[0] + "#descriptionProvidedByIRI"), Literal(self.iriSet[2], datatype=XSD.string)))

        reqGraph.add((URIRef(self.iriSet[0] + "#hasTaskInputParameter"), RDF.type, self.owlobj))
        reqGraph.add((task, URIRef(self.iriSet[0] + "#hasTaskInputParameter"), parameter))  # task parameter

        tosend = self.libbug(timestamp, reqGraph,  iri)  # transmits config solving the rdflib bug of xml:base
        received=self.transmit(tosend.encode(), True)
        g = rdflib.Graph()
        g.parse(data=received)
        for s, b in g.subject_objects(URIRef(self.iriSet[0] + "#hasStatus")):
            if (str(b) == self.iriSet[1] + "#succeded_status"):
                print("Device installation confirmed by the hub")
            else:
                print("Device installation not confirmed by the hub")
        #f=open("test.owl", "w")
        #f.write(g.serialize(format="pretty-xml").decode())
        return 1

    def uninstall_device(self):
        if (self.hubInfo[0] == '' or self.hubInfo[1] == ''):
            return 0
        timestamp = self.getTimeStamp()

        reqGraph = rdflib.Graph()

        iri = str(self.iriSet[2]).rsplit('.', 1)[0] + "-request" + timestamp + ".owl"
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[1])))
        # if(self.iriSet[4] != ''):
        #    reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[4])))

        self.generateRequest(reqGraph, iri)

        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add((agent, RDF.type, URIRef(self.iriSet[0] + "#Device")))  # has request

        request = URIRef(iri + "#request")  # the request
        reqGraph.add((URIRef(self.iriSet[0] + "#requests"), RDF.type, self.owlobj))
        reqGraph.add((agent, URIRef(self.iriSet[0] + "#requests"), request))  # has request

        task = URIRef(iri + "#task")  # the task
        reqGraph.add((URIRef(self.iriSet[0] + "#hasTaskOperator"), RDF.type, self.owlobj))
        reqGraph.add(
            (task, URIRef(self.iriSet[0] + "#hasTaskOperator"), URIRef(self.iriSet[1] + "#uninstall")))  # task operator

        reqGraph.add((URIRef(self.iriSet[0] + "#hasTaskObject"), RDF.type, self.owlobj))
        reqGraph.add((task, URIRef(self.iriSet[0] + "#hasTaskObject"), agent))  # task object
        reqGraph.add(
            (agent, URIRef(self.iriSet[0] + "#hasType"), URIRef(self.iriSet[1] + "#device_type")))  # task object

        tosend = self.libbug(timestamp, reqGraph, iri)  # transmits config solving the rdflib bug of xml:base
        received = self.transmit(tosend.encode(), True)
        g = rdflib.Graph()
        g.parse(data=received)
        for s, b in g.subject_objects(URIRef(self.iriSet[0] + "#hasStatus")):
            if (str(b) == self.iriSet[1] + "#succeded_status"):
                print("Device uninstallation confirmed by the hub")
            else:
                print("Device uninstallation not confirmed by the hub")
        # f=open("test.owl", "w")
        # f.write(tosend)
        return 1

    def transmit(self, data, response):
        #print(data)
        received=''
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect( (self.hubInfo[0], int(self.hubInfo[1])))
        client_socket.send(data)
        if(response):
          received=self.recvall(client_socket).decode()
        client_socket.close()
        return received

    def set_hub(self, host, port):
        self.hubInfo[0]=host
        self.hubInfo[1]=port
        return 1

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
          self.ServerManager(clientsocket, address)
      print("Server stopped. Goodbye.")
      serversocket.close()
      return




##############################################################################################

def setTestPath(self):
 p = Path(__file__).parents[1]
 os.chdir(p)
 return


class Console(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.start()
        return

    def start_command(self):
        return Agent("ontologies/test/rasb/rasb-lightagent.owl", {"ontologies/test/rasb/lightagent-from-template.owl"},
                     "http://www.dmi.unict.it/lightagent.owl", "http://www.dmi.unict.it/lightagent-template.owl")

    def stop_command(self, agent):
        agent.stop()
        return True

    def exit_command(self, agent):
        self.stop_command(agent)
        print("Console closing. Goodbye.")
        return False

    def status_command(self, agent):
        return agent.alive

    def install_device(self, agent):
        return agent.install_device()

    def uninstall_device(self, agent):
        return agent.uninstall_device()

    def set_hub(self, agent, host, port):
        return agent.set_hub(host, port)

    def checkAgent(self, agent):
        if(agent == None):
            print("Agent not started. Please start the agent first")
            return 0
        return 1


    def run(self):
        agent = None
        exec_status = True
        while (exec_status):
            print("Enter a command:")
            command = input(" ---> ").strip()
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
            elif command == "install":
                 if self.checkAgent(agent):
                   if( self.install_device(agent)):
                       print("Device installation complete")
                   else:
                       print("Device cannot be installed. Make sure the hub is correctly set.")
            elif command == "uninstall":
                if self.checkAgent(agent):
                    if (self.uninstall_device(agent)):
                        print("Device uninstallation complete")
                    else:
                        print("Device cannot be uninstalled. Make sure the hub is correctly set.")
            elif command.startswith("set hub"):
                if not self.checkAgent(agent):
                   continue
                parms=command.split();
                if self.set_hub(agent, parms[2], parms[3]):
                   print("The hub is located at address ", parms[2], "port ", parms[3])
                else:
                   print ("The hub cannot be configured, check the parameters")
            else:
                print("Unrecognized command")
                print("Use start | stop | status | install | uninstall | set hub [localhost] [port]")
            time.sleep(1)
        return

def main():
     setTestPath('')
     Console()

if __name__ == '__main__':
    main()

