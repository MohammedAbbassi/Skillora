package Tests;

import Utils.DBConnection;

import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();

        if (conn != null) {
            System.out.println("Test réussi : connexion OK");
        } else {
            System.out.println("Test échoué : connexion impossible");
        }
    }
}