-- 用户基础信息表
CREATE TABLE IF NOT EXISTS `user`(  
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID，无业务意义',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除，0：否 / 1：是',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '数据版本，乐观锁',
    `user_id` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户ID，业务唯一',
    `nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '用户基础信息表';