package com.hackathon.demo.service;

import com.hackathon.demo.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserService() {
        // Initialize with some sample data
        users.add(User.builder().id(idGenerator.getAndIncrement()).username("john_doe").email("john@example.com").fullName("John Doe").build());
        users.add(User.builder().id(idGenerator.getAndIncrement()).username("jane_smith").email("jane@example.com").fullName("Jane Smith").build());
        users.add(User.builder().id(idGenerator.getAndIncrement()).username("bob_wilson").email("bob@example.com").fullName("Bob Wilson").build());
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public Optional<User> getUserById(Long id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public User createUser(User user) {
        user.setId(idGenerator.getAndIncrement());
        users.add(user);
        return user;
    }

    public Optional<User> updateUser(Long id, User updatedUser) {
        return getUserById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setFullName(updatedUser.getFullName());
            return user;
        });
    }

    public boolean deleteUser(Long id) {
        return users.removeIf(u -> u.getId().equals(id));
    }
}