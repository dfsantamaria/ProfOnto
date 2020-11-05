import rdflib
from rdflib import *
from datetime import datetime
import time
import re
import socket


class Utils():
    owlobj = URIRef("http://www.w3.org/2002/07/owl#ObjectProperty")
    owldat = URIRef("http://www.w3.org/2002/07/owl#DatatypeProperty")

    def getOWLobj (self):
        return self.owlobj

    def getOWLdat(self):
        return owldat

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


    def getGraph(self, value):
        g = rdflib.Graph()
        g.parse(data=value)
        return g

    def replacenth(self, string, sub, wanted, n):
        where = [m.start() for m in re.finditer(sub, string)][n - 1]
        before = string[:where]
        after = string[where:]
        after = after.replace(sub, wanted, 1)
        newString = before + after
        return newString

    def libbug(self, graph, iri):
        tosend = graph.serialize(format='pretty-xml').decode()  # transmits template
        replace = "  xml:base=\"" + iri + "\"> \n"
        tosend = self.replacenth(self, tosend, ">", replace, 2)
        return tosend
###

    def checkAddress(self, address, port):
       if int(port) < 0 or int(port) > 65535:
         return 0

    def checkStatus(self, g , oasisIri, status):
        for b in g.objects(None,URIRef(oasisIri + "hasStatusType")):
            if str(b) == status:
              return 1
        return 0


    def generateExecutionStatus(self, g, execution, status, iri, oasisIRI, oasisaboxIRI, iriassist):

        task = URIRef(iri + "#task")
        object = URIRef(iri + "#belief-data")  # the obj
        parameter = URIRef(iri + "#parameter")  # the parameter

        operator = URIRef(oasisaboxIRI + "add")  # task operator
        Utils.generateRequest(Utils, g, iri, oasisIRI, task, object, oasisIRI+"refersAsNewTo", operator, None, parameter)

        g.add((URIRef(execution), RDF.type, URIRef(oasisIRI+"TaskExecution")))
        g.add((URIRef(iriassist), RDF.type, URIRef(oasisIRI + "Device")))  # has request
        g.add((URIRef(iriassist), URIRef(oasisIRI + "requests"), URIRef(iri + "#request")))
        g.add((URIRef(oasisIRI + "requests"), RDF.type, Utils.owlobj))
        g.add((URIRef(oasisIRI + "hasInformationObjectType"), RDF.type, Utils.owlobj))

        g.add((object, URIRef(oasisIRI + "hasInformationObjectType"),
               URIRef(oasisaboxIRI + "belief_description_object_type")))
        g.add((object, URIRef(oasisIRI + "descriptionProvidedByURL"), Literal(iri, datatype=XSD.string)))

        g.add((parameter, RDF.type, URIRef(oasisIRI + "OntologyDescriptionObject")))
        g.add((parameter, URIRef(oasisIRI + "hasInformationObjectType"),
               URIRef(oasisaboxIRI + "ontology_description_object_type")))

        g.add((URIRef(oasisIRI + "descriptionProvidedByEntityIRI"), RDF.type, Utils.owldat))
        g.add((parameter, URIRef(oasisIRI + "descriptionProvidedByEntityIRI"), Literal(execution, datatype=XSD.string)))

        # g.add((URIRef( self.oasis + "refersExactlyTo"), RDF.type, Utils.owlobj))
        # g.add((parameter, URIRef( self.oasis + "refersExactlyTo"), URIRef(execution)))

        f=Graph()
        g.add((URIRef(oasisIRI + "hasStatus"), RDF.type, Utils.owlobj))
        f.add((URIRef(oasisIRI + "hasStatus"), RDF.type, Utils.owlobj))
        g.add((URIRef(oasisIRI + "hasStatusType"), RDF.type, Utils.owlobj))
        f.add((URIRef(oasisIRI + "hasStatusType"), RDF.type, Utils.owlobj))
        thestatusob = URIRef(iri + "#exec-status-obj")
        g.add((URIRef(execution), URIRef(oasisIRI + "hasStatus"), URIRef(thestatusob)))
        f.add((URIRef(execution), URIRef(oasisIRI + "hasStatus"), URIRef(thestatusob)))
        g.add((URIRef(thestatusob), URIRef(oasisIRI + "hasStatusType"), URIRef(status)))
        f.add((URIRef(thestatusob), URIRef(oasisIRI + "hasStatusType"), URIRef(status)))
        return f


    def addImportAxioms(self, g, iri, axioms):
       for s in axioms:
           g.add((URIRef(iri), OWL.imports, URIRef(s)))



    def generateRequest(self, reqGraph, iri, iriOasis,  task, object, objectReferProp, operator, argument, parameter):

        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))

        request = URIRef(iri+"#request")             #the request
        reqGraph.add(( request, RDF.type, URIRef(iriOasis+"PlanDescription")))  # request type

        goal = URIRef(iri + "#goal")  # the goal
        reqGraph.add((goal, RDF.type, URIRef(iriOasis + "GoalDescription")))  # goal type

        reqGraph.add((task, RDF.type, URIRef(iriOasis + "TaskDescription")))  # task type

        reqGraph.add((URIRef(iriOasis + "consistsOfGoalDescription"), RDF.type, Utils.owlobj))
        reqGraph.add((request, URIRef(iriOasis + "consistsOfGoalDescription"), goal))  # has goal

        reqGraph.add((URIRef(iriOasis + "consistsOfTaskDescription"), RDF.type, Utils.owlobj))
        reqGraph.add((goal, URIRef(iriOasis + "consistsOfTaskDescription"), task))  # has goal

        taskObject = URIRef(iri + "#taskObject")  # the taskobject
        reqGraph.add((taskObject, RDF.type, URIRef(iriOasis + "TaskObject")))
        reqGraph.add((URIRef(iriOasis + "hasTaskObject"), RDF.type, Utils.owlobj))

        reqGraph.add((task, URIRef(iriOasis + "hasTaskObject"), taskObject))  # task object
        reqGraph.add((taskObject, URIRef(objectReferProp), object))  # task object

        taskOperator = URIRef(iri + "#taskOperator")  # the taskobject
        reqGraph.add((taskOperator, RDF.type, URIRef(iriOasis + "TaskOperator")))
        reqGraph.add((URIRef(iriOasis + "hasTaskOperator"), RDF.type, Utils.owlobj))
        reqGraph.add((task, URIRef(iriOasis + "hasTaskOperator"),
             taskOperator))  # task operator
        reqGraph.add((taskOperator, URIRef(iriOasis + "refersExactlyTo"), operator))  # task object

        if parameter is not None:
           taskParameter = URIRef(iri + "#taskInputParameter")  # the taskobject
           reqGraph.add((taskParameter, RDF.type, URIRef(iriOasis + "TaskActualInputParameter")))
           reqGraph.add((URIRef(iriOasis + "hasTaskActualInputParameter"), RDF.type, Utils.owlobj))
           reqGraph.add((task, URIRef(iriOasis + "hasTaskActualInputParameter"), taskParameter))  # task parameter
           reqGraph.add((taskParameter, URIRef(iriOasis + "refersAsNewTo"), parameter))

        if argument is not None:
            opArgument = URIRef(iri + "#taskOperatorArgument")  # the taskobject
            reqGraph.add((opArgument, RDF.type, URIRef(iriOasis + "TaskOperatorArgument")))
            reqGraph.add((URIRef(iriOasis + "hasTaskOperatorArgument"), RDF.type, Utils.owlobj))
            reqGraph.add((task, URIRef(iriOasis + "hasTaskOperatorArgument"), opArgument))  # argument
            reqGraph.add((opArgument, URIRef(iriOasis + "refersExactlyTo"), argument))

        return


    def transmit(self, data, response, address, port):
        # print(data)
        received = ''
        try:
            client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            client_socket.connect((address, int(port)))
            client_socket.send(data)
            if (response):
                received = Utils.recvall(Utils, client_socket).decode()
        except socket.error:
            client_socket.close()
            return None
        else:
            client_socket.close()
            return received

    def serverTransmit(self, data, sock, addr, server_socket):
        print("Sending response to: ", addr, "port ", sock)
        try:
            server_socket.send(data)
        except socket.error:
            return 0
        #server_socket.close()
        else:
            return 1