import socket, time
from threading import *
from pathlib import Path
import rdflib
from rdflib import *
import os
from datetime import datetime
import re

from lib.utils import *
# current date and time


class ServerManager(Thread):
    def __init__(self, socket, address, agent):
        Thread.__init__(self)
        self.sock = socket
        self.addr = address
        self.agent=agent
        self.start()

    def performOperation(self, taskOperator, taskObject):
        print("\n Action ", taskOperator, "on ", taskObject)
        return

    def response(self, request):
        g = rdflib.Graph()
        g.parse(data=request)
        execution = next(g.subjects(RDF.type, URIRef(self.agent.iriSet[0]+ "#TaskExecution")))
        taskObject = next(g.objects(execution, URIRef(self.agent.iriSet[0] + "#hasTaskObject")))
        taskOperator = next(g.objects(execution, URIRef(self.agent.iriSet[0] + "#hasTaskOperator")))
        self.performOperation(taskOperator, taskObject)
        status = "succeded_status"
        timestamp = Utils.getTimeStamp(None)
        reqGraph = rdflib.Graph()

        iri = str(execution).rsplit('.', 1)[0] + "-updatestatus" + timestamp + ".owl"
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.agent.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.agent.iriSet[1])))

        task = URIRef(iri + "#task")  # the task
        object = URIRef(iri + "#belief-data")  # the task
        Utils.generateRequest(None, reqGraph, iri, self.agent.iriSet[0], task, object, URIRef(self.agent.iriSet[1] + "#add"), None, None)

        agent = URIRef(self.agent.iriSet[2] + "#" + self.agent.agentInfo[0])
        reqGraph.add((agent, RDF.type, URIRef(self.agent.iriSet[0] + "#Device")))  # has request

        request = URIRef(iri + "#request")  # the request
        reqGraph.add((URIRef(self.agent.iriSet[0] + "#requests"), RDF.type, Utils.owlobj))
        reqGraph.add((agent, URIRef(self.agent.iriSet[0] + "#requests"), request))  # has request

        reqGraph.add((URIRef(self.agent.iriSet[0] + "#hasInformationObjectType"), RDF.type, Utils.owlobj))
        reqGraph.add((object, URIRef(self.agent.iriSet[0] + "#hasInformationObjectType"),
                      URIRef(self.agent.iriSet[1] + "#belief_description_object_type")))

        parameter = URIRef(iri + "#parameter")  # the parameter
        reqGraph.add((parameter, RDF.type, URIRef(self.agent.iriSet[0] + "#TaskInputParameter")))
        reqGraph.add((parameter, RDF.type, URIRef(self.agent.iriSet[0] + "#OntologyDescriptionObject")))

        reqGraph.add((URIRef(self.agent.iriSet[0] + "#refersTo"), RDF.type, Utils.owlobj))
        reqGraph.add((parameter, URIRef(self.agent.iriSet[0] + "#refersTo"), execution))  # task object
        reqGraph.add((execution, URIRef(self.agent.iriSet[0] + "#hasStatus"),
                      URIRef(self.agent.iriSet[0] + "#" + status)))  # task object

        reqGraph.add((URIRef(self.agent.iriSet[0] + "#hasInformationObjectType"), RDF.type, Utils.owlobj))
        reqGraph.add((parameter, URIRef(self.agent.iriSet[0] + "#hasInformationObjectType"),
                      URIRef(self.agent.iriSet[1] + "#ontology_description_object_type")))

        reqGraph.add((URIRef(self.agent.iriSet[0] + "#descriptionProvidedByIRI"), RDF.type, Utils.owldat))
        reqGraph.add((parameter, URIRef(self.agent.iriSet[0] + "#descriptionProvidedByIRI"),
                      Literal(iri, datatype=XSD.string)))

        reqGraph.add((URIRef(self.agent.iriSet[0] + "#hasTaskInputParameter"), RDF.type, Utils.owlobj))
        reqGraph.add((task, URIRef(self.agent.iriSet[0] + "#hasTaskInputParameter"), parameter))  # task parameter

        tosend = Utils.libbug(Utils, reqGraph, iri)
        # f=open("test.owl", "w")
        # f.write(tosend)

        self.sock.send(tosend.encode())
        print("---> ", end='')
        return 1

    def run(self):
        request = Utils.recvall(Utils, self.sock)
        self.response(request)
        return




