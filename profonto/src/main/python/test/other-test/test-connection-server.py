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
        client(clients, address)
    return

class client(Thread):
    def __init__(self, socket, address):
        Thread.__init__(self)
        self.sock = socket
        self.addr = address
        self.start()

    def run(self):
        request=''
        while 1:
          data=self.sock.recv(1024).decode()
          if not data:
             break
          request+=data
        print (request, self.sock.getsockname()[1], self.sock.getsockname()[0])


init_server()
