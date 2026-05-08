package utils;

import java.sql.Connection;
import java.sql.Statement;

public class DbUpdate {
    public static void main(String[] args) {
        try {
            Connection cnx = MyDatabase.getInstance().getCnx();
            if (cnx != null) {
                Statement st = cnx.createStatement();
                st.executeUpdate("ALTER TABLE skillora.utilisateurs ADD COLUMN est_en_ligne tinyint(1) NOT NULL DEFAULT 0;");
                System.out.println("Column added successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
