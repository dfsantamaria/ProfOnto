from py4j.java_gateway import JavaGateway


gateway = JavaGateway()                   # connect to the JVM
profonto = gateway.entry_point
value = profonto.readData("file_to_read")