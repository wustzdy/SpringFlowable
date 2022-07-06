package com.wustzdy.springboot.flowable.demo.util;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class LocalhostInfo implements Serializable {

    private String ip;
    private String mac;

    private double cpuUsage;
    private int processNum;

    private float memTotal;
    private float memUsed;
    private double memUsage;
    private String memUnit = "MB";

    private List<InterfaceInfo> net = new ArrayList<>();

    private double diskUsage;
    private List<DiskInfo> disk = new ArrayList<>();
    private String diskUnit = "MB";

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public int getProcessNum() {
        return processNum;
    }

    public void setProcessNum(int processNum) {
        this.processNum = processNum;
    }

    public float getMemTotal() {
        return memTotal;
    }

    public void setMemTotal(float memTotal) {
        this.memTotal = memTotal;
    }

    public float getMemUsed() {
        return memUsed;
    }

    public void setMemUsed(float memUsed) {
        this.memUsed = memUsed;
    }

    public double getMemUsage() {
        return memUsage;
    }

    public void setMemUsage(double memUsage) {
        this.memUsage = memUsage;
    }

    public List<InterfaceInfo> getNet() {
        return net;
    }

    public void setNet(List<InterfaceInfo> net) {
        this.net = net;
    }

    public double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public List<DiskInfo> getDisk() {
        return disk;
    }

    public void setDisk(List<DiskInfo> disk) {
        this.disk = disk;
    }

    public void autoComputed() {
        if (this.memTotal > 0.0 && this.memUsed > 0.0) {
            this.memUsage = NumKit.percent(this.memUsed / this.memTotal * 100.0);
        }
        if (this.disk != null) {
            long diskTotal = this.disk.stream().map(DiskInfo::getTotal).reduce(0L, Long::sum);
            long diskUsed = this.disk.stream().map(DiskInfo::getUsed).reduce(0L, Long::sum);
            if (diskTotal > 0) {
                this.diskUsage = NumKit.percent((diskUsed * 1.0) / (diskTotal * 1.0) * 100.0);
            }
        }
    }

    public String getMemUnit() {
        return memUnit;
    }

    public void setMemUnit(String memUnit) {
        this.memUnit = memUnit;
    }

    public String getDiskUnit() {
        return diskUnit;
    }

    public void setDiskUnit(String diskUnit) {
        this.diskUnit = diskUnit;
    }

    /**
     * 磁盘信息
     */
    public static class DiskInfo {
        private String name;
        private long total; // M
        private long used; // M
        private double usage;

        public DiskInfo() {
            super();
        }

        public DiskInfo(String name, double usage) {
            this.name = name;
            this.usage = usage;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getUsed() {
            return used;
        }

        public void setUsed(long used) {
            this.used = used;
        }

        public double getUsage() {
            return usage;
        }

        public void setUsage(double usage) {
            this.usage = usage;
        }
    }

    /**
     * 网络接口信息
     */
    public static class InterfaceInfo {
        private String name;
        private String ip;
        private String mask;
        private long txPkts;
        private long rxPkts;
        private long txBytes;
        private long rxBytes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getMask() {
            return mask;
        }

        public void setMask(String mask) {
            this.mask = mask;
        }

        public long getTxPkts() {
            return txPkts;
        }

        public void setTxPkts(long txPkts) {
            this.txPkts = txPkts;
        }

        public long getRxPkts() {
            return rxPkts;
        }

        public void setRxPkts(long rxPkts) {
            this.rxPkts = rxPkts;
        }

        public long getTxBytes() {
            return txBytes;
        }

        public void setTxBytes(long txBytes) {
            this.txBytes = txBytes;
        }

        public long getRxBytes() {
            return rxBytes;
        }

        public void setRxBytes(long rxBytes) {
            this.rxBytes = rxBytes;
        }
    }
}
