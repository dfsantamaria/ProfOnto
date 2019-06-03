from py4j.java_gateway import JavaGateway
import subprocess
import os
import time
from pathlib import Path

def readOntoFile(file):
 f=open(file,"r")
 return f.read()



p = Path(__file__).parents[1]
os.chdir(p)


folder = 'ontologies/devices'
for the_file in os.listdir(folder):
    file_path = os.path.join(folder, the_file)
    try:
        if os.path.isfile(file_path):
            os.unlink(file_path)
        #elif os.path.isdir(file_path): shutil.rmtree(file_path)
    except Exception as e:
        print(e)

jar="java -jar Prof-Onto-1.0-SNAPSHOT.jar"
process = subprocess.Popen(jar, shell=True, stdout = subprocess.PIPE)
#stdout, stderr = process.communicate()
#print(stdout)

time.sleep(10)

profontoGateWay = JavaGateway()                   # connect to the JVM
profonto = profontoGateWay .entry_point

home=readOntoFile("ontologies/test/homeassistant.owl")
value = profonto.addDevice(home, "ProfHomeAssistant")  #read the device data
print("Home assistant added with exit code:", value)

user=readOntoFile("ontologies/test/alan.owl")
value = profonto.addUser(user, "alan")  #read the user data
print("User added with exit code:", value)



device=readOntoFile("ontologies/test/lightagent.owl")
value = profonto.addDevice(device, "device")  #read the device data
print("Device added with exit code:", value)


config=readOntoFile("ontologies/test/alan-config.owl")
value = profonto.addConfiguration(config, "device", "device-Conf1", "alan")  #read the device configuration data
print("Configuration added with exit code:", value)

#value=profonto.syncReasonerDataBehavior(); # sync the reasoner
#print("Data Behavior synchronized with exit code:", value)

request=readOntoFile("ontologies/test/user-request.owl")
value=profonto.acceptUserRequest(request, "http://www.dmi.unict.it/user-request.owl#alan-task-1-1-1",
                                                                                "http://www.dmi.unict.it/ontoas/alan.owl#Alan",
                                                               "http://www.dmi.unict.it/user-request.owl#alan-task-1-1-1-exec",
                                                                    "http://www.dmi.unict.it/user-request.owl#alan-goal-1-1-1");

print("Request output:", value)


print("Testing parseRequest step 1...")
request=readOntoFile("ontologies/test/user-request.owl")
value=profonto.parseRequest(request)
print ("Request:", value)

value=profonto.removeUser("alan")  #remove user
print("User removed with exit code:", value)
value=profonto.removeDevice("device") #remove data
print("Device removed with exit code:", value)

print("Testing parseRequest step 2...")

request=readOntoFile("ontologies/test/light-uninstallation-request.owl")
print("Request red...")
value=profonto.parseRequest(request)
print("Printing request")
print ("Request:", value)

value=profonto.retrieveAssertions("http://www.dmi.unict.it/light-uninstallation-request.owl#light-uninstallation-req-task");
print ("Graph:", value)


value=profonto.removeDevice("ProfHomeAssistant") #remove assistant
print("Home assistant removed with exit code:", value)


profontoGateWay.shutdown() #Shutdown the gateway