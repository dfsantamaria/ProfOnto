import socket, time
from threading import *
from pathlib import Path
import rdflib
from rdflib import *
import os
from datetime import datetime
import re
from lib.utils import *
from lib.agent import *


class LightAgentServerManager(AgentServerManager):
    def performOperation(self, g, execution):
        taskObject = next(g.objects(execution, URIRef(self.agent.iriSet[0] + "#hasTaskObject")))
        taskOperator = next(g.objects(execution, URIRef(self.agent.iriSet[0] + "#hasTaskOperator")))
        print("\n Action ", taskOperator, "on ", taskObject)
        print(Console.inputText, end='')
        return



def setTestPath():
    p = Path(__file__).parents[1]
    os.chdir(p)
    return

def main():
    setTestPath()
    agent=Agent(LightAgentServerManager(), "ontologies/test/rasb/rasb-lightagent.owl",
          {"ontologies/test/rasb/lightagent-from-template.owl"},
          "http://www.dmi.unict.it/lightagent.owl", "http://www.dmi.unict.it/lightagent-template.owl")
    Console(agent, [StartCommand(), StopCommand(), ExitCommand(), StatusCommand(), SetHubCommand(), InstallCommand(), CheckInstallCommand(), UninstallCommand(), SetDeviceCommand() ])

if __name__ == '__main__':
    main()

