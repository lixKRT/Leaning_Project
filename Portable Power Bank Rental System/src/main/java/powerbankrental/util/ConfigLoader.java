package powerbankrental.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//�����ļ����ع�����-���ڼ�����Ŀ�����ļ�  ûд��������
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    //Ĭ�������ļ���ַ
    public static final String DEFAULT_CONFIG_FILE = "config.properties";
    //����ʵ��
    private static ConfigLoader instance;
    //�洢�������Properties����
    private Properties properties;

    //�����ƶ������ļ�
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

    //���¼��������ļ�
    public boolean reloadConfig(){
        return loadConfig(DEFAULT_CONFIG_FILE);
    }

    //��ȡ�ַ������͵�������
    public String getProperty(String key){
        String value = properties.getProperty(key);
        if(value == null){
            logger.warn("Property {} not found", key);
        }
        return value;
    }

    //��ȡ�ַ������͵�������-��Ĭ��ֵ
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

    //��ȡ�������͵�������
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

    //��ȡ�������͵�������
    public boolean getBooleanProperty(String key, boolean defaultValue){
        String value = getProperty(key);
        if(value == null){
            return  defaultValue;
        }

        return  Boolean.parseBoolean(value);
    }

    //��ȡ����������
    public Properties getAllProperties(){
        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }

    //˽�й��캯��������Ĭ�������ļ�
    private ConfigLoader() {
        loadConfig(DEFAULT_CONFIG_FILE);
    }

    //��ȡ����ʵ��
    public static ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }
}
/*д���˲ŷ��ֺ���ûʲô��*/