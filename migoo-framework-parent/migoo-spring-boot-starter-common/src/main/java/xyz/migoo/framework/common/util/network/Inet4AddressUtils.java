package xyz.migoo.framework.common.util.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author xiaomi
 * Created on 2022/1/9 16:10
 */
public class Inet4AddressUtils {

    public static String getHostname() {
        InetAddress ip = getInetAddress();
        return ip != null ? ip.getHostName() : "";
    }

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
}
