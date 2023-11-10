/**
 * Pool de conexiones a la base de datos.
 * Esta clase implementa un pool de conexiones para gestionar la obtención y devolución de conexiones a la base de datos.
 * Utiliza un archivo de propiedades (configBD.properties) para configurar los parámetros de conexión.
 * La clase sigue el patrón de diseño Singleton para garantizar una única instancia del pool.
 *
 * @author Eneko, Egoitz y Josu
 * @version 1.0
 */
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
 * Pool de conexiones que gestiona la obtención y devolución de conexiones a la
 * base de datos.
 */
public class Pool {

    private static final Logger LOG = Logger.getLogger(Pool.class.getName());

    private String driver;
    private String url;
    private String user;
    private String pass;

    private static Pool pool;
    private static Stack<Connection> poolStack = new Stack<>();

    /**
     * Constructor por defecto que configura los valores de conexión desde un
     * archivo de propiedades (configBD.properties).
     */
    public Pool() {
        ResourceBundle configBD = ResourceBundle.getBundle("model.configBD"); // Nombre del archivo de propiedades
        this.driver = configBD.getString("Driver");
        this.url = configBD.getString("Conn");
        this.user = configBD.getString("DBUser");
        this.pass = configBD.getString("DBPass");
    }

    /**
     * Abre una nueva conexión a la base de datos.
     *
     * @return Objeto Connection que representa la nueva conexión.
     * @throws ConnectionException Si no se puede establecer una conexión.
     * @throws NoOperativeDataBaseException Si la base de datos no está
     * operativa.
     */
    public synchronized Connection openConnection() throws ConnectionException, NoOperativeDataBaseException {
        try {
            LOG.info("Estamos entrando a la conexion");
            Connection conn = DriverManager.getConnection(url, user, pass);
            return conn;
        } catch (SQLException e) {
            throw new NoOperativeDataBaseException(url);
        }
    }

    /**
     * Obtiene la instancia única del pool de conexiones.
     *
     * @return Instancia única del pool.
     */
    public synchronized static Pool getPool() {
        LOG.info("Ejecutando metodo getPool");
        if (pool == null) {
            pool = new Pool();
        }
        return pool;
    }

    /**
     * Obtiene una conexión del pool.
     *
     * @return Objeto Connection que representa la conexión obtenida.
     * @throws TimeOutException Si ocurre un timeout al intentar obtener una
     * conexión.
     */
    public synchronized Connection getConnection() throws TimeOutException {
        LOG.info("Recogiendo las conexiones del pool");
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

    /**
     * Devuelve una conexión al pool.
     *
     * @param con Objeto Connection que se va a devolver al pool.
     * @throws TimeOutException Si ocurre un timeout al intentar devolver la
     * conexión al pool.
     */
    public synchronized void returnConnection(Connection con) throws TimeOutException {
        LOG.info("Devolver una conexión");
        poolStack.push(con);
    }
}
