from py4j.java_gateway import JavaGateway

def readOntoFile(file):
 f=open(file,"r")
 return f.read()


profontoGateWay = JavaGateway()                   # connect to the JVM
profonto = profontoGateWay .entry_point

device=readOntoFile("../../../ontologies/test/lightagent.owl")
value = profonto.addDevice(device, "device")  #read the device
print("Device added with exit code:", value)



profontoGateWay .shutdown() #Shutdown the gateway