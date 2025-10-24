package com.ooad.home4paws.service;

import com.ooad.home4paws.dto.PasswordChangeRequest;
import com.ooad.home4paws.dto.RegisterRequest;
import com.ooad.home4paws.dto.UpdateProfileRequest;
import com.ooad.home4paws.entity.Role;
import com.ooad.home4paws.entity.User;
import com.ooad.home4paws.exception.UserAlreadyExistsException;
import com.ooad.home4paws.exception.UserNotFoundException;
import com.ooad.home4paws.repository.RoleRepository;
import com.ooad.home4paws.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUser(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + req.getUsername());
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + req.getEmail());
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());

        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            return roleRepository.save(r);
        });
        user.getRoles().add(userRole);

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, RegisterRequest update) {
        var u = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        if (!u.getEmail().equals(update.getEmail()) && userRepository.existsByEmail(update.getEmail())) {
            throw new UserAlreadyExistsException("Email already used: " + update.getEmail());
        }
        u.setFirstName(update.getFirstName());
        u.setLastName(update.getLastName());
        u.setEmail(update.getEmail());
        if (update.getPassword() != null && !update.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(update.getPassword()));
        }
        return userRepository.save(u);
    }

    public User updateCurrentUser(User current, UpdateProfileRequest update) {
        if (!current.getEmail().equals(update.getEmail()) && userRepository.existsByEmail(update.getEmail())) {
            throw new UserAlreadyExistsException("Email already used: " + update.getEmail());
        }
        current.setFirstName(update.getFirstName());
        current.setLastName(update.getLastName());
        current.setEmail(update.getEmail());
        // We intentionally do not allow changing username here to keep things simple
        return userRepository.save(current);
    }

    public void changePassword(User current, PasswordChangeRequest req) {
        if (!passwordEncoder.matches(req.getCurrentPassword(), current.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        current.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(current);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void assignRole(Long userId, String roleName) {
        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        var role = roleRepository.findByName(roleName).orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.getRoles().add(role);
        userRepository.save(user);
    }
}
