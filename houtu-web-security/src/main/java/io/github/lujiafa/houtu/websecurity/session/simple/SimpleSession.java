package io.github.lujiafa.houtu.websecurity.session.simple;

import io.github.lujiafa.houtu.websecurity.session.Session;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimpleSession implements Session {

	// Session的Id
	private String id;
	// 创建时间
	private LocalDateTime createTime;
	// session属性
	private Map<String, Object> attributes = new HashMap<String, Object>();
	// 权限集合
	private Set<String> permissions = new HashSet<String>();
	// 角色集合
	private Set<String> roles = new HashSet<String>();

	public SimpleSession(String id) {
		this(id, null);
	}

	public SimpleSession(String id, LocalDateTime createTime) {
		Assert.notNull(id, "session id must be not null");
		this.id = id;
		this.createTime = createTime == null ? LocalDateTime.now() : createTime;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public LocalDateTime getCreateTime() {
		return createTime;
	}

	@Override
	public void setAttribute(String attributeName, Object attributeValue) {
		attributes.put(attributeName, attributeValue);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		if (attributes != null) {
			this.attributes.putAll(attributes);
		}
	}

	@Override
	public Object getAttribute(String attributeName) {
		return attributes.get(attributeName);
	}

	@Override
	public Object removeAttribute(String attributeName) {
		return attributes.remove(attributeName);
	}

	@Override
	public void addPermission(String permission) {
		if (permission != null) {
			permissions.add(permission);
		}
	}

	@Override
	public void addPermissions(Set<String> permissions) {
		if (permissions != null) {
			permissions.stream()
				.filter((p) -> p != null && !this.permissions.contains(p))
				.forEach(this.permissions::add);
		}
	}

	@Override
	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		if (permissions != null) {
			this.permissions.addAll(permissions);
		}
	}

	@Override
	public void addRole(String role) {
		if (role != null) {
			permissions.add(role);
		}
	}

	@Override
	public void addRoles(Set<String> roles) {
		if (roles != null) {
			roles.stream()
			.filter((p) -> p != null && !this.roles.contains(p))
			.forEach(this.roles::add);
		}
	}

	@Override
	public Set<String> getRoles() {
		return permissions;
	}

	public void setRoles(Set<String> roles) {
		if (roles != null) {
			this.roles.addAll(roles);
		}
	}

}