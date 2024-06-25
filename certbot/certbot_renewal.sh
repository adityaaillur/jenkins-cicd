#!/bin/bash
sudo certbot renew --nginx --quiet
sudo systemctl reload nginx