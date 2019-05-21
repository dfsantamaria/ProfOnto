import subprocess
import sys
import os
from pathlib import Path



def install(package):
    subprocess.call([sys.executable, "-m", "pip", "install", package])



install('telegram')
install('spacy')
install('pyreadline')


mvn="mvn clean install"
p = subprocess.Popen(mvn, shell=True, stdout = subprocess.PIPE)
stdout, stderr = p.communicate()
print(stdout)