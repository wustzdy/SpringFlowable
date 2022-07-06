package com.wustzdy.springboot.flowable.demo.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * ResourceKit
 */
public class ResourceKit {

    private static final Logger logger = LoggerFactory.getLogger(ResourceKit.class);

    /**
     * 读取资源文件内容
     *
     * @param resourcePath resourcePath
     * @return String
     */
    public static String readFileToString(String resourcePath) {
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            return new String(IOUtils.readFully(in, in.available()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Load resource conf {} failed! Error is: {}!", resourcePath, e.getMessage());
            return "";
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * 提取资源文件
     *
     * @param resourcePath resourcePath
     * @param diskPath     distPath
     * @return boolean
     */
    public static boolean readFileToDisk(String resourcePath, String diskPath) {
        if (StringUtils.isBlank(resourcePath) || StringUtils.isBlank(diskPath)) {
            logger.error("Path parameter is invalid!");
            return false;
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            // check file
            diskPath = new File(diskPath).getCanonicalPath();
            FileKit.ensureDirExist(diskPath.substring(0, diskPath.lastIndexOf(File.separatorChar)));

            File target = new File(diskPath);
            if (target.exists()) {
                logger.info("File {} already exist, will skip...", diskPath);
                return true;
            } else {
                if (!target.createNewFile()) {
                    logger.error("Create file failed {}!", diskPath);
                }
            }

            // copy file
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            out = new FileOutputStream(diskPath);
            IOUtils.copy(in, out);

            return true;
        } catch (Exception e) {
            logger.error("extractFileToDisk {} is failed! {}", resourcePath, e.getMessage());
            return false;
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }
}
