package top.wsido.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import top.wsido.entity.User;
import top.wsido.mapper.UserMapper;

@Component
public class SecurityUtils {

    @Autowired
    private UserMapper userMapper;

    /**
     * Get the currently authenticated User entity.
     *
     * @return User entity or null if not authenticated or principal is not User type or cannot be resolved.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            } else if (principal instanceof String) {
                String username = (String) principal;
                if (userMapper != null) {
                    return userMapper.findByUsername(username);
                } else {
                    System.err.println("UserMapper not injected in SecurityUtils");
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Get the ID of the currently authenticated user.
     *
     * @return User ID or null if not available.
     */
    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return (currentUser != null) ? currentUser.getId() : null;
    }

    /**
     * Get the type (e.g., "admin", "user") of the currently authenticated user.
     *
     * @return User type string or null if not available.
     */
    public String getCurrentUserType() {
        User currentUser = getCurrentUser();
        return (currentUser != null) ? currentUser.getType() : null;
    }

    /**
     * Check if the current user is an admin.
     *
     * @return true if admin, false otherwise.
     */
    public boolean isAdmin() {
        String userType = getCurrentUserType();
        return "admin".equalsIgnoreCase(userType);
    }
} 