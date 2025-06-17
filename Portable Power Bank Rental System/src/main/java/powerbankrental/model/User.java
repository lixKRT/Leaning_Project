package powerbankrental.model;

import powerbankrental.model.enums.Role;
import java.time.LocalDateTime;
import java.util.Objects;

//用户实体类
public class User {
    private Long userId;
    private String userName;
    private String passwordHash;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginTime;

    public User(String userName, String passwordHash, Role role) {
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    //用于数据库加载数据
    public User(Long userId, String userName, String passwordHash, Role role,
                boolean isActive, LocalDateTime createdAt, LocalDateTime lastLoginTime) {
        this.userId = userId;
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.lastLoginTime = lastLoginTime;
    }

    public Long getUserId() {
        return this.userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public  String getUserName() {
        return this.userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return this.role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    public  boolean isActive() {
        return this.isActive;
    }
    public void setActive(boolean active) {
        this.isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {}

    public LocalDateTime getLastLoginTime() {
        return this.lastLoginTime;
    }
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        User user = (User)obj;
        return this.userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
