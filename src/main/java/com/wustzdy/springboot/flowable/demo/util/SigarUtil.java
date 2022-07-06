package com.wustzdy.springboot.flowable.demo.util;


import com.wustzdy.springboot.flowable.demo.constant.Constant;
import org.hyperic.sigar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SigarUtil
 */
public class SigarUtil {

    private static final Logger logger = LoggerFactory.getLogger(SigarUtil.class);

    private static final Sigar sigar;

    static {
        SigarInit.init();
        sigar = new Sigar();
    }

    public static Sigar getSigar() {
        return sigar;
    }

    public static LocalhostInfo getLocalhostInfo() {
        final LocalhostInfo localhost = new LocalhostInfo();
        try {
            // ip
            localhost.setIp(NetKit.getLocalAddress() != null ? NetKit.getLocalAddress().getHostAddress() : "127.0.0.1");

            // cpu
            localhost.setCpuUsage(NumKit.percent(sigar.getCpuPerc().getUser() * 100.0));
            localhost.setProcessNum(sigar.getProcList().length);

            // mem
            localhost.setMemTotal(sigar.getMem().getTotal() / (1024 * 1024));
            localhost.setMemUsed(sigar.getMem().getUsed() / (1024 * 1024));

            // file
            FileSystem[] fsList = sigar.getFileSystemList();
            if (fsList != null && fsList.length > 0) {
                List<LocalhostInfo.DiskInfo> list = Arrays.stream(fsList)
                        .filter(fs -> fs.getType() == FileSystem.TYPE_LOCAL_DISK)
                        .map(fs -> {
                            try {
                                FileSystemUsage usage = sigar.getFileSystemUsage(fs.getDirName());
                                final LocalhostInfo.DiskInfo diskInfo = new LocalhostInfo.DiskInfo();
                                diskInfo.setName(fs.getDevName());
                                diskInfo.setTotal(usage.getTotal() / 1024);
                                diskInfo.setUsed(usage.getUsed() / 1024);
                                diskInfo.setUsage(NumKit.percent(usage.getUsePercent() * 100.0));
                                return diskInfo;
                            } catch (Exception e) {
                                logger.error(e.getMessage());
                                return null;
                            }

                        }).collect(Collectors.toList());
                localhost.setDisk(list);
            }
            // net
            String ifNames[] = sigar.getNetInterfaceList();
            if (ifNames != null && ifNames.length > 0) {
                List<LocalhostInfo.InterfaceInfo> list = new ArrayList<>();
                for (String ifName : ifNames) {
                    NetInterfaceConfig ifConfig = sigar.getNetInterfaceConfig(ifName);
                    if ((ifConfig.getFlags() & 1L) <= 0L || !ifConfig.getAddress().equals(NetKit.getLocalAddress().getHostAddress())) {
                        continue;
                    }
                    NetInterfaceStat ifStat = sigar.getNetInterfaceStat(ifName);

                    final LocalhostInfo.InterfaceInfo ifInfo = new LocalhostInfo.InterfaceInfo();
                    ifInfo.setName(ifName);
                    ifInfo.setIp(ifConfig.getAddress());
                    ifInfo.setMask(ifConfig.getNetmask());
                    ifInfo.setTxBytes(ifStat.getTxBytes());
                    ifInfo.setRxBytes(ifStat.getRxBytes());
                    ifInfo.setTxPkts(ifStat.getTxPackets());
                    ifInfo.setRxPkts(ifStat.getRxPackets());

                    list.add(ifInfo);
                }
                localhost.setNet(list);
            }

            // 计算使用率
            localhost.autoComputed();


        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return localhost;
    }

    /**
     * 获取MAC地址
     *
     * @return mac string
     */
    public static String getMAC() {
        try {
            String[] ifs = sigar.getNetInterfaceList();
            for (String if0 : ifs) {
                NetInterfaceConfig cfg = sigar.getNetInterfaceConfig(if0);
                if (NetFlags.LOOPBACK_ADDRESS.equals(cfg.getAddress()) || (cfg.getFlags() & NetFlags.IFF_LOOPBACK) != 0
                        || NetFlags.NULL_HWADDR.equals(cfg.getHwaddr())) {
                    continue;
                }
                return cfg.getHwaddr();
            }
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * SigarInit: 内部初始化类
     */
    private static class SigarInit {

        private static final String ConfPath = "libs/";
        private static final String LibPath = "./" + Constant.BASE_RESOURCE_NAME + "/ext/";

        private static void init() {
            // To mk dirs
            FileKit.ensureDirExist(LibPath);

            // Extract file
            final String[] fileArray = new String[]{"libsigar-amd64-linux.so", "libsigar-x86-linux.so",
                    "sigar-amd64-winnt.dll", "sigar-x86-winnt.dll"};
            for (String file : fileArray) {
                String from = ConfPath + file;
                String to = LibPath + file;
                ResourceKit.readFileToDisk(from, to);
            }

            // Add to java lib path
            String javaLibPath = System.getProperty("java.library.path");
            String libPath = new File(LibPath).getAbsolutePath();
            if (!javaLibPath.contains(libPath)) {
                javaLibPath += (EnvKit.isWin() ? ";" : ":") + libPath;
                System.setProperty("java.library.path", javaLibPath);
            }
        }
    }
}
