package tests;

import entities.User;
import services.UserService;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();
        
        User user = new User("greatgreat", "med@gmail.com", "password123", "Mohammed", "Abbassi");
        
        try {
            userService.add(user);
            System.out.println(userService.getAll());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}