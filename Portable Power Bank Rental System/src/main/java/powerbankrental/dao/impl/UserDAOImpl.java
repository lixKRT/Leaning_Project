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

//�û����ݷ��ʽӿ�ʵ����-�����û�������ݿ����
public class UserDAOImpl implements UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    //������û������ݿ�
    @Override
    public long addUser(User user) {
        //�������ݿ����ӣ�Ԥ�������ͽ��������
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "INSERT INTO users (username, password_hash, password_salt," +
                    " role, is_active, created_at, last_login_time)" + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            //����Ԥ������䣬���ƶ�������Ҫ�����Զ����ɵ�����
            statement = connection.prepareStatement(sql,  Statement.RETURN_GENERATED_KEYS);
            /*Statement.RETURN_GENERATED_KEYS ��������JDBC������
            ��ִ�в����������Ҫ�������ݿ��Զ����ɵ�����ֵ
                    �����ʹ�����������ı�ǳ���Ҫ*/

            //����Ԥ�������Ͳ���ֵ
            /*statement.setLong(1, user.getUserId());
            ���ʹ�����ݿ�����ID����ô��Ӧ�ã�
                ɾ�� statement.setLong(1, user.getUserId())
                �޸�SQL��䣬�Ƴ� userId �ֶ�
                ������ȡ�Զ����ɼ��Ĵ���*/
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getPasswordSalt());
            statement.setString(4, user.getRole().name());
            statement.setBoolean(5, user.isActive());
            statement.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt())//�ڹ��췽����ʼ����creatAT�����ܶ��һ��
                    != null ? Timestamp.valueOf(user.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
            statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            //ִ�в����������ȡӰ�������
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                logger.warn("addUser() failed. No rows affected.");
                return -1;
            }

            //��ȡ�Զ����ɵ��û�ID
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                long user_id = resultSet.getInt(1);//��ȡ���ݿ��Զ����ɵ�����ֵ��
                // ����1��ʾ������ĵ�һ�У���������IDֵ��������ָ�û�IDΪ1��
                user.setUserId(Long.valueOf(user_id));//��������IDΪuserId
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
            //����ǵùر���Դ
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //������������û���ʹ���ṩ�����ӣ�
    @Override
    public int addUserInTransaction(User user, Connection connection){
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            //ʹ���ṩ�����ӣ��������ӳػ�ȡ������
            //�������ɵ��÷�������������ύ/�ع�

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
            //ֻ�ر�Statement��ResultSet �����ر�Connecttion���ɵ����߹���- Ӧ���ǻ���ʹ����
            if (resultSet != null) {
                DatabaseUtil.closeConnection(resultSet);
            }
            if (statement != null) {
                DatabaseUtil.closeConnection(statement);
            }
        }
    }

    //�����û�ID��ѯ�û���Ϣ
    @Override
    public User getUserById(long userId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM users WHERE user_id = ?";
            //���ò���
            statement = connection.prepareStatement(sql);
            statement.setLong(1, userId);//1 ��ʾ��һ��ռλ����������λ�ã���userId��Ϊ����ֵ

            //ִ�в�ѯ-ִ��һ��SQL��ѯ��䣨ͨ����SELECT��䣩������ȡ��ѯ���������ResuSet�����
            resultSet = statement.executeQuery();

            //����鵽�˾����User
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);//ʹ�ø�������
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

    //�����û�����ѯ�û���Ϣ
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

    //�����û���Ϣ�����������룩
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

            //ִ�и��²���
            int affectedRows = statement.executeUpdate();

            //�ж��Ƿ���³ɹ�
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

    //�����û�����
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

            //ִ�и���
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

    //�����û�״̬���������ã�
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

    //ɾ���û�
    @Override
    public boolean deleteUser(long userId) {
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "DELETE FROM users WHERE user_id = ?";
            statement = connection.prepareStatement(sql);

            statement.setLong(1, userId);

            //ִ��ɾ��
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

    //��ѯ�����û�
    @Override
    public List<User> getAllUsers() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        //�����б�洢��ѯ���
        List<User> users = new ArrayList<>();

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM users";
            statement = connection.prepareStatement(sql);

            //ִ��sql���
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

    //���û���ɫ��ѯ
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

    //�����û�������ʱ��Ϊ��ǰʱ��
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

    //����û����Ƿ����
    @Override
    public boolean isUsernameExist(String username){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = DatabaseUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            /*String sql = "SELECT * FROM users WHERE username = ?";
            *//*    SQL�����SELECT * FROM users WHERE username = ?����᷵�������û���¼
            ��һ�п�����user_id���ж����Ƿ����0������ȷ���ж��û����������ķ���*/
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

    //��������-�ӽ��������ȡ�û�����
    private User extractUserFromResultSet(ResultSet resultSet) throws SQLException{
        //��ResultSet����ȡUser����
        User user = new User();

        //�ӽ�����ж�ȡ������䵽�û�������
        user.setUserId(resultSet.getLong("user_id"));
        user.setUserName(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setPasswordSalt(resultSet.getString("password_salt"));
        user.setRole(Role.valueOf(resultSet.getString("role")));
        user.setActive(resultSet.getBoolean("is_active"));

        //�������Ϊnull������ʱ��-���user���캯������ĺú�����д
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if(createdAt != null){
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        //ͬ�ϣ�������ʱ���
        Timestamp lastLoginTime = resultSet.getTimestamp("last_login_time");
        if(lastLoginTime != null){
            user.setLastLoginTime(lastLoginTime.toLocalDateTime());
        }

        return user;
    }
}
