from py4j.java_gateway import JavaGateway

def readOntoFile(file):
 f=open(file,"r")
 return f.read()


profontoGateWay = JavaGateway()                   # connect to the JVM
profonto = profontoGateWay .entry_point


user=readOntoFile("../../../ontologies/test/alan.owl")
value = profonto.addUser(user, "alan")  #read the user data
print("User added with exit code:", value)


device=readOntoFile("../../../ontologies/test/lightagent.owl")
value = profonto.addDevice(device, "device")  #read the device data
print("Device added with exit code:", value)


config=readOntoFile("../../../ontologies/test/alan-config.owl")
value = profonto.addConfiguration(config, "device", "device-Conf1", "alan")  #read the device configuration data
print("Configuration added with exit code:", value)

value=profonto.syncReasonerDataBehavior(); # sync the reasoner
print("Data Behavior synchronized with exit code:", value)

request=readOntoFile("../../../ontologies/test/user-request.owl")
value=profonto.acceptUserRequest(request, "http://www.dmi.unict.it/user-request.owl#alan-task-1-1-1",
                                                                                "http://www.dmi.unict.it/ontoas/alan.owl#Alan",
                                                               "http://www.dmi.unict.it/user-request.owl#alan-task-1-1-1-exec",
                                                                    "http://www.dmi.unict.it/user-request.owl#alan-goal-1-1-1");

print("Request output:", value)

value=profonto.removeUser("alan")  #remove user
print("User removed with exit code:", value)
value=profonto.removeDevice("device") #remove data
print("Device removed with exit code:", value)

profontoGateWay.shutdown() #Shutdown the gateway