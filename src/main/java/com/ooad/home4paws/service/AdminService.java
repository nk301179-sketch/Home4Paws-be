package com.ooad.home4paws.service;

import com.ooad.home4paws.entity.Role;
import com.ooad.home4paws.entity.User;
import com.ooad.home4paws.repository.RoleRepository;
import com.ooad.home4paws.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Check if a user is an admin
     */
    public boolean isAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
                .orElse(false);
    }

    /**
     * Get admin user by username
     */
    public User getAdminByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
                .orElse(null);
    }

    /**
     * Create a new admin user
     */
    public User createAdmin(String username, String email, String password, String firstName, String lastName) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEnabled(true);
        admin.setRoles(Set.of(adminRole));

        return userRepository.save(admin);
    }

    /**
     * Check if admin exists
     */
    public boolean adminExists(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
                .orElse(false);
    }
}
