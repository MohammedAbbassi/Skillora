package com.skillora.service;

import com.skillora.interfaces.user_interface.*;

import com.skillora.model.user.User;
import com.skillora.model.user.UserPreferences;
import com.skillora.model.user.UserStats;
import com.skillora.repository.user_repo.UserPreferencesRepository;
import com.skillora.repository.user_repo.UserRepository;
import com.skillora.repository.user_repo.UserStatsRepository;
import com.skillora.util.user_util.PasswordUtil;

public class UserService implements IUserService {

    private final IUserRepository userRepo;
    private final IUserStatsRepository statsRepo;
    private final IUserPreferencesRepository prefsRepo;

    public UserService() {
        this.userRepo = new UserRepository();
        this.statsRepo = new IUserStatsRepository();
        this.prefsRepo = new IUserPreferencesRepository();
    }

    public UserService(IUserRepository userRepo, IUserStatsRepository statsRepo, IUserPreferencesRepository prefsRepo) {
        this.userRepo = userRepo;
        this.statsRepo = statsRepo;
        this.prefsRepo = prefsRepo;
    }

    // ✅ REGISTER
    public boolean register(User user, String plainPassword) {

        // check if email exists
        if (userRepo.findByEmail(user.getEmail()) != null) {
            System.out.println("Email already exists!");
            return false;
        }

        // hash password
        String hashed = PasswordUtil.hashPassword(plainPassword);
        user.setPasswordHash(hashed);

        // save user
        userRepo.addUser(user);

        // 🔁 Auto-create stats & preferences
        if (user.getId() > 0) {
            // Default Stats
            UserStats stats = new UserStats(user.getId(), 0, 1, "Novice");
            statsRepo.add(stats);

            // Default Preferences
            UserPreferences prefs = new UserPreferences(user.getId(), "Arial", 14, "Light", "Normal");
            prefsRepo.add(prefs);
            
            return true;
        }

        return false;
    }

    // ✅ LOGIN
    public User login(String email, String password) {

        User user = userRepo.findByEmail(email);

        if (user == null) {
            System.out.println("User not found");
            return null;
        }

        if (PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            return user;
        } else {
            System.out.println("Wrong password");
            return null;
        }
    }

    // ✅ UPDATE EMAIL
    public void updateEmail(User user, String newEmail) {
        user.setEmail(newEmail);
        userRepo.update(user);
    }

    // ✅ DELETE
    public void deleteUser(int id) {
        userRepo.delete(id);
    }
}