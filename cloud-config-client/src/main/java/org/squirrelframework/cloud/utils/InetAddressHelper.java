package org.squirrelframework.cloud.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by kailianghe on 15/12/11.
 */
public abstract class InetAddressHelper {

    private static final Logger logger = LoggerFactory.getLogger(InetAddressHelper.class);

    public static final List<String> localIpAddress;
    static {
        List<String> tmp = Lists.newArrayList();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if(inetAddress instanceof Inet4Address) {
                        tmp.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("Cannot get local machine ip address");
        }
        localIpAddress = Collections.unmodifiableList(tmp);
    }

    public static long inetAddressToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    public static boolean isIpAddressInRange(InetAddress ipStart, InetAddress ipEnd, InetAddress ipToCheck) {
        long ipLo = inetAddressToLong(ipStart);
        long ipHi = inetAddressToLong(ipEnd);
        long ipToTest = inetAddressToLong(ipToCheck);
        return (ipToTest >= ipLo && ipToTest <= ipHi);
    }

    public static boolean isIpAddressInRange(String ipStart, String ipEnd, String ipToCheck) {
        try {
            InetAddress ipLo = InetAddress.getByName(ipStart);
            InetAddress ipHi = InetAddress.getByName(ipEnd);
            InetAddress ipToTest = InetAddress.getByName(ipToCheck);
            return isIpAddressInRange(ipLo, ipHi, ipToTest);
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static boolean isLocalMachineIpAddressInRange(String ipRange) {
        String[] ips = StringUtils.tokenizeToStringArray(ipRange, "-:");
        final String ipStart;
        final String ipEnd;
        if (ips.length == 1) {
            ipStart = ipEnd = ips[0].trim();
        } else if(ips.length == 2) {
            ipStart = ips[0].trim();
            ipEnd = ipStart.substring(0, ipStart.lastIndexOf('.')+1)+ips[1].trim();
        } else {
            return false;
        }
        return isLocalMachineIpAddressInRange(ipStart, ipEnd);
    }

    public static boolean isLocalMachineIpAddressInRange(final String ipStart, final String ipEnd) {
        return Iterables.any(localIpAddress, new Predicate<String>() {
            @Override
            public boolean apply(String ipToCheck) {
                return isIpAddressInRange(ipStart, ipEnd, ipToCheck);
            }
        });
    }
}
