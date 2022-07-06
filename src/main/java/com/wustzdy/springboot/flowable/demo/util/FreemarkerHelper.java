package com.wustzdy.springboot.flowable.demo.util;


import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.Map;


public class FreemarkerHelper {

    private static final Logger logger = LoggerFactory.getLogger(FreemarkerHelper.class);

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String SUFFIX = ".ftl";

    private static Configuration configuration = null;

    private static boolean initOK = false;

    /**
     * 初始化freemarker
     */
    public static void init(String templateDirPath) {
        if (!FileKit.isDirExist(templateDirPath)) {
            throw new IllegalArgumentException("Freemarker templateDirPath is invalid!");
        }
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDefaultEncoding(DEFAULT_ENCODING);
        try {
            configuration.setDirectoryForTemplateLoading(new File(templateDirPath));
            initOK = true;
        } catch (IOException e) {
            initOK = false;
            throw new RuntimeException("Freemarker templateDirPath is invalid!");
        }
    }

    /**
     * 生成Html文件
     */
    public static void process(String templateFileName, String dstFilePath, Map<String, Object> dataMap)
            throws IOException, TemplateException {
        if (!initOK) {
            throw new IllegalStateException("Freemarker config is not ok!");
        }
        OutputStream os = null;
        Writer writer = null;
        try {
            FileKit.ensureFileExist(dstFilePath);
            os = new FileOutputStream(dstFilePath);
            writer = new OutputStreamWriter(os, DEFAULT_ENCODING);
            Template template = configuration.getTemplate(templateFileName.endsWith(SUFFIX) ? templateFileName : templateFileName + SUFFIX);
            template.process(dataMap, writer);
            writer.flush();
        } catch (IOException | TemplateException e) {
            logger.error(e.getMessage());
            throw e;
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(os);
        }

    }

    /**
     * 生成String
     */
    public static String process(String templateFileName, Map<String, Object> dataMap)
            throws IOException, TemplateException {
        if (!initOK) {
            throw new IllegalStateException("Freemarker config is not ok!");
        }
        Template template = configuration.getTemplate(templateFileName.endsWith(SUFFIX) ? templateFileName : templateFileName + SUFFIX);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, dataMap);
    }

    /**
     * 生成String
     */
    public static String processTemplate(String templateFileName, String templateValue, Map<String, Object> dataMap)
            throws IOException, TemplateException {

        Template template = new Template(templateFileName, templateValue, configuration);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, dataMap);
    }
}
