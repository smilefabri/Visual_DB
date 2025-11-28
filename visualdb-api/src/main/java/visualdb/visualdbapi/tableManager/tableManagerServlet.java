package visualdb.visualdbapi.tableManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import visualdb.visualdbapi.dbManager.ServicedbManager;
import visualdb.visualdbapi.login.LoginService;
import visualdb.visualdbapi.login.TokenStorage;

@WebServlet(
  name = "table_Manager_servlet",
  urlPatterns = {
    ServiceTableManager.RENAME_PATH,
    ServiceTableManager.CREATE_PATH,
    ServiceTableManager.SEARCH_PATH,
    ServiceTableManager.SAVE_PATH,
    ServiceTableManager.DELETE_PATH,
    ServiceTableManager.ADDCOLUMN_PATH,
  }
)
public class tableManagerServlet extends HttpServlet {

  ServiceTableManager tableManager = new ServiceTableManager();
  ServicedbManager dbManager = new ServicedbManager();

  public void init() {}

  public void doGet(HttpServletRequest request, HttpServletResponse response) {}

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    JsonObject result = null;
    String errorMessage;
    boolean ris = false;
    response.setContentType("application/json");
    BufferedReader in = request.getReader();
    JsonObject loginObject = JsonParser.parseReader(in).getAsJsonObject();
    String token = request.getHeader("authorization");
    String usernameSession = TokenStorage.getUsernameFromToken(token);
    int privilege = TokenStorage.getPrivilegi(usernameSession);
    PrintWriter out = response.getWriter();

