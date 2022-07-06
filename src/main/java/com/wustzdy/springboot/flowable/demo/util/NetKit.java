package com.wustzdy.springboot.flowable.demo.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * NetKit
 */
public class NetKit {

    private static final Logger logger = LoggerFactory.getLogger(NetKit.class);

    private static final String LOCAL_IP = "127.0.0.1";
    private static final String ANY_IP = "0.0.0.0";
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
    private static InetAddress LOCAL_ADDRESS = null;

    /**
     * 判断是否为本地IP
     *
     * @param ip ip
     * @return boolean
     */
    public static boolean isLocalIp(String ip) {
        return ip != null && (LOCAL_IP.equals(ip) || ip.equals(getLocalIp()));
    }

    /**
     * 判断是否为任意IP
     *
     * @param ip ip
     * @return boolean
     */
    public static boolean isAnyIp(String ip) {
        return ANY_IP.equals(ip);
    }

    /**
     * 判断IP是否可达
     *
     * @param ipAddress ipAddress
     * @return boolean
     */
    public static boolean ping(String ipAddress) {
        return ping(ipAddress, 3000);
    }

    /**
     * 判断IP是否可达
     *
     * @param ipAddress     ipAddress
     * @param timeoutSecond timeoutSecond
     * @return boolean
     */
    public static boolean ping(String ipAddress, int timeoutSecond) {
        if (ipAddress == null) return false;
        if (timeoutSecond < 0 || timeoutSecond > 10) {
            timeoutSecond = 3;
        }
//        try {
//            if (EnvKit.isWin()) {
//                return 0 == Runtime.getRuntime().exec("ping -n 1 -w " + timeoutSecond * 1000 + ' ' + ipAddress).waitFor();
//            } else {
//                return 0 == Runtime.getRuntime().exec("ping -c 1 -W " + timeoutSecond + ' ' + ipAddress).waitFor();
//            }
//        } catch (InterruptedException | IOException e) {
//            return false;
//        }
        try {
            return InetAddress.getByName(ipAddress).isReachable(timeoutSecond * 1000);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断端口是否可达
     *
     * @param ip   ip
     * @param port port
     * @return boolean
     */
    public static boolean isPortReachable(String ip, int port) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port));
        } catch (IOException e) {
            return false;
        } finally {
            IOUtils.closeQuietly(socket);
        }
        return true;
    }

    /**
     * 根据host获取IP地址
     *
     * @param host host
     * @return ip address or hostName if UnknownHostException
     */
    public static String getIpByHost(String host) {
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            return host;
        }
    }

    /**
     * 获取本地主机有效IP地址
     *
     * @return InetAddress
     */
    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS == null) {
            LOCAL_ADDRESS = getLocalAddress0();
        }
        return LOCAL_ADDRESS;
    }

    /**
     * 获取本地主机IP
     *
     * @return String
     */
    public static String getLocalIp() {
        InetAddress address = getLocalAddress();
        if (address != null) {
            return address.getHostAddress();
        }
        return LOCAL_IP;
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            try {
                                InetAddress address = addresses.nextElement();
                                if (isValidAddress(address)) {
                                    return address;
                                }
                            } catch (Throwable e) {
                                logger.error(e.getMessage());
                            }
                        }
                    } catch (Throwable e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }
        return localAddress;
    }

    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostAddress();
        return name != null
                && !ANY_IP.equals(name)
                && !LOCAL_IP.equals(name)
                && IP_PATTERN.matcher(name).matches();
    }

    public static void main(String[] args) {
        System.out.println(ping("192.168.33.1", 1000));
    }
}
