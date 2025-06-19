package powerbankrental.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

//���ݿ����ӹ����࣬���𴴽�������͹ر����ݿ�����
public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);

    private static String url;
    private static String username;
    private static String password;

    //��̬����飬�������ʱִ�У���ȡ���ݿ�����   ��һ��д
    static {
        //�������ݿ�����
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            //�������ļ��������ݿ�����
            Properties properties = new Properties();
            InputStream inputStream = DatabaseUtil.class.getClassLoader()
                    .getResourceAsStream("db_config.properties");

            if (inputStream != null) {
                try {
                    properties.load(inputStream);
                } catch (IOException e) {
                    logger.error("Error loading db_config.properties", e);
                    throw new ExceptionInInitializerError("Error loading db_config.properties");
                }

                url = properties.getProperty("url");
                username = properties.getProperty("username");
                password = properties.getProperty("password");

                logger.info("Database config loaded successfully");
            }else {
                logger.error("Database config file not found!");
                throw new ExceptionInInitializerError("Database config file not found!");
            }
        } catch (ClassNotFoundException e) {
            logger.error("Driver class not found!", e);
            throw new ExceptionInInitializerError("Driver class not found!");
        }
    }

    //��ȡ���ݿ�����
    public static Connection getConnection() throws SQLException{
        try{
            Connection connection = DriverManager.getConnection(url,username,password);
            logger.debug("Database connection established successfully");
            return connection;
        }catch (SQLException e){
            logger.error("Database connection establishment failed!", e);
            throw e;
        }
    }

    //�ر����ݿ�
    public static void closeConnection(Connection connection
            , PreparedStatement preparedStatement, ResultSet resultSet){
        try{
            if(resultSet != null){
                resultSet.close();
            }
        }catch (SQLException e){
            logger.error("Error closing resultset!", e);
        }

        try{
            if(preparedStatement != null){}
            preparedStatement.close();
        }catch (SQLException e){
            logger.error("Error closing prepared statement!", e);
        }

        try{
            if(connection != null){
                connection.close();
                logger.debug("Database connection closed successfully");
            }
        } catch (SQLException e) {
            logger.error("Error closing connection!", e);
        }
    }
    public static void closeConnection(Connection connection){
        closeConnection(connection, null, null);
    }
    public static void closeConnection(Connection connection, PreparedStatement preparedStatement){
        closeConnection(connection, preparedStatement, null);
    }
    public static void closeConnection(ResultSet resultSet){
        closeConnection(null, null, resultSet);
    }
    public static void closeConnection(PreparedStatement preparedStatement){
        closeConnection(null, preparedStatement, null);
    }
}
