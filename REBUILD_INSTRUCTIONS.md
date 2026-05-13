# How to Fix the Compilation Error

## The Problem
IntelliJ IDEA has stale compiled classes in its cache that are causing the error:
```
IService cannot be resolved to a type
```

## Solution: Clean and Rebuild in IntelliJ IDEA

### Step 1: Invalidate Caches
1. In IntelliJ IDEA, go to **File** → **Invalidate Caches...**
2. Check **all boxes**:
   - ✅ Clear file system cache and Local History
   - ✅ Clear downloaded shared indexes
   - ✅ Clear VCS Log caches and indexes
   - ✅ Invalidate and Restart
3. Click **Invalidate and Restart**

### Step 2: Clean Build
After IntelliJ restarts:
1. Go to **Build** → **Clean Project**
2. Wait for it to finish
3. Go to **Build** → **Rebuild Project**

### Step 3: Run the Application
1. Right-click on `src/main/java/tests/Main.java`
2. Select **Run 'Main.main()'**

## Alternative: Command Line Clean Build

If the above doesn't work, try this from the terminal:

```bash
# Windows PowerShell
cd C:\Users\Abbassi_Mohammed\Desktop\Skillora
Remove-Item -Recurse -Force target
Remove-Item -Recurse -Force .idea\compiler.xml
mvn clean compile
```

Then in IntelliJ:
1. **File** → **Reload All from Disk**
2. **Build** → **Rebuild Project**

## Why This Happened

When we updated the `User` entity to use the `Role` enum instead of `String`, IntelliJ's incremental compiler kept some old `.class` files that still referenced the old String-based role. This created a mismatch.

## Verify It's Fixed

After rebuilding, you should see:
- ✅ No red underlines in ServiceUser.java
- ✅ No compilation errors in the Build output
- ✅ Application starts successfully

## Register Feature Should Work

Once the build is clean, the register feature will work properly because:
- ✅ `LoginController` sets `user.setRole(Role.ETUDIANT)` for new users
- ✅ `ServiceUser.add()` converts `role.name()` to String for database
- ✅ All role comparisons use enum equality

The register button will create new users with the ETUDIANT role correctly!
