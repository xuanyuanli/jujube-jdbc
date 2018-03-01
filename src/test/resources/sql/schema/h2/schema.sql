drop SCHEMA if exists demo;
CREATE SCHEMA demo;

drop table if exists demo.user;
CREATE TABLE demo.user(
	id BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
	name NVARCHAR(200),
	age INT(3)
);
