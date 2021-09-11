## 1. PSQL Guide

IPU uses postgresql as DBMS. Here's the structure and user spec for IPU.
Below's sql is command to make database. All tabes will be automatically created when needed.

### 1.1. PostgreSQL Setup

> Run below command in order to install PostgreSQL to your server.   
> `sudo apt-get update`   
> `sudo apt-get install postgresql postgresql-contrib`

### 1.2 USER settings
> `CREATE USER <USERNAME> WITH SUPERUSER;`

### 1.3 IDENTIFICATION DB

> Database name: identification;   
> Table name: iden   
> `CREATE DATABASE identification OWNER <USERNAME>;`
> ```sql
> CREATE TABLE IF NOT EXISTS iden(
> user_code BIGSERIAL NOT NULL,
> user_id VARCHAR(50) NOT NULL,
> user_name VARCHAR(50) NOT NULL,
> user_password CHAR(88) NOT NULL,
> user_salt CHAR(88) NOT NULL,
> invite_code CHAR(4) NOT NULL,
> bio VARCHAR(500) NOT NULL,
> privilege CHAR(1) NOT NULL,
> email VARCHAR(20) NOT NULL,
> joined CHAR(24) NOT NULL,
> experience INTEGER NOT NULL,
> aes_iv CHAR(16) NOT NULL,
> last_solve CHAR(24) NOT NULL,
> last_login CHAR(24) NOT NULL
> );
> ```

### 1.4 USER SOLVE HISTORY DB

> Database name: solves;   
> Table name: <each user's name>   
> `CREATE DATABASE solves OWNER <USERNAME>;`
> ```sql
> CREATE TABLE IF NOT EXISTS u<usercode>(
> code SERIAL NOT NULL,
> problem_code INTEGER NOT NULL,
> solved_time CHAR(24) NOT NULL,
> solving_time INTEGER NOT NULL,
> correct BOOLEAN NOT NULL
> );
> ```

### 1.5 PROBLEM DB

> Database name: problem   
> Table name: prob   
> `CREATE DATABASE problem OWNER <USERNAME>;`
> ```sql
> CREATE TABLE IF NOT EXISTS prob(
> problem_code SERIAL NOT NULL,
> problem_name VARCHAR(50) NOT NULL,
> problem_category CHAR(4) NOT NULL,
> problem_difficulty CHAR(4) NOT NULL,
> problem_content TEXT NOT NULL,
> problem_solution TEXT NOT NULL,
> problem_answer TEXT NOT NULL,
> problem_hint TEXT NOT NULL,
> has_hint BOOLEAN NOT NULL,
> auther_name VARCHAR(50) NOT NULL,
> added_at CHAR(24) NOT NULL,
> last_modified CHAR(24) NOT NULL,
> tags TEXT NOT NULL
> );
> ```

### 1.5 INVITE CODE DB

> Database name: invite;   
> Table name: codes   
> `CREATE DATABASE invite OWNER <USERNAME>;`
> ```sql
> CREATE TABLE IF NOT EXISTS codes (
> code CHAR(5) NOT NULL,
> added_by VARCHAR(50),
> added_at CHAR(24)
> );
> ```