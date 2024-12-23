package com.example.UserService.service;

import com.example.UserService.dto.UserDTO;
import com.example.UserService.entity.User;
import com.example.UserService.entity.UserRole;
import com.example.UserService.helper.PasswordHelper;
import com.example.UserService.repository.UserRepository;
import com.example.UserService.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordHelper passwordHelper;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository, PasswordHelper passwordHelper) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordHelper = passwordHelper;
    }

    public String authenticateUser(String username, String password) {
        UserDTO userDTO = getUserDetails(username);

        if(passwordHelper.matches(password,userDTO.getPassword())){
            return "redirect:http://localhost:8081/api/orders/products";
        } else {
            return "/login";
        }

    }

    // Register user method now returns success/failure
    public String registerUser(UserDTO userDTO) {
        if(userRepository.existsByUsername(userDTO.getUsername())) {
            return "failure";  // Username already exists
        }
        String encodedPassword = passwordHelper.hashPassword(userDTO.getPassword());
        User user = new User();
        UserRole user_role = new UserRole();
        user.setUsername(userDTO.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setAddress(userDTO.getAddress());
        user.setCity(userDTO.getCity());
        user.setCountry(userDTO.getCountry());
        user.setZipCode(userDTO.getZipCode());
        user_role.setUserId(user.getUser_id());
        user_role.setRole("USER");

        userRoleRepository.save(user_role);
        userRepository.save(user);
        return "success";  // Successful registration
    }

    @Transactional
    public String deleteUser(String username) {
        if (userRepository.existsByUsername(username)) {
            System.out.println("Deleting " + username);
            userRoleRepository.deleteByUserId(userRepository.findByUsername(username).getUser_id());
            userRepository.deleteByUsername(username);
            System.out.println("Deleted " + username);
            return "success";
        } else {
            System.out.println("Not able to delete " + username);
            return "failure";
        }
    }

    public String getRole(String username){
        User user = userRepository.findByUsername(username);
        return userRoleRepository.findByUserId(user.getUser_id()).getRole();
    }

    // Login user method now returns success/failure

    public UserDTO getUserDetails(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return new UserDTO(
                    user.getUsername(),
                    user.getPassword(), // You might not need to return the password
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPhoneNumber(),
                    user.getAddress(),
                    user.getCity(),
                    user.getCountry(),
                    user.getZipCode()
            );
        }
        return null;  // If user not found
    }
}
