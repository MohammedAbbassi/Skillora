# Test Accounts for Skillora

## Quick Login Credentials

### 1. **Admin Account** (Built-in Bypass)
- **Email**: `admin`
- **Password**: `admin`
- **Role**: Admin
- **Notes**: No email validation required, works immediately

### 2. **Pam Test Account** (Built-in Bypass)
- **Email**: `pam`
- **Password**: `pam`
- **Role**: Student
- **Notes**: No email validation required, works immediately

## Setting Up Pam Account in Database

If you want the "pam" account to persist in the database (recommended for testing notifications and mentions), run this SQL:

```sql
USE `skillora`;

-- Insert test user pam
INSERT INTO `utilisateurs` 
  (`id_utilisateur`, `nom_utilisateur`, `email`, `mot_de_passe`, `prenom`, `nom`, `role`, `est_actif`, `xp_points`, `streak_days`) 
VALUES 
  (999, 'pam', 'pam', 'pam', 'Pam', 'Test', 'ETUDIANT', 1, 0, 0)
ON DUPLICATE KEY UPDATE 
  `email` = 'pam', 
  `mot_de_passe` = 'pam',
  `nom_utilisateur` = 'pam',
  `prenom` = 'Pam',
  `nom` = 'Test';

-- Add preferences for pam
INSERT INTO `preferences_utilisateur` 
  (`id_preference`, `id_utilisateur`)
VALUES 
  (999, 999)
ON DUPLICATE KEY UPDATE 
  `id_utilisateur` = 999;

-- Add statistics for pam
INSERT INTO `statistiques_utilisateur` 
  (`id_statistique`, `id_utilisateur`)
VALUES 
  (999, 999)
ON DUPLICATE KEY UPDATE 
  `id_utilisateur` = 999;
```

Or simply run the file: **`add_test_user_pam.sql`**

## How It Works

### Login Bypass Logic

The `LoginController` has been updated to skip email validation for these test accounts:

```java
boolean isAdminBypass = "admin".equalsIgnoreCase(email) && "admin".equals(pass);
boolean isPamBypass = "pam".equalsIgnoreCase(email) && "pam".equals(pass);
boolean isBypass = isAdminBypass || isPamBypass;

if (!isBypass) {
    // Email format validation only runs for non-bypass accounts
    if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
        showError("Please enter a valid email address.");
        return;
    }
}
```

## Testing Scenarios

### Test Mentions & Notifications

1. **Login as User A** (any account)
2. **Create a post** in Community
3. **Login as Pam** (`pam:pam`)
4. **Comment on the post**: `@UserA check this out!`
5. **Check database**: 
   ```sql
   SELECT * FROM notification WHERE id_utilisateur = [UserA's ID];
   ```
6. **Verify**: Notification was created for UserA

### Test Nested Replies

1. **Login as Pam**
2. **Create a comment** on any post
3. **Login as another user**
4. **Reply to Pam's comment**: `@pam great point!`
5. **Login as Pam again**
6. **Check notifications** (once UI is implemented)

## Benefits

✅ **No email validation** - Just type "pam" and "pam"  
✅ **Fast testing** - No need to remember complex credentials  
✅ **Persistent data** - If you run the SQL, the account stays in DB  
✅ **Mention testing** - Easy to mention: `@pam` or `@Pam Test`  
✅ **Multiple accounts** - Use pam + other accounts to test interactions

## Adding More Test Accounts

To add more bypass accounts, edit `LoginController.java`:

```java
boolean isPamBypass = "pam".equalsIgnoreCase(email) && "pam".equals(pass);
boolean isJohnBypass = "john".equalsIgnoreCase(email) && "john".equals(pass);
boolean isBypass = isAdminBypass || isPamBypass || isJohnBypass;
```

Then add the user to the database with the corresponding SQL.
