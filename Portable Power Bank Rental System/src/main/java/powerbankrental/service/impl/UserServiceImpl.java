package powerbankrental.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import powerbankrental.service.UserService;
import powerbankrental.dao.impl.UserDAOImpl;
import powerbankrental.dao.UserDAO;
import powerbankrental.model.User;
import powerbankrental.model.enums.Role;
import powerbankrental.util.DatabaseUtil;
import powerbankrental.util.PasswordUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

//用户服务接口实现类-实现与用户相关的所有业务逻辑
public class UserServiceImpl implements UserService{
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    //数据访问对象
    private final UserDAO userDAO;

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }
    //用于依赖注入的构造方法（单元测试用）
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User registerUser(String username, String password) {
        return createUser(username, password, Role.USER, true);
    }

    @Override
    public User createUser(String username, String password, Role role, boolean isActive) {
        //检查输入
        if(Objects.isNull(username) || Objects.isNull(password) || username.isEmpty() || password.isEmpty()){
            logger.error("register user failed,username or password is null");
            return null;
        }

        //检查用户名是否存在
        if(userDAO.isUsernameExist(username)){
            logger.error("register user failed,username already exist");
        }

        try{
            //创造用户对象
            User user = new User();
            user.setUserName(username.trim());

            //生产密码盐值和哈希密码
            String salt = PasswordUtil.generateSalt();
            String passwordHash = PasswordUtil.hashPassword(password, salt);
            user.setPasswordSalt(PasswordUtil.generateSalt());
            user.setPasswordHash(passwordHash);

            user.setRole(role);
            user.setActive(isActive);
            user.setCreatedAt(LocalDateTime.now());

            //添加用户到数据库
            Long userId = userDAO.addUser(user);
            if ((Math.toIntExact(userId)) > 0) {
                user.setUserId(userId);
                logger.info("register user success, ID is {},role is {}" , user.getUserId(),  user.getRole());
                return user;
            } else {
                logger.error("register user failed, insert user failed");
                return null;
            }
        }catch (Exception e){
            logger.error("register user failed", e);
            return null;
        }
    }

    @Override
    public User login(String username, String password) {
        if(Objects.isNull(username) || Objects.isNull(password) || username.isEmpty() || password.isEmpty()){
            logger.error("login user failed,username or password is null");
            return null;
        }

        try{
            //获取用户信息
            User user = userDAO.getUserByName(username);

            if (Objects.isNull(user)) {
                logger.error("login user failed,user is null");
                return null;
            }
            if (user.isActive()) {
                logger.error("login user failed,user is unactive");
                return null;
            }
            //验证密码
            boolean isPasswordValid = PasswordUtil.verifyPassword(password, user.getPasswordSalt(), user.getPasswordHash());
            if (!isPasswordValid) {
                logger.error("login user failed,password is invalid");
                return null;
            }

            userDAO.updateLastLoginTime(user.getUserId());
            user.setLastLoginTime(LocalDateTime.now());

            logger.info("login user success, ID is {},username is {}" , user.getUserId(), user.getUserName());
            return user;
        }catch (Exception e){
            logger.error("login user failed", e);
            return null;
        }
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        if (Objects.isNull(oldPassword) || Objects.isNull(newPassword) || oldPassword.isEmpty() || newPassword.isEmpty()) {
            logger.error("change password failed, old or new password is empty");
            return false;
        }

        try{
            //各种验证
            User user = userDAO.getUserById(userId);
            if (Objects.isNull(user)) {
                logger.error("change password failed, user is null");
                return false;
            }
            boolean isOldPasswordValid = PasswordUtil.verifyPassword(oldPassword, user.getPasswordSalt(), user.getPasswordHash());
            if (!isOldPasswordValid) {
                logger.error("change password failed, old password is invalid");
                return false;
            }

            String newSalt = PasswordUtil.generateSalt();
            String newHash = PasswordUtil.hashPassword(newPassword, newSalt);

            boolean success = userDAO.updatePassword(user.getUserId(), newHash, newSalt);
            if (success) {
                logger.info("set password success, ID is {}" , user.getUserId());
            }else {
                logger.error("change password failed, update password failed, Id is {}" , user.getUserId());
            }
            return success;
        }catch (Exception e){
            logger.error("change password failed", e);
            return false;
        }
    }

    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        if (Objects.isNull(newPassword) || newPassword.isEmpty()) {
            logger.error("reset password failed, old password is empty");
            return false;
        }

        try{
            User user = userDAO.getUserById(userId);
            if (Objects.isNull(user)) {
                logger.error("reset password failed, user is null");
                return false;
            }

            String salt = PasswordUtil.generateSalt();
            String newHash = PasswordUtil.hashPassword(newPassword, salt);

            boolean success = userDAO.updatePassword(user.getUserId(), newHash, salt);
            if (success) {
                logger.info("reset password success, ID is {}" , user.getUserId());
            }else {
                logger.error("reset password failed, update password failed, Id is {}" , user.getUserId());
            }
            return success;
        }catch (Exception e){
            logger.error("reset password failed", e);
            return false;
        }
    }

    @Override
    public boolean updateUserStatus(Long userId, boolean isActive) {
        try{
            User user = userDAO.getUserById(userId);
            if (Objects.isNull(user)) {
                logger.error("update user failed, user is null");
                return false;
            }

            boolean success = userDAO.updateUserStatus(user.getUserId(), isActive);
            if (success) {
                logger.info("update user status success, ID is {}" , user.getUserId());
            }else {
                logger.error("update user failed, update user status failed, Id is {}" , user.getUserId());
            }
            return success;
        }catch (Exception e){
            logger.error("update user failed", e);
            return false;
        }
    }

    @Override
    public boolean deleteUser(Long userId) {
        try{
            User user = userDAO.getUserById(userId);
            if (Objects.isNull(user)) {
                logger.error("delete user failed, user is null");
            }
            if(user.getRole() == Role.SUPER_ADMIN){
                logger.info("SUPER_ADMIN cannot be deleted. ");
                return false;
            }

            boolean success = userDAO.deleteUser(user.getUserId());
            if (success) {
                logger.info("delete user success, ID is {}" , user.getUserId());
            }else{
                logger.error("delete user failed, delete user failed, Id is {}" , user.getUserId());
            }
            return success;
        }catch (Exception e){
            logger.error("delete user failed", e);
            return false;
        }
    }

    @Override
    public User getUserById(Long userId) {
        try{
            return userDAO.getUserById(userId);
        }catch (Exception e){
            logger.error("get user by ID failed", e);
            return null;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        try{
            return userDAO.getUserByName(username);
        }catch (Exception e){
            logger.error("get user by name failed", e);
            return null;
        }
    }

    @Override
    public List<User> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (Exception e) {
            logger.error("get all user failed", e);
            return null;
        }
    }

    @Override
    public List<User> getAllUsersByRole(Role role) {
        try {
            return userDAO.getUsersByRole(role);
        }catch (Exception e){
            logger.error("get all users by role failed", e);
            return null;
        }
    }

    @Override
    public boolean isUsernameExists(String username) {
        try{
            return userDAO.isUsernameExist(username);
        }catch (Exception e){
            logger.error("is username exists failed", e);
            return false;
        }
    }
}