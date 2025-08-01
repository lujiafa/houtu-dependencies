package com.houtu.websecurity.autoconfigure;

import com.houtu.websecurity.prop.SecurityProperties;
import com.houtu.websecurity.session.SessionContext;
import com.houtu.websecurity.session.SessionRepository;
import com.houtu.websecurity.session.SessionValidator;
import com.houtu.websecurity.session.repository.EfficientSessionRepository;
import com.houtu.websecurity.session.repository.RedisSessionRepository;
import com.houtu.websecurity.session.validator.SimpleSessionValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;

@Import({SessionConfiguration.Cache2kSessionRepositoryConfiguration.class, SessionConfiguration.EfficientSessionRepositoryConfiguration.class, SessionConfiguration.SessionCacheConfiguration.class})
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

	public static class SessionCacheConfiguration {
		@Bean
		@ConditionalOnMissingBean
		public SessionRepository sessionRepository(RedisTemplate redisTemplate, SecurityProperties securityProperties) {
			return new RedisSessionRepository(redisTemplate, securityProperties);
		}
	}

	@ConditionalOnClass(name = "org.springframework.cache.caffeine.CaffeineCacheManager")
	public static class EfficientSessionRepositoryConfiguration {
		@Bean
		@ConditionalOnBean(org.springframework.cache.caffeine.CaffeineCacheManager.class)
		@ConditionalOnMissingBean
		public SessionRepository sessionRepository(org.springframework.cache.caffeine.CaffeineCacheManager cacheManager, RedisTemplate redisTemplate, SecurityProperties securityProperties) {
			return new EfficientSessionRepository(cacheManager, redisTemplate, securityProperties);
		}
	}

	@ConditionalOnClass(name = "org.cache2k.extra.spring.SpringCache2kCacheManager")
	public static class Cache2kSessionRepositoryConfiguration {
		@Bean
		@ConditionalOnBean(org.cache2k.extra.spring.SpringCache2kCacheManager.class)
		@ConditionalOnMissingBean
		public SessionRepository sessionRepository(org.cache2k.extra.spring.SpringCache2kCacheManager cacheManager, RedisTemplate redisTemplate, SecurityProperties securityProperties) {
			return new EfficientSessionRepository(cacheManager, redisTemplate, securityProperties);
		}
	}
}