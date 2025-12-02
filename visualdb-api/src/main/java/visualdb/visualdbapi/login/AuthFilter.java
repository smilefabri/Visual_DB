package visualdb.visualdbapi.login;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.util.logging.Logger;
import java.util.logging.Level;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import visualdb.visualdbapi.register.RegisterService;

@WebFilter(urlPatterns = "*")
public class AuthFilter extends HttpFilter {
    private static final Logger logger = Logger.getLogger(AuthFilter.class.getName());

    @Override
    public void doFilter(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        String origin = request.getHeader("Origin");


        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader(
                "Access-Control-Allow-Methods",
                "GET, OPTIONS, HEAD, PUT, POST, PATCH, DELETE"
        );
        response.setHeader("Access-Control-Allow-Headers", "*, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (
                request.getServletPath().equals(LoginService.LOGIN_PATH) ||
                        request.getServletPath().equals(LoginService.LOGOUT_PATH) ||
                        request.getServletPath().equals(RegisterService.REGISTER_PATH)
        ) {
            chain.doFilter(request, response);

            return;
        }

        String token = request.getHeader("authorization");

        //TokenStorage.printTokenMap();

        if (token != null && token.startsWith("Bearer ")) {
            try {
                // Rimuovi il prefisso "Bearer " dal token
                //token = token.substring(7);
                token = token.replace("Bearer $", "");
                DecodedJWT decodedJWT = JwtUtil.verifyToken(token);
                String username = decodedJWT.getClaim("username").asString();
                //Verifica se il token è valido nella tua struttura dati
                if (TokenStorage.isValidToken(username, token)) {
                    //System.out.println("[DEBUG AUTH FILTER] -> utente autorizzato: " + username);
                    // L'utente è autenticato, procedi con la richiesta

                    chain.doFilter(request, response);
                } else {
                    // Token non valido
                    logger.log(Level.WARNING, "token invalide");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token non valido");
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Token non valido", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token non valido");
            }
        } else {
            logger.info("Token non valido");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token mancante");
        }
    }
}