    if (request.getServletPath().equals(ServiceTableManager.SAVE_PATH)) {
      System.out.println(loginObject);
      String nameTable = loginObject.get("nameTable").getAsString();
      nameTable = nameTable.replace(' ','_');
      int idTable = loginObject.get("idTable").getAsInt();
      int idDb = loginObject.get("idDb").getAsInt();
      JsonArray arrayTable = loginObject.get("array").getAsJsonArray();
      errorMessage = "errore...con il salvataggio";
      if (tableManager.ownThisTable(nameTable, usernameSession, idTable)) {

        ris = tableManager.saveTable(nameTable, idDb, arrayTable);
      } else {
        errorMessage = "DEBUG: non puoi modificare una tabella non tua";
      }
      if (ris) {
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] ->  salvataggio tabella: "+nameTable+", operazione riuscita.");
        tableManager.AddOperationTable(
          usernameSession,
          TypeOperationOnTable.OP_SAVE_TABLE,
          idTable
        );
        result =
          ServicedbManager.JsonResponsedoPost(
            "Creazione tabella",
            ris,
            "creazione riuscita!"
          );
      } else {
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] ->  salvataggio tabella: "+nameTable+", operazione fallita.");
        result =
          ServicedbManager.JsonResponsedoPost(
            "Creazione tabella",
            ris,
            errorMessage
          );
      }
      out.println(result);
    }
    if (request.getServletPath().equals(ServiceTableManager.SEARCH_PATH)) {
      errorMessage = "errore... non trovo la tabella";
      //System.out.println("Table manager");
      //System.out.println("login obj: " + loginObject);
      loginObject = loginObject.get("body").getAsJsonObject();
      String nameTable = loginObject.get("nameTable").getAsString();
      nameTable = nameTable.replace(' ','_');
      int idDb = loginObject.get("idDb").getAsInt();
      int idTable = loginObject.get("idTable").getAsInt();

      JsonArray resultArray = null;

      if (tableManager.ownThisTable(nameTable, usernameSession, idTable)) {
        ris = true;
        resultArray = tableManager.searchTable(nameTable, idDb);
        //System.out.println("resultArray " + resultArray);
      } else {
        errorMessage = "DEBUG: non puoi cercare una tabella non tua";
      }
      if (!ris) {
        System.out.println("[DEBUG TABLE SERVLET] [utente:"+usernameSession+"] ->  cercare la tabella: "+nameTable+", operazione fallita.");
        result =
          ServicedbManager.JsonResponsedoPost(
            "cerca tabella",
            ris,
            errorMessage
          );
        out.println(result);
      } else {
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] -> cercare la tabella: "+nameTable+", operazione riuscita.");
        tableManager.AddOperationTable(
          usernameSession,
          TypeOperationOnTable.OP_SEARCH_TABLE,
          idTable
        );
        out.println(resultArray);
      }
    }
    if (request.getServletPath().equals(ServiceTableManager.CREATE_PATH)) {
      System.out.println(loginObject);
      loginObject = loginObject.get("body").getAsJsonObject();
      System.out.println(loginObject);
      String nameTable = loginObject.get("NameTable").getAsString();
      nameTable = nameTable.replace(' ','_');
      int idDb = loginObject.get("idDb").getAsInt();
      ResultBoolInt resultCreate = new ResultBoolInt(false, 0);
      errorMessage = "tabella non creata";

      System.out.println(dbManager.OwnThisDatabase(idDb, usernameSession));
      if (dbManager.OwnThisDatabase(idDb, usernameSession)) {
        if (!tableManager.existTable(nameTable, idDb)) {
          resultCreate = tableManager.createTable(nameTable, idDb);

        } else {
          errorMessage = "la tabella esiste di già";
        }
      } else {
        errorMessage = "non possiedi questo database";
      }
      if (resultCreate.getBoolResult()) {
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] -> creazione della tabella: "+nameTable+", operazione riuscita.");
        result =
          ServicedbManager.JsonResponsedoPost(
            "Creazione tabella",
            resultCreate.getBoolResult(),
            "creazione riuscita!"
          );
          result.addProperty("idtable",resultCreate.getId());
      } else {
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] -> creazione della tabella: "+nameTable+", operazione fallita.");
        result =
          ServicedbManager.JsonResponsedoPost(
            "Creazione tabella",
            ris,
            errorMessage
          );
      }
      out.println(result);
    }
    if (request.getServletPath().equals(ServiceTableManager.ADDCOLUMN_PATH)) {
      System.out.println("Riga 128: " + loginObject);
      String nameTable = loginObject.get("nameTable").getAsString();
      nameTable = nameTable.replace(' ','_');
      String nameNewColumn = loginObject.get("nameNewColumn").getAsString();
      int idDb = loginObject.get("idDb").getAsInt();
      int idTable = loginObject.get("idTable").getAsInt();
      String typeData = loginObject.get("typeData").getAsString();
      errorMessage = "colonna  non creata";

      String completeName = nameTable + "_databaseid_" + idDb;
      if (tableManager.ownThisTable(nameTable, usernameSession, idTable)) {
        if (!tableManager.doesColumnExist(completeName, nameNewColumn)) {
          ris =
            tableManager.addcolumn(nameNewColumn, nameTable, typeData, idDb);
        } else {
          errorMessage = "la collona esiste già";
        }
      } else {
        errorMessage =
          "DEBUG: non puoi aggiungere una collona a una table non tua";
      }

      if (ris) {
        tableManager.AddOperationTable(
          usernameSession,
          TypeOperationOnTable.OP_ADDCOLUMN_TABLE,
          idTable
        );
        result =
          ServicedbManager.JsonResponsedoPost(
            "nuova colonna",
            ris,
            "Inserimento riuscito!"
          );
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] -> aggiunta nuova collona alla tabella: "+nameTable+", operazione riuscita.");
      } else {
        result =
          ServicedbManager.JsonResponsedoPost(
            "Nuova colonna",
            ris,
            errorMessage
          );
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] -> aggiunta nuova collona alla tabella: "+nameTable+", operazione fallita.");

      }
      out.println(result);
    }
    if (request.getServletPath().equals(ServiceTableManager.RENAME_PATH)) {

      loginObject = loginObject.get("body").getAsJsonObject();

      int idTable = loginObject.get("idTable").getAsInt();
      String nameTable = loginObject.get("nameTable").getAsString();
      nameTable = nameTable.replace(' ','_');
      int idDb = loginObject.get("idDatabase").getAsInt();
      String newName = loginObject.get("newNameTable").getAsString();
      errorMessage = "cambio nome non riuscito";
      if (tableManager.ownThisTable(nameTable, usernameSession, idTable)) {
        ris = tableManager.renameTable(idTable, nameTable, idDb, newName);
      } else {
        errorMessage = "DEBUG: non puoi rinominare una tabella non tua";
      }
      if (ris) {
        tableManager.AddOperationTable(
          usernameSession,
          TypeOperationOnTable.OP_RENAME_TABLE,
          idTable
        );
        result =
          ServicedbManager.JsonResponsedoPost(
            "nuova colonna",
            ris,
            "Inserimento riuscito!"
          );
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] -> rinominazione della tabella: "+nameTable+" in "+newName+", operazione riuscita.");

      } else {
        result =
          ServicedbManager.JsonResponsedoPost(
            "Nuova colonna",
            ris,
            errorMessage
          );
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] -> rinominazione della tabella: "+nameTable+" in "+newName+", operazione fallita.");


      }
      out.println(result);
    }
  }

  public void doDelete(
    HttpServletRequest request,
    HttpServletResponse response
  ) throws IOException {
    if (request.getServletPath().equals(ServiceTableManager.DELETE_PATH)) {
      JsonObject result = null;
      response.setContentType("application/json");
      Map<String, String[]> pars = request.getParameterMap();
      System.out.println("pars: " + pars.toString());

      int idDb = 0;
      int idTable = 0;
      String nameTable = "";
      if (
        pars.containsKey("idDb") &&
        pars.containsKey("idTable") &&
        pars.containsKey("nameTable")
      ) {
        idDb = Integer.parseInt(pars.get("idDb")[0]);
        idTable = Integer.parseInt(pars.get("idTable")[0]);
        nameTable = pars.get("nameTable")[0];
        nameTable = nameTable.replace(' ','_');
      } else {
        //i valori non sono presenti nel url
      }

      String token = request.getHeader("authorization");
      String usernameSession = TokenStorage.getUsernameFromToken(token);
      PrintWriter out = response.getWriter();
      String errorMessage = " Delete non riuscita!";
      boolean ris = false;

      if (tableManager.ownThisTable(nameTable, usernameSession, idTable)) {
        ris = tableManager.deleteTable(nameTable, idDb);
      } else {
        errorMessage = "DEBUG: non puoi eliminare una table non tua";
      }
      if (ris) {
        result =
          ServicedbManager.JsonResponsedoPost(
            "Delete",
            ris,
            "Delete riuscita!"
          );
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] -> rinominazione della tabella: "+nameTable+", operazione riuscita.");

      } else {
        result =
          ServicedbManager.JsonResponsedoPost("Delete", ris, errorMessage);
        System.out.println("[DEBUG TABLE SERVLET][utente:"+usernameSession+"] -> eliminazione della tabella: "+nameTable+", operazione fallita.");

      }
      out.println(result);
    }
  }

  public void destroy() {}
}
