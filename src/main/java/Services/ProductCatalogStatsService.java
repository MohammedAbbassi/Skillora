package Services;

import com.skillora.model.CatalogStats;
import utils.MyDatabase;

import java.sql.*;

public class ProductCatalogStatsService {

    private final Connection cnx = MyDatabase.getInstance().getCnx();

    public CatalogStats loadStats() throws SQLException {
        CatalogStats s = new CatalogStats();
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(
                "SELECT COUNT(*), COALESCE(AVG(prix),0), COALESCE(MIN(prix),0), COALESCE(MAX(prix),0) FROM produit")) {
            if (rs.next()) {
                s.setTotalProducts(rs.getInt(1));
                s.setAvgPrice(rs.getDouble(2));
                s.setMinPrice(rs.getDouble(3));
                s.setMaxPrice(rs.getDouble(4));
            }
        }
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery("SELECT categorie, COUNT(*) FROM produit GROUP BY categorie")) {
            while (rs.next()) {
                String t = rs.getString(1);
                if (t != null) {
                    s.getCountByType().put(t, rs.getInt(2));
                }
            }
        }
        return s;
    }
}
