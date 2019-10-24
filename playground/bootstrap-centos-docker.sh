#!/usr/bin/env bash

# Maintainer: virtuz.blr@gmail.com 
#
# Description: Get Docker CE for CentOS
# Reference: https://docs.docker.com/install/linux/docker-ce/centos/
#
# Shell Style Guide: https://google.github.io/styleguide/shell.xml

# A function to print out log messages
log() {
  echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@"
}

# A function to print out error messages
err() {
  log $@ >&2
}

# Check if running as root
if [[ $(id -u) -eq 0 ]]; then
    err "This script should NOT be run using sudo or as the root user."
    exit 1
fi

# SET UP THE REPOSITORY

log "Install required packages."
sudo yum install -y yum-utils \
  device-mapper-persistent-data \
  lvm2

log "Workarroung to avoid using IPv6 by yum"
log "Otherwise you can get the issue 'Could not fetch/save url https://download.docker.com'"
echo "ip_resolve=4" | sudo tee --append /etc/yum.conf

log "Set up the stable repository."
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo

# INSTALL DOCKER CE

log "Install the latest version of Docker CE and containerd."
sudo yum install -y docker-ce docker-ce-cli containerd.io

log "Start Docker.Start Docker."
sudo systemctl start docker

log "Verify that Docker CE is installed correctly by running the hello-world image."
sudo docker run hello-world

# Post-installation steps for Linux
# Reference: https://docs.docker.com/install/linux/linux-postinstall/

# Manage Docker as a non-root user
log "Add your user to the docker group."
sudo usermod -aG docker $USER

log "Configure Docker to start on boot."
sudo systemctl enable docker
