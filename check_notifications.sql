-- Check if notification table exists and has data
USE `skillora`;

-- Check if table exists
SHOW TABLES LIKE 'notification';

-- Check all notifications
SELECT * FROM notification ORDER BY date_creation DESC;

-- Check unread notifications for all users
SELECT 
    n.id_notification,
    n.type,
    n.message,
    n.lu as 'read',
    u.nom_utilisateur as 'receiver',
    n.date_creation
FROM notification n
JOIN utilisateurs u ON n.id_utilisateur = u.id_utilisateur
WHERE n.lu = 0
ORDER BY n.date_creation DESC;

-- Check recent comments with mentions
SELECT 
    c.id_commentaire,
    c.contenu,
    u.nom_utilisateur as 'author',
    c.date_creation
FROM commentaire c
JOIN utilisateurs u ON c.id_utilisateur = u.id_utilisateur
WHERE c.contenu LIKE '%@%'
ORDER BY c.date_creation DESC
LIMIT 10;
