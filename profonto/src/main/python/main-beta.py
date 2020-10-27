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
from lib.utils import *
import re



class ProfOnto (Thread):
    def __init__(self, oasis, oasisabox, assistant, address, port):
        Thread.__init__(self)
        self.profonto = None
        self.oasis = oasis
        self.oasisabox = oasisabox
        self.assistant=''
        self.host = address
        self.port = port
        self.alive=True
        self.restart=True
        self.iriassistant=assistant
        self.start()


    def run(self):
        self.init_gateway()
        print(open("amens/logo.txt", "r").read())
        # Adding HomeAssistant
        self.home = Utils.readOntoFile(Utils, "ontologies/test/homeassistant.owl")
        if self.host != None and self.port != None:
            self.home=self.modifyConnection(self.home, self.host, self.port)
        self.assistant = self.profonto.addDevice(self.home)  # read the device data
        if self.assistant == None:
            print("The assistant cannot be started")
            return
        sarray = self.profonto.getConnectionInfo(self.assistant)
        self.host = sarray[0]
        self.port = sarray[1]
        print("Home assistant added:", self.assistant, "at ", self.host, self.port)
        self.init_server()

    def modifyConnection(self, home, host, port):
        g= Utils.getGraph(Utils, home)
        for s,o in g.subject_objects(predicate=URIRef( self.oasis + "hasPortNumber")):
            g.remove((s,URIRef( self.oasis + "hasPortNumber"), o))
            g.add((s, URIRef( self.oasis + "hasPortNumber"), Literal(port, datatype=XSD.integer)))

        for s, o in g.subject_objects(predicate=URIRef(self.oasis + "hasIPAddress")):
            g.remove((s, URIRef(self.oasis + "hasIPAddress"), o))
            g.add((s, URIRef(self.oasis + "hasIPAddress"), Literal(host, datatype=XSD.string)))
        return Utils.libbug(Utils, g, self.assistant)



    def setConnection(self, host, port):
        if(Utils.checkAddress(Utils, host, port)==0):
             print("Invalid port typed.")
             return 0
        if self.profonto.modifyConnection(self.iriassistant, host, port) == 1 :
            self.host=host
            self.port=port
            self.alive=False
            #self.serversocket.shutdown(socket.SHUT_WR)
            self.serversocket.close()
            time.sleep(2)
            return 1
        else:
            return 0

    def stop(self):
        self.alive=False
        self.restart=False
        self.serversocket.close()
        print("Server is closing. Wait.")
        try:
            self.profontoGateWay.close()
            self.profontoGateWay.shutdown()
        except Exception as e:
            return
        return

    def init_server(self):
        print("Prof-Onto Assistant has been started")
        while self.restart:
          print("Prof-Onto Assistant is listening:", self.host, "port ", self.port)
          self.serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
          self.serversocket.bind((self.host, int(self.port)))
          self.serversocket.listen(5)
          self.alive=True
          while self.alive:
              try:
                  clients, address = self.serversocket.accept()
                  request = Utils.recvall(Utils, clients).decode()
                  self.decide(request, address[1], address[0], clients)
              except Exception as e:
                  break
             #client(self, clients, address, serversocket)
        return


    def setExecutionStatus(self, graph):
        for execution, status in graph.subject_objects(predicate=URIRef( self.oasis + "hasStatus")):
          ret= self.profonto.setExecutionStatus(execution, status)
          if ret<1:
              print("Execution status of " + execution + "cannot be updated")


    def transmitExecutionStatus(self, execution, status, addr, sock,  server_socket):
        g=Graph()
        iri=Utils.retrieveURI(Utils, execution).replace(".owl","-response.owl")
        task = URIRef(iri + "#task")
        object = URIRef(iri + "#belief-data")  # the obj
        parameter = URIRef(iri + "#parameter")  # the parameter
        operator=URIRef(self.oasis + "add")  # task operator
        Utils.generateRequest(Utils, g, iri, self.oasis, task, object, operator, None, parameter)
        iriassist=self.iriassistant+'#'+self.assistant


        g.add((URIRef(iriassist), RDF.type, URIRef( self.oasis + "Device")))  # has request
        g.add((URIRef(iriassist), URIRef( self.oasis + "requests"), URIRef(iri + "#request")))


        g.add((URIRef( self.oasis + "hasInformationObjectType"), RDF.type, Utils.owlobj))
        g.add((object, URIRef( self.oasis + "hasInformationObjectType"),
                      URIRef( self.oasisabox + "belief_description_object_type")))

        g.add((parameter, RDF.type, URIRef( self.oasis + "OntologyDescriptionObject")))

        g.add((parameter, URIRef( self.oasis + "hasInformationObjectType"),URIRef( self.oasisabox + "ontology_description_object_type")))

        g.add((URIRef( self.oasis + "descriptionProvidedByIRI"), RDF.type, Utils.owldat))
        g.add((parameter, URIRef( self.oasis + "descriptionProvidedByIRI"), Literal(iri, datatype=XSD.string)))

        g.add((URIRef( self.oasis + "refersTo"), RDF.type, Utils.owlobj))
        g.add((parameter, URIRef( self.oasis + "refersTo"), URIRef(execution)))

        g.add((URIRef( self.oasis + "hasStatus"), RDF.type, Utils.owlobj))
        g.add((URIRef(execution),URIRef( self.oasis + "hasStatus"), URIRef(status)))


        res=Utils.serverTransmit(Utils, g.serialize(format='pretty-xml'), sock, addr,  server_socket)
        server_socket.close()
        return res


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
        for t in graph.objects(execution, URIRef( self.oasis + "hasTaskActualInputParameter")): # retrieving source
           for s in graph.objects(t, URIRef( self.oasis + "descriptionProvidedByURL")):
               if (s is not None):
                 file = Utils.readOntoFile(Utils, s)
                 return file
           for s in graph.objects(t, URIRef( self.oasis + "descriptionProvidedByIRI")):
             if (s is not None):
                 file=s
                 return s



    def createRequest(self, graph, request, execution):
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


    def device_engage(self, graph, execution):
        #for s,p,o in graph.triples( (None,None,None) ):
            #print(s,p,o)
        toreturn = Graph()
        taskOb = next(graph.objects(execution, URIRef( self.oasis + "hasTaskObject")))
        taskOp = next(graph.objects(execution, URIRef( self.oasis + "hasTaskOperator")))
        taskObject = next(graph.objects(taskOb, URIRef(self.oasis + "refersExactlyTo")))
        taskOperator = next(graph.objects(taskOp, URIRef(self.oasis + "refersExaclyTo")))

        performer = next(graph.subjects(URIRef( self.oasis + "performs"), execution))
        devip=next(graph.objects(subject=None, predicate=URIRef( self.oasis + "hasIPAddress")))
        devport=next(graph.objects(subject=None, predicate=URIRef( self.oasis + "hasPortNumber")))
        value=100
        for s, t in graph.subject_objects(URIRef(self.oasis + "hasTaskActualInputParameter")):
            o=next(graph.objects(t, URIRef(self.oasis + "refersExaclyTo")))
            toreturn.add((s, URIRef(self.oasis + "hasTaskActualInputParameter"), t))
            toreturn.add((t, URIRef(self.oasis + "refersExacltyTo"), o))
            for v in graph.objects(o, URIRef(self.oasis + "hasDataValue")):
                toreturn.add((o,URIRef(self.oasis + "hasDataValue"),v))
                value=v
                break
        print("To engage:", performer, taskObject, value, taskOperator, devip, devport)

        self.createRequest(graph, toreturn, execution)
        toreturn=toreturn.serialize(format='xml')
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((devip, int(devport)))
        client_socket.send(toreturn)
        message = Utils.recvall(Utils, client_socket)
        client_socket.close()
        return message


    # Actions that the assistant performs
    def profhome_decide(self, graph, execution, addr, sock, server_socket):
        req = next(graph.objects(execution, URIRef( self.oasis + "hasTaskObject")))
        requester = next(graph.objects(req, URIRef( self.oasis + "refersExactlyTo")))
        operator=next(graph.objects(execution, URIRef( self.oasis + "hasTaskOperator")))
        for actions in graph.objects(operator, URIRef( self.oasis + "refersExactlyTo")):
            res=0
            if actions == URIRef(self.oasisabox + "install"):
                file =  self.getOntologyFile(graph, execution)
                value =  self.profonto.addDevice(file)  # read the device data
                if value == None or value=="":
                    print ("A device cannot be added")
                    res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock, server_socket)
                else:
                    print("Device", value, "added.")
                    res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox+"succeded_status"), addr, sock, server_socket)

            elif actions == URIRef(self.oasisabox + "update"):
                file =  self.getOntologyFile(graph, execution)
                value =  self.profonto.modifyDevice(file)  # read the device data
                if value == None:
                    print ("A device cannot be updated")
                    res= self.transmitExecutionStatus(execution, URIRef(self.oasisabox + "failed_status"), addr, sock, server_socket)
                else:
                    print("Device", value, "updated.")
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
                for arg in graph.objects(execution, URIRef(self.oasis + "hasOperatorArgument")):
                    argument = next(graph.objects(arg, URIRef(self.oasis + "refersExactlyTo")))
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

        g = Utils.getGraph(Utils, value)


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
                    Utils.serverTransmit(Utils, message, sock, addr, server_socket)
                    belief =  self.profonto.parseRequest(message.decode())[1]
                    if belief == None:
                        print("A belief from " + execution + " cannot be added")
                    else:
                        self.profonto.addDataBelief(belief)
                        # print(belief)




    def init_gateway(self):
        #p = Path(__file__).parents[1]
        #os.chdir(p)
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


