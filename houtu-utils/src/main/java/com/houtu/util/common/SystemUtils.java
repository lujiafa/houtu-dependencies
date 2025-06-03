package com.houtu.util.common;

import com.houtu.util.data.HexUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SystemUtils {

	private static final int PROCESS_ID;
	private static final List<String> DEFAULT_MAC_LIST;
	private static final List<byte[]> DEFAULT_MAC_BYTE_LIST;

	static {
		try {
			DEFAULT_MAC_LIST = getMacList(true, true);
			DEFAULT_MAC_BYTE_LIST = DEFAULT_MAC_LIST.stream().map((p) -> HexUtils.toBinary(p)).collect(Collectors.toList());
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
	 * @Title getDefaultMacByteList
	 * @Description 获取默认mac地址
	 * @return List<byte[]>
	 */
	public static List<byte[]> getDefaultMacByteList() {
		return DEFAULT_MAC_BYTE_LIST;
	}
	
	/**
	 * @Title getDefaultMacList
	 * @Description 获取默认mac地址
	 * @return List<String>
	 */
	public static List<String> getDefaultMacList() {
		return DEFAULT_MAC_LIST;
	}

	/**
	 * @Title getMacByteList
	 * @Description 获取启用并且当前服务可用的网卡mac地址
	 * @return List<byte[]>
	 */
	public static List<byte[]> getMacByteList() {
		return getMacList(true, true).stream().map((p) -> HexUtils.toBinary(p)).collect(Collectors.toList());
	}
	
	/**
	 * @Title getMacByteList
	 * @Description 是否为
	 * @param onlyUp 是否仅返回启用的网卡mac地址
	 * @param onlySiteLocalAddress 是否仅返回当前服务可用的网卡mac地址
	 * @return List<String> mac地址16进制集合
	 */
	public static List<byte[]> getMacByteList(boolean onlyUp, boolean onlySiteLocalAddress) {
		return getMacList(onlyUp, onlySiteLocalAddress).stream().map((p) -> HexUtils.toBinary(p)).collect(Collectors.toList());
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
	 * @Description 是否为
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
	
}