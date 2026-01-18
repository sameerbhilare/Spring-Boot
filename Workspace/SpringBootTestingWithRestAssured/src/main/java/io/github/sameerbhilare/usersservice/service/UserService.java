package io.github.sameerbhilare.usersservice.service;

import io.github.sameerbhilare.usersservice.ui.model.User;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User createUser(User user);
    User getUser(String userId);
    List<User> getUsers(int page, int limit);
}
