package powerbankrental.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//配置文件加载工具类-用于加载项目配置文件  没写过，抄的
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    //默认配置文件地址
    public static final String DEFAULT_CONFIG_FILE = "config.properties";
    //单例实例
    private static ConfigLoader instance;
    //存储配置项的Properties对象
    private Properties properties;

    //加载制定配置文件
    public boolean loadConfig(String  configFile){
        properties = new Properties();
        try(InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)){
            if(input == null){
                logger.error("Config file {} not found", configFile);
                return false;
            }

            properties.load(input);
            logger.info("Successfully loaded config file {}", configFile);
            return true;
        }catch (IOException e){
            logger.error("Failed to load config file {}", configFile, e);
            return false;
        }
    }

    //重新加载配置文件
    public boolean reloadConfig(){
        return loadConfig(DEFAULT_CONFIG_FILE);
    }

    //获取字符串类型的配置项
    public String getProperty(String key){
        String value = properties.getProperty(key);
        if(value == null){
            logger.warn("Property {} not found", key);
        }
        return value;
    }

    //获取字符串类型的配置项-待默认值
    public String getProperty(String key, String defaultValue){
        String value = properties.getProperty(key,  defaultValue);
        if(value == null){
            return  defaultValue;
        }

        try{
            return value;
        }catch (NumberFormatException e){
            logger.warn("Property {} is not an effective integer", key, value, defaultValue);
            return defaultValue;
        }
    }

    //获取整数类型的配置项
    public int getIntProperty(String key, int defaultValue){
        String value = getProperty(key);
        if(value == null){
            return  defaultValue;
        }

        try{
            return Integer.parseInt(value);
        }catch (NumberFormatException e){
            logger.warn("Property {} is not an effective integer", key, value, defaultValue);
            return defaultValue;
        }
    }

    //获取布尔类型的配置项
    public boolean getBooleanProperty(String key, boolean defaultValue){
        String value = getProperty(key);
        if(value == null){
            return  defaultValue;
        }

        return  Boolean.parseBoolean(value);
    }

    //获取所有配置项
    public Properties getAllProperties(){
        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }

    //私有构造函数，加载默认配置文件
    private ConfigLoader() {
        loadConfig(DEFAULT_CONFIG_FILE);
    }

    //获取单例实例
    public static ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }
}
/*写完了才发现好像没什么用*/