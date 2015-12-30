
## Few usefull commands for deployment

Package
`mvn clean package`

Upload jar to EC2
`scp -i  ~/.ssh/dzida-container.pem target/server-app-0.1-SNAPSHOT-jar-with-dependencies.jar ec2-user@52.59.245.12:~/dzida-server/server.jar`

Connect to EC2
`ssh -i \"~/.ssh/dzida-container.pem\" ec2-user@52.59.245.12`

Run container on server:
`cd dzida-server`
`nohup java -DassetsAddress=http://assets.dzida-online.pl -DcontainerHost=ec2-52-59-245-12.eu-central-1.compute.amazonaws.com -jar server.jar 2>&1 >> logfile.log &`