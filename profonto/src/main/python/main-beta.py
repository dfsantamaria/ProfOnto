import sys

sys.path.insert(0, "./lib")

from py4j.java_gateway import JavaGateway
import subprocess
import os
import time
import socket

from lib.profeta.Types import *
from lib.profeta.Main import *
from lib.profeta.Lib import *
from pathlib import Path
from threading import *

profonto=''

class client(Thread):
    def __init__(self, socket, address):
        Thread.__init__(self)
        self.sock = socket
        self.addr = address
        self.start()

    def run(self):
        while 1:
            print('Client sent:', self.sock.recv(1024).decode())
            self.sock.send(b'Oi you sent something to me')


class init(Goal): pass



class init_gateway(Action):
  def execute(self):
      print("init ontological core")



class init_server(Action):
  def execute(self):
      print("init server")
      serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      host = 'localhost'
      port = 8000
      serversocket.bind((host, port))
      serversocket.listen(5)
      while 1:
          clientsocket, address = serversocket.accept()
          client(clientsocket, address)



init() >> [init_gateway(), init_server()]