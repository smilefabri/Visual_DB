package visualdb.visualdbapi.dbManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import visualdb.visualdbapi.login.TokenStorage;
import visualdb.visualdbapi.tableManager.ResultBoolInt;

@WebServlet(
  name = "VisualDb_dbManager_Servlet",
  urlPatterns = {
    ServicedbManager.RETURN_ALL_TABLE,
    ServicedbManager.SEARCH_PATH,
    ServicedbManager.NEW_PATH,
    ServicedbManager.RENAME_PATH,
    ServicedbManager.DELETE_PATH,
  }
)
public class dbManagerServlet extends HttpServlet {

  ServicedbManager dbManager = new ServicedbManager();

  public void init() {}

  /*
   * GET /database/search :
   * - restituisce il database cercato
   * - value = all restituisce tutti i database
   * */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    JsonArray result = null;
    String token = request.getHeader("authorization");
    String username = TokenStorage.getUsernameFromToken(token);

    if (request.getServletPath().equals(ServicedbManager.SEARCH_PATH)) {
      System.out.println("[DEBUG DATABASE SERVLET][utente:"+username+"] -> Search database dell'utente: " + username);
      result = dbManager.searchDatabase(username);
    }
    dbManager.AddOperationDb(username, TypeOperationOnDb.OP_SEARCH_ALL_DB, 1);

    out.println(result);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    JsonObject result = null;
    response.setContentType("application/json");
    BufferedReader in = request.getReader();
    JsonObject loginObject = JsonParser.parseReader(in).getAsJsonObject();
    String token = request.getHeader("authorization");
    String username = TokenStorage.getUsernameFromToken(token);
    int privilege = TokenStorage.getPrivilegi(username);

    String errorMessage;
    if (request.getServletPath().equals(ServicedbManager.RETURN_ALL_TABLE)) {
      boolean ris = false;
      loginObject = loginObject.get("body").getAsJsonObject();
      int idDb = loginObject.get("idDb").getAsInt();
      PrintWriter out = response.getWriter();
      JsonArray resultArray = null;
      errorMessage = "operazione non riuscita";
      if (dbManager.OwnThisDatabase(idDb, username)) {
        ris = true;
        resultArray = dbManager.returnAllTableFromADatabase(idDb);
      } else {
        errorMessage = "DEBUG: il database non ti appartiene";
      }
      if (!ris) {
        result =
          ServicedbManager.JsonResponsedoPost(
            "cerca tabella",
            ris,
            errorMessage
          );
        System.out.println("[DEBUG DATABASE SERVLET][utente:"+username+"] -> Tutte le tabelle del database con id: " + idDb);
        out.println(result);
      } else {
        dbManager.AddOperationDb(
          username,
          TypeOperationOnDb.OP_RETURN_ALL_TABLE_DB,
          idDb
        );
        System.out.println("[DEBUG DATABASE SERVLET][utente:"+username+"] -> Tutte le tabelle del database con id: " + idDb);

        out.println(resultArray);
      }
    }
    if (request.getServletPath().equals(ServicedbManager.NEW_PATH)) {
      loginObject = loginObject.get("body").getAsJsonObject();
      String NewNamedb = loginObject.get("NewName").getAsString();

      PrintWriter out = response.getWriter();

      ResultBoolInt ris = dbManager.createNewDatabase(username, NewNamedb);

      if (ris.getBoolResult()) {
        System.out.println("[DEBUG DATABASE SERVLET][utente:"+username+"] -> Creazione nuovo database con nome: "+ NewNamedb+", operazione riuscita");
        result =
          ServicedbManager.JsonResponsedoPost(
            "Inserimento",
            ris.getBoolResult(),
            "Inserimento riuscito!"
          );
      } else {
        result =
          ServicedbManager.JsonResponsedoPost(
            "Inserimento",
            ris.getBoolResult(),
            "Inserimento non riuscito"
          );
        System.out.println("[DEBUG DATABASE SERVLET][utente:"+username+"] -> Creazione nuovo database con nome: "+ NewNamedb+", operazione fallita");

      }
      out.println(result);
    }
    if (request.getServletPath().equals(ServicedbManager.RENAME_PATH)) {

      loginObject = loginObject.get("body").getAsJsonObject();
      int id = loginObject.get("id").getAsInt();
      String newName = loginObject.get("NewName").getAsString();
      PrintWriter out = response.getWriter();
      boolean ris = false;
      if (dbManager.OwnThisDatabase(id, username)) {
        ris = dbManager.renameDatabase(id, newName);
      } else {
        errorMessage = "DEBUG: Non possiedi questo db";
      }

      if (ris) {
        dbManager.AddOperationDb(username, TypeOperationOnDb.OP_RENAME_DB, id);

        result =
          ServicedbManager.JsonResponsedoPost(
            "Rename",
            ris,
            "Rename riuscito!"
          );
        System.out.println("[DEBUG DATABASE SERVLET][utente:"+username+"] -> Eliminazione database con id: " +id+" in "+newName+", operazione riuscita");

      } else {
        result =
          ServicedbManager.JsonResponsedoPost(
            "Rename",
            ris,
            "Rename non riuscito"
          );
        System.out.println("[DEBUG DATABASE SERVLET][utente:"+username+"] -> Eliminazione database con id: " +id+" in "+newName+", operazione fallita");

      }
      out.println(result);
    }
  }

  public void doDelete(
    HttpServletRequest request,
    HttpServletResponse response
  ) throws IOException {
    if (request.getServletPath().equals(ServicedbManager.DELETE_PATH)) {
      response.setContentType("application/json");
      boolean ris = false;
      String errorMessage = "delete database Non riuscita!";
      Map<String, String[]> pars = request.getParameterMap();
      //System.out.println("pars: " + pars.toString());
      String id = "";

      if (pars.containsKey("id")) {
        id = pars.get("id")[0];
      }

      int id_db = Integer.parseInt(id);
      String token = request.getHeader("authorization");
      String usernameSession = TokenStorage.getUsernameFromToken(token);
      PrintWriter out = response.getWriter();

      if (dbManager.OwnThisDatabase(id_db, usernameSession)) {
        ris = dbManager.deleteDatabase(id_db, usernameSession);
      } else {
        errorMessage = "DEBUG: non puoi eliminare una table non tua";
      }

      JsonObject result;
      if (ris) {
        result =
          ServicedbManager.JsonResponsedoPost(
            "Delete",
            ris,
            "Delete riuscita!"
          );
        System.out.println("[DEBUG DATABASE SERVLET][utente:"+usernameSession+"] -> Eliminazione database con id: " +id_db+", operazione riuscita");

      } else {
        result =
          ServicedbManager.JsonResponsedoPost("Delete", ris, errorMessage);
        System.out.println("[DEBUG DATABASE SERVLET][utente:"+usernameSession+"] -> Eliminazione database con id: " +id_db+", operazione fallita");

      }

      out.println(result);
    }
  }
}
