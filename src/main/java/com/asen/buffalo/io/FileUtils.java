package com.asen.buffalo.io;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @description: 文件工具类
 * @author: Asen
 * @create: 2019/10/29
 */
@Slf4j
public class FileUtils {

    public static String readFile(String filePath) {
        try {
            return readFile(new FileInputStream(new File(filePath)));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String readFile(InputStream inputStream) {
        try {
            List<String> lines = IOUtils.readLines(inputStream, Charset.defaultCharset().name());
            StringBuilder builder = new StringBuilder();
            lines.forEach(line -> builder.append(line).append("\n"));
            return builder.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
