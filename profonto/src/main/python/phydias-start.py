import sys
#
#
sys.path.insert(0, "./lib")

from phidias.Types  import *
from phidias.Main import *
from phidias.Lib import *


class say_hello(Procedure): pass

say_hello() >> [ show_line("Hello world from Phidias") ]


print("Starting Phydias Shell")
PHIDIAS.run()
PHIDIAS.shell(globals())
