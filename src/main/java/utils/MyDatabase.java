package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private  final  String URL="jdbc:mysql://localhost:3306/skillora";
    private  final  String USER="root";
    private  final  String PASSWORD="";
    private Connection cnx;
    private  static  MyDatabase instance ;

    private MyDatabase() {
        try {
            // Chargement explicite du driver (optionnel pour les versions récentes mais plus sûr)
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database: " + URL);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trouvé : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            System.err.println("Assurez-vous que le serveur MySQL est lancé et que la base 'skillora' existe.");
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null)
            instance = new MyDatabase();
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
