package com.houtu.monitor.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author jon
 * @date 2020年12月17日
 */
@ConfigurationProperties(prefix = MonitorProperties.PREFIX)
public class MonitorProperties {

	public static final String PREFIX = "houtu.monitor";

	/**
	 * 业务名称
	 */
	private String businessName;

	/**
	 * 服务器IP
	 */
	private String svrIp;

	/**
	 * 是否打开全部请求监控 默认关闭
	 */
	private boolean fullRequest = false;

	/**
	 * 监控窗口周期。默认1秒
	 */
	private Duration period = Duration.ofSeconds(1);
	/**
	 * 监控数据延期多久处理，防止高并发时临界错位问题。默认100毫秒
	 */
	private Duration delay = Duration.ofMillis(100);
	/**
	 * 监控日志Collect队列容量
	 */
	private int collectQueueCapacity = 5000;
	/**
	 * 监控日志输出队列容量
	 */
	private int outputQueueCapacity = 5000;



	public void setFullRequest(boolean fullRequest) {
		this.fullRequest = fullRequest;
	}

	public boolean isFullRequest() {
		return fullRequest;
	}

	public Duration getPeriod() {
		return period;
	}

	public void setPeriod(Duration period) {
		this.period = period;
	}

	public Duration getDelay() {
		return delay;
	}

	public void setDelay(Duration delay) {
		this.delay = delay;
	}

	public int getCollectQueueCapacity() {
		return collectQueueCapacity;
	}

	public void setCollectQueueCapacity(int collectQueueCapacity) {
		this.collectQueueCapacity = collectQueueCapacity;
	}

	public int getOutputQueueCapacity() {
		return outputQueueCapacity;
	}

	public void setOutputQueueCapacity(int outputQueueCapacity) {
		this.outputQueueCapacity = outputQueueCapacity;
	}

	public String getSvrIp() {
		return svrIp;
	}

	public void setSvrIp(String svrIp) {
		this.svrIp = svrIp;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
}
