import socket
from threading import *

client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
message="test"
client_socket.send(message.encode())
client_socket.close()