import subprocess
import sys
import os
import re
from pathlib import Path
from distutils.dir_util import copy_tree


def install(package):
    subprocess.call([sys.executable, "-m", "pip", "install", package])

print("Installing required python packages, please wait...")

install('telegram')
install('spacy')
install('pyreadline')
install('py4j')
install('rdflib')

print("Running maven, please wait...")

mvn="mvn clean install"
p = subprocess.Popen(mvn, shell=True, stdout = subprocess.PIPE)
stdout, stderr = p.communicate()
print(stdout)
if re.search('BUILD SUCCESS', stdout.decode('utf-8'), re.IGNORECASE):
   print("Almost finished...")
   print("Copying required files...")
   copy_tree("ontologies", "target/ontologies")
   copy_tree("src/main/python", "target/python")
   print("Setup finished")
else:
    print("Uncompilable Java source")