class Console(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.start()
        return

    def start_command(self, address, port):
        return ProfOnto('http://www.dmi.unict.it/oasis.owl#', 'http://www.dmi.unict.it/oasis-abox.owl#', 'http://www.dmi.unict.it/profonto-home.owl', address, port)

    def stop_command(self, agent):
        agent.stop()
        return True

    def exit_command(self, agent):
        self.stop_command(agent)
        print("Console closing. Goodbye.")
        return False

    def setTestPath(self):
        p = Path(__file__).parents[1]
        os.chdir(p)
        return

    def set_connection(self, agent, host, port):
        return agent.setConnection(host, port)


    def run(self):
        self.setTestPath()
        agent = None
        exec_status = True
        while (exec_status):
            print("Enter a command:  ---> ", end='')
            command = input('').strip()
            if command.startswith("start"):
                parms = command.split();
                if( len(parms)==1):
                  agent = self.start_command(None, None) #default address, port
                elif(len(parms)==3):
                  agent = self.start_command(parms[1], parms[2])
                else:
                  print("Use: start | start address port")
            elif agent !=None:
                if command == "stop":
                   self.stop_command(agent)
                elif command == "exit":
                   exec_status = self.exit_command(agent)
                elif command.startswith("set"):
                   parms = command.split();
                   if len(parms) == 3 :
                     exec_status = self.set_connection(agent, parms[1], parms[2])
                     if exec_status == 1:
                         print("Connection successifully modified")
                     else:
                         print("Connection cannot be modified")
                   else:
                      print("Use: set address port")
                else:
                   print("Unrecognized command")
                   print("Use start [address] [port] | stop | exit | set address port")
            else:
                print("Start the agent first. Use: start | start address port")
            time.sleep(2)
        return




def main():
     Console()

if __name__ == '__main__':
    main()


