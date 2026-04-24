package com.skillora.interfaces;

import com.skillora.model.user.UserPreferences;
import java.util.List;

public interface IUserPreferencesRepository {
    void add(UserPreferences pref);
    List<UserPreferences> getAll();
    UserPreferences getByUserId(int userId);
    void update(UserPreferences pref);
    void deleteByUserId(int userId);
}
