package com.example.nguyenthanhdat.repository;

import com.example.nguyenthanhdat.model.Role; 
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.stereotype.Repository; 
@Repository 
public interface IRoleRepository extends JpaRepository<Role, Long>{ 
Role findRoleById(Long id); 
Optional<Role> findByName(String name);
} 