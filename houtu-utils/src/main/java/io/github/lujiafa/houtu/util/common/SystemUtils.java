package io.github.lujiafa.houtu.util.common;

import io.github.lujiafa.houtu.util.data.ByteUtils;
import io.github.lujiafa.houtu.util.data.HexUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.stream.Collectors;

public class SystemUtils extends org.apache.commons.lang3.SystemUtils {

	private static final int PROCESS_ID;

	static {
		try {
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			PROCESS_ID = Integer.valueOf(runtime.getName().split("@")[0]);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @Title getProcessId
	 * @Description 获取当前服务进程ID
	 */
	public static int getProcessId() {
		return PROCESS_ID;
	}
	
	/**
	 * @Title getMacByteList
	 * @Description 获取启用并且当前服务可用的网卡mac地址
	 * @return List<byte[]>
	 */
	public static List<byte[]> getMacByteList() {
		return getMacList(true, true).stream().map((p) -> ByteUtils.toBinary(p)).collect(Collectors.toList());
	}
	
	/**
	 * @Title getMacByteList
	 * @Description 是否为
	 * @param onlyUp 是否仅返回启用的网卡mac地址
	 * @param onlySiteLocalAddress 是否仅返回当前服务可用的网卡mac地址
	 * @return List<String> mac地址16进制集合
	 */
	public static List<byte[]> getMacByteList(boolean onlyUp, boolean onlySiteLocalAddress) {
		return getMacList(onlyUp, onlySiteLocalAddress).stream().map((p) -> ByteUtils.toBinary(p)).collect(Collectors.toList());
	}
	
	/**
	 * @Title getMacList
	 * @Description 获取启用并且当前服务可用的网卡mac地址
	 * @return List<String>
	 */
	public static List<String> getMacList() {
		return getMacList(true, true);
	}
	
	/**
	 * @Title getMacList
	 * @Description 获取Mac集合
	 * @param onlyUp 是否仅返回启用网卡mac地址
	 * @param onlySiteLocalAddress 是否仅返回当前服务可用的网卡mac地址
	 * @return List<String> mac地址16进制集合
	 */
	public static List<String> getMacList(boolean onlyUp, boolean onlySiteLocalAddress) {
		try {
			Set<String> set = new LinkedHashSet<String>();
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface.isVirtual() || networkInterface.isLoopback()) {
					continue;
				}
				if (onlyUp && !networkInterface.isUp()) {
					continue;
				}
				List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
				for (InterfaceAddress interfaceAddress : interfaceAddresses) {
					InetAddress inetAddress = interfaceAddress.getAddress();
					if (inetAddress == null || inetAddress.isLoopbackAddress()) {
						continue;
					}
					if (onlySiteLocalAddress && !inetAddress.isSiteLocalAddress()) {
						continue;
					}
					byte[] hardwareAddress = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
					set.add(HexUtils.toHex(hardwareAddress));
				}
			}
			return set.stream().collect(Collectors.toList());
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 获取当前服务器的IP地址
	 * @return String IP地址
	 */
	public static String getServerIp() {
		List<String> serverIps = getServerIps();
		return serverIps.isEmpty() ? "" : serverIps.get(0);
	}

	/**
	 * 获取当前服务器的IP地址列表
	 * @return List<String> IP地址集合，不排除为EMPTY
	 */
	public static List<String> getServerIps() {
		try {
			List<String> candidateIps = new ArrayList<>();
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				// 跳过虚拟接口和未启用的接口
				if (networkInterface.isLoopback() || !networkInterface.isUp() || networkInterface.isVirtual()) {
					continue;
				}
				// 排除常见虚拟接口（按名称模式匹配）
				String displayName = networkInterface.getDisplayName().toLowerCase();
				if (displayName.contains("docker") // 排除Docker接口
						|| displayName.contains("br-") // 排除Docker容器的接口
						|| displayName.contains("veth")) {
					continue;
				}
				// 遍历该接口的所有IP地址
				for (InetAddress addr : Collections.list(networkInterface.getInetAddresses())) {
					// 仅处理IPv4地址
					if (addr instanceof Inet4Address) {
						candidateIps.add(addr.getHostAddress());
					}
				}
			}
			return candidateIps;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
}