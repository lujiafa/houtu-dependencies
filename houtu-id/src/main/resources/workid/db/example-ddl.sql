CREATE TABLE snowflake_worker
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    biz_code       VARCHAR(64)  NOT NULL COMMENT '业务隔离码',
    datacenter_id  INT          NOT NULL COMMENT '数据中心ID',
    worker_id      INT          NOT NULL COMMENT '分配出的workerId',
    identity       VARCHAR(128) NOT NULL COMMENT '持有者标识 ip:port',
    last_heartbeat BIGINT       NOT NULL COMMENT '最后心跳毫秒；优雅关闭直接 DELETE 行，行残留表示崩溃/强行KILL等，超过过期阈值即可被回收',
    version        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_time    DATETIME     NOT NULL,
    update_time    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_dc_worker (biz_code, datacenter_id, worker_id),
    KEY            idx_identity (biz_code, datacenter_id, identity)
) ENGINE=InnoDB COMMENT='雪花算法workerId分配表';