package com.example.nguyenthanhdat.service;

import com.example.nguyenthanhdat.model.Role;
import com.example.nguyenthanhdat.model.User;
import com.example.nguyenthanhdat.repository.IRoleRepository;
import com.example.nguyenthanhdat.repository.IUserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IRoleRepository roleRepository;

    // Lưu người dùng mới vào cơ sở dữ liệu sau khi mã hóa mật khẩu.
    public void save(@NotNull User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(user);
    }

    // Gán vai trò mặc định cho người dùng dựa trên tên người dùng.
    public void setDefaultRole(String username) {
        userRepository.findByUsername(username).ifPresentOrElse(
                user -> {
                    Role defaultRole = roleRepository.findByName("USER")
                            .or(() -> roleRepository.findByName("ROLE_USER"))
                            .orElseGet(() -> {
                                Role role = new Role();
                                role.setName("USER");
                                role.setDescription("Default role for registered users");
                                return roleRepository.save(role);
                            });
                    user.getRoles().add(defaultRole);
                    userRepository.save(user);
                },
                () -> {
                    throw new UsernameNotFoundException("User not found");
                });
    }

    // Tải thông tin chi tiết người dùng để xác thực.
    @Override 
    public UserDetails loadUserByUsername(String username) throws 
UsernameNotFoundException { 
        var user = userRepository.findByUsername(username) 
                .orElseThrow(() -> new UsernameNotFoundException("User not found")); 
        return org.springframework.security.core.userdetails.User 
                .withUsername(user.getUsername()) 
                .password(user.getPassword()) 
                .authorities(user.getAuthorities()) 
                .accountExpired(!user.isAccountNonExpired()) 
                .accountLocked(!user.isAccountNonLocked()) 
                .credentialsExpired(!user.isCredentialsNonExpired()) 
                .disabled(!user.isEnabled()) 
                .build(); 
    }

    // Tìm kiếm người dùng dựa trên tên đăng nhập.
    public Optional<User> findByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(String username, String email, String rawPassword, String phone, String roleName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
        user.setPhone(phone);
        user.setRoles(resolveRoles(roleName));
        return userRepository.save(user);
    }

    public User updateUser(Long id, String username, String email, String rawPassword, String phone, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));

        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);

        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
        }

        if (roleName != null && !roleName.isBlank()) {
            user.setRoles(resolveRoles(roleName));
        }

        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    private Set<Role> resolveRoles(String roleName) {
        String normalizedRole = (roleName == null || roleName.isBlank()) ? "USER" : roleName.trim().toUpperCase();

        Role role = roleRepository.findByName(normalizedRole)
                .or(() -> roleRepository.findByName("ROLE_" + normalizedRole))
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(normalizedRole);
                    newRole.setDescription("Generated from User API");
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        return roles;
    }
}
