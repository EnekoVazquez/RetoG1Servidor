/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import exception.ConnectionException;
import exception.NoOperativeDataBaseException;
import exception.TimeOutException;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Logger;
import java.sql.DriverManager;
import java.util.logging.Level;

/**
 *
 * @author Eneko.
 */
public class Pool {

    private static final Logger LOG = Logger.getLogger(Pool.class.getName());

    private ResourceBundle configBD;
    private String driver;
    private String url;
    private String user;
    private String pass;

    private static Pool pool;

    private static Stack<Connection> poolStack = new Stack<>();

    public Connection openConnection() throws ConnectionException, NoOperativeDataBaseException {
        this.configBD = ResourceBundle.getBundle("");
        this.driver = configBD.getString("DRIVER");
        this.url = configBD.getString("CONN");
        this.user = configBD.getString("DBUSER");
        this.pass = configBD.getString("DBPASS");
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

    public Connection getConnection() throws TimeOutException {
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
        LOG.info("Devolver una conexion");
        poolStack.push(con);
    }

}
