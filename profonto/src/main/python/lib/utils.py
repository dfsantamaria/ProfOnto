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

    def generateRequest(self, reqGraph, iri, iriOasis, task, object, operator, argument, parameter):

        request = URIRef(iri+"#request")             #the request
        reqGraph.add(( request, RDF.type, URIRef(iriOasis+"#PlanDescription")))  # request type

        goal = URIRef(iri + "#goal")  # the goal
        reqGraph.add((goal, RDF.type, URIRef(iriOasis + "#GoalDescription")))  # goal type

        reqGraph.add((task, RDF.type, URIRef(iriOasis + "#TaskDescription")))  # task type

        reqGraph.add((URIRef(iriOasis + "#consistsOfGoalDescription"), RDF.type, Utils.owlobj))
        reqGraph.add((request, URIRef(iriOasis + "#consistsOfGoalDescription"), goal))  # has goal

        reqGraph.add((URIRef(iriOasis + "#consistsOfTaskDescription"), RDF.type, Utils.owlobj))
        reqGraph.add((goal, URIRef(iriOasis + "#consistsOfTaskDescription"), task))  # has goal

        reqGraph.add((object, RDF.type, URIRef(iriOasis + "#TaskObject")))
        reqGraph.add((URIRef(iriOasis + "#hasTaskObject"), RDF.type, Utils.owlobj))
        reqGraph.add((task, URIRef(iriOasis + "#hasTaskObject"), object))  # task object

        reqGraph.add((URIRef(iriOasis + "#hasTaskOperator"), RDF.type, Utils.owlobj))
        reqGraph.add((task, URIRef(iriOasis + "#hasTaskOperator"),
             operator))  # task operator

        if parameter is not None:
           reqGraph.add((parameter, RDF.type, URIRef(iriOasis + "#TaskInputParameter")))
           reqGraph.add((URIRef(iriOasis + "#hasTaskInputParameter"), RDF.type, Utils.owlobj))
           reqGraph.add((task, URIRef(iriOasis + "#hasTaskInputParameter"), parameter))  # task parameter


        if argument is not None:
            reqGraph.add((URIRef(iriOasis + "#hasOperatorArgument"), RDF.type, Utils.owlobj))
            reqGraph.add((task, URIRef(iriOasis + "#hasOperatorArgument"), argument))  # argument

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