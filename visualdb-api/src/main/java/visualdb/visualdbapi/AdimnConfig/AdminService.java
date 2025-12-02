package visualdb.visualdbapi.AdimnConfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import visualdb.visualdbapi.db.PoolingPersistenceManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import static visualdb.visualdbapi.login.TokenStorage.isUserActive;

public class AdminService {
    public static final String INFO_USER_PATH = "/admin/infoUser";
    private static final Logger logger = Logger.getLogger(AdminService.class.getName());
    public AdminService(){
    }

    public JsonArray GetInfoUser() {
        JsonArray jsonArray = new JsonArray();

        String query = "SELECT U.username, U.email, " +
                "COUNT(DISTINCT D.id) AS num_db, " +
                "COUNT(T.id) AS num_table " +
                "FROM public.\"Utenti\" U " +
                "LEFT JOIN public.\"Database\" D ON U.username = D.proprietario " +
                "LEFT OUTER JOIN public.\"Table\" T ON D.id = T.database " +
                "GROUP BY U.username, U.email";

        try (Connection conn = PoolingPersistenceManager
                .getPersistenceManager()
                .getConnection()) {
            PreparedStatement st = conn.prepareStatement(query);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("username", rs.getString("username"));
                jsonObject.addProperty("email", rs.getString("email"));
                jsonObject.addProperty("num_database", rs.getInt("num_db"));
                jsonObject.addProperty("num_table", rs.getInt("num_table"));
                boolean isOnline = isUserActive(rs.getString("username"));
                jsonObject.addProperty("isOnline", isOnline);
                jsonArray.add(jsonObject);
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return jsonArray;
    }

}
