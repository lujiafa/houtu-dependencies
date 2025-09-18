package com.houtu.websecurity.session;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 会话对象
 */
public interface Session extends Serializable {

	long serialVersionUID = 0L;

	/**
	 * @Title getId
	 * @Description 获取Session的ID
	 * @return Session的ID
	 */
	String getId();

	/**
	 * @Title getCreateTime
	 * @Description 获取创建时间
	 * @return 创建时间
	 */
	LocalDateTime getCreateTime();
	
	/**
	 * @Title setAttribute
	 * @Description 设置属性
	 * @param attributeName
	 *            属性名/键
	 * @param attributeValue
	 *            属性值
	 */
	void setAttribute(String attributeName, Object attributeValue);

	/**
	 * @Title getAttributes
	 * @Description 获取属性名集合
	 * @return 属性名称集合
	 */
	Map<String, Object> getAttributes();

	/**
	 * @Title getAttribute
	 * @Description 获取属性值
	 * @param attributeName
	 *            属性Key
	 * @return 属性值
	 */
	Object getAttribute(String attributeName);

	/**
	 * @Title removeAttribute
	 * @Description 通过属性名移除属性并返回其值
	 * @param attributeName
	 *            属性名/键
	 * @return 被移除属性值
	 */
	Object removeAttribute(String attributeName);
	
	/**
	 * 
	 * @Title addPermission
	 * @Description 添加权限
	 * @param permission
	 */
	void addPermission(String permission);
	
	/**
	 * 
	 * @Title addPermissions
	 * @Description 添加权限集合
	 * @param permissions
	 */
	void addPermissions(Set<String> permissions);
	
	/**
	 * 
	 * @Title addPermissions
	 * @Description 添加权限集合
	 */
	Set<String> getPermissions();
	
	/**
	 * 
	 * @Title addRole
	 * @Description 添加权限
	 * @param role
	 */
	void addRole(String role);
	
	/**
	 * 
	 * @Title addRoles
	 * @Description 添加权限集合
	 * @param roles
	 */
	void addRoles(Set<String> roles);
	
	/**
	 * 
	 * @Title getRoles
	 * @Description 添加权限集合
	 */
	Set<String> getRoles();
	
}