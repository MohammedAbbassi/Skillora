package com.skillora.interfaces;

import com.skillora.model.user.UserStats;
import java.util.List;

public interface IUserStatsRepository {
    void add(UserStats stats);
    List<UserStats> getAll();
    UserStats getByUserId(int userId);
    void update(UserStats stats);
    void deleteByUserId(int userId);
}
