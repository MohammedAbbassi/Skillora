package com.skillora.interfaces;

import com.skillora.model.user.User;

public interface IUserService {
    boolean register(User user, String plainPassword);
    User login(String email, String password);
    void updateEmail(User user, String newEmail);
    void deleteUser(int id);
}