class Agent(Thread):
    def __init__(self, address, port, path, templates, iriAgent, iriTemplate):
        Thread.__init__(self)
        self.alive=True
        self.restart = True
        self.serversocket = None
        #declare class members
        self.graphSet=['','']
        self.iriSet=['http://www.dmi.unict.it/oasis.owl','http://www.dmi.unict.it/oasis-abox.owl', iriAgent , iriTemplate] #2->agent 3->template
        self.agentInfo=['','',''] #0->short name, 1->host, 2->port
        self.hubInfo=['',''] #0->address, 1-> port
        graphSet = ['', '', '']  # 0->Agent,  1->Templates

        #end declare
        #set agent graphs
        self.setAgentTemplates(templates)
        self.setAgentOntology(path)
        #self.setAgentConfiguration(configuration)
        self.setAgentIRIs(self.graphSet[0])
        # end set
        #set connection
        self.setAgentConnectionInfo(address, port, self.graphSet[0])
        #end set
        self.start()

    def stop(self):
        self.alive=False
        self.restart=False
        self.serversocket.close()
        print("Server is closing. Wait.")
        return


    def setAgentTemplates(self, templates):
        self.graphSet[1]=None
        for tem in templates:
            if(self.graphSet[1]==None):
               self.graphSet[1]=Utils.getGraph(Utils, Utils.readOntoFile(Utils,tem))
            else:
               self.graphSet[1]+= Utils.getGraph(Utils, Utils.readOntoFile(Utils,tem))
        return

    def setAgentOntology(self, path):
        self.graphSet[0] = Utils.getGraph(Utils, Utils.readOntoFile(Utils,path))
        return

   # def setAgentConfiguration(self, path):
   #     self.graphSet[1] = self.getGraph(self.readOntoFile(path),self.iriSet[4])
   #     return

    def setAgentConnectionInfo(self, address, port, graph):
        the_port=''
        the_address=''
        the_connection=None
        for agent, connection in graph.subject_objects(predicate=URIRef(self.iriSet[0] + "#hasConnection")):
           for property, data in graph.predicate_objects(subject=connection):
              if property == URIRef(self.iriSet[0]+"#hasIPAddress"):
                  the_address=data
              elif property== URIRef(self.iriSet[0]+"#hasPortNumber"):
                  the_port=data
              the_connection=connection
        if port!=None and address !=None:
           graph.remove((the_connection, URIRef(self.iriSet[0] + "#hasPortNumber"), None))
           graph.remove((the_connection, URIRef(self.iriSet[0] + "#hasIPAddress"), None))
           graph.add((the_connection, URIRef(self.iriSet[0] + "#hasIPAddress"),  Literal(address, datatype=XSD.string)))
           graph.add((the_connection, URIRef(self.iriSet[0] + "#hasPortNumber"),  Literal(port, datatype=XSD.integer)))
           self.agentInfo[1] = address
           self.agentInfo[2] = port
        else:
           self.agentInfo[1]=the_address
           self.agentInfo[2]=the_port
        return 1

    def setAgentIRIs(self, graph):
        for agent in graph.subjects(predicate=RDF.type,  object=URIRef(self.iriSet[0]+'#Device')):
            names= str(agent).split('#')
            self.agentInfo[0]=names[1]
            break
        return


    def check_install(self):
        if(self.hubInfo[0]=='' or self.hubInfo[1]==''):
           return 0
        timestamp = Utils.getTimeStamp(Utils)
        reqGraph = rdflib.Graph()
        iri= str(self.iriSet[2]).rsplit('.',1)[0]+"-request"+timestamp+".owl"
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[1])))

        task = URIRef(iri + "#task")  # the task
        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add((agent, URIRef(self.iriSet[0] + "#hasType"), URIRef(self.iriSet[1] + "#device_type") ))  # task object

        Utils.generateRequest(Utils,reqGraph, iri, self.iriSet[0], task, agent, URIRef(self.iriSet[1] + "#check"), URIRef(self.iriSet[1] + "#installation"), None)

        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add((agent, RDF.type, URIRef(self.iriSet[0] + "#Device")))  # has request

        request = URIRef(iri + "#request")  # the request
        reqGraph.add((URIRef(self.iriSet[0] + "#requests"), RDF.type, Utils.owlobj))
        reqGraph.add((agent, URIRef(self.iriSet[0] + "#requests"), request))  # has request

        tosend = Utils.libbug(Utils, reqGraph,  iri)  # transmits config solving the rdflib bug of xml:base
        received= Utils.transmit(Utils,tosend.encode(), True, self.hubInfo[0], self.hubInfo[1])
        if received == None:
           return 0
        g = rdflib.Graph()
        g.parse(data=received)
        for s, b in g.subject_objects(URIRef(self.iriSet[0] + "#hasStatus")):
            if str(b) == self.iriSet[1] + "#succeded_status":
               return 1
        return 0

    def install_device(self):
        if(self.hubInfo[0]=='' or self.hubInfo[1]==''):
           return 0
        timestamp = Utils.getTimeStamp(Utils)

        tosend=Utils.libbug(Utils, self.graphSet[1], self.iriSet[3])  # transmits template solving the rdflib bug of xml:base
        state=Utils.transmit(Utils, tosend.encode(), False, self.hubInfo[0], self.hubInfo[1])
        if state == None:
            return 0

        tosend = Utils.libbug(Utils, self.graphSet[0], self.iriSet[2])  # transmits behavior solving the rdflib bug of xml:base
        state=Utils.transmit(Utils, tosend.encode(), False, self.hubInfo[0], self.hubInfo[1])
        if state == None:
            return 0
        reqGraph = rdflib.Graph()

        iri= str(self.iriSet[2]).rsplit('.',1)[0]+"-request"+timestamp+".owl"
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[1])))
        #if(self.iriSet[4] != ''):
        #    reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[4])))

        task = URIRef(iri + "#task")  # the task
        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add(
            (agent, URIRef(self.iriSet[0] + "#hasType"), URIRef(self.iriSet[1] + "#device_type")))  # task object
        parameter = URIRef(iri +"#parameter")  # the parameter
        Utils.generateRequest(Utils,reqGraph, iri, self.iriSet[0], task, agent,URIRef(self.iriSet[1] + "#install"), None, parameter )

        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add((agent, RDF.type, URIRef(self.iriSet[0] + "#Device")))  # has request

        request = URIRef(iri + "#request")  # the request
        reqGraph.add((URIRef(self.iriSet[0] + "#requests"), RDF.type, Utils.owlobj))
        reqGraph.add((agent, URIRef(self.iriSet[0] + "#requests"), request))  # has request

        reqGraph.add((parameter, RDF.type, URIRef(self.iriSet[0] + "#OntologyDescriptionObject")))

        reqGraph.add((URIRef(self.iriSet[0] + "#hasInformationObjectType"), RDF.type, Utils.owlobj))
        reqGraph.add((parameter, URIRef(self.iriSet[0] + "#hasInformationObjectType"), URIRef(self.iriSet[1] + "#ontology_description_object_type")))

        reqGraph.add((URIRef(self.iriSet[0] + "#descriptionProvidedByIRI"), RDF.type, Utils.owldat))
        reqGraph.add((parameter, URIRef(self.iriSet[0] + "#descriptionProvidedByIRI"), Literal(self.iriSet[2], datatype=XSD.string)))

        tosend = Utils.libbug(Utils, reqGraph,  iri)  # transmits config solving the rdflib bug of xml:base
        received= Utils.transmit(Utils,tosend.encode(), True, self.hubInfo[0], self.hubInfo[1])
        if received == None:
           return 0
        g = rdflib.Graph()
        g.parse(data=received)
        for s, b in g.subject_objects(URIRef(self.iriSet[0] + "#hasStatus")):
            if (str(b) == self.iriSet[1] + "#succeded_status"):
                print("Device installation confirmed by the hub")
            else:
                print("Device installation not confirmed by the hub")
                return 0
        #f=open("test.owl", "w")
        #f.write(g.serialize(format="pretty-xml").decode())
        return 1

    def uninstall_device(self):
        if (self.hubInfo[0] == '' or self.hubInfo[1] == ''):
            return 0
        timestamp = Utils.getTimeStamp(Utils)

        reqGraph = rdflib.Graph()

        iri = str(self.iriSet[2]).rsplit('.', 1)[0] + "-request" + timestamp + ".owl"
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[1])))
        # if(self.iriSet[4] != ''):
        #    reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[4])))
        task = URIRef(iri + "#task")  # the task
        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        Utils.generateRequest(Utils, reqGraph, iri, self.iriSet[0], task, agent, URIRef(self.iriSet[1] + "#uninstall"), None, None)

        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add(
            (agent, URIRef(self.iriSet[0] + "#hasType"), URIRef(self.iriSet[1] + "#device_type")))  # task object
        reqGraph.add((agent, RDF.type, URIRef(self.iriSet[0] + "#Device")))  # has request

        request = URIRef(iri + "#request")  # the request
        reqGraph.add((URIRef(self.iriSet[0] + "#requests"), RDF.type, Utils.owlobj))
        reqGraph.add((agent, URIRef(self.iriSet[0] + "#requests"), request))  # has request


        tosend = Utils.libbug(Utils, reqGraph, iri)  # transmits config solving the rdflib bug of xml:base
        received = Utils.transmit(Utils, tosend.encode(), True, self.hubInfo[0], self.hubInfo[1])
        if (received == None):
            return 0
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

    def set_hub(self, host, port):
        self.hubInfo[0]=host
        self.hubInfo[1]=port
        return 1

    
    def set_connection(self, host, port):
        if self.hubInfo[0]=='' or self.hubInfo[1]=='':
            return 0

        self.agentInfo[1]=host
        self.agentInfo[2]=port
        self.alive = False
        self.serversocket.close()
        time.sleep(2)

        timestamp = Utils.getTimeStamp(Utils)

        self.setAgentConnectionInfo(host, port, self.graphSet[0])

        tosend = Utils.libbug(Utils, self.graphSet[0],
                              self.iriSet[2])  # transmits behavior solving the rdflib bug of xml:base
        state = Utils.transmit(Utils, tosend.encode(), False, self.hubInfo[0], self.hubInfo[1])
        if state == None:
            return 0
        reqGraph = rdflib.Graph()

        iri = str(self.iriSet[2]).rsplit('.', 1)[0] + "-request" + timestamp + ".owl"
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[1])))
        # if(self.iriSet[4] != ''):
        #    reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[4])))

        task = URIRef(iri + "#task")  # the task
        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add(
            (agent, URIRef(self.iriSet[0] + "#hasType"), URIRef(self.iriSet[1] + "#device_type")))  # task object
        parameter = URIRef(iri + "#parameter")  # the parameter
        Utils.generateRequest(Utils, reqGraph, iri, self.iriSet[0], task, agent, URIRef(self.iriSet[1] + "#update"), None,
                              parameter)

        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add((agent, RDF.type, URIRef(self.iriSet[0] + "#Device")))  # has request

        request = URIRef(iri + "#request")  # the request
        reqGraph.add((URIRef(self.iriSet[0] + "#requests"), RDF.type, Utils.owlobj))
        reqGraph.add((agent, URIRef(self.iriSet[0] + "#requests"), request))  # has request

        reqGraph.add((parameter, RDF.type, URIRef(self.iriSet[0] + "#OntologyDescriptionObject")))

        reqGraph.add((URIRef(self.iriSet[0] + "#hasInformationObjectType"), RDF.type, Utils.owlobj))
        reqGraph.add((parameter, URIRef(self.iriSet[0] + "#hasInformationObjectType"),
                      URIRef(self.iriSet[1] + "#ontology_description_object_type")))

        reqGraph.add((URIRef(self.iriSet[0] + "#descriptionProvidedByIRI"), RDF.type, Utils.owldat))
        reqGraph.add((parameter, URIRef(self.iriSet[0] + "#descriptionProvidedByIRI"),
                      Literal(self.iriSet[2], datatype=XSD.string)))

        tosend = Utils.libbug(Utils, reqGraph, iri)  # transmits config solving the rdflib bug of xml:base
        received = Utils.transmit(Utils, tosend.encode(), True, self.hubInfo[0], self.hubInfo[1])
        if received == None:
            return 0
        g = rdflib.Graph()
        g.parse(data=received)
        for s, b in g.subject_objects(URIRef(self.iriSet[0] + "#hasStatus")):
            if (str(b) == self.iriSet[1] + "#succeded_status"):
                print("Device update confirmed by the hub")
            else:
                print("Device update not confirmed by the hub")
        # f=open("test.owl", "w")
        # f.write(g.serialize(format="pretty-xml").decode())
        return 1

    def printGraph (self, graph):
        print("--- printing raw triples ---")
        for s, p, o in graph:
           print((s, p, o))

    def retrieveEntityName(self, iri):
        start=iri.rfind('#')
        return iri[start+1:]

    def run(self):
        while(self.restart):
          self.serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
          self.serversocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
          self.serversocket.bind((self.agentInfo[1], int(self.agentInfo[2])))
          self.serversocket.listen(5)
          print("Client listening on", self.agentInfo[1], "port", self.agentInfo[2])
          self.alive = True
          while self.alive:
              try:
                clientsocket, address = self.serversocket.accept()
                ServerManager(clientsocket, address, self)
              except Exception as e:
                  break
        print("Server stopped. Goodbye.")
        return

    #############################################################################################



class Console(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.start()
        return

    def start_command(self, address, port):
        return Agent(address, port, "ontologies/test/rasb/rasb-lightagent.owl",
                        {"ontologies/test/rasb/lightagent-from-template.owl"},
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

    def check_install(self, agent):
        return agent.check_install()

    def uninstall_device(self, agent):
        return agent.uninstall_device()

    def set_hub(self, agent, host, port):
        return agent.set_hub(host, port)

    def set_connection(self, agent, host, port):
        return agent.set_connection(host, port)

    def checkAgent(self, agent):
        if(agent == None):
            print("Agent not started. Please start the agent first")
            return 0
        return 1

    def setTestPath(self):
        p = Path(__file__).parents[1]
        os.chdir(p)
        return

    def run(self):
        self.setTestPath()
        agent = None
        exec_status = True
        while (exec_status):
            print("Enter a command: ---> ", end='')
            command = input("").strip()
            if command.startswith("start"):
                parms = command.split();
                if( len(parms)==1):
                  agent = self.start_command(None, None) #default address, port
                elif(len(parms)==3):
                  agent = self.start_command(parms[1], parms[2])
                else:
                  print("Use: start | start address port")
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
                       print("Device cannot be installed. Make sure the hub is correctly set")
            elif command == "uninstall":
                if self.checkAgent(agent):
                    if (self.uninstall_device(agent)):
                        print("Device uninstallation complete")
                    else:
                        print("Device cannot be uninstalled. Make sure the hub is correctly set")
            elif command.startswith("set hub"):
                if not self.checkAgent(agent):
                   continue
                parms=command.split();
                if len(parms)==4:
                  if self.set_hub(agent, parms[2], parms[3]):
                    print("The hub is located at address ", parms[2], "port ", parms[3])
                  else:
                   print ("The hub cannot be configured, check the parameters")
                else:
                    print("Use: set hub address port")
            elif command.startswith("set device"):
                if not self.checkAgent(agent):
                    continue
                parms = command.split();
                if len(parms) == 4:
                    if self.set_connection(agent, parms[2], parms[3]):
                        print("The device has been updated")
                    else:
                        print ("The device cannot be updated, check the parameters")
                else:
                    print("Use: set hub address port")
            elif command == "check install":
                if self.checkAgent(agent):
                    if (self.check_install(agent)):
                        print("The device is installed")
                    else:
                        print("The device is not installed")
            else:
                print("Unrecognized command")
                print("Use start | start [address] [port] | stop | exit | status | install | uninstall | set hub [address] [port] | set device address port check install")
            time.sleep(1)
        return

def main():
     Console()

if __name__ == '__main__':
    main()

