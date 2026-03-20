package com.example.nguyenthanhdat.controller;

import com.example.nguyenthanhdat.model.User;
import com.example.nguyenthanhdat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserApiResponse> getAllUsers() {
        return userService.getAllUsers().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserApiResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found on :: " + id));
        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping
    public UserApiResponse createUser(@RequestBody UserApiRequest request) {
        validateRequiredFields(request);
        User createdUser = userService.createUser(
                request.getResolvedUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getResolvedPhone(),
                request.getRole()
        );
        return toResponse(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserApiResponse> updateUser(@PathVariable Long id, @RequestBody UserApiRequest request) {
        if (request.getResolvedUsername() == null || request.getResolvedUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        User updatedUser = userService.updateUser(
                id,
                request.getResolvedUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getResolvedPhone(),
                request.getRole()
        );

        return ResponseEntity.ok(toResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found on :: " + id));
        userService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }

    private void validateRequiredFields(UserApiRequest request) {
        if (request.getResolvedUsername() == null || request.getResolvedUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }
    }

    private UserApiResponse toResponse(User user) {
        String role = user.getRoles().stream().findFirst().map(r -> r.getName()).orElse("USER");
        return new UserApiResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                role,
                null,
                null,
                user.getPhone(),
                null
        );
    }

    public static class UserApiRequest {
        private String username;
        private String name;
        private String email;
        private String password;
        private String role;
        private String dateOfBirth;
        private String address;
        private String phone;
        private String phoneNumber;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getResolvedUsername() {
            return username != null && !username.isBlank() ? username : name;
        }

        public String getResolvedPhone() {
            return phoneNumber != null && !phoneNumber.isBlank() ? phoneNumber : phone;
        }
    }

    public record UserApiResponse(
            Long id,
            String name,
            String email,
            String role,
            String dateOfBirth,
            String address,
            String phoneNumber,
            String createdAt
    ) {
    }
}