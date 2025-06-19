package powerbankrental.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

//数据库连接工具类，负责创建、管理和关闭数据库连接
public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);

    private static String url;
    private static String username;
    private static String password;

    //静态代码块，在类加载时执行，读取数据库配置   第一次写
    static {
        //加载数据库驱动
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            //从配置文件加载数据库配置
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

    //获取数据库连接
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

    //关闭数据库
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
