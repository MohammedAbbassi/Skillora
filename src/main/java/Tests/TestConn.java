package Tests;

import Entites.User;
import Services.UserCRUD;
import Utils.MyBD;

import java.sql.SQLException;

public class TestConn {
    public static void main(String[] args) {
        MyBD bd = MyBD.getInstance();

        User u1 = new User("amine", "amine@bz.com", "password123", "amine", "bzz");
        UserCRUD uc = new UserCRUD();

        try {
            //uc.ajouter(u1);
            //System.out.println(uc.afficher());
            
            uc.supprimer(2);
            System.out.println(uc.afficher());
        } catch (SQLException s) {
            System.out.println(s.getMessage());
        }
    }
}