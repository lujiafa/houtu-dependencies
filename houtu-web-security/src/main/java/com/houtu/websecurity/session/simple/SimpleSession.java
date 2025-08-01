package com.houtu.websecurity.session.simple;

import com.houtu.websecurity.session.Session;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;

public class SimpleSession implements Session {

	// Session的Id
	private String id;
	// 创建时间
	private LocalDateTime createTime;
	// session属性
	private Map<String, Object> sessionAttrs = new HashMap<String, Object>();
	// 权限集合
	private Set<String> permissions = new HashSet<String>();
	// 角色集合
	private Set<String> roles = new HashSet<String>();

	public SimpleSession(String id) {
		Assert.notNull(id, "session id must be not null");
		this.id = id;
		this.createTime = LocalDateTime.now();
	}

	public String getId() {
		return id;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setAttribute(String attributeName, Object attributeValue) {
		sessionAttrs.put(attributeName, attributeValue);
	}

	public Set<String> getAttributeNames() {
		return sessionAttrs.keySet();
	}

	public Object getAttribute(String attributeName) {
		return sessionAttrs.get(attributeName);
	}

	public Object removeAttribute(String attributeName) {
		return sessionAttrs.remove(attributeName);
	}
	
	public void addPermission(String permission) {
		if (permission != null) {
			permissions.add(permission);
		}
	}
	
	public void addPermissions(Set<String> permissions) {
		if (permissions != null) {
			permissions.stream()
				.filter((p) -> p != null && !this.permissions.contains(p))
				.forEach(this.permissions::add);
		}
	}
	
	public Set<String> getPermissions() {
		return permissions;
	}
	
	public void addRole(String role) {
		if (role != null) {
			permissions.add(role);
		}
	}
	
	public void addRoles(Set<String> roles) {
		if (roles != null) {
			roles.stream()
			.filter((p) -> p != null && !this.roles.contains(p))
			.forEach(this.roles::add);
		}
	}
	
	public Set<String> getRoles() {
		return permissions;
	}

}