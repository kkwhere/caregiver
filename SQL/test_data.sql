-- ========================================
-- 陪伴家园 - 测试数据
-- ========================================

USE `caregiver`;

-- 清空现有数据（可选，如果需要重新插入测试数据）
-- TRUNCATE TABLE `advertisement`;
-- TRUNCATE TABLE `message`;
-- TRUNCATE TABLE `review`;
-- TRUNCATE TABLE `contract`;
-- TRUNCATE TABLE `caregiver`;
-- TRUNCATE TABLE `user`;

-- ========================================
-- 1. 插入用户数据
-- ========================================

-- 客户用户
INSERT INTO `user` (`id`, `openid`, `nickname`, `avatar`, `user_type`, `phone`, `deleted`, `create_time`) VALUES
(1, 'client_openid_001', '张三', NULL, 'client', '13800138001', 0, NOW()),
(2, 'client_openid_002', '李四', NULL, 'client', '13800138002', 0, NOW()),
(3, 'client_openid_003', '王五', NULL, 'client', '13800138003', 0, NOW());

-- 护理员用户
INSERT INTO `user` (`id`, `openid`, `nickname`, `avatar`, `user_type`, `phone`, `deleted`, `create_time`) VALUES
(4, 'caregiver_openid_001', '刘护士', NULL, 'caregiver', '13900139001', 0, NOW()),
(5, 'caregiver_openid_002', '陈护士', NULL, 'caregiver', '13900139002', 0, NOW()),
(6, 'caregiver_openid_003', '赵护士', NULL, 'caregiver', '13900139003', 0, NOW()),
(7, 'caregiver_openid_004', '孙护士', NULL, 'caregiver', '13900139004', 0, NOW()),
(8, 'caregiver_openid_005', '周护士', NULL, 'caregiver', '13900139005', 0, NOW());

-- ========================================
-- 2. 插入护理员数据
-- ========================================

INSERT INTO `caregiver` (`id`, `user_id`, `name`, `avatar`, `gender`, `birth_date`, `province`, `city`, `hospital`, `intro`, `status`, `rating_avg`, `review_count`, `deleted`, `create_time`) VALUES
(1, 4, '刘芳', NULL, 'female', '1985-03-15', '北京市', '北京市', '北京协和医院', '从事护理工作15年，擅长老年护理和术后康复护理，有丰富的临床经验，耐心细致，深受患者好评。', 'available', 9.2, 25, 0, NOW()),
(2, 5, '陈静', NULL, 'female', '1990-07-20', '上海市', '上海市', '上海瑞金医院', '护理专业本科毕业，持有高级护理证书，擅长ICU护理和慢性病管理，工作认真负责。', 'available', 8.8, 18, 0, NOW()),
(3, 6, '赵敏', NULL, 'female', '1988-11-08', '广东省', '广州市', '广州中山大学附属第一医院', '10年护理经验，擅长儿童护理和产后护理，温柔体贴，深受家属信赖。', 'busy', 9.5, 32, 0, NOW()),
(4, 7, '孙丽', NULL, 'female', '1992-05-12', '浙江省', '杭州市', '浙江大学医学院附属第一医院', '年轻有活力，护理技术扎实，擅长康复护理和心理疏导，善于与患者沟通。', 'available', 8.5, 12, 0, NOW()),
(5, 8, '周婷', NULL, 'female', '1987-09-25', '江苏省', '南京市', '南京鼓楼医院', '资深护理师，擅长重症护理和临终关怀，专业素养高，服务态度好。', 'available', 9.0, 20, 0, NOW());

-- ========================================
-- 3. 插入签约数据
-- ========================================

