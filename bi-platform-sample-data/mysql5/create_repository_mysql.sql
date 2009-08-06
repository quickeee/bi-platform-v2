CREATE DATABASE IF NOT EXISTS `hibernate` DEFAULT CHARACTER SET latin1;

USE hibernate;

GRANT ALL ON hibernate.* TO 'hibuser'@'localhost' identified by 'password'; 

DROP TABLE IF EXISTS DATASOURCE;

--  Create Users Table
CREATE TABLE DATASOURCE(NAME VARCHAR(50) NOT NULL PRIMARY KEY,MAXACTCONN INTEGER NOT NULL,DRIVERCLASS VARCHAR(50) NOT NULL,IDLECONN INTEGER NOT NULL,USERNAME VARCHAR(50) NULL,PASSWORD VARCHAR(150) NULL,URL VARCHAR(100) NOT NULL,QUERY VARCHAR(100) NULL,WAIT INTEGER NOT NULL);
commit;
