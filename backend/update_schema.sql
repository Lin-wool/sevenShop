-- 更新sys_user表结构
ALTER TABLE sys_user ADD COLUMN phone VARCHAR(20) AFTER email;
ALTER TABLE sys_user ADD COLUMN status INT DEFAULT 1 AFTER phone;

-- 为现有用户设置默认值
UPDATE sys_user SET phone = '' WHERE phone IS NULL;
UPDATE sys_user SET status = 1 WHERE status IS NULL;

-- 优化订单查询性能：添加 created_at 索引
CREATE INDEX idx_orders_created_at ON orders(created_at);