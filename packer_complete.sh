#!/bin/bash

# Configure Nginx
sudo rm /etc/nginx/sites-enabled/default
sudo ln -s /etc/nginx/sites-available/jenkins /etc/nginx/sites-enabled/

# Add service
sudo sh -c "echo '[Unit]
Description=Run Certbot Initial Script at Boot
After=network.target

[Service]
Type=oneshot
ExecStart=/usr/local/bin/certbot_initial.sh
RemainAfterExit=true

[Install]
WantedBy=multi-user.target' >> /lib/systemd/system/certbot.service"
sudo systemctl daemon-reload
sudo systemctl enable certbot

# Schedule Certbot renewal
sudo crontab -l > /tmp/cron
echo '0 3 * * * /usr/local/bin/certbot_renewal.sh' >> /tmp/cron
sudo crontab /tmp/cron

# Wait for Jenkins
while [ "$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080)" != "403" ]; do
    echo "Waiting"
    sleep 5
done

# Jenkins CLI
sudo wget http://localhost:8080/jnlpJars/jenkins-cli.jar -O /tmp/jenkins-cli.jar

# Install Jenkins plugins
sudo java -jar /tmp/jenkins-cli.jar -s http://localhost:8080/ -auth admin:$(sudo cat /var/lib/jenkins/secrets/initialAdminPassword) install-plugin git github github-api credentials job-dsl docker-workflow conventional-commits github-branch-source

# Install Docker
sudo apt-get install docker.io -y
sudo apt-get install docker-buildx -y
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Install Packer
curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo apt-key add -
sudo apt-add-repository "deb [arch=amd64] https://apt.releases.hashicorp.com $(lsb_release -cs) main" -y
sudo apt-get update && sudo apt-get install packer

# Installing Helm
sudo wget https://get.helm.sh/helm-v3.15.2-linux-amd64.tar.gz
sudo tar -zxvf helm-v3.15.2-linux-amd64.tar.gz
sudo mv linux-amd64/helm /usr/local/bin/helm

# Install Terraform
sudo wget https://releases.hashicorp.com/terraform/1.8.5/terraform_1.8.5_linux_amd64.zip
sudo unzip terraform_1.8.5_linux_amd64.zip -d /usr/local/bin

# Install yamllint
sudo apt-get install yamllint -y

# Moving required files
sudo mkdir -p /var/lib/jenkins/workspace/seed-job
sudo mkdir -p /var/lib/jenkins/init.groovy.d
sudo mv /home/ubuntu/nginx/jenkins_nginx_initial.conf /etc/nginx/sites-available/jenkins
sudo mv /home/ubuntu/certbot/* /usr/local/bin/
sudo mv /home/ubuntu/jenkins/init/* /var/lib/jenkins/init.groovy.d/
sudo mv /home/ubuntu/jenkins/seed.groovy /var/lib/jenkins/workspace/seed-job/seed.groovy
sudo chmod 755 /usr/local/bin/certbot_initial.sh
sudo chmod 755 /usr/local/bin/certbot_renewal.sh
sudo chmod 755 /var/lib/jenkins/init.groovy.d -R
sudo chmod 755 /var/lib/jenkins/workspace/seed-job/seed.groovy
sudo chown -R jenkins:jenkins /var/lib/jenkins
sudo systemctl restart jenkins
echo "Restarting Jenkins"
JENKINS_PW=$(sudo cat /var/lib/jenkins/secrets/initialAdminPassword)
echo "Jenkins password is $JENKINS_PW"
echo "All done!"