-- 已完成的签约
INSERT INTO `contract` (`id`, `caregiver_id`, `client_id`, `status`, `request_time`, `accept_time`, `start_time`, `end_request_time`, `end_time`, `remark`, `deleted`, `create_time`) VALUES
(1, 1, 1, 'completed', '2026-02-01 10:00:00', '2026-02-01 11:00:00', '2026-02-01 11:00:00', '2026-02-15 18:00:00', '2026-02-15 19:00:00', '服务非常满意', 0, '2026-02-01 10:00:00'),
(2, 2, 2, 'completed', '2026-02-05 09:00:00', '2026-02-05 10:00:00', '2026-02-05 10:00:00', '2026-02-20 17:00:00', '2026-02-20 18:00:00', '护理专业，态度好', 0, '2026-02-05 09:00:00'),
(3, 1, 3, 'completed', '2026-02-10 14:00:00', '2026-02-10 15:00:00', '2026-02-10 15:00:00', '2026-02-25 16:00:00', '2026-02-25 17:00:00', '非常感谢', 0, '2026-02-10 14:00:00');

-- 服务中的签约
INSERT INTO `contract` (`id`, `caregiver_id`, `client_id`, `status`, `request_time`, `accept_time`, `start_time`, `remark`, `deleted`, `create_time`) VALUES
(4, 3, 1, 'in_service', '2026-03-01 10:00:00', '2026-03-01 11:00:00', '2026-03-01 11:00:00', '长期护理', 0, '2026-03-01 10:00:00');

-- 待接受的签约
INSERT INTO `contract` (`id`, `caregiver_id`, `client_id`, `status`, `request_time`, `remark`, `deleted`, `create_time`) VALUES
(5, 4, 2, 'pending', '2026-03-07 15:00:00', '需要康复护理', 0, '2026-03-07 15:00:00');

-- ========================================
-- 4. 插入评价数据
-- ========================================

INSERT INTO `review` (`id`, `contract_id`, `caregiver_id`, `user_id`, `rating`, `content`, `images`, `deleted`, `create_time`) VALUES
(2, 1, 1, 1, 9, '服务很到位，就是有时候稍微有点忙，但总体非常满意。', NULL, 0, '2026-02-16 10:00:00'),
(3, 2, 2, 2, 9, '陈护士很细心，每天都会详细记录护理情况，让我们很放心。', NULL, 0, '2026-02-20 19:00:00'),
(4, 3, 1, 3, 8, '护理工作做得不错，但希望能更主动一些沟通。', NULL, 0, '2026-02-25 18:00:00');

-- ========================================
-- 5. 插入消息数据
-- ========================================

-- 签约请求消息
INSERT INTO `message` (`id`, `sender_id`, `receiver_id`, `message_type`, `contract_id`, `content`, `is_read`, `deleted`, `create_time`) VALUES
(1, 2, 4, 'contract_request', 5, '您收到一个新的签约请求', 0, 0, '2026-03-07 15:00:00');

-- 签约接受消息
INSERT INTO `message` (`id`, `sender_id`, `receiver_id`, `message_type`, `contract_id`, `content`, `is_read`, `deleted`, `create_time`) VALUES
(2, 3, 1, 'contract_accept', 4, '护理员已接受您的签约请求', 1, 0, '2026-03-01 11:00:00');

-- 结束服务请求消息
INSERT INTO `message` (`id`, `sender_id`, `receiver_id`, `message_type`, `contract_id`, `content`, `is_read`, `deleted`, `create_time`) VALUES
(3, 1, 1, 'service_end_request', 1, '护理员请求结束服务', 1, 0, '2026-02-15 18:00:00');

-- ========================================
-- 6. 插入广告数据
-- ========================================

INSERT INTO `advertisement` (`id`, `title`, `image_url`, `link_url`, `position`, `sort_order`, `status`, `deleted`, `create_time`) VALUES
(1, '专业护理服务', NULL, NULL, 'home_top', 1, 'active', 0, NOW()),
(2, '24小时在线服务', NULL, NULL, 'home_top', 2, 'active', 0, NOW()),
(3, '资深护理团队', NULL, NULL, 'home_top', 3, 'active', 0, NOW());

-- ========================================
-- 完成
-- ========================================

SELECT '测试数据插入完成！' AS message;
