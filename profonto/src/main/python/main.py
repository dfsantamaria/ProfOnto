from py4j.java_gateway import JavaGateway


gateway = JavaGateway()                   # connect to the JVM
profonto = gateway.entry_point

f=open("../../../ontologies/test/lightagent.owl", "r")   #read the device


value = profonto.addDevice(f.read(),"device")
print(value)