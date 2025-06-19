package powerbankrental.dao;

import powerbankrental.model.User;
import powerbankrental.model.enums.Role;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

//用户数据访问接口-定义用户相关的数据库操作方法
public interface UserDAO {
    int addUser(User user);    //添加用户
    int addUserInTransaction(User user, Connection connection);

    User getUserById(long userId);    //根据用户ID查询用户
    User getUserByName(String username);    //根据用户名查询用户
    List<User> getAllUsers();
    List<User> getUsersByRole(Role role);

    boolean updateUser(User user);
    boolean updatePassword(long userId, String newPassword, String newSalt);
    boolean updateUserStatus(long userId, boolean status);
    boolean deleteUser(long userId);
    boolean updateLastLoginTime(long userId);
    boolean isUsernameExist(String username);
}
