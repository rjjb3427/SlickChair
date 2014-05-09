# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "ASSIGNMENT" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"PAPERID" TEXT NOT NULL,"PERSONID" TEXT NOT NULL);
create table "AUTHOR" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"PAPERID" TEXT NOT NULL,"PERSONID" TEXT NOT NULL,"POSITION" INTEGER NOT NULL);
create table "BID" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"PAPERID" TEXT NOT NULL,"PERSONID" TEXT NOT NULL,"VALUE" INTEGER NOT NULL);
create table "COMMENT" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"PAPERID" TEXT NOT NULL,"PERSONID" TEXT NOT NULL,"CONTENT" TEXT NOT NULL);
create table "EMAIL" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"TO" TEXT NOT NULL,"SUBJECT" TEXT NOT NULL,"CONTENT" TEXT NOT NULL);
create table "FILE" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"NAME" TEXT NOT NULL,"SIZE" BIGINT NOT NULL,"CONTENT" BLOB NOT NULL);
create table "LOGINTOKENS" ("UUID" text NOT NULL PRIMARY KEY,"EMAIL" text NOT NULL,"CREATIONTIME" TIMESTAMP NOT NULL,"EXPIRATIONTIME" TIMESTAMP NOT NULL,"ISSIGNUP" BOOLEAN NOT NULL,"ISINVITATION" BOOLEAN NOT NULL);
create table "LOGINUSERS" ("UID" TEXT NOT NULL,"PID" TEXT NOT NULL,"EMAIL" TEXT NOT NULL,"FIRSTNAME" TEXT NOT NULL,"LASTNAME" TEXT NOT NULL,"AUTHMETHOD" TEXT NOT NULL,"HASHER" TEXT,"PASSWORD" TEXT,"SALT" TEXT);
alter table "LOGINUSERS" add constraint "LOGINUSERS_PK" primary key("UID","PID");
create table "PAPER" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"TITLE" TEXT NOT NULL,"FORMAT" INTEGER NOT NULL,"KEYWORDS" TEXT NOT NULL,"ABSTRCT" TEXT NOT NULL,"NAUTHORS" INTEGER NOT NULL,"FILE" TEXT);
create table "PAPERTOPIC" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"PAPERID" TEXT NOT NULL,"TOPICID" TEXT NOT NULL);
create table "PERSON" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"FIRSTNAME" TEXT NOT NULL,"LASTNAME" TEXT NOT NULL,"ORGANIZATION" VARCHAR NOT NULL,"ROLE" INTEGER NOT NULL,"EMAIL" TEXT NOT NULL);
create table "REVIEW" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"PAPERID" TEXT NOT NULL,"PERSONID" TEXT NOT NULL,"CONFIDENCE" INTEGER NOT NULL,"EVALUATION" INTEGER NOT NULL,"CONTENT" TEXT NOT NULL);
create table "TOPIC" ("ID" TEXT NOT NULL,"UPDATEDAT" TIMESTAMP NOT NULL,"UPDATEDBY" TEXT NOT NULL,"NAME" TEXT NOT NULL);

# --- !Downs

drop table "ASSIGNMENT";
drop table "AUTHOR";
drop table "BID";
drop table "COMMENT";
drop table "EMAIL";
drop table "FILE";
drop table "LOGINTOKENS";
alter table "LOGINUSERS" drop constraint "LOGINUSERS_PK";
drop table "LOGINUSERS";
drop table "PAPER";
drop table "PAPERTOPIC";
drop table "PERSON";
drop table "REVIEW";
drop table "TOPIC";

