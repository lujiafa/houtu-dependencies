package com.houtu.springcloud.sentinel.handler;

import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.houtu.util.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 修改写入数据源委托类。
 *      详情参考：ModifyRulesCommandHandler和WritableDataSourceRegistry
 * @author jonlu
 * @date 2020/7/23
 */
public class DelegateWritableDataSource<T> implements WritableDataSource<List<T>> {

    private final static Logger logger = LoggerFactory.getLogger(DelegateWritableDataSource.class);

    private List<WritableDataSource<List<T>>> delegates = new ArrayList<>();

    public DelegateWritableDataSource() {}

    public DelegateWritableDataSource(List<WritableDataSource<List<T>>> delegates) {
        Assert.notNull(delegates, "parameter delegate cannot be null.");
        this.delegates.addAll(delegates);
    }

    public List<WritableDataSource<List<T>>> getDelegates() {
        return delegates;
    }

    @Override
    public void write(List<T> ts) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("write data to delegate data source, content:{}", ts == null ? "null" : JsonUtils.toString(ts));
        }
        for (WritableDataSource<List<T>> delegate : delegates) {
            delegate.write(ts);
        }
    }

    @Override
    public void close() throws Exception {
        for (WritableDataSource<List<T>> delegate : delegates) {
            delegate.close();
        }
    }
}
