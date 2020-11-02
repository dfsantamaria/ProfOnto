import socket, time
from threading import *
from pathlib import Path
import rdflib
from rdflib import *
import os
from datetime import datetime
import re

oasis = 'http://www.dmi.unict.it/oasis.owl#'
oasisabox = 'http://www.dmi.unict.it/oasis-abox.owl#'

port=8000

def recvall(sock):
    BUFF_SIZE = 1024 # 1 KiB
    data = b''
    timeout = time.time() + 60
    while time.time() < timeout:
        part = sock.recv(BUFF_SIZE)
        data += part
        if len(part) < BUFF_SIZE:
            # either 0 or end of data
            break
    return data

def getTimeStamp():
    return (str(datetime.timestamp(datetime.now()))).replace(".", "-")

def readOntoFile(file):
 f=open(file,"r")
 return f.read()


def replacenth(string, sub, wanted, n):
    where = [m.start() for m in re.finditer(sub, string)][n - 1]
    before = string[:where]
    after = string[where:]
    after = after.replace(sub, wanted, 1)
    newString = before + after
    return newString

def libbug(timestamp, graph, iri):
    tosend = graph.serialize(format='pretty-xml').decode()  # transmits template
    replace = "  xml:base=\"" + iri + "\"> \n"
    tosend = replacenth(tosend, ">", replace, 2)
    return tosend

p = Path(__file__).parents[1]
os.chdir(p)


file=readOntoFile("ontologies/test/rdf/alan.owl")
iri="http://www.dmi.unict.it/ontoas/alan.owl"
g=rdflib.Graph();
tosend=libbug(getTimeStamp(), g.parse(data=file), iri)
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(tosend.encode())
client_socket.close()

file=readOntoFile("ontologies/test/add-user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(file.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()


file=readOntoFile("ontologies/test/rdf/alan-config.owl")
iri="http://www.dmi.unict.it/alan-config.owl"
g=rdflib.Graph();
tosend=libbug(getTimeStamp(), g.parse(data=file), iri)
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(tosend.encode())
client_socket.close()


#adding configuration
file=readOntoFile("ontologies/test/rdf/add-user-configuration-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(file.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()
# #
file=readOntoFile("ontologies/test/user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(file.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()
# #
# #
#removing configuration
file=readOntoFile("ontologies/test/remove-user-configuration-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(file.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()
# #
# # #removing user
# #
file=readOntoFile("ontologies/test/remove-user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(file.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()