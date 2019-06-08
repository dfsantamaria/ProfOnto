import socket, time
from pathlib import Path
import os

def readOntoFile(file):
 f=open(file,"r")
 return f.read()

p = Path(__file__).parents[2]
os.chdir(p)

home=readOntoFile("ontologies/test/homeassistant.owl")

client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
client_socket.send(home.encode())
client_socket.close()
