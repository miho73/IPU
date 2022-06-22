# PSQL Guide

IPU uses postgresql as DBMS. Here's the structure and user spec for IPU.

## 1.DBMS Setup
### 1.1. PostgreSQL Setup
> Run below command in order to install PostgreSQL to your server.   
> `sudo apt-get update`   
> `sudo apt-get install postgresql postgresql-contrib`
### 1.2 USER setup
> `CREATE USER <USERNAME> WITH ENCRYPTED PASSWORD '<Password>';`

## 2. IDENTIFICATION DB
> Database name: identification  
> `CREATE DATABASE identification OWNER <USERNAME>;`
### 2.1 identification table
> Table name: iden
> ```sql
> CREATE TABLE IF NOT EXISTS iden(
> user_code SERIAL PRIMARY KEY NOT NULL,
> user_id VARCHAR(50) NOT NULL,
> user_name VARCHAR(50) NOT NULL,
> user_password CHAR(88) NOT NULL,
> user_salt CHAR(88) NOT NULL,
> invite_code CHAR(4) NOT NULL,
> bio VARCHAR(500) NOT NULL,
> privilege CHAR(5) NOT NULL,
> email VARCHAR(50),
> joined TIMESTAMP WITH TIME ZONE NOT NULL,
> experience INTEGER NOT NULL,
> aes_iv CHAR(16),
> last_solve TIMESTAMP WITH TIME ZONE,
> last_login TIMESTAMP WITH TIME ZONE,
> stared_problem TEXT NOT NULL
> );
> ```
### 2.2 User judge history
> ```
> CREATE TABLE IF NOT EXISTS judges(
> judge_code SERIAL PRIMARY KEY NOT NULL,
> user_code INTEGER NOT NULL,
> problem_code INTEGER NOT NULL,
> judge_time TIMESTAMP WITH TIME ZONE NOT NULL,
> time_took SMALLINT NOT NULL,
> judge_result TEXT NOT NULL,
> corrects SMALLINT NOT NULL,
> total SMALLINT NOT NULL,
> experience INTEGER NOT NULL
> );
> ```

## 3 PROBLEM DB
> Database name: problem  
> `CREATE DATABASE problem OWNER <USERNAME>;`
### 3.1. problem table
> Table name: prob
> ```sql
> CREATE TABLE IF NOT EXISTS prob(
> problem_code SERIAL PRIMARY KEY NOT NULL,
> problem_name VARCHAR(50) NOT NULL,
> problem_category CHAR(4) NOT NULL,
> problem_difficulty CHAR(4) NOT NULL,
> problem_content TEXT NOT NULL,
> problem_solution TEXT NOT NULL,
> author_name VARCHAR(50) NOT NULL,
> active BOOLEAN NOT NULL,
> added_at TIMESTAMP WITH TIME ZONE NOT NULL,
> last_modified TIMESTAMP WITH TIME ZONE NOT NULL,
> tags TEXT NOT NULL,
> answer VARCHAR(1000) NOT NULL
> );
> ```
### 3.2. resource table
> Table name: resources
> ```sql
> CREATE TABLE IF NOT EXISTS resources(
> resource_code CHAR(24) PRIMARY KEY,
> resource BYTEA NOT NULL,
> registered TIMESTAMP WITH TIME ZONE NOT NULL,
> registered_by VARCHAR(50) NOT NULL,
> resource_name VARCHAR(100) NOT NULL
> );
> ```
### 3.3 problem issue table
> Table name: prob_issue
> ```sql
> CREATE TABLE IF NOT EXISTS prob_issue(
> issue_code SERIAL PRIMARY KEY NOT NULL,
> issue_name VARCHAR(100) NOT NULL,
> issue_content TEXT NOT NULL,
> vote INTEGER NOT NULL DEFAULT 0,
> status INTEGER NOT NULL DEFAULT 0,
> for_problem INTEGER NOT NULL,
> issue_type INTEGER NOT NULL,
> author VARCHAR(50) NOT NULL,
> written_at TIMESTAMP WITH TIME ZONE NOT NULL
> );
> ```
### 3.4 Issue commects table
> Table name: issue_comments
> ```sql
> CREATE TABLE IF NOT EXISTS issue_comments (
> comment_code SERIAL PRIMARY KEY NOT NULL,
> comment_content TEXT NOT NULL,
> related_issue INTEGER NOT NULL,
> author VARCHAR(50) NOT NULL,
> written_at TIMESTAMP WITH TIME ZONE NOT NULL
> );
> ```

## 5. Invite DB
> Database name: invite;  
> `CREATE DATABASE invite OWNER <USERNAME>;`
### 1.5 INVITE CODE DB
> Table name: codes
> ```sql
> CREATE TABLE IF NOT EXISTS codes (
> code CHAR(5) PRIMARY KEY NOT NULL
> );
> ```
>