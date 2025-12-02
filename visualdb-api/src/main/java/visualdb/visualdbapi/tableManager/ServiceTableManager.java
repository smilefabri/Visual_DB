package visualdb.visualdbapi.tableManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.sql.*;
import visualdb.visualdbapi.db.PoolingPersistenceManager;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ServiceTableManager {

  public static final String SEARCH_PATH = "/table/search";
  public static final String CREATE_PATH = "/table/create";
  public static final String ADDCOLUMN_PATH = "/table/addcolumn";
  public static final String DELETE_PATH = "/table/delete";
  public static final String SAVE_PATH = "/table/save";
  public static final String RENAME_PATH = "/table/rename";

  public ServiceTableManager() {}
    private static final Logger logger = Logger.getLogger(ServiceTableManager.class.getName());

    public static JsonArray ResultSetToJson(ResultSet rs) throws SQLException {
    JsonArray jsonArray = new JsonArray();
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();
    if (!rs.isBeforeFirst()) { // Check if the ResultSet is empty
      // If ResultSet is empty, add a JSON object with null values for each column
      JsonObject nullRow = new JsonObject();
      for (int i = 1; i <= columnCount; i++) {
        String columnName = metaData.getColumnName(i);
        if (columnName.equals("id")) {
          nullRow.addProperty(columnName, 1); // Set id to 1
        } else {
          nullRow.addProperty(columnName, (String) null); // Set other columns to null
        }
      }
      jsonArray.add(nullRow);
    } else {
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
    }

    return jsonArray;
  }

  public JsonArray searchTable(String nameTable, int idDb) {
    JsonArray result = null;
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      String sql =
        "SELECT * FROM \"VisualDB\".public." +
        nameTable +
        "_databaseid_" +
        idDb;

      PreparedStatement st = conn.prepareStatement(sql);
      ResultSet rs = st.executeQuery();
      result = ResultSetToJson(rs);

      st.close();
      rs.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE,"errore durante la consulta del table: "+nameTable,e);
    }

    return result;
  }

  public JsonArray searchAllTableById(int idDb) {
    JsonArray result = new JsonArray();
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      String query =
        "SELECT nome FROM \"VisualDB\".public.\"Table\" WHERE database = ?";

      PreparedStatement st = conn.prepareStatement(query);
      st.setInt(1, idDb);
      ResultSet rs = st.executeQuery();

      while (rs.next()) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("nome", rs.getString("nome"));
        result.add(jsonObject);
      }


      st.close();
      rs.close();
    } catch (SQLException e) {
        logger.log(Level.SEVERE,"errore durante la consulta della table by id: "+ idDb ,e);
    }
    return result;
  }

  public Boolean deleteTable(String nameTable, int idDb) {
    String query = "DROP TABLE IF EXISTS " + nameTable + "_databaseId_" + idDb;

    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(query);
      int rowsAffected = st.executeUpdate();
      if (rowsAffected > 0) {

        return false;
      }
      st.close();
    } catch (SQLException e) {
        logger.log(Level.SEVERE,"errore durante l'eliminazione della table: "+nameTable,e);
    }

    boolean result = false;
    String deleteQuery =
      "DELETE FROM \"VisualDB\".public.\"Table\" WHERE nome = ? AND database = ?";
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(deleteQuery);
      st.setInt(2, idDb);
      st.setString(1, nameTable);

      int rowsAffected = st.executeUpdate();
      if (rowsAffected > 0) {
        //System.out.println("eliminazione riuscita!");
        result = true;
      }
      st.close();
    } catch (SQLException e) {
        logger.log(Level.SEVERE,"errore durante l'eliminazione della table: "+nameTable,e);
    }
    return result;
  }

  public boolean AddOperationTable(
    String username,
    String typeOfOp,
    int idTable
  ) {
    boolean result = true;
    String query =
      "INSERT INTO \"VisualDB\".public.\"OperazioneTable\" (utente,table,data_operazione,tipo,ora_operazione) VALUES (?,?,?,?,?)";
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      Date currentDate = new Date(System.currentTimeMillis());
      Time timeCreation = new Time(System.currentTimeMillis());
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, username);
      st.setInt(2, idTable);
      st.setDate(3, currentDate);
      st.setString(4, typeOfOp);
      st.setTime(5, timeCreation);
      int rowsAffected = st.executeUpdate();

      if (!(rowsAffected > 0)) {
        result = false;
      }

      st.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE,"errore durante la modifica,con tipo operazione: "+typeOfOp+",  da parte dell'utente: "+username+", alla tabella cond id: "+idTable,e);
    }
    return result;
  }

  public ResultBoolInt createTable(String nomeTable, int idDatabase) {
    boolean result = true;
    int generatedId = 0;
    String query =
      "INSERT INTO \"VisualDB\".public.\"Table\" (nome,database,data_creazione,ora_creazione) VALUES (?,?,?,?)";

    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {

      Date currentDate = new Date(System.currentTimeMillis());
      Time timeCreation = new Time(System.currentTimeMillis());
      //controllo se la stringa ha spazi se ha spazi le sostituisco con un a _
      nomeTable = nomeTable.replace(' ','_');


      PreparedStatement st = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
      st.setString(1, nomeTable);
      st.setInt(2, idDatabase);
      st.setDate(3, currentDate);
      st.setTime(4, timeCreation);
      int rowsAffected = st.executeUpdate();

      if (rowsAffected > 0) {
        ResultSet generatedKeys = st.getGeneratedKeys();
        if (generatedKeys.next()) {
          generatedId = generatedKeys.getInt(1);
        } else {
            logger.severe("Errore nel recupero dell'ID generato");
        }
      } else {
        return new ResultBoolInt(false, 0);
      }

      st.close();

      String queryNewTable =
        "CREATE TABLE IF NOT EXISTS " +
        nomeTable +
        "_databaseId_" +
        idDatabase +
        "(id serial PRIMARY KEY)";
      PreparedStatement preparedStatement = conn.prepareStatement(
        queryNewTable
      );

      rowsAffected = preparedStatement.executeUpdate();

      if (rowsAffected > 0) {
        result = false;
      }
      preparedStatement.close();
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Errore durante la creazione della table: "+nomeTable+", nel database con id: "+idDatabase, e);
    }
    return new ResultBoolInt(result, generatedId);
  }

  public boolean doesColumnExist(String tableName, String columnName) {
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      DatabaseMetaData metaData = conn.getMetaData();

      try (
        ResultSet columns = metaData.getColumns(
          null,
          null,
          tableName,
          columnName
        )
      ) {
        return columns.next(); // Restituirà true se la colonna esiste
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }

  public boolean addcolumn(
    String nameNewColumn,
    String nameTable,
    String typeDataColumn,
    int idDb
  ) {
    //TODO: inserire un po di controlli
    boolean result = true;

    if (
      !(
        typeDataColumn.equalsIgnoreCase(typeData.TYPE_INT) ||
        typeDataColumn.equalsIgnoreCase(typeData.TYPE_FLOAT) ||
        typeDataColumn.equalsIgnoreCase(typeData.TYPE_DECIMAL) ||
        typeDataColumn.equalsIgnoreCase(typeData.TYPE_CHAR) ||
        typeDataColumn.equalsIgnoreCase(typeData.TYPE_BOOLEAN)
      )
    ) {
      return false;
    }
    String completeName = nameTable + "_databaseid_" + idDb;

    String query =
      "ALTER TABLE " +
      completeName +
      " ADD COLUMN " +
      nameNewColumn +
      " " +
      typeDataColumn;

    System.out.println(query);
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      Statement st = conn.createStatement();
      st.execute(query);

      st.close();
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Errore durante la creazione di una colonna:"+nameNewColumn+", nella table: "+nameTable+", con tipo:"+typeDataColumn+", nel database con id: "+ idDb, e);
    }
    return result;
  }

  public boolean renameTable(
    int idTable,
    String nameTable,
    int idDatabase,
    String newName
  ) {
    boolean result = false;
    String query =
      "UPDATE \"VisualDB\".public.\"Table\" SET nome = ? WHERE id = ?";
    String queryTwo =
      "ALTER TABLE " +
      nameTable +
      "_databaseId_" +
      idDatabase +
      " RENAME TO " +
      newName +
      "_databaseId_" +
      idDatabase;
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, newName);
      st.setInt(2, idTable);
      int rowsAffected = st.executeUpdate();

      if (rowsAffected > 0) {
        result = true;
      }

      PreparedStatement preparedStatement = conn.prepareStatement(queryTwo);

      rowsAffected = preparedStatement.executeUpdate();

      if (rowsAffected > 0) {
        //System.out.println(" rename: Inserimento riuscito!");
        result = false;
      }

      st.close();
      preparedStatement.close();
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Errore nel rinominare la table: "+nameTable+"nel database con id: " + idDatabase, e);
    }

    return result;
  }

  private static void setParameterValue(
    PreparedStatement preparedStatement,
    int parameterIndex,
    String type,
    JsonElement jsonElement
  ) throws SQLException {
    System.out.println("type2:" + type + " temp:" + jsonElement);
    if (jsonElement.isJsonPrimitive()) {
      JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();

      if (
        jsonPrimitive.isJsonNull() ||
        jsonPrimitive.getAsString().trim().equalsIgnoreCase("null") ||
        jsonPrimitive.getAsString().trim().isEmpty()
      ) {
        preparedStatement.setNull(parameterIndex, Types.NULL);
      } else {
        String JsontoStringPrimitive = jsonPrimitive
          .toString()
          .substring(1, jsonPrimitive.toString().length() - 1);

        if (type.equalsIgnoreCase(typeData.TYPE_INT)) {
          if (isInteger(JsontoStringPrimitive)) {

            preparedStatement.setObject(
              parameterIndex,
              jsonPrimitive.getAsInt()
            );
          } else {
            preparedStatement.setNull(parameterIndex, Types.NULL);
          }
        } else if (type.equalsIgnoreCase(typeData.TYPE_BOOLEAN)) {
          if (isBoolean(JsontoStringPrimitive)) {

            preparedStatement.setObject(
              parameterIndex,
              jsonPrimitive.getAsBoolean()
            );
          } else {
            preparedStatement.setNull(parameterIndex, Types.NULL);
          }
        } else if (type.equalsIgnoreCase(typeData.TYPE_DECIMAL)) {
          if (isInteger(JsontoStringPrimitive)) {

            preparedStatement.setObject(
              parameterIndex,
              jsonPrimitive.getAsNumber()
            );
          } else {
            preparedStatement.setNull(parameterIndex, Types.NULL);
          }
        } else if (type.equalsIgnoreCase(typeData.TYPE_CHAR)) {

          preparedStatement.setObject(
            parameterIndex,
            jsonPrimitive.getAsString()
          );
        } else if (type.equalsIgnoreCase(typeData.TYPE_FLOAT)) {
          if (isFloat(JsontoStringPrimitive)) {

            preparedStatement.setObject(
              parameterIndex,
              jsonPrimitive.getAsFloat()
            );
          } else {
            preparedStatement.setNull(parameterIndex, Types.NULL);
          }
        }
      }
    } else {

      //System.out.println("caso strano null, forse un errore");
      preparedStatement.setNull(parameterIndex, Types.NULL);
    }
  }

  // Metodo per verificare se la stringa è un intero
  public static boolean isInteger(String s) {
    try {
      Integer.parseInt(s);

      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean isDouble(String s) {
    try {
      Double.parseDouble(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // Metodo per verificare se la stringa è un booleano
  public static boolean isBoolean(String s) {
    return s.equalsIgnoreCase("True") || s.equalsIgnoreCase("False")|| s.equalsIgnoreCase("1")|| s.equalsIgnoreCase("0");
  }

  // Metodo per verificare se la stringa è un float
  public static boolean isFloat(String s) {
    try {
      Double.parseDouble(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
  //TODO: aggiungere il commit false visto che ci sono due operazioni fa eseguire
  private void insertRow(
    Connection conn,
    String nameTable,
    JsonObject asJsonObject
  ) throws SQLException {


    String queryColumnAndType =
      "SELECT column_name, data_type " +
      "FROM \"VisualDB\".information_schema.\"columns\" " +
      "WHERE table_schema = 'public' " +
      "AND table_name = '" +
      nameTable.toLowerCase() +
      "'";
    conn.setAutoCommit(false);

    //System.out.println(queryColumnAndType);
    JsonArray jsonArray = new JsonArray();


    try (
        PreparedStatement preparedStatement = conn.prepareStatement(
          queryColumnAndType
        )
      ) {
        ResultSet rs = preparedStatement.executeQuery();
        conn.commit();

        // Creare un oggetto JsonArray per contenere i risultati

        while (rs.next()) {
          // Creare un oggetto JsonObject per ogni riga nella query
          JsonObject jsonObject = new JsonObject();
          jsonObject.addProperty("column_name", rs.getString("column_name"));
          jsonObject.addProperty("data_type", rs.getString("data_type"));
          // Aggiungere l'oggetto JsonObject all'array
          jsonArray.add(jsonObject);
        }

        // Chiudere il PreparedStatement e il ResultSet
        preparedStatement.close();
        rs.close();

        // Stampare l'array JSON risultante
        //System.out.println("JSON Result: " + jsonArray);
      } catch (SQLException e) {
        conn.setAutoCommit(true);
        logger.log(Level.SEVERE, "Errore durante l'esecuzione della query le informazioni: "+jsonArray+",\n per inserire una nuova row nella table: "+nameTable, e);
        throw new RuntimeException(
          "Errore durante l'esecuzione della query",
          e
        );
      }

    StringBuilder columns = new StringBuilder();
    StringBuilder values = new StringBuilder();
    //System.out.println("Line 287: asJsonObject ->: " + asJsonObject);
    for (String key : asJsonObject.keySet()) {
      if (!key.equals("id")) {
        columns.append(key).append(",");
        values.append("?,");
      }
    }
    if (columns.length() != 0 && values.length() != 0) {


      // Rimuovi l'ultima virgola dalle stringhe columns e values

      columns.deleteCharAt(columns.length() - 1);
      values.deleteCharAt(values.length() - 1);

      String query =
        "INSERT INTO " +
        nameTable +
        " (" +
        columns +
        ") VALUES (" +
        values +
        ") " +
        "ON CONFLICT (id) DO UPDATE SET " +
        asJsonObject
          .keySet()
          .stream()
          .filter(key -> !key.equals("id"))
          .map(key -> key + " = EXCLUDED." + key)
          .reduce((a, b) -> a + ", " + b)
          .orElse("");

      try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
        int parameterIndex = 1;

        for (String key : asJsonObject.keySet()) {

          if (!key.equals("id")) {

            for (JsonElement obj : jsonArray) {
              JsonObject jsonObject = (JsonObject) obj;
              String columnName = jsonObject.get("column_name").toString();
              columnName = columnName.substring(1, columnName.length() - 1);
              String type = jsonObject.get("data_type").toString();
              type = type.substring(1, type.length() - 1);
              //System.out.println("sono uguali:"+key+"=="+columnName+",bool:"+ key.equals(columnName.substring(1, columnName.length()-1)));
              if (key.equalsIgnoreCase(columnName)) {

                setParameterValue(
                  preparedStatement,
                  parameterIndex,
                  type,
                  asJsonObject.get(key)
                );
                parameterIndex++;
              }
            }
          }
        }

        preparedStatement.executeUpdate();
        conn.commit();
      } catch (SQLException e) {
          logger.log(Level.SEVERE, "error durante l'esecuzione, avvio rollback", e);
        conn.rollback();
        throw new RuntimeException(e);
      }finally {
          conn.setAutoCommit(true);
      }
    }
  }

  private static void deleteTableContent(
    Connection connection,
    String nameTable
  ) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      // Cancella il contenuto della tabella
      statement.executeUpdate("DELETE FROM " + nameTable);
      // Resettare il contatore della chiave primaria
      statement.executeUpdate(
        "TRUNCATE TABLE " + nameTable + " RESTART IDENTITY"
      );
    }
  }

  public boolean saveTable(String nameTable, int idDb, JsonArray arrayTable) {
    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      String completeName = nameTable + "_databaseId_" + idDb;
      deleteTableContent(conn, completeName);

      if (arrayTable != null) {
        for (JsonElement jsonElement : arrayTable) {
          if (jsonElement.isJsonObject()) {
            insertRow(conn, completeName, jsonElement.getAsJsonObject());
          }
        }
      } else {
        return false;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean existTable(String nameTable, int idDb) {
    boolean result = false;
    String query =
      "SELECT COUNT(*) FROM \"VisualDB\".public.\"Table\" WHERE nome = ? AND database = ?  ";

    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, nameTable);
      st.setInt(2, idDb);

      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        int rowCount = rs.getInt(1);
        result = rowCount > 0;
      }

      st.close();
      rs.close();
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Errore nel verifare se la table: "+nameTable+": nel database con id: "+idDb+", esiste", e);
    }

    return result;
  }

  public boolean ownThisTable(
    String nameTableToDelete,
    String nameOfOwner,
    int idTable
  ) {
    boolean result = false;
    String query =
      "SELECT COUNT(*) FROM \"VisualDB\".public.\"Table\" " +
      "JOIN \"VisualDB\".public.\"Database\" ON \"VisualDB\".public.\"Table\".database = \"VisualDB\".public.\"Database\".id " +
      "WHERE \"VisualDB\".public.\"Table\".nome = ? AND \"VisualDB\".public.\"Database\".proprietario = ? AND \"VisualDB\".public.\"Table\".id = ?";

    try (
      Connection conn = PoolingPersistenceManager
        .getPersistenceManager()
        .getConnection()
    ) {
      PreparedStatement st = conn.prepareStatement(query);
      st.setString(1, nameTableToDelete);
      st.setString(2, nameOfOwner);
      st.setInt(3, idTable);

      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        int rowCount = rs.getInt(1);
        result = rowCount > 0;
      }

      st.close();
      rs.close();
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Errore durante la verifica del posidimento della table: "+nameTableToDelete+" da parte di: "+nameOfOwner, e);
    }

    return result;

  }
}
