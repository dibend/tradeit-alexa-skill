#!/bin/bash

#Scrapes US symbols going ex-dividend today
#To be run every trading day on EC2 and data pushed to S3 for Alexa App to use

wget http://www.nasdaq.com/dividend-stocks/dividend-calendar.aspx
grep "<td><a href=\"http://www.nasdaq.com/symbol/" dividend-calendar.aspx | cut -d/ -f5 | tr '.' '/' | awk '{print toupper($0)}' > div.txt
aws s3 cp div.txt s3://tradingticket/div.txt
rm dividend-calendar.aspx
rm div.txt
