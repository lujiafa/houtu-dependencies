package com.houtu.websecurity.autoconfigure;

import com.houtu.websecurity.prop.SecurityProperties;
import com.houtu.websecurity.session.SessionContext;
import com.houtu.websecurity.session.SessionRepository;
import com.houtu.websecurity.session.SessionValidator;
import com.houtu.websecurity.session.repository.EfficientSessionRepository;
import com.houtu.websecurity.session.repository.EfficientSessionRepositoryMessageListener;
import com.houtu.websecurity.session.repository.RedisSessionRepository;
import com.houtu.websecurity.session.validator.SimpleSessionValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

@Import({SessionConfiguration.Cache2kSessionRepositoryConfiguration.class, SessionConfiguration.CaffeineSessionRepositoryConfiguration.class, SessionConfiguration.SessionCacheConfiguration.class})
public class SessionConfiguration {

	@Bean
	@Scope("singleton")
	public SessionContext sessionContext(SessionRepository sessionRepository, SecurityProperties securityProperties) {
		return SessionContext.getInstance(sessionRepository, securityProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	public SessionValidator sessionValidator() {
		return new SimpleSessionValidator();
	}

	static class SessionCacheConfiguration {
		@Bean
		@ConditionalOnMissingBean
		public SessionRepository sessionRepository(RedisTemplate redisTemplate, SecurityProperties securityProperties) {
			return new RedisSessionRepository(redisTemplate, securityProperties);
		}
	}

	static abstract class EfficientSessionRepositoryConfiguration<T> {
		public abstract EfficientSessionRepository sessionRepository(T cacheManager, RedisTemplate redisTemplate, SecurityProperties securityProperties);

		@Bean
		@ConditionalOnBean(EfficientSessionRepository.class)
		@ConditionalOnMissingBean
		public RedisMessageListenerContainer sessionMessageListenerContainer(RedisTemplate redisTemplate,
																			 SecurityProperties securityProperties,
																			 EfficientSessionRepository sessionRepository) {
			String syncChannel = securityProperties.getSession().getCache().getSyncChannel();
			EfficientSessionRepositoryMessageListener sessionMessageListener = new EfficientSessionRepositoryMessageListener(sessionRepository.getCache(), redisTemplate, syncChannel);
			RedisMessageListenerContainer sessionMessageListenerContainer = new RedisMessageListenerContainer();
			sessionMessageListenerContainer.setConnectionFactory(redisTemplate.getConnectionFactory());
			sessionMessageListenerContainer.addMessageListener(sessionMessageListener, Topic.channel(syncChannel));
			return sessionMessageListenerContainer;
		}
	}

	@ConditionalOnClass(name = "org.springframework.cache.caffeine.CaffeineCacheManager")
	static class CaffeineSessionRepositoryConfiguration extends EfficientSessionRepositoryConfiguration<org.springframework.cache.caffeine.CaffeineCacheManager> {
		@Bean
		@ConditionalOnBean(org.springframework.cache.caffeine.CaffeineCacheManager.class)
		@ConditionalOnMissingBean
		public EfficientSessionRepository sessionRepository(org.springframework.cache.caffeine.CaffeineCacheManager cacheManager,
															RedisTemplate redisTemplate,
															SecurityProperties securityProperties) {
			return new EfficientSessionRepository(cacheManager, redisTemplate, securityProperties);
		}
	}

	@ConditionalOnClass(name = "org.cache2k.extra.spring.SpringCache2kCacheManager")
	static class Cache2kSessionRepositoryConfiguration extends EfficientSessionRepositoryConfiguration<org.cache2k.extra.spring.SpringCache2kCacheManager> {
		@Bean
		@ConditionalOnBean(org.cache2k.extra.spring.SpringCache2kCacheManager.class)
		@ConditionalOnMissingBean
		public EfficientSessionRepository sessionRepository(org.cache2k.extra.spring.SpringCache2kCacheManager cacheManager,
															RedisTemplate redisTemplate,
															SecurityProperties securityProperties) {
			return new EfficientSessionRepository(cacheManager, redisTemplate, securityProperties);
		}
	}

}