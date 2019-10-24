# Playground

## Local: Jenkins in Docker in Virtual Machine on Windows 10

### Pre-requisites
- [VirtualBox](https://www.virtualbox.org/)
- [Vagrant](https://www.vagrantup.com/)

### How to run?
- install pre-requisites on your machine
- clone / copy [Vagrantfile](Vagrantfile) to your machine
- in directory with Vagrantfile, run ```vagrant up```, once it's up, run ```vagrant ssh```
- in vagrant, run ```docker run --detach --rm --name jenkins -p 8080:8080 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts```
- Then get generated password ```docker exec -it $(docker ps -qf name=jenkins) cat /var/jenkins_home/secrets/initialAdminPassword```
- Open http://localhost:8080
- In case you need look into logs: ```docker logs $(docker ps -qf name=jenkins) --follow```
- In case you need to jump into container: ```docker exec -it $(docker ps -qf name=jenkins) bash```

### Useful references
- [Official Jenkins Docker image](https://github.com/jenkinsci/docker)