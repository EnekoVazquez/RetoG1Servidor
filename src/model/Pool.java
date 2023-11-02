package model;

import exception.ConnectionException;
import exception.NoOperativeDataBaseException;
import exception.TimeOutException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Pool de conexiones a la base de datos.
 */
public class Pool {

    private static final Logger LOG = Logger.getLogger(Pool.class.getName());

    private String driver;
    private String url;
    private String user;
    private String pass;

    private static Pool pool;
    private static Stack<Connection> poolStack = new Stack<>();

    public Pool() {
        // Configura los valores de conexión desde un archivo de propiedades (configBD.properties)
        ResourceBundle configBD = ResourceBundle.getBundle("model.configBD"); // Nombre del archivo de propiedades
        this.driver = configBD.getString("Driver");
        this.url = configBD.getString("Conn");
        this.user = configBD.getString("DBUser");
        this.pass = configBD.getString("DBPass");
    }

    public Connection openConnection() throws ConnectionException, NoOperativeDataBaseException {
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            return conn;
        } catch (SQLException e) {
            throw new NoOperativeDataBaseException(url);
        }
    }

    public static Pool getPool() {
        if (pool == null) {
            pool = new Pool();
        }
        return pool;
    }

    public synchronized Connection getConnection() throws TimeOutException {
        Connection conn = null;
        if (poolStack.size() > 0) {
            conn = poolStack.pop();
        } else {
            try {
                conn = openConnection();
            } catch (ConnectionException ex) {
                Logger.getLogger(Pool.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoOperativeDataBaseException ex) {
                Logger.getLogger(Pool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return conn;
    }

    public void returnConnection(Connection con) throws TimeOutException {
        LOG.info("Devolver una conexión");
        poolStack.push(con);
    }
}
