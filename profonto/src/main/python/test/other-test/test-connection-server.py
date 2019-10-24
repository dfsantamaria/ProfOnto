import socket
from threading import *

host='localhost'
port=8000

def init_server():
    serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    serversocket.bind((host, int(port)))
    serversocket.listen(5)
    while 1:
        clients, address = serversocket.accept()
        client(clients, address, serversocket)
    return

class client(Thread):
    def __init__(self, socket, address, serversocket):
        Thread.__init__(self)
        self.sock = socket
        self.addr = address
        self.serversocket=serversocket
        self.start()

    def run(self):
        request=''
        while 1:
          data=self.sock.recv(1024).decode()
          if not data:
             break
          request+=data
          print (request, self.addr[1], self.addr[0])
          message = "test"
          #self.serversocket.connect(( self.addr[0], int(self.addr[1]) ))
          self.sock.send(message.encode())

init_server()
