import subprocess
import sys


if __name__ == "__main__":
    name = sys.argv[1]
    subprocess.run("tmux kill-sess -t " + name, shell=True)

    if name == "genome-nexus":
        subprocess.run("sudo docker stop gn-mongo", shell=True)
        subprocess.run("sudo docker rm gn-mongo", shell=True)
    elif name == "person-controller":
        subprocess.run("sudo docker stop mongodb", shell=True)
        subprocess.run("sudo docker rm mongodb", shell=True)
    elif name == "problem-controller":
        subprocess.run("sudo docker stop mysql", shell=True)
        subprocess.run("sudo docker rm mysql", shell=True)
    elif name == "user-management":
        subprocess.run("sudo docker stop mysqldb", shell=True)
        subprocess.run("sudo docker rm mysqldb", shell=True)
