package io.github.lujiafa.houtu.springcloud.sentinel.handler;

import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.transport.util.WritableDataSourceRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.List;

public class WritableDataSourceBeanProcessor implements BeanPostProcessor, SmartInitializingSingleton {

    private List<WritableDataSource<List<FlowRule>>> flowDataSources = new ArrayList<>();
    private List<WritableDataSource<List<AuthorityRule>>> authorityDataSources = new ArrayList<>();
    private List<WritableDataSource<List<DegradeRule>>> degradeDataSources = new ArrayList<>();
    private List<WritableDataSource<List<SystemRule>>> systemRules = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof WritableDataSource writeableDataSource) {
            ResolvableType beanType = ResolvableType.forType(bean.getClass());
            if (ResolvableType.forClassWithGenerics(
                    WritableDataSource.class,
                    ResolvableType.forClassWithGenerics(List.class, FlowRule.class)).isAssignableFrom(beanType)) {
                flowDataSources.add((WritableDataSource<List<FlowRule>>) bean);
            }
            if (ResolvableType.forClassWithGenerics(
                    WritableDataSource.class,
                    ResolvableType.forClassWithGenerics(List.class, AuthorityRule.class)).isAssignableFrom(beanType)) {
                authorityDataSources.add((WritableDataSource<List<AuthorityRule>>) bean);
            }
            if (ResolvableType.forClassWithGenerics(
                    WritableDataSource.class,
                    ResolvableType.forClassWithGenerics(List.class, DegradeRule.class)).isAssignableFrom(beanType)) {
                degradeDataSources.add((WritableDataSource<List<DegradeRule>>) bean);
            }
            if (ResolvableType.forClassWithGenerics(
                    WritableDataSource.class,
                    ResolvableType.forClassWithGenerics(List.class, SystemRule.class)).isAssignableFrom(beanType)) {
                systemRules.add((WritableDataSource<List<SystemRule>>) bean);
            }
        }
        return bean;
    }

    @Override
    public void afterSingletonsInstantiated() {
        WritableDataSourceRegistry.registerFlowDataSource(new DelegateWritableDataSource<FlowRule>(flowDataSources));
        WritableDataSourceRegistry.registerAuthorityDataSource(new DelegateWritableDataSource<AuthorityRule>(authorityDataSources));
        WritableDataSourceRegistry.registerDegradeDataSource(new DelegateWritableDataSource<DegradeRule>(degradeDataSources));
        WritableDataSourceRegistry.registerSystemDataSource(new DelegateWritableDataSource<SystemRule>(systemRules));
    }

}
