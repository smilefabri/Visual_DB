package visualdb.visualdbapi.visualdb;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import visualdb.visualdbapi.db.PoolingPersistenceManager;
import org.mindrot.jbcrypt.BCrypt;

public class VisualDbManager {

    private static final VisualDbManager manager = new VisualDbManager(); // Singleton thread-safe
    private final PoolingPersistenceManager persistence;
    private static final Logger logger = Logger.getLogger(VisualDbManager.class.getName());

    private VisualDbManager() {
        persistence = PoolingPersistenceManager.getPersistenceManager();
    }

    public static VisualDbManager getManager() {
        if (manager == null) {
            manager = new VisualDbManager();
        }
        return manager;
    }

    /**
     * Validazione credenziali utente.
     * @param username
     * @param passwordInserita
     * @return int: -1 = credenziali non valide, 0 = utente normale, 1 = admin
     */
    public int validateCredentials(String username, String passwordInserita) {
        int result = -1;

        String query = "SELECT password, privilegi FROM \"VisualDB\".public.\"Utenti\" WHERE username = ?";

        try (Connection conn = persistence.getConnection();
             PreparedStatement st = conn.prepareStatement(query)) {

            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String hashSalvato = rs.getString("password");
                    int privilegi = rs.getInt("privilegi");

                    if (BCrypt.checkpw(passwordInserita, hashSalvato)) {
                        result = privilegi; // Password corretta
                    }
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Errore di accesso al DB durante login", ex);
        }

        return result;
    }

    /**
     * Inserisce un record di accesso.
     * @param username
     */
    public void insertAccesso(String username) {
        String query = "INSERT INTO \"VisualDB\".public.\"Accesso\" (data_accesso, username, ora_accesso) VALUES (?, ?, ?)";

        Date currentDate = new Date(System.currentTimeMillis());
        Time currentTime = new Time(System.currentTimeMillis());

        try (Connection conn = persistence.getConnection();
             PreparedStatement st = conn.prepareStatement(query)) {

            st.setDate(1, currentDate);
            st.setString(2, username);
            st.setTime(3, currentTime);

            st.executeUpdate(); // uso corretto per INSERT

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Errore durante l'inserimento dell'accesso per un utente", ex);
        }
    }

}
