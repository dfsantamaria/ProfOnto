import sys

from py4j.java_gateway import JavaGateway
import subprocess
import os
import time
import socket

from pathlib import Path
from threading import *

def getProcessOut(process):
  welcome=''
  while True:
    out = process.stdout.read(1)
    if out != '\n':
        welcome += out
    else:
        break
  return welcome

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


def init_gateway():
    p = Path(__file__).parents[1]
    os.chdir(p)
    folder = 'ontologies/devices'
    for the_file in os.listdir(folder):
        file_path = os.path.join(folder, the_file)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            # elif os.path.isdir(file_path): shutil.rmtree(file_path)
        except Exception as e:
            print(e)
    jar = "java -jar Prof-Onto-1.0-SNAPSHOT.jar"
    process = subprocess.Popen(jar, universal_newlines=True, stdout=subprocess.PIPE)
    # stdout, stderr = process.communicate()
    print(getProcessOut(process))
    profontoGateWay = JavaGateway()  # connect to the JVM
    profonto = profontoGateWay.entry_point
    return


def init_server():
      serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      host = 'localhost'
      port = 8000
      serversocket.bind((host, port))
      serversocket.listen(5)
      print("Prof-Onto Assistant has been started, send requests to:", host, "port ", port)
      #Manage me suitably
      PHIDIAS.achieve(say_hello())
      #
      while 1:
          clientsocket, address = serversocket.accept()
          client(clientsocket, address)
      return



#################################################PHIDIAS PART ##############################

sys.path.insert(0, "./lib")

from phidias.Types  import *
from phidias.Main import *
from phidias.Lib import *


class say_hello(Procedure): pass

say_hello() >> [ show_line("Hello world from Phidias") ]

################################################ END PHIDIAS PART ##########################

PHIDIAS.run()
init_gateway()
init_server()