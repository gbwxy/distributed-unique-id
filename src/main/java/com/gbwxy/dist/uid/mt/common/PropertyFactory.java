package com.gbwxy.dist.uid.mt.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * 描述：
 *
 * @Author wangjun
 * @Date 2020/7/22
 */
public class PropertyFactory {
    private static final Logger logger = LoggerFactory.getLogger(PropertyFactory.class);
    private static final Properties prop = new Properties();

    public PropertyFactory() {
    }

    public static Properties getProperties() {
        return prop;
    }

    static {
        try {
            prop.load(PropertyFactory.class.getClassLoader().getResourceAsStream("id.properties"));
        } catch (IOException var1) {
            logger.warn("Load ID Properties Ex", var1);
        }

    }
}
