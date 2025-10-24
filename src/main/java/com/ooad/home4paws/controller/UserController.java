package com.ooad.home4paws.controller;

import com.ooad.home4paws.dto.PasswordChangeRequest;
import com.ooad.home4paws.dto.UpdateProfileRequest;
import com.ooad.home4paws.dto.UserDto;
import com.ooad.home4paws.entity.User;
import com.ooad.home4paws.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> user = userService.findByUsername(userDetails.getUsername());
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        User u = user.get();
        UserDto dto = new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody UpdateProfileRequest update) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> caller = userService.findByUsername(userDetails.getUsername());
        if (caller.isEmpty()) return ResponseEntity.status(401).build();
        var updated = userService.updateCurrentUser(caller.get(), update);
        return ResponseEntity.ok(new UserDto(
                updated.getId(),
                updated.getUsername(),
                updated.getEmail(),
                updated.getFirstName(),
                updated.getLastName()
        ));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                              @RequestBody PasswordChangeRequest req) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> caller = userService.findByUsername(userDetails.getUsername());
        if (caller.isEmpty()) return ResponseEntity.status(401).build();
        userService.changePassword(caller.get(), req);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteMe(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> caller = userService.findByUsername(userDetails.getUsername());
        if (caller.isEmpty()) return ResponseEntity.status(401).build();
        userService.deleteUser(caller.get().getId());
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id,
                                    @RequestBody com.ooad.home4paws.dto.RegisterRequest update,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> caller = userService.findByUsername(userDetails.getUsername());
        if (caller.isEmpty()) return ResponseEntity.status(401).build();
        if (!caller.get().getId().equals(id) &&
                userDetails.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        var updated = userService.updateUser(id, update);
        return ResponseEntity.ok(new UserDto(
                updated.getId(),
                updated.getUsername(),
                updated.getEmail(),
                updated.getFirstName(),
                updated.getLastName()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<User> caller = userService.findByUsername(userDetails.getUsername());
        if (caller.isEmpty()) return ResponseEntity.status(401).build();
        if (!caller.get().getId().equals(id) &&
                userDetails.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
