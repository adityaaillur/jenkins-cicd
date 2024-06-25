#!/bin/bash

# Install Node and Commitlint
sudo apt update -y
sudo apt remove --purge nodejs npm -y
sudo apt autoremove -y
sudo apt clean
sudo apt autoclean
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt update -y
sudo apt install -y nodejs
sudo npm install -g npm
sudo npm install -g @commitlint/{cli,config-conventional}
sudo npm install -g semantic-release @semantic-release/changelog @semantic-release/git @semantic-release/github release-it github-release

# Update and install dependencies
sudo apt-get update -y
sudo apt-get install -y fontconfig openjdk-17-jre wget zip unzip gnupg2 nginx software-properties-common

# Install jenkins
sudo wget -O /usr/share/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian/jenkins.io-2023.key
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt-get update
sudo apt-get install -y jenkins

# Enable and start Jenkins
sudo systemctl enable jenkins
sudo systemctl start jenkins
sudo systemctl status jenkins

# Install certbot for Let's Encrypt
sudo apt-get update -y
sudo apt-get install -y certbot python3-certbot-nginx