CREATE TABLE IF NOT EXISTS todo_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    deadline DATE,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    category VARCHAR(50) DEFAULT 'Learning',
    progress INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS todo_objectives (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    target_date DATE,
    progress INT DEFAULT 0,
    motivational_status VARCHAR(100) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS todo_objective_tasks (
    objective_id INT NOT NULL,
    task_id INT NOT NULL,
    PRIMARY KEY (objective_id, task_id),
    FOREIGN KEY (objective_id) REFERENCES todo_objectives(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES todo_tasks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS todo_achievements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon VARCHAR(50) DEFAULT '🎯',
    xp_rewarded INT DEFAULT 0,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS todo_productivity_stats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    tasks_completed INT DEFAULT 0,
    xp_earned INT DEFAULT 0,
    streak_count INT DEFAULT 0,
    productivity_score DOUBLE DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_date (user_id, date),
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS todo_badges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_key VARCHAR(50) NOT NULL,
    badge_name VARCHAR(255) NOT NULL,
    badge_icon VARCHAR(50) DEFAULT '🏅',
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_badge (user_id, badge_key),
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE
);
