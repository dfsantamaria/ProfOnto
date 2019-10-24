import socket
from threading import *

client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
message="test"
client_socket.send(message.encode())
request = ''
while 1:
  data = client_socket.recv(2048).decode()
  if not data:
   break
  request += data
  print(request)
client_socket.close()