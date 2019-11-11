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
from datetime import datetime


class client(Thread):
    def __init__(self, prof, socket, address, server_socket):
        Thread.__init__(self)
        self.sock = socket
        self.addr = address
        self.server_socket = server_socket
        self.prof = prof
        self.start()

    def run(self):
        request = Utils.recvall(Utils, self.sock).decode()
        self.prof.decide(request, self.addr[1], self.addr[0], self.sock)


class ProfOnto (Thread):
    def __init__(self):
        self.profonto = None
        self.oasis = 'http://www.dmi.unict.it/oasis.owl#'
        self.oasisabox = 'http://www.dmi.unict.it/oasis-abox.owl#'
        self.assistant=''
        self.host = None
        self.port = None
        self.owlobj=URIRef("http://www.w3.org/2002/07/owl#ObjectProperty")
        self.owldat=URIRef("http://www.w3.org/2002/07/owl#DatatypeProperty")
        self.init_gateway()
        print(open("amens/logo.txt", "r").read())
        # Adding HomeAssistant
        self.home = Utils.readOntoFile(Utils, "ontologies/test/homeassistant.owl")
        self.assistant = self.profonto.addDevice(self.home)  # read the device data
        if self.assistant == None:
            print("The assistant cannot be started")
            return

        sarray = self.profonto.getConnectionInfo(self.assistant)
        self.host = sarray[0]
        self.port = sarray[1]
        print("Home assistant added:", self.assistant, "at ", self.host, self.port)
        self.init_server(self.host, self.port)


    def init_server(self, host, port):
        serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        serversocket.bind((host, int(port)))
        serversocket.listen(5)
        print("Prof-Onto Assistant has been started, send requests to:", self.host, "port ", self.port)
        while 1:
            clients, address = serversocket.accept()
            request = Utils.recvall(Utils, clients).decode()
            self.decide(request, address[1], address[0], clients)
            #client(self, clients, address, serversocket)
        return


    def getGraph(self, value):
        g = rdflib.Graph()
        g.parse(data=value)
        return g

    def setExecutionStatus(self, graph):
        for execution, status in graph.subject_objects(predicate=URIRef( self.oasis + "hasStatus")):
          ret= self.profonto.setExecutionStatus(execution, status)
          if ret<1:
              print("Execution status of " + execution + "cannot be updated")

    def transmitExecutionStatus(self, execution, status, addr, sock,  server_socket):
        g=Graph()
        iri=Utils.retrieveURI(Utils, execution).replace(".owl","-response.owl")
        self.generateRequest(g, iri)
        iriassist="http://www.dmi.unict.it/profonto-home.owl#"+self.assistant

        g.add((URIRef(iriassist), RDF.type, URIRef( self.oasis + "Device")))  # has request
        g.add((URIRef(iriassist), URIRef( self.oasis + "requests"), URIRef(iri + "#request")))

        task = URIRef(iri + "#task")
        g.add((URIRef( self.oasis + "hasTaskOperator"), RDF.type, self.owlobj))
        g.add((task, URIRef( self.oasis + "hasTaskOperator"), URIRef( self.oasis + "add")))  # task operator

        object = URIRef(iri + "#belief-data")  # the obj
        g.add((URIRef( self.oasis + "hasTaskObject"), RDF.type, self.owlobj))
        g.add((object, RDF.type, URIRef( self.oasis + "TaskObject")))
        g.add((URIRef( self.oasis + "hasInformationObjectType"), RDF.type, self.owlobj))
        g.add((object, URIRef( self.oasis + "hasInformationObjectType"),
                      URIRef( self.oasisabox + "belief_description_object_type")))
        g.add((task, URIRef( self.oasis + "hasTaskObject"), object))  # task object

        parameter = URIRef(iri + "#parameter")  # the parameter
        g.add((parameter, RDF.type, URIRef( self.oasis + "TaskInputParameter")))
        g.add((parameter, RDF.type, URIRef( self.oasis + "OntologyDescriptionObject")))

        g.add((URIRef( self.oasis+ "hasInformationObjectType"), RDF.type, self.owlobj))
        g.add((parameter, URIRef( self.oasis + "hasInformationObjectType"),URIRef( self.oasisabox + "ontology_description_object_type")))

        g.add((URIRef( self.oasis + "descriptionProvidedByIRI"), RDF.type, self.owldat))
        g.add((parameter, URIRef( self.oasis + "descriptionProvidedByIRI"), Literal(iri, datatype=XSD.string)))

        g.add((URIRef( self.oasis + "refersTo"), RDF.type, self.owlobj))
        g.add((parameter, URIRef( self.oasis + "refersTo"), URIRef(execution)))

        g.add((URIRef( self.oasis + "hasTaskInputParameter"), RDF.type, self.owlobj))
        g.add((task, URIRef( self.oasis + "hasTaskInputParameter"), parameter))  # task parameter

        g.add((URIRef( self.oasis + "hasStatus"), RDF.type, self.owlobj))
        g.add((URIRef(execution),URIRef( self.oasis + "hasStatus"), URIRef(status)))

       # f=open("test.owl", "w")
       # f.write(g.serialize(format="pretty-xml").decode())

        res=self.transmit(g.serialize(format='pretty-xml'), sock, addr,  server_socket)
        server_socket.close()
        return res

    def transmit(self, data, sock, addr, server_socket):
        print("Sending response to: ", addr, "port ", sock)
        try:
            server_socket.send(data)
        except socket.error:
            return 0
        #server_socket.close()
        else:
            return 1


    def generateRequest(self, reqGraph, iri):

        request = URIRef(iri+"#request")             #the request
        reqGraph.add(( request, RDF.type, URIRef( self.oasis+"PlanDescription")))  # request type

        goal = URIRef(iri + "#goal")  # the goal
        reqGraph.add((goal, RDF.type, URIRef( self.oasis + "GoalDescription")))  # goal type

        task = URIRef(iri + "#task")  # the task
        reqGraph.add((task, RDF.type, URIRef( self.oasis + "TaskDescription")))  # task type

        reqGraph.add((URIRef( self.oasis + "consistsOfGoalDescription"), RDF.type, self.owlobj))
        reqGraph.add((request, URIRef( self.oasis + "consistsOfGoalDescription"), goal))  # has goal

        reqGraph.add((URIRef( self.oasis + "consistsOfTaskDescription"), RDF.type, self.owlobj))
        reqGraph.add((goal, URIRef( self.oasis + "consistsOfTaskDescription"), task))  # has goal
        return

    def computesDependencies(self, graph, executions):
          for first, second in graph.subject_objects(predicate=URIRef( self.oasis + "dependsOn")):
              index=0
              while index < len(executions):
                  key,value = executions[index]
                  if second == key :
                      executions[index]= (key,value+1)
                  index += 1

    def getOntologyFile(self, graph, execution):
        file=None
        for t in graph.objects(execution, URIRef( self.oasis + "hasTaskInputParameter")): # retrieving source
           for s in graph.objects(t, URIRef( self.oasis + "descriptionProvidedByURL")):
               if (s is not None):
                 file = Utils.readOntoFile(Utils, s)
                 return file
           for s in graph.objects(t, URIRef( self.oasis + "descriptionProvidedByIRI")):
             if (s is not None):
                 file=s
                 return s



    def createRequest(self, graph,execution):
        request=Graph()
        uri='http://www.dmi.unict.it/profonto-home.owl#'
        name=Utils.retrieveEntityName(Utils, execution)
        request.add((execution, RDF.type, URIRef( self.oasis + "TaskExecution")))
        request.add((URIRef(uri+name+"PlanRequest"), RDF.type, URIRef( self.oasis+ "PlanDescription") ))
        request.add((URIRef(uri + name + "GoalRequest"), RDF.type, URIRef( self.oasis + "GoalDescription")))
        request.add((URIRef(uri + name + "PlanRequest"), URIRef( self.oasis + "consistsOfGoalDescription"), URIRef(uri + name + "GoalRequest")))
        request.add((URIRef(uri + name + "TaskRequest"), RDF.type, URIRef( self.oasis + "TaskDescription")))
        request.add((URIRef(uri + name + "GoalRequest"), URIRef( self.oasis + "consistsOfTaskDescription"), URIRef(uri + name + "TaskRequest")))
        request.add((URIRef(uri + name + "TaskRequest"), URIRef( self.oasis + "hasTaskObject"), execution))
        request.add((URIRef(uri + name + "TaskRequest"), URIRef( self.oasis + "hasTaskOperator"), URIRef( self.oasisabox + "performs")))
        taskObject = next(graph.objects(execution, URIRef( self.oasis + "hasTaskObject")))
        taskOperator = next(graph.objects(execution, URIRef( self.oasis + "hasTaskOperator")))
        #performer = next(graph.subjects(URIRef(oasis + "performs"), execution))
        request.add((execution, URIRef( self.oasis+"hasTaskObject"), taskObject))
        request.add((execution, URIRef( self.oasis + "hasTaskOperator"), taskOperator))
        return request

    def device_engage(self, graph, execution):
        #for s,p,o in graph.triples( (None,None,None) ):
        #    print(s,p,o)
        taskObject = next(graph.objects(execution, URIRef( self.oasis + "hasTaskObject")))
        taskOperator = next(graph.objects(execution, URIRef( self.oasis + "hasTaskOperator")))
        performer = next(graph.subjects(URIRef( self.oasis + "performs"), execution))
        devip=next(graph.objects(subject=None, predicate=URIRef( self.oasis + "hasIPAddress")))
        devport=next(graph.objects(subject=None, predicate=URIRef( self.oasis + "hasPortNumber")))
        print("To engage:", performer, taskObject, taskOperator, devip, devport)
        toreturn = self.createRequest(graph,execution).serialize(format='xml')
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((devip, int(devport)))
        client_socket.send(toreturn)
        message = Utils.recvall(Utils, client_socket)
        client_socket.close()
        return message


    # Actions that the assistant performs
    def profhome_decide(self, graph, execution, addr, sock, server_socket):
        requester = next(graph.objects(execution, URIRef( self.oasis + "hasTaskObject")))
        for actions in graph.objects(execution, URIRef( self.oasis + "hasTaskOperator")):
            res=0
            if actions == URIRef(self.oasisabox + "install"):
                file =  self.getOntologyFile(graph, execution)
                value =  self.profonto.addDevice(file)  # read the device data
                if value == None:
                    print ("A device cannot be added")
                    res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock, server_socket)
                else:
                    print("Device", value, "added.")
                    res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"), addr, sock, server_socket)

            elif actions == URIRef(self.oasisabox + "uninstall"):  # uninstallation task
                #requester = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
                value =  self.profonto.removeDevice(Utils.retrieveEntityName(Utils, requester))  # read the device data
                if int(value) < 1:
                    print("Device", Utils.retrieveEntityName(Utils, requester)+ " cannot be removed")
                    res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock, server_socket)
                else:
                    print("Device", Utils.retrieveEntityName(Utils, requester), "correctly removed")
                    res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"), addr, sock, server_socket)

            elif actions == URIRef(self.oasisabox + "add") or actions == URIRef(self.oasisabox + "remove"):  # add user task
                 for thetype in graph.objects(requester, URIRef(self.oasis + "hasType")):
                     if thetype== URIRef(self.oasisabox + "user_type"): #adding or removing user
                         if actions == URIRef(self.oasisabox + "add"):
                             file =  self.getOntologyFile(graph, execution)
                             value=  self.profonto.addUser(file)
                             if value==None:
                                 print ("A user cannot be added")
                                 res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                         server_socket)
                             else:
                                self.profonto.setExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"), )
                                res= self.transmitExecutionStatus(execution,URIRef(self.oasisabox+"succeded_status"), addr, sock,  server_socket)
                                print("User", value, "added.")
                         elif actions == URIRef(self.oasisabox + "remove"):
                              value= self.profonto.removeUser(Utils.retrieveEntityName(Utils, requester))
                              if int(value) < 1:
                                  print("User " + Utils.retrieveEntityName(Utils, requester)+ " cannot be removed")
                                  res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                          server_socket)
                              else:
                                 self.profonto.setExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"))
                                 res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"), addr, sock,  server_socket)
                                 print("User", Utils.retrieveEntityName(Utils, requester), "correctly removed")
                     elif thetype == URIRef(self.oasisabox + "user_configuration_type"):  # adding or removing user
                         if actions == URIRef(self.oasisabox + "add"):
                             file =  self.getOntologyFile(graph, execution)
                             value=  self.profonto.addConfiguration(file)
                             if value== None:
                                 print("A configuration cannot be added")
                                 res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                         server_socket)
                             else:
                                 self.profonto.setExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"))
                                 res= self.transmitExecutionStatus(execution, "succeded_status", addr, sock,  server_socket)
                                 print("Configuration added:", value,".")
                         elif actions == URIRef(self.oasisabox + "remove"):
                             value =  self.profonto.removeConfiguration(Utils.retrieveEntityName(Utils, requester))
                             if int(value) < 1:
                                 print("A configuration cannot be removed")
                                 res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                         server_socket)
                             else:
                                 self.profonto.setExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"))
                                 res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"), addr, sock,  server_socket)
                                 print("Configuration", Utils.retrieveEntityName(Utils, requester), "correctly removed.")
                     elif  thetype == URIRef(self.oasisabox + "belief_description_object_type"):
                          file =  self.getOntologyFile(graph, execution)
                          if actions == URIRef(self.oasisabox + "add"):
                             value =  self.profonto.addDataBelief(file)
                             if int(value) < 1:
                                 print("Data belief cannot be added")
                                 res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                         server_socket)
                             else:
                                 self.profonto.setExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"))
                                 res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"), addr, sock,  server_socket)
                                 print("Belief  correctly added")
                          elif actions == URIRef(self.oasisabox + "remove"):
                             value =  self.profonto.removeDataBelief(file)
                             if int(value) < 1:
                                 print("Belief cannot be removed")
                                 res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                         server_socket)
                             else:
                                 self.profonto.setExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"))
                                 res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"), addr, sock,  server_socket)
                                 print("Belief correctly removed")
                     else:
                         res =  self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                       server_socket)

            elif actions == URIRef(self.oasisabox + "parse"):
                for thetype in graph.objects(requester, URIRef(self.oasis + "hasType")):
                    if thetype == URIRef(self.oasisabox + "generalUtterance"):
                        print("General utterances parser is being developed... stay tuned!")
                        res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox+"failed_status"), addr, sock, server_socket)
                    else:
                        res =  self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                      server_socket)

            elif actions == URIRef(self.oasisabox + "retrieve"):
                for thetype in graph.objects(requester, URIRef(self.oasis + "hasType")):
                    if thetype == URIRef(self.oasisabox + "belief_description_object_type"):
                        file =  self.getOntologyFile(graph, execution)
                        value =  self.profonto.retrieveDataBelief(file)
                        if value == None:
                            print ("Belief cannot be retrieved")
                            res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                    server_socket)
                        else:
                            self.profonto.setExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"))
                            res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"), addr, sock,  server_socket)
                            print("Belief retrieved:\n"+ value)
                    else:
                        res =  self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                  server_socket)
            elif actions == URIRef(self.oasisabox + "check"):
                for argument in graph.objects(execution, URIRef(self.oasis + "hasOperatorArgument")):
                    if argument == URIRef(self.oasisabox + "installation"):
                        print("Checking for the presence of ", requester)
                        isPresent= self.profonto.checkDevice(requester)
                        if isPresent == 1:
                            res =  self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "succeded_status"), addr, sock,
                                                          server_socket)
                            print("Device ", requester, " is installed")
                        else:
                            res =  self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                      server_socket)
                            print("Device ", requester, " is not installed")
                    else:
                        res =  self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock,
                                                      server_socket)
            else:
                print("Action", actions, "not supported yet")
                res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox+"failed_status"), addr, sock, server_socket)

            if res < 1:
                print("Execution status cannot be transmitted")


    def decide(self, rdf_source, sock, addr, server_socket):
        value = self.profonto.parseRequest(rdf_source)[0]
        # print("Client send request:", value)
        if value == None:
            print ("Received data from " + str(addr) + " " + str(sock))
            return

        g = self.getGraph(value)

        executions = []
        for execution in g.subjects(RDF.type, URIRef( self.oasis + "TaskExecution")):
            executions.append((execution, 0))

        if len(executions) > 1:
            self.computesDependencies(g, executions)
            executions = sorted(executions, key=lambda x: x[1])

        for execution, val in executions:
            for executer in g.subjects(URIRef( self.oasis + "performs"), execution):
                if (Utils.retrieveEntityName(Utils, executer) == self.assistant):
                    self.profhome_decide(g, execution, addr, sock, server_socket)
                else:
                    message =  self.device_engage(g, execution)
                    self.transmit(message, sock, addr, server_socket)
                    belief =  self.profonto.parseRequest(message.decode())[1]
                    if belief == None:
                        print("A belief from " + execution + " cannot be added")
                    else:
                        self.profonto.addDataBelief(belief)
                        # print(belief)




    def init_gateway(self):
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
        print(self.getProcessOut(process))
        self.profontoGateWay = JavaGateway()  # connect to the JVM
        self.profonto =  self.profontoGateWay.entry_point

    def getProcessOut(self, process):
        message = ''
        while True:
            out = process.stdout.read(1)
            if out != '\n':
                message += out
            else:
                break
        return message


class Utils():
    def recvall(self, sock):
        BUFF_SIZE = 1024  # 1 KiB
        data = b''
        timeout = time.time() + 60
        while time.time() < timeout:
            part = sock.recv(BUFF_SIZE)
            data += part
            if len(part) < BUFF_SIZE:
                # either 0 or end of data
                break
        return data

    def getTimeStamp(self):
      return (str(datetime.timestamp(datetime.now()))).replace(".", "-")

    def retrieveURI(self, string):
        out = string.split("#", 1)[0]
        return out

    def retrieveEntityName(self, string):
        out = string.split("#", 1)[1]
        return out

    def readOntoFile(self, file):
        f = open(file, "r")
        return f.read()

###

ProfOnto()

