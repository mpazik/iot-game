
## Few useful commands for deployment

Package
`mvn clean package`

Upload jar to EC2
`scp target/server-app-{version}-jar-with-dependencies.jar iot-inst1:~/server.jar`

Connect to EC2
`ssh iot-inst1`

Run container on server:
`cd dzida-server`
-DdevMode=true -DserverTimeOffset=10000 
`nohup java -DassetsAddress=http://assets.isles-of-tales.com -DcontainerHost=localhost -Ddatabase.host=iot-inst1.c16tsjrk308r.eu-central-1.rds.amazonaws.com:5432 -Ddatabase.name=prod_db -Ddatabase.user=prod_user -Ddatabase.password=prod_password -D -jar server.jar 2>&1 >> logfile.log &`