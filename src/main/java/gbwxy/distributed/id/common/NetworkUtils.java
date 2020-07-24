package gbwxy.distributed.id.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 描述：
 *
 * @Author wangjun
 * @Date 2020/7/22
 */
public class NetworkUtils {
    private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);

    public NetworkUtils() {
    }

    /**
     * 获取本地IP
     *
     * @return
     */
    public static String getIp() {
        String ip;
        try {
            List<String> ipList = getHostAddress((String) null);
            ip = !ipList.isEmpty() ? (String) ipList.get(0) : "";
        } catch (Exception var2) {
            ip = "";
            logger.warn("ParamValidateUtils get IP warn", var2);
        }

        return ip;
    }

    public static String getIp(String interfaceName) {
        interfaceName = interfaceName.trim();

        String ip;
        try {
            List<String> ipList = getHostAddress(interfaceName);
            ip = !ipList.isEmpty() ? (String) ipList.get(0) : "";
        } catch (Exception var3) {
            ip = "";
            logger.warn("ParamValidateUtils get IP warn", var3);
        }

        return ip;
    }

    private static List<String> getHostAddress(String interfaceName) throws SocketException {
        List<String> ipList = new ArrayList(5);
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
            Enumeration allAddress = ni.getInetAddresses();

            while (allAddress.hasMoreElements()) {
                InetAddress address = (InetAddress) allAddress.nextElement();
                if (!address.isLoopbackAddress() && !(address instanceof Inet6Address)) {
                    String hostAddress = address.getHostAddress();
                    if (null == interfaceName) {
                        ipList.add(hostAddress);
                    } else if (interfaceName.equals(ni.getDisplayName())) {
                        ipList.add(hostAddress);
                    }
                }
            }
        }

        return ipList;
    }
}
