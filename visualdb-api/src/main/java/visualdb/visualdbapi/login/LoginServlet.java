package visualdb.visualdbapi.login;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import visualdb.visualdbapi.visualdb.VisualDbManager;

@WebServlet(
        name = "VisualDb_Login_Servlet",
        urlPatterns = {LoginService.LOGIN_PATH, LoginService.LOGOUT_PATH}
)
public class LoginServlet extends HttpServlet {

    public void init() {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String token = request.getHeader("authorization");
        System.out.println("hello world!");
        // GET accetta sia /login che /logout
        // GET /login restituisce il login status corrente
        // GET /logout effettua il logout e restituisce l'esito
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject result = null;
        String username = TokenStorage.getUsernameFromToken(token);
        int privilege = TokenStorage.getPrivilegi(username);
        System.out.println("[DEBUG LOGIN SERVLET] -> status utente: "+username);

        if (request.getServletPath().equals(LoginService.LOGIN_PATH)) {

            if (!username.isEmpty()) {
                result = outputJsonBuild("status", privilege, true, "");
                result.addProperty("username", username);
                result.addProperty("token", token);
            } else {
                result = outputJsonBuild("status", -1, false, "No logged user");
            }
        } else if (request.getServletPath().equals(LoginService.LOGOUT_PATH)) {
            if (!username.isEmpty()) {
                System.out.println("[DEBUG LOGIN SERVLET] -> logout  utente: " + username);
                LoginService.doLogOut(username);
                result = outputJsonBuild("logout", privilege, true, "");
                result.addProperty("username", username);
            } else {
                result = outputJsonBuild("logout", -1, false, "No logged user");
            }
        }
        out.println(result);
    }

    private JsonObject outputJsonBuild(
            String operation,
            int privilege,
            boolean status,
            String errorMessage
    ) {
        JsonObject result = new JsonObject();
        result.addProperty("operation", operation);
        result.addProperty("privilege", privilege);
        result.addProperty("status", status);
        result.addProperty("errorMessage", errorMessage);
        return result;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (request.getServletPath().equals(LoginService.LOGIN_PATH)) {
            response.setContentType("application/json");
            BufferedReader in = request.getReader();
            JsonObject loginObject = JsonParser.parseReader(in).getAsJsonObject();
            loginObject =
                    JsonParser
                            .parseString(loginObject.get("body").getAsString())
                            .getAsJsonObject();
            String username = loginObject.get("username").getAsString();
            String password = loginObject.get("password").getAsString();

            JsonObject result = null;

            int valid = VisualDbManager
                    .getManager()
                    .validateCredentials(username, password);
            if (valid == 0 || valid == 1) {
                System.out.println("[DEBUG LOGIN SERVLET] -> login utente: "+username);
                String token = JwtUtil.generateToken(username);
                TokenStorage.addToken(username, token, valid);
                TokenStorage.printTokenMap();
                response.setStatus(HttpServletResponse.SC_OK);
                result = outputJsonBuild("login", valid, true, "");
                result.addProperty("token", token);
                result.addProperty("username", username);
                VisualDbManager.getManager().insertAccesso(username);
            } else {
                result = outputJsonBuild("login", valid, false, "Invalid credentials");
            }
            PrintWriter out = response.getWriter();
            out.println(result);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    /*
        if (request.getServletPath().equals(LoginService.LOGIN_PATH)) {
            response.setContentType("application/json");
            BufferedReader in = request.getReader();
            JsonObject loginObject = JsonParser.parseReader(in).getAsJsonObject();

            loginObject = JsonParser.parseString(loginObject.get("body").getAsString()).getAsJsonObject();
            String username = loginObject.get("username").getAsString();
            String previous = LoginService.getCurrentLogin(request.getSession());
            int privilege = LoginService.getCurrentPrivilege(request.getSession());
            JsonObject result = null;
            if (!previous.isEmpty() && !previous.equals(username)) {
                result = outputJsonBuild("login", previous, privilege, false, true, "Already logged in as a different user.");
            } else {
                String password = loginObject.get("password").getAsString();
                int valid = VisualDbManager.getManager().validateCredentials(username, password);
                if (valid==0 || valid==1) {
                    if (previous.isEmpty()) LoginService.doLogIn(request.getSession(), username, valid);
                    result = outputJsonBuild("login", username, valid, true, false, "");
                    result.addProperty("errorMessage", "");
                } else {
                    result = outputJsonBuild("login", username, valid, false, true, "Invalid credentials");
                }
            }
            PrintWriter out = response.getWriter();
            out.println(result);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

         */
    }

    public void destroy() {
    }
}
