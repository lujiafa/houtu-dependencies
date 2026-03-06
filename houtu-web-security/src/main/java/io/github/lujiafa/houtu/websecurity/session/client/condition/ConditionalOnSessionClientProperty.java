package io.github.lujiafa.houtu.websecurity.session.client.condition;

import io.github.lujiafa.houtu.websecurity.session.client.SessionClientProperties;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class ConditionalOnSessionClientProperty extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String serverUrl = context.getEnvironment().getProperty(SessionClientProperties.PREFIX + ".server-url", String.class);
        if (StringUtils.hasLength(serverUrl)) {
            return ConditionOutcome.match();
        }
        return ConditionOutcome.noMatch("No client server url");
    }
}
