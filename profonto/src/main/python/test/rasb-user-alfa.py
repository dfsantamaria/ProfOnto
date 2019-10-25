import socket, time
from threading import *
from pathlib import Path
import rdflib
from rdflib import *
import os

oasis = 'http://www.dmi.unict.it/oasis.owl#'
oasisabox = 'http://www.dmi.unict.it/oasis-abox.owl#'

def recvall(sock):
    BUFF_SIZE = 1024 # 1 KiB
    data = b''
    while True:
        part = sock.recv(BUFF_SIZE)
        data += part
        if len(part) < BUFF_SIZE:
            # either 0 or end of data
            break
    return data

def readOntoFile(file):
 f=open(file,"r")
 return f.read()

p = Path(__file__).parents[2]
os.chdir(p)

home=readOntoFile("ontologies/test/rasb/add-user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
#client_socket.close()
request = recvall(client_socket).decode()
print(request)
client_socket.close()

#adding configuration
home=readOntoFile("ontologies/test/rasb/add-user-configuration-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
#client_socket.close()
request = recvall(client_socket).decode()
print(request)
client_socket.close()

home=readOntoFile("ontologies/test/rasb/user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
#request = recvall(client_socket).decode()
#print(request)
client_socket.close()


#removing configuration
home=readOntoFile("ontologies/test/rasb/remove-user-configuration-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()

#removing user

home=readOntoFile("ontologies/test/rasb/remove-user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()