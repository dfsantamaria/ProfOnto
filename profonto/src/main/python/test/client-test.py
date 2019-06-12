import socket, time
from pathlib import Path
import os

def readOntoFile(file):
 f=open(file,"r")
 return f.read()

p = Path(__file__).parents[2]
os.chdir(p)


#installing device

home=readOntoFile("ontologies/test/light-installation-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
client_socket.close()


#adding user

home=readOntoFile("ontologies/test/add-user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
client_socket.close()


#adding configuration
home=readOntoFile("ontologies/test/add-user-configuration-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
client_socket.close()

#a request
home=readOntoFile("ontologies/test/user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
client_socket.close()





time.sleep(5)


#adding configuration
home=readOntoFile("ontologies/test/remove-user-configuration-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
client_socket.close()

#removing device

home=readOntoFile("ontologies/test/light-uninstallation-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
client_socket.close()

#removing user

home=readOntoFile("ontologies/test/remove-user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
client_socket.close()
