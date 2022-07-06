package com.wustzdy.springboot.flowable.demo.util;

import com.sun.istack.internal.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemMap;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Sigar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class FileKit {


    /**
     * 确保目录存在，若不存在则自动创建
     *
     * @param path path
     * @return boolean
     */
    public static boolean ensureDirExist(String path) {
        if (isDirExist(path)) {
            return true;
        } else {
            File file = new File(path);
            return file.mkdirs();
        }
    }

    /**
     * 确保文件存在，若不存在则自动创建
     *
     * @param path path
     * @return boolean
     */
    public static boolean ensureFileExist(String path) {
        if (isFileExist(path)) {
            return true;
        } else {
            try {
                File file = new File(path);
                FileUtils.forceMkdir(file.getParentFile());
                return file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * 是否为文件
     *
     * @param file File
     * @return boolean
     */
    public static boolean isFile(File file) {
        return file != null && file.exists() && file.isFile();
    }

    /**
     * 是否为目录
     *
     * @param file File
     * @return boolean
     */
    public static boolean isDir(File file) {
        return file != null && file.exists() && file.isDirectory();
    }

    /**
     * 检查文件是否存在
     *
     * @param path path
     * @return boolean
     */
    public static boolean isFileExist(String path) {
        if (StringUtils.isBlank(path)) return false;
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    /**
     * 检查目录是否存在
     *
     * @param path path
     * @return boolean
     */
    public static boolean isDirExist(String path) {
        if (StringUtils.isBlank(path)) return false;
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    /**
     * 合并路径
     *
     * @param paths paths
     * @return String
     */
    public static String join(String... paths) {
        if (paths == null) return "";
        return StringUtils.join(paths, File.separator);
    }

    /**
     * 复制文件
     *
     * @param srcPath srcPath
     * @param dstPath dstPath
     */
    public static void copy(String srcPath, String dstPath) {
        try {
            FileUtils.copyFile(new File(srcPath), new File(dstPath));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath filePath
     */
    public static void deleteFile(String filePath) {
        if (FileKit.isFileExist(filePath)) {
            FileUtils.deleteQuietly(new File(filePath));
        }
    }

    /**
     * 删除目录
     *
     * @param dirPath dirPath
     */
    public static void deleteDir(String dirPath) {
        if (FileKit.isDirExist(dirPath)) {
            FileUtils.deleteQuietly(new File(dirPath));
        }
    }

    /**
     * 获取绝对路径
     *
     * @param path path
     * @return String
     */
    public static String absolutePath(String path) {
        try {
            return new File(path).getAbsolutePath();
        } catch (Exception e) {
            return path;
        }
    }

    /**
     * 获取文件所在文件夹路径
     *
     * @param path path
     * @return String
     */
    public static String getDirPath(String path) {
        try {
            File file = new File(path);
            if (file.isDirectory()) {
                return file.getCanonicalPath();
            } else if (file.isFile()) {
                String filePath = file.getCanonicalPath();
                int pos = filePath.lastIndexOf(File.separatorChar);
                return filePath.substring(0, pos);
            } else {
                return file.getCanonicalPath();
            }
        } catch (Exception e) {
            return path;
        }
    }

    /**
     * 获取文件或目录所在磁盘分区的路径。
     *
     * @param dirPath dirPath
     * @return String
     */
    public static String getRoot(String dirPath) {
        if (StringUtils.isEmpty(dirPath)) {
            return null;
        }
        try {
            Sigar sigar = SigarUtil.getSigar();
            FileSystemMap fsMap = sigar.getFileSystemMap();

            File file = new File(dirPath).getCanonicalFile();
            while (file != null && file.getCanonicalPath().length() > 0) {
                String path = file.getCanonicalPath();
                if (fsMap.containsKey(path)) {
                    return path;
                }
                if (fsMap.containsKey(path.toUpperCase())) {
                    return path.toUpperCase();
                }
                file = file.getParentFile();
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取磁盘某分区使用率，当分区不存在时返回负数
     *
     * @param rootPath the root path
     * @return double
     */
    public static double getRootUsage(String rootPath) {
        if (StringUtils.isEmpty(rootPath)) {
            return -1;
        }
        try {
            Sigar sigar = SigarUtil.getSigar();
            FileSystemMap fsMap = sigar.getFileSystemMap();
            if (fsMap.containsKey(rootPath)) {
                FileSystem fs = fsMap.getFileSystem(rootPath);
                FileSystemUsage fileSystemUsage = sigar.getFileSystemUsage(fs.getDirName());
                long total = fileSystemUsage.getTotal();
                long used = fileSystemUsage.getUsed();
                return NumKit.percent(used, total) * 100;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return -1;
        }
        return -1;
    }

    /**
     * 打印磁盘分区情况
     */
    public static void printRoots() {
        Sigar sigar = SigarUtil.getSigar();
        try {
            FileSystem[] fileSystems = sigar.getFileSystemList();
            for (FileSystem fs : fileSystems) {
                System.out.println(fs.getDevName() + "~" + fs.getDirName());
                try {
                    FileSystemUsage fileSystemUsage = sigar.getFileSystemUsage(fs.getDirName());
                    long total = fileSystemUsage.getTotal();
                    long used = fileSystemUsage.getUsed();
                    System.out.println(used + "/" + total + "=" + NumKit.percent(used, total) * 100 + "%");
                    System.out.println();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 读取所有字节数据
     *
     * @param file file
     * @return byte[]
     */
    public static byte[] readBytes(File file) {
        if (file == null) return null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            IOUtils.readFully(in, bytes);
            return bytes;
        } catch (IOException e) {
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
