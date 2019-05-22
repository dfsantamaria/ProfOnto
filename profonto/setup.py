import subprocess
import sys
import os
from pathlib import Path
from distutils.dir_util import copy_tree


def install(package):
    subprocess.call([sys.executable, "-m", "pip", "install", package])


install('telegram')
install('spacy')
install('pyreadline')


mvn="mvn clean install"
p = subprocess.Popen(mvn, shell=True, stdout = subprocess.PIPE)
stdout, stderr = p.communicate()
print(stdout)

copy_tree("ontologies", "target/ontologies")
copy_tree("src/main/python", "target/python")