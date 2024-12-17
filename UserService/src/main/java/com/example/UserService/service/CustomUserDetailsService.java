package com.example.UserService.service;

import com.example.UserService.entity.User;
import com.example.UserService.entity.UserRole;
import com.example.UserService.repository.UserRepository;
import com.example.UserService.repository.UserRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public CustomUserDetailsService(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository=userRepository;
        this.userRoleRepository=userRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        UserRole userRole = userRoleRepository.findByUserId(user.getUser_id());
        if (userRole == null) {
            throw new UsernameNotFoundException("User role not found for: " + username);
        }

        System.out.println(userRole.getRole() + " " + userRole.getUserId());
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole.getRole());
        System.out.println(authority.getAuthority());
        System.out.println(username);

        return new org.springframework.security.core.userdetails.User(
                username,  user.getPassword(), Collections.singletonList(authority));
    }
}