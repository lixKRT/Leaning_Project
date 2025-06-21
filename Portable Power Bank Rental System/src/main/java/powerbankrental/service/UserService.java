package powerbankrental.service;

import powerbankrental.model.User;
import powerbankrental.model.enums.Role;

import java.util.List;

public interface UserService {
    User registerUser(String username, String password);
    User createUser(String username, String password, Role role, boolean isActive);
    User login(String username, String password);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    boolean resetPassword(Long userId, String newPassword);
    boolean updateUserStatus(Long userId, boolean isActive);
    boolean deleteUser(Long userId);
    User getUserById(Long userId);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    List<User> getAllUsersByRole(Role role);
    boolean isUsernameExists(String username);
}
