package visualdb.visualdbapi.AdimnConfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import visualdb.visualdbapi.login.TokenStorage;
import visualdb.visualdbapi.tableManager.ServiceTableManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "Admin_Servlet",urlPatterns = {
        AdminService.INFO_USER_PATH,
})
public class AdminServlet extends HttpServlet {

    AdminService AdminManager = new AdminService();


    public void init() {}

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String errorMessage="";
        response.setContentType("application/json");
        BufferedReader in = request.getReader();
        //JsonObject loginObject = JsonParser.parseReader(in).getAsJsonObject();
        String token = request.getHeader("authorization");
        String usernameSession = TokenStorage.getUsernameFromToken(token);
        int privilege = TokenStorage.getPrivilegi(usernameSession);
        PrintWriter out = response.getWriter();


        if (request.getServletPath().equals(AdminService.INFO_USER_PATH)) {
            if(privilege == 1){
                JsonArray result = null;
                result = AdminManager.GetInfoUser();
                out.println(result);
            }else{
                JsonObject result = new JsonObject();
                result.addProperty("status",false);
                errorMessage = "non sei un admin";
                result.addProperty("errorMessage",errorMessage);
                out.println(result);

            }


        }
    }

    public void destroy(){}

}
