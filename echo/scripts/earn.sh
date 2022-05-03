#!/bin/bash

#Scrapes US symbols releasing earnings today
#To be run every trading day on EC2 and data pushed to S3 for Alexa App to use

w3m -no-cookie -dump -T text/html http://www.bloomberg.com/markets/earnings-calendar/us | grep :US | cut -d: -f1 > earn.txt
aws s3 cp earn.txt s3://tradingticket/earn.txt
rm earn.txt
