-- --------------------------------------------------------
-- 陪护家园 - 新版数据库表结构
-- 主机: 116.62.237.84
-- 数据库: caregiver
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- 使用数据库
USE `caregiver`;

-- ========================================
-- 1. 用户表（扩展）
-- ========================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(100) DEFAULT NULL COMMENT '微信openid',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `user_type` varchar(20) NOT NULL DEFAULT 'client' COMMENT '用户类型：client-客户, caregiver-护理员, both-两者都是',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_user_type` (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ========================================
-- 2. 护理员表（扩展）
-- ========================================
DROP TABLE IF EXISTS `caregiver`;
CREATE TABLE `caregiver` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别：male-男, female-女',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `province` varchar(50) DEFAULT NULL COMMENT '所在省份',
  `city` varchar(50) DEFAULT NULL COMMENT '所在城市',
  `hospital` varchar(100) DEFAULT NULL COMMENT '所属医院',
  `intro` text COMMENT '个人介绍',
  `status` varchar(20) NOT NULL DEFAULT 'available' COMMENT '状态：available-空闲, busy-护理中',
  `rating_avg` decimal(3,1) DEFAULT '0.0' COMMENT '平均评分（10分制）',
  `review_count` int DEFAULT '0' COMMENT '评价数量',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_hospital` (`hospital`),
  KEY `idx_rating` (`rating_avg`),
  KEY `idx_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理员表';

-- ========================================
-- 3. 评价表（扩展）
-- ========================================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `contract_id` bigint NOT NULL COMMENT '关联签约ID',
  `caregiver_id` bigint NOT NULL COMMENT '护理员ID',
  `user_id` bigint NOT NULL COMMENT '客户ID',
  `rating` int NOT NULL COMMENT '评分（1-10分）',
  `content` text COMMENT '评价内容',
  `images` json DEFAULT NULL COMMENT '评价图片URL数组',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract` (`contract_id`),
  KEY `idx_caregiver` (`caregiver_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价表';

-- ========================================
-- 4. 签约表（新建）
-- ========================================
DROP TABLE IF EXISTS `contract`;
CREATE TABLE `contract` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `caregiver_id` bigint NOT NULL COMMENT '护理员ID',
  `client_id` bigint NOT NULL COMMENT '客户ID',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待接受, accepted-已接受, rejected-已拒绝, in_service-服务中, end_requested-请求结束, completed-已完成, cancelled-已取消',
  `request_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `accept_time` datetime DEFAULT NULL COMMENT '接受时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始服务时间',
  `end_request_time` datetime DEFAULT NULL COMMENT '请求结束时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `remark` text COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_caregiver` (`caregiver_id`),
  KEY `idx_client` (`client_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='签约表';

-- ========================================
-- 5. 消息表（新建）
-- ========================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `message_type` varchar(30) NOT NULL COMMENT '消息类型：contract_request-签约请求, contract_accept-签约接受, contract_reject-签约拒绝, service_end_request-结束服务请求, service_end_accept-结束服务接受, service_end_reject-结束服务拒绝',
  `contract_id` bigint DEFAULT NULL COMMENT '关联签约ID',
  `content` text COMMENT '消息内容',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_contract` (`contract_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息表';

-- ========================================
-- 6. 广告表（新建）
-- ========================================
DROP TABLE IF EXISTS `advertisement`;
CREATE TABLE `advertisement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(100) DEFAULT NULL COMMENT '广告标题',
  `image_url` varchar(255) NOT NULL COMMENT '广告图片URL',
  `link_url` varchar(255) DEFAULT NULL COMMENT '跳转链接',
  `position` varchar(20) NOT NULL DEFAULT 'home_top' COMMENT '广告位置：home_top-首页顶部, home_middle-首页中部',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-启用, inactive-停用',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='广告表';

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
