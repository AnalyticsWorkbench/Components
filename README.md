# Analytics Workbench Setup with netbeans using postgresSQL

## Components

**Requirements:**
- Current JDK
- PostgresSQL
	- create database "workbench" and a corresponding user with all rights granted for the DB.
- R (https://cran.r-project.org/)
	- install necessary R packages (Runiversal, igraph, blockmodeling, fpc, Matrix, dplyr)
	- install.packages(c(“Runiversal”,”igraph”,”blockmodeling”,”fpc”,"Matrix", "dplyr"))
  
 

## Workbench UI

**Requirements:**
- Redis (https://redis.io/download or on Windows https://github.com/dmajkic/redis/downloads)
- NodeJS (https://nodejs.org)
  - install necessary module by executing node, navigationg into “webworkbench” and executing „npm install“ (in the terminal/comand line) in the „webworkbench“ directory
  
Create folders and files (if they are not already there):

- Folder "results": the folder which is used to server analysis results

- Folder "security": the folder containing certificates for https/wss
	- nodeworkbench.key - private key file
	- nodeworkbench.crt - certificate file (filename is an example, may be configured)	


## Installation
	
### 1. Change sisob.config to local settings:

```
## hostname for message connection
server.message.name=localhost

## internalname for message connection (e.g. database for PSQL)
server.message.internalname=workbench

## internalname for data connection (e.g. database for PSQL)
server.data.internalname=workbench

## location of the Rscript executable
rwrapper.executable=/usr/lib/R/bin/Rscript or on Windows usually C:\\Program Files\\R\\R-3.x.x\\bin\\Rscript.exe

## directory for the created output
slideshow.serverdir=path_to_project/webworkbench/results/

## directory from which the uploader offers files
upload.directory=path_to_project/webworkbench/upload files

## directory in which the resultcollector searches for created results
results.filelocation=path_to_project/webworkbench/results/
```

### 2. Initialise DB

In UI folder:
	- Run usercreator.js (Initial admin user (user:admin, pwd:admin-pw) and regular user (user:user, pwd:user-pw) will be created
	- Run postgresinitializer.js


In components folder 

- Run Maven build (mvn clean install)
- Run postgresinit.java in executer folder


### 3. Startup
- Start the UI
-- Start redis-server
-- Run node server (node workbench.js) in webworkbench folder (Server listens at localhost:3081)
- Start the components framework: 
-- Run components/components-executor/Executor.java (All analytics components listed in executor.xml will be started)

