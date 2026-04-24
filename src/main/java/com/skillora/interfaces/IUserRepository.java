package com.skillora.interfaces;

import com.skillora.model.user.User;

public interface IUserRepository {
    void addUser(User user);
    User findByEmail(String email);
    void update(User user);
    void delete(int id);
}
