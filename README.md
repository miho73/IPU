# IPU

## 1. PSQL Guide

IPU uses postgresql as DBMS. Here's the structure and user spec for IPU.
Below's sql is command to make database. All tabes will be automatically created when needed.

### 1.1. PostgreSQL Setup
> Run below command in order to install PostgreSQL to your computer.   
> `sudo apt-get update`   
> `sudo apt-get install postgresql postgresql-contrib`

### 1.2 USER settings

> |name|value|
> |-|-|
> |host|localhost|
> |user|miho|
> |port|5432|
>
> `CREATE USER miho WITH SUPERUSER`

### 1.3 IDENTIFICATION DB

> Database name: identification;   
> Table name: iden   
> `CREATE DATABASE identification OWNER miho;`

### 1.4 USER SOLVE HISTORY DB

> Database name: solves;   
> Table name: <each user's name>   
> `CREATE DATABASE solves OWNER miho;`

### 1.5 PROBLEM DB

> Database name: problem   
> Table name: prob   
> `CREATE DATABASE problem OWNER miho;`