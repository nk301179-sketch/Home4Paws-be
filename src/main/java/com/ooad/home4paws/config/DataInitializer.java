package com.ooad.home4paws.config;

import com.ooad.home4paws.entity.Role;
import com.ooad.home4paws.entity.User;
import com.ooad.home4paws.repository.RoleRepository;
import com.ooad.home4paws.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize default roles if they don't exist
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            return roleRepository.save(r);
        });

        roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_ADMIN");
            return roleRepository.save(r);
        });

        // Seed demo user if not exists
        if (userRepository.findByUsername("demo").isEmpty()) {
            User demo = new User();
            demo.setUsername("demo");
            demo.setEmail("demo@example.com");
            demo.setFirstName("Demo");
            demo.setLastName("User");
            demo.setPassword(passwordEncoder.encode("Demo123!"));
            demo.setRoles(Set.of(userRole));
            userRepository.save(demo);
            System.out.println("Created demo user (demo / Demo123!)");
        }

        // Seed admin user if not exists
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_ADMIN");
            return roleRepository.save(r);
        });

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@home4paws.com");
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            System.out.println("Created admin user (admin / Admin123!)");
        }
    }
}
