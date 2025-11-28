package visualdb.visualdbapi.register;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.*;

@WebServlet(
  name = "VisualDb_Register_Servlet",
  urlPatterns = { RegisterService.REGISTER_PATH }
)
public class RegisterServlet extends HttpServlet {

  public void init() {}

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    RegisterService registerService = new RegisterService();
    response.setContentType("application/json");
    BufferedReader in = request.getReader();

    JsonObject registerObject = JsonParser.parseReader(in).getAsJsonObject();
    registerObject =
      JsonParser
        .parseString(registerObject.get("body").getAsString())
        .getAsJsonObject();
    String username = registerObject.get("username").getAsString();
    String email = registerObject.get("email").getAsString();
    String password = registerObject.get("password").getAsString();
    String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    if (request.getServletPath().equals(RegisterService.REGISTER_PATH)) {
      PrintWriter out = response.getWriter();
      JsonObject result = null;

      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(email);

      if ((username == null || username.isEmpty())) {
        System.out.println("[DEBUG REGISTER SERVLET]: tentativo di registrazione fallita, errore: l'utente esiste di già");
        result =
          registerService.responseJsonRegister(
            "register",
            false,
            "username non valida"
          );
        out.println(result);
        return;
      } else if (email.isEmpty() || !matcher.matches()) {
        System.out.println("[DEBUG REGISTER SERVLET]: tentativo di registrazione fallita, errore: email non valida");
        result =
          registerService.responseJsonRegister(
            "register",
            false,
            "email non valida"
          );
        out.println(result);
        return;
      } else if (password == null || password.isEmpty()) {
        System.out.println("[DEBUG REGISTER SERVLET]: tentativo di registrazione fallita, errore: password non valida");
        result =
          registerService.responseJsonRegister(
            "register",
            false,
            "password non valida"
          );
        out.println(result);
        return;
      }

      if (!registerService.existUsername(username)) {
        if (!registerService.existEmail(email)) {
          System.out.println("[DEBUG LOG]: registrazione nuovo utente:"+username);
          registerService.RegisterUsername(username, email, password, 0);
          result = registerService.responseJsonRegister("register", true, "");
          out.println(result);
          return;
        } else {
          System.out.println("[DEBUG REGISTER SERVLET]: tentativo di registrazione fallita, errore: l'email  esiste di già");
          result =
            registerService.responseJsonRegister(
              "register",
              false,
              "l'email  esiste di già"
              );
          out.println(result);
          return;
        }
      }
      System.out.println("[DEBUG REGISTER SERVLET]: tentativo di registrazione fallita, errore: l'utente esiste di già");
      result =
        registerService.responseJsonRegister(
          "register",
          false,
          "l'utente esiste di già"
        );
      out.println(result);
    }
  }
}
