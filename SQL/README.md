# 数据库更新说明

## 执行SQL文件

新的数据库表结构文件位于：`SQL/tables_new.sql`

### 方式一：使用HeidiSQL或其他数据库客户端

1. 连接到数据库：
   - 主机：116.62.237.84
   - 端口：3306
   - 用户名：root
   - 密码：303803
   - 数据库：caregiver

2. 打开 `SQL/tables_new.sql` 文件

3. 执行整个SQL文件

### 方式二：使用MySQL命令行

```bash
mysql -h 116.62.237.84 -P 3306 -u root -p303803 caregiver < SQL/tables_new.sql
```

## 表结构变更说明

### 1. user表（用户表）- 扩展
- 新增字段：
  - `user_type`：用户类型（client-客户, caregiver-护理员, both-两者都是）
  - `phone`：手机号
  - `deleted`：逻辑删除标记
  - `update_time`：更新时间

### 2. caregiver表（护理员表）- 扩展
- 新增字段：
  - `user_id`：关联用户ID
  - `gender`：性别（male-男, female-女）
  - `birth_date`：出生日期
  - `province`：所在省份
  - `city`：所在城市
  - `status`：状态（available-空闲, busy-护理中）
  - `review_count`：评价数量
  - `deleted`：逻辑删除标记
  - `update_time`：更新时间
- 修改字段：
  - `rating_avg`：改为10分制（decimal(3,1)）

### 3. review表（评价表）- 扩展
- 新增字段：
  - `contract_id`：关联签约ID
  - `images`：评价图片（JSON格式）
  - `deleted`：逻辑删除标记
- 修改字段：
  - `rating`：改为10分制（1-10分）

### 4. contract表（签约表）- 新建
管理护理员和客户的签约关系，包含签约状态、时间等信息。

### 5. message表（消息表）- 新建
管理用户之间的消息通知，包括签约请求、接受、拒绝、结束服务等消息。

### 6. advertisement表（广告表）- 新建
管理首页广告位，支持不同位置的广告展示。

## 注意事项

⚠️ **重要**：执行此SQL文件会删除现有的表并重新创建，现有数据将会丢失！

如果需要保留现有数据，请先备份数据库：

```bash
mysqldump -h 116.62.237.84 -P 3306 -u root -p303803 caregiver > backup.sql
```

## 后续步骤

1. 执行SQL文件创建新表结构
2. 重启后端服务
3. 测试API接口
4. 更新前端代码
