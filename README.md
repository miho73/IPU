# IPU

[![Distribute to server](https://github.com/miho73/IPU/actions/workflows/distribute_to_server.yml/badge.svg)](https://github.com/miho73/IPU/actions/workflows/distribute_to_server.yml)
[![Java CI with Gradle](https://github.com/miho73/IPU/actions/workflows/gradle.yml/badge.svg)](https://github.com/miho73/IPU/actions/workflows/gradle.yml)

### *Backend migration is in progress! DO NOT UPDATE frontend!*

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
> `CREATE USER miho WITH SUPERUSER;`

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

## 2. Workflow

> * All commits to main branch will be automatically sent to IPU server and distributed.
> * Most of  contents can be updated without server restart but in order to update server module, server must be restarted.
> * Commiting incomplete server to main branch may cause severe error to ACTUAL IPU server. Make sure to make branch to commit incompleted product.
> * Updating files that don't have any relation with server components is okay to be commited to main branch directly
