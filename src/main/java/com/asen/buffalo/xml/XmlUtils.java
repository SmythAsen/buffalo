package com.asen.buffalo.xml;

import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @description: xml工具类
 * 依赖于{@link XStream}，所以需要了解相关文档
 * @author: Asen
 * @since: 2020/05/21
 */
public class XmlUtils {
    public static <T> T toObject(InputStream xmlInputStream, Class<T> clazz) {
        XStream xstream = new XStream();
        xstream.processAnnotations(clazz);
        xstream.autodetectAnnotations(true);
        xstream.ignoreUnknownElements();
        return (T) xstream.fromXML(xmlInputStream);
    }

    public static <T> T toObject(String xmlFilePath, Class<T> clazz) {
        try (
                InputStream inputStream = new FileInputStream(new File(xmlFilePath));
        ) {
            return toObject(inputStream, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
