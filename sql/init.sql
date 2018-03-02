-- SDP DDL Init script for DB, users, tables, etc.

DROP DATABASE IF EXISTS wallhub1;
CREATE DATABASE wallhub1 CHARACTER SET utf8 COLLATE utf8_general_ci;

-- Create a user for our java app. Allow connections from 192.168.1.x

DROP USER IF EXISTS 'lreader'@'192.168.1.%';
CREATE USER lreader@'192.168.1.%' IDENTIFIED BY 'secret';
GRANT SELECT, INSERT, UPDATE ON wallhub1.* TO 'lreader'@'192.168.1.%';
FLUSH PRIVILEGES;

USE wallhub1;

-- HTTPLOG table contains the raw data from the HTTP access log

DROP TABLE IF EXISTS HTTPLOG;
CREATE TABLE HTTPLOG (
  REQ_TM DATETIME NOT NULL,
  IP_ADDR varchar(15) NOT NULL,
  REQ_TYPE varchar(30) NOT NULL,
  RESP_CODE varchar(3) NOT NULL,
  USER_AGENT varchar(250) NOT NULL
);

-- We search on REQ_TM, so an index will speed up queries
ALTER TABLE HTTPLOG ADD INDEX tmStampReq (REQ_TM);

-- Results of query table business intelligence
-- Not mentioned explicitly in requirements, so this is my best guess
-- as to whats needed

DROP TABLE IF EXISTS BIZINTEL;
CREATE TABLE BIZINTEL (
  IP_ADDR varchar(15) NOT NULL,
  IP_CNT INTEGER NOT NULL,
  BIZ_REASON varchar(250)
);

