package io.github.lujiafa.houtu.websecurity.session.client.condition;

import io.github.lujiafa.houtu.websecurity.session.client.SessionClientProperties;
import io.github.lujiafa.houtu.websecurity.session.client.SessionPropertyLoader;
import org.springframework.boot.autoconfigure.condition.*;

public class ConditionalOnSessionClientProperty extends AnyNestedCondition {
    public ConditionalOnSessionClientProperty() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnBean(SessionPropertyLoader.class)
    static class SessionPropertyLoaderPresent {
    }

    @ConditionalOnProperty(prefix = SessionClientProperties.PREFIX, name = "server-url")
    static class SessionClientServerUrlProperty {
    }

}
