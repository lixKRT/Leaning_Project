package powerbankrental.dao;

import powerbankrental.model.User;
import powerbankrental.model.enums.Role;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

//�û����ݷ��ʽӿ�-�����û���ص����ݿ��������
public interface UserDAO {
    int addUser(User user);    //����û�
    int addUserInTransaction(User user, Connection connection);

    User getUserById(long userId);    //�����û�ID��ѯ�û�
    User getUserByName(String username);    //�����û�����ѯ�û�
    List<User> getAllUsers();
    List<User> getUsersByRole(Role role);

    boolean updateUser(User user);
    boolean updatePassword(long userId, String newPassword, String newSalt);
    boolean updateUserStatus(long userId, boolean status);
    boolean deleteUser(long userId);
    boolean updateLastLoginTime(long userId);
    boolean isUsernameExist(String username);
}
