package powerbankrental.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import powerbankrental.dao.UserDAO;
import powerbankrental.model.User;
import powerbankrental.model.enums.Role;
import powerbankrental.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//用户数据访问接口实现类-负责用户相关数据库操作
public class UserDAOImpl implements UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    //添加新用户到数据库
    @Override
    public long addUser(User user) {
        //声明数据库连接，预处理语句和结果集对象
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "INSERT INTO users (username, password_hash, password_salt," +
                    " role, is_active, created_at, last_login_time)" + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            //创造预处理语句，并制定返回需要返回自动生成的主键
            statement = connection.prepareStatement(sql,  Statement.RETURN_GENERATED_KEYS);
            /*Statement.RETURN_GENERATED_KEYS 参数告诉JDBC驱动：
            在执行插入操作后，需要返回数据库自动生成的主键值
                    这对于使用自增主键的表非常重要*/

            //设置预处理语句和参数值
            /*statement.setLong(1, user.getUserId());
            如果使用数据库自增ID，那么你应该：
                删除 statement.setLong(1, user.getUserId())
                修改SQL语句，移除 userId 字段
                保留获取自动生成键的代码*/
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getPasswordSalt());
            statement.setString(4, user.getRole().name());
            statement.setBoolean(5, user.isActive());
            statement.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt())//在构造方法初始化了creatAT，可能多此一举
                    != null ? Timestamp.valueOf(user.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
            statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            //执行插入操作，获取影响的行数
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                logger.warn("addUser() failed. No rows affected.");
                return -1;
            }

            //获取自动生成的用户ID
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                long user_id = resultSet.getInt(1);//获取数据库自动生成的主键值。
                // 参数1表示结果集的第一列，即自增的ID值，而不是指用户ID为1。
                user.setUserId(Long.valueOf(user_id));//设置自增ID为userId
                logger.info("addUser() successful with id " + user_id);
                return user_id;
            }else {
                logger.warn("addUser() failed. No rows affected.");
                return -1;
            }
        }catch(SQLException e){
            logger.warn("Adding user failed.", e);
            return -1;
        }finally {
            //用完记得关闭资源
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //在事务中添加用户（使用提供的链接）
    @Override
    public int addUserInTransaction(User user, Connection connection){
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            //使用提供的链接，不从连接池获取新链接
            //该连接由调用方负责开启事务和提交/回滚

            String sql = "INSERT INTO users (username, password_hash, password_salt, role, is_active, created_at)"
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getPasswordSalt());
            statement.setString(4, user.getRole().name());
            statement.setBoolean(5, user.isActive());
            statement.setTimestamp(6, user.getCreatedAt() != null ?
                            Timestamp.valueOf(user.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                logger.warn("addUserInTransaction() failed. No rows affected.");
                return -1;
            }

            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                int userId = resultSet.getInt(1);
                logger.info("addUserInTransaction() successful with id " + userId);
                return userId;
            }else {
                logger.warn("addUserInTransaction() failed. Can not get id of row.");
                return -1;
            }
        }catch (SQLException e){
            logger.warn("Adding user failed.SQL error", e);
            return -1;
        }finally {
            //只关闭Statement和ResultSet ，不关闭Connecttion（由调用者管理）- 应该是还在使用中
            if (resultSet != null) {
                DatabaseUtil.closeConnection(resultSet);
            }
            if (statement != null) {
                DatabaseUtil.closeConnection(statement);
            }
        }
    }

    //根据用户ID查询用户信息
    @Override
    public User getUserById(long userId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM users WHERE user_id = ?";
            //设置参数
            statement = connection.prepareStatement(sql);
            statement.setLong(1, userId);//1 表示第一个占位符（？）的位置，将userId作为？的值

            //执行查询-执行一条SQL查询语句（通常是SELECT语句），并获取查询结果。返回ResuSet结果集
            resultSet = statement.executeQuery();

            //如果查到了就输出User
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);//使用辅助方法
            }else  {
                logger.warn("getUserById() failed. No rows affected.");
                return null;
            }
        }catch(SQLException e){
            logger.warn("getUserById() failed.", e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //根据用户名查询用户信息
    @Override
    public User getUserByName(String userName) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM users WHERE username = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, userName);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);
            }else  {
                logger.warn("getUserByUserName() failed. No user found.");
                return null;
            }
        }catch (SQLException e){
            logger.warn("getUserByUserName() failed.", e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //更新用户信息（不包括密码）
    @Override
    public boolean updateUser(User user) {
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "UPDATE users SET username = ?, role = ?, is_active = ? WHERE user_id = ?";
            statement  = connection.prepareStatement(sql);

            statement.setString(1, user.getUserName());
            statement.setString(2, user.getRole().name());
            statement.setBoolean(3, user.isActive());
            statement.setLong(4, user.getUserId());

            //执行更新操作
            int affectedRows = statement.executeUpdate();

            //判断是否更新成功
            if (affectedRows > 0){
                logger.info("updateUser() successful with id " + user.getUserId());
                return true;
            }else  {
                logger.warn("updateUser() failed. No rows affected.");
                return false;
            }
        }catch (SQLException e){
            logger.warn("updateUser() failed.", e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, null);
        }
    }

    //更新用户密码
    @Override
    public boolean updatePassword(long userId, String newPasswordHash, String newSalt){
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();

            String sql = "UPDATE users SET password_hash = ?, password_salt = ? WHERE user_id = ?";
            statement = connection.prepareStatement(sql);

            statement.setString(1, newPasswordHash);
            statement.setString(2, newSalt);
            statement.setLong(3, userId);

            //执行更新
            int affecteRows = statement.executeUpdate();

            if (affecteRows > 0){
                logger.info("updataPassword() successful with id " + userId);
                return true;
            }else   {
                logger.warn("updataPassword() failed. No rows affected.");
                return false;
            }
        }catch (SQLException e){
            logger.warn("updataPassword() failed.", e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, null);
        }
    }

    //更新用户状态（激活或禁用）
    @Override
    public boolean updateUserStatus(long userId, boolean isActive){
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
            statement = connection.prepareStatement(sql);

            statement.setBoolean(1, isActive);
            statement.setLong(2, userId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0){
                logger.info("updataUserStatus() successful with id " + userId);
                return true;
            }else   {
                logger.warn("updataUserStatus() failed. No rows affected.");
                return false;
            }
        }catch (SQLException e){
            logger.warn("updataUserStatus() failed.", e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, null);
        }
    }

    //删除用户
    @Override
    public boolean deleteUser(long userId) {
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "DELETE FROM users WHERE user_id = ?";
            statement = connection.prepareStatement(sql);

            statement.setLong(1, userId);

            //执行删除
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0){
                logger.info("deleteUser() successful with id " + userId);
                return true;
            }else{
                logger.warn("deleteUser() failed. No rows affected.");
                return false;
            }
        }catch (SQLException e){
            logger.warn("deleteUser() failed.", e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, null);
        }
    }

    //查询所有用户
    @Override
    public List<User> getAllUsers() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        //创造列表存储查询结果
        List<User> users = new ArrayList<>();

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM users";
            statement = connection.prepareStatement(sql);

            //执行sql语句
            resultSet = statement.executeQuery();
            while (resultSet.next()){
                User user  = extractUserFromResultSet(resultSet);
                users.add(user);
            }
            logger.info("getAllUsers() successful, the number of rows affected: " + users.size());
            return users;
        }catch (SQLException e){
            logger.warn("getAllUsers() failed.", e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //按用户角色查询
    @Override
    public List<User> getUsersByRole(Role role){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<User> users = new ArrayList<>();

        try {
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM users WHERE role = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, role.name());

            resultSet = statement.executeQuery();

            while (resultSet.next()){
                User user = extractUserFromResultSet(resultSet);
                users.add(user);
            }

            logger.info("getUsersByRole() successful, the number of rows affected of {}: " + users.size(), role.name());
            return users;
        }catch (SQLException e){
            logger.warn("getUsersByRole() failed.", e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //更新用户最后登入时间为当前时间
    @Override
    public boolean updateLastLoginTime(long userId){
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();
            String sql = "UPDATE users SET last_login_time = ? WHERE user_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(2, userId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0){
                logger.info("updateLastLoginTime() successful with id " + userId);
                return true;
            }else {
                logger.warn("updateLastLoginTime() failed. No rows affected.");
                return false;
            }
        }catch (SQLException e){
            logger.warn("updateLastLoginTime() failed.", e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, null);
        }
    }

    //检查用户名是否存在
    @Override
    public boolean isUsernameExist(String username){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = DatabaseUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            /*String sql = "SELECT * FROM users WHERE username = ?";
            *//*    SQL语句是SELECT * FROM users WHERE username = ?，这会返回完整用户记录
            第一列可能是user_id，判断它是否大于0不是正确的判断用户名存在与否的方法*/
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);

            resultSet = statement.executeQuery();

            if (resultSet.next()){
                return resultSet.getInt(1) > 0;
            }

            return false;
        }catch (SQLException e){
            logger.warn("isUsernameExists() failed.", e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //辅助方法-从结果集中提取用户数据
    private User extractUserFromResultSet(ResultSet resultSet) throws SQLException{
        //从ResultSet中提取User对象
        User user = new User();

        //从结果集中读取数据填充到用户对象中
        user.setUserId(resultSet.getLong("user_id"));
        user.setUserName(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setPasswordSalt(resultSet.getString("password_salt"));
        user.setRole(Role.valueOf(resultSet.getString("role")));
        user.setActive(resultSet.getBoolean("is_active"));

        //处理可能为null的日期时间-如果user构造函数处理的好好像不用写
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if(createdAt != null){
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        //同上，最后登入时间的
        Timestamp lastLoginTime = resultSet.getTimestamp("last_login_time");
        if(lastLoginTime != null){
            user.setLastLoginTime(lastLoginTime.toLocalDateTime());
        }

        return user;
    }
}
