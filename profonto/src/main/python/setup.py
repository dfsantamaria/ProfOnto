import subprocess
import sys
def install(package):
    subprocess.call([sys.executable, "-m", "pip", "install", package])


install('telegram')
install('spacy')
install('pyreadline')

subprocess.call(["mvn", "clean", "install"])