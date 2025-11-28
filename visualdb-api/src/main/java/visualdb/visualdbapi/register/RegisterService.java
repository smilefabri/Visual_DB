package visualdb.visualdbapi.register;

import com.google.gson.JsonObject;
import java.sql.*;
import visualdb.visualdbapi.db.PoolingPersistenceManager;
import visualdb.visualdbapi.tableManager.ResultBoolInt;

public class RegisterService {

  public static final String REGISTER_PATH = "/register";

  JsonObject responseJsonRegister(
    String operation,
    boolean status,
    String errorMessage
  ) {
    JsonObject result = new JsonObject();
    result.addProperty("operation", operation);
    result.addProperty("status", status);
    result.addProperty("errorMessage", errorMessage);
    return result;
  }

  public boolean existUsername(String Username) {
    boolean state = false;

    String query =
      "SELECT COUNT(*) FROM \"VisualDB\".public.\"Utenti\" WHERE username = ? ";

    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, Username);

      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        int rowCount = rs.getInt(1);
        state = rowCount > 0;
      }
      st.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return state;
  }

  public boolean existEmail(String email) {
    boolean state = false;

    String query =
      "SELECT COUNT(*) FROM \"VisualDB\".public.\"Utenti\" WHERE email = ? ";

    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, email);

      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        int rowCount = rs.getInt(1);
        state = rowCount > 0;
      }
      st.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return state;
  }

  public ResultBoolInt RegisterUsername(
    String username,
    String email,
    String password,
    int privilegi
  ) {
    ResultBoolInt result = new ResultBoolInt(false, 0);

    String query =
      "INSERT INTO \"VisualDB\".public.\"Utenti\" (username,email,password,privilegi) VALUES (?, ?,?,?)";
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, username);
      st.setString(2, email);
      st.setString(3, password);
      st.setInt(4, privilegi);

      int rowsAffected = st.executeUpdate();

      if (rowsAffected > 0) {

        result = new ResultBoolInt(true, 0);
      }
      st.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return result;
  }
}
