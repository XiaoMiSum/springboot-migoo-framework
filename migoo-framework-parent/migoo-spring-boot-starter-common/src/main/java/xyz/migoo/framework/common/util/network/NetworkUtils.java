package xyz.migoo.framework.common.util.network;

import java.net.*;
import java.util.Enumeration;

/**
 * 网络工具类
 *
 * @author migoo
 * @since 1.3.16
 */
public final class NetworkUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST = "127.0.0.1";
    private static final String SEPARATOR = ",";

    private NetworkUtils() {
    }

    /**
     * 判断 IP 是否为 unknown
     */
    public static boolean isUnknown(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        return UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 获取本机 IP 地址
     */
    public static String getLocalHost() {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface anInterface = interfaces.nextElement();
                // 在特定接口上遍历所有IP地址
                Enumeration<InetAddress> inetAddresses = anInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.isSiteLocalAddress()) {
                            return inetAddress.getHostAddress();
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddress;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress.getHostAddress();
            }
            // 如果没有找到非回环地址，则使用本地主机名
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress != null) {
                return jdkSuppliedAddress.getHostAddress();
            }
        } catch (SocketException | UnknownHostException e) {
            // 忽略异常
        }
        return LOCALHOST;
    }

    /**
     * 获取主机名
     */
    public static String getHostname() {
        InetAddress ip = getInetAddress();
        return ip != null ? ip.getHostName() : "";
    }

    /**
     * 获取 IP 地址
     */
    public static String getIpAddress() {
        InetAddress ip = getInetAddress();
        return ip != null ? ip.getHostAddress() : "";
    }

    private static InetAddress getInetAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip instanceof Inet4Address) {
                        return ip;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e);
        }
        return null;
    }

    /**
     * 获取客户端 IP 地址（从代理头中解析）
     */
    public static String getClientIp(String ip, String otherProxyIps) {
        if (isUnknown(ip)) {
            return ip;
        }
        // 如果有多个代理 IP，取第一个
        if (otherProxyIps != null && !otherProxyIps.isEmpty()) {
            String[] ips = otherProxyIps.split(SEPARATOR);
            for (String proxyIp : ips) {
                proxyIp = proxyIp.trim();
                if (!isUnknown(proxyIp)) {
                    return proxyIp;
                }
            }
        }
        return ip;
    }

    /**
     * 获取多级代理的真实 IP
     *
     * @param ip            第一层代理 IP
     * @param otherProxyIps 后续代理 IP 列表（逗号分隔）
     * @return 真实客户端 IP
     */
    public static String getMultistageReverseProxyIp(String ip, String otherProxyIps) {
        if (isUnknown(ip)) {
            return ip;
        }
        // 从后往前遍历，最后一个非 unknown 的 IP 是真实 IP
        if (otherProxyIps != null && !otherProxyIps.isEmpty()) {
            String[] ips = otherProxyIps.split(SEPARATOR);
            for (int i = ips.length - 1; i >= 0; i--) {
                String proxyIp = ips[i].trim();
                if (!isUnknown(proxyIp)) {
                    return proxyIp;
                }
            }
        }
        return ip;
    }

    /**
     * 判断是否是内网 IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isSiteLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
