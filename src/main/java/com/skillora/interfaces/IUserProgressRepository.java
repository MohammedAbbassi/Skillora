package com.skillora.interfaces;

import com.skillora.model.user.UserProgress;
import java.util.List;

public interface IUserProgressRepository {
    void add(UserProgress progress);
    List<UserProgress> getAll();
    List<UserProgress> getByUserId(int userId);
    void update(UserProgress progress);
    void delete(int userId, int courseId);
}
