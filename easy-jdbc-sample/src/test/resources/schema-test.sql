drop table if exists `user`;
CREATE TABLE `user`(
	id BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
	name NVARCHAR(200),
	age INT(3)
);
