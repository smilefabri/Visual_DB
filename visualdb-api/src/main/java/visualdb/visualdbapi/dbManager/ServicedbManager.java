package visualdb.visualdbapi.dbManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import visualdb.visualdbapi.db.PoolingPersistenceManager;
import visualdb.visualdbapi.tableManager.ResultBoolInt;
import visualdb.visualdbapi.tableManager.ServiceTableManager;

public class ServicedbManager {

  public static final String SEARCH_PATH = "/database/search";
  public static final String DELETE_PATH = "/database/delete";
  public static final String NEW_PATH = "/database/new";
  public static final String RETURN_ALL_TABLE = "/database/returntable";
  public static final String RENAME_PATH = "/database/rename";

  private static final Logger logger = Logger.getLogger(ServicedbManager.class.getName());

  public ServicedbManager() {}

  //GET Una funzione generica che converta i risultati della ricerca in json
  public static JsonArray ResultSetToJson(ResultSet rs) throws SQLException {
    JsonArray jsonArray = new JsonArray();
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();
    while (rs.next()) {
      JsonObject jsonObject = new JsonObject();

      for (int i = 1; i <= columnCount; i++) {
        String columnName = metaData.getColumnLabel(i);
        Object value = rs.getObject(i);
        jsonObject.addProperty(
          columnName,
          value != null ? value.toString() : null
        );
      }

      jsonArray.add(jsonObject);
    }

    return jsonArray;
  }

  public static JsonObject JsonResponsedoPost(
    String operation,
    boolean success,
    String Message
  ) {
    JsonObject result = new JsonObject();
    result.addProperty("operation", operation);
    result.addProperty("success", success);
    result.addProperty("Message", Message);
    return result;
  }

  // POST
  public JsonArray searchDatabase(String username) {
    JsonArray result = null;
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(
        "SELECT * FROM \"VisualDB\".public.\"Database\" WHERE proprietario = ? "
      );
      st.setString(1, username);
      ResultSet rs = st.executeQuery();
      result = ResultSetToJson(rs);

      st.close();
      rs.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }

    return result;
  }

  //POST
  public ResultBoolInt createNewDatabase(String username, String Newdbname) {
    ResultBoolInt result = new ResultBoolInt(false, 0);
    String query =
      "INSERT INTO \"VisualDB\".public.\"Database\" (nome,proprietario,data_creazione,ora_creazione) VALUES (?, ?,?,?)";
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      Date currentDate = new Date(System.currentTimeMillis());
      Time timeCreation = new Time(System.currentTimeMillis());
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, Newdbname);
      st.setString(2, username);
      st.setDate(3, currentDate);
      st.setTime(4, timeCreation);

      int rowsAffected = st.executeUpdate();

      if (rowsAffected > 0) {
        //System.out.println("Inserimento riuscito!");
        result = new ResultBoolInt(true, 0);
      }
      st.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }

    return result;
  }

  //DELETE
  public boolean deleteDatabase(int id, String username) {
    boolean result = false;
    String query =
      "DELETE FROM \"VisualDB\".public.\"Database\" WHERE id = ? AND proprietario = ?";
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      //elimina tutte le tabelle con quel id

      ServiceTableManager tableManager = new ServiceTableManager();

      JsonArray tableToDelete = tableManager.searchAllTableById(id);
      //se il database non ha tabelle posso semplicemente eliminarlo altrimenti elimino prima le tabelle;


      if (!tableToDelete.isEmpty()) {
        for (JsonElement c : tableToDelete) {
          JsonObject jsonObject = c.getAsJsonObject();
          tableManager.deleteTable(jsonObject.get("nome").getAsString(), id);
        }
      }

      // elimina il database, dopo aver eliminato tutte le tabelle di quel db
      PreparedStatement st = conn.prepareStatement(query);
      st.setInt(1, id);
      st.setString(2, username);

      int rowsAffected = st.executeUpdate();
      if (rowsAffected > 0) {
        //System.out.println("eliminazione riuscita!");
        result = true;
      }
      st.close();
    } catch (SQLException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return result;
  }

  public boolean renameDatabase(int id, String newName) {
    boolean result = false;
    String query =
      "UPDATE \"VisualDB\".public.\"Database\" SET nome = ? WHERE id = ?";
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, newName);
      st.setInt(2, id);
      int rowsAffected = st.executeUpdate();

      if (rowsAffected > 0) {
        result = true;
      }
      st.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }

    return result;
  }

  public boolean OwnThisDatabase(int idDb, String nameUser) {
    boolean result = false;
    String query =
      "SELECT COUNT(*) FROM \"VisualDB\".public.\"Database\" WHERE proprietario = ? AND id = ? ";

    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, nameUser);
      st.setInt(2, idDb);

      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        int rowCount = rs.getInt(1);
        result = rowCount > 0;
      }
      st.close();
      rs.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }

    return result;
  }

  public JsonArray returnAllTableFromADatabase(int idDb) {
    JsonArray result = null;
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(
        "SELECT * FROM \"VisualDB\".public.\"Table\" WHERE database = ? "
      );
      st.setInt(1, idDb);
      ResultSet rs = st.executeQuery();
      result = ResultSetToJson(rs);

      st.close();
      rs.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }

    return result;
  }

  public boolean AddOperationDb(String username, String typeOfOp, int idDb) {
    boolean result = true;
    String query =
      "INSERT INTO \"VisualDB\".public.\"OperazioneDatabase\" (username,database,data_operazione,tipo,ora_operazione) VALUES (?,?,?,?,?)";
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      Date currentDate = new Date(System.currentTimeMillis());
      Time timeCreation = new Time(System.currentTimeMillis());
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, username);
      st.setInt(2, idDb);
      st.setDate(3, currentDate);
      st.setString(4, typeOfOp);
      st.setTime(5, timeCreation);
      int rowsAffected = st.executeUpdate();

      if (!(rowsAffected > 0)) {
        return false;
      }

      st.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return result;
  }
}
