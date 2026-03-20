package com.example.nguyenthanhdat.repository;

import com.example.nguyenthanhdat.model.User; 
import org.springframework.data.jpa.repository.JpaRepository; 
public interface UserRepository extends JpaRepository<User, Long> { 
User findByUsername(String username); 
} 
