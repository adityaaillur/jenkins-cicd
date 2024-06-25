#!/bin/bash
sudo certbot --nginx -d jenkins.illur.cloud --non-interactive --agree-tos -m chlokesh1306@gmail.com
sudo mv /home/ubuntu/nginx/jenkins_nginx_final.conf /etc/nginx/sites-available/jenkins
sudo systemctl reload nginx