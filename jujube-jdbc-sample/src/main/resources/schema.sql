drop table if exists `user`;
CREATE TABLE `user`(
	id BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
	name NVARCHAR(200),
	age INT(3),
	department_id bigint(20),
	f_info_id_ int(12)
);
drop table if exists `department`;
CREATE TABLE `department`(
	id BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
	name NVARCHAR(200)
);

