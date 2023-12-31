/**
 * Clase que implementa la interfaz Sign y proporciona funcionalidades relacionadas con la autenticación de usuarios
 * y la gestión de la base de datos.
 *
 * @author Eneko, Egoitz y Josu
 * @version 1.0
 */
package model;

import exception.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación de la interfaz Sign que maneja la autenticación y la gestión
 * de la base de datos.
 */
public class DaoImplementacion implements Sign {

    private Connection conn;
    private PreparedStatement stmt;
    private static Pool pool;

    // Consultas SQL para realizar diversas operaciones en la base de datos
    private final String SignUpResUser = "INSERT INTO res_users (company_id, partner_id, write_uid, create_date, login, password, create_uid, write_date, notification_type) VALUES (1, ?, 1, now(), ?, ?, 2, now(), 'email');";
    private final String SignUpResPart = "INSERT INTO res_partner (id, create_date, name, commercial_partner_id, write_uid, create_uid, street, zip, city, phone, active) VALUES (?, now(), ?, ?, 1, 1, ?, ?, ?, ?, 'true')";
    private final String SignUpResComp = "INSERT INTO res_company_users_rel (cid, user_id) VALUES (1, ?);";
    private final String SignUpResGroup = "INSERT INTO res_groups_users_rel(gid, uid) VALUES (16, ?),(26, ?),(28, ?),(31, ?)";
    private final String lastRestPartnerId = "SELECT MAX(id) AS id FROM res_partner;";
    private final String lastRestUserId = "SELECT MAX(id) AS id FROM res_users;";
    private final String confirmSignIn = "SELECT * FROM res_users WHERE login = ? AND password = ?";
    private final String nameSignIn = "SELECT name from res_partner where id = (SELECT partner_id from res_users where login = ?);";
    private final String comprobacionEmail = "SELECT login from res_users where login = ? and password = ?";

    private static final Logger LOG = Logger.getLogger(DaoImplementacion.class.getName());

    /**
     * Constructor por defecto que inicializa la conexión a la base de datos
     * llamando a {@code connectionBD()}.
     */
    public DaoImplementacion() {
        connectionBD(); // Llama a la función de inicialización.
    }

    /**
     * Inicializa la conexión a la base de datos utilizando un pool de
     * conexiones.
     */
    public void connectionBD() {
        // Configuración estándar para conectarnos a nuestra base de datos
        this.pool = Pool.getPool();
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * @param user Objeto User que contiene la información del usuario a
     * registrar.
     * @return El objeto User después del registro.
     * @throws ServerErrorException Si ocurre un error en el servidor.
     * @throws UserAlreadyExistsException Si el usuario ya existe en la base de
     * datos.
     */
    @Override
    public User getExecuteSignUp(User user) throws ServerErrorException, UserAlreadyExistsException {
        int lastIdPartner = 0;
        int lastIdUsers = 0;
        ResultSet rs = null;
        Boolean exists;
        exists = existe(user.getEmail(), user.getPassword());
        try {
            conn = pool.getConnection();

            // Verificar si el usuario ya existe
            if (exists) {
                throw new UserAlreadyExistsException("El usuario ya existe en la base de datos");
            } else {
                conn.setAutoCommit(false);
                stmt = conn.prepareStatement(lastRestPartnerId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    lastIdPartner = rs.getInt("id");
                }

                stmt = conn.prepareStatement(SignUpResPart);

                stmt.setInt(1, lastIdPartner + 1);
                stmt.setString(2, user.getNombre());
                stmt.setInt(3, lastIdPartner + 1);
                stmt.setString(4, user.getDireccion());
                stmt.setInt(5, user.getCodigoPostal());
                stmt.setString(6, user.getCiudad());
                stmt.setInt(7, user.getTelefono());
                stmt.executeUpdate();

                stmt = conn.prepareStatement(SignUpResUser);
                stmt.setInt(1, lastIdPartner + 1);
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getPassword());
                stmt.executeUpdate();

                stmt = conn.prepareStatement(lastRestUserId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    lastIdUsers = rs.getInt("id");
                }

                stmt = conn.prepareStatement(SignUpResComp);
                stmt.setInt(1, lastIdUsers);
                stmt.executeUpdate();

                stmt = conn.prepareStatement(SignUpResGroup);
                stmt.setInt(1, lastIdUsers);
                stmt.setInt(2, lastIdUsers);
                stmt.setInt(3, lastIdUsers);
                stmt.setInt(4, lastIdUsers);
                stmt.executeUpdate();

                conn.commit();
            }

        } catch (TimeOutException ex) {
            Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    stmt.close();
                }
                pool.returnConnection(conn);
            } catch (TimeOutException ex) {
                Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return user;
    }

    /**
     * Autentica a un usuario en la base de datos.
     *
     * @param user Objeto User que contiene la información del usuario a
     * autenticar.
     * @return El objeto User después de la autenticación.
     * @throws ServerErrorException Si ocurre un error en el servidor.
     * @throws UserNotFoundException Si el usuario no se encuentra en la base de
     * datos.
     */
    @Override
    public User getExecuteSignIn(User user) throws ServerErrorException, UserNotFoundException {
        ResultSet rs = null;

        try {
            conn = pool.getConnection();
            if (!existe(user.getEmail(), user.getPassword())) {
                throw new UserNotFoundException("Usuario no encontrado");
            } else {
                stmt = conn.prepareStatement(confirmSignIn);
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getPassword());

                rs = stmt.executeQuery();
                if (rs.next()) {
                    user.setEmail(rs.getString("login"));
                    user.setPassword(rs.getString("password"));
                }

                stmt = conn.prepareStatement(nameSignIn);
                stmt.setString(1, user.getEmail());

                rs = stmt.executeQuery();

                if (rs.next()) {
                    user.setNombre(rs.getString("name"));
                }
            }

        } catch (TimeOutException ex) {
            Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                pool.returnConnection(conn);
                //rs.close();
                stmt.close();
            } catch (TimeOutException ex) {
                Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return user;
    }

    /**
     * Verifica la existencia de un usuario en la base de datos.
     *
     * @param email El correo electrónico del usuario.
     * @param passwd La contraseña del usuario.
     * @return true si el usuario existe, false en caso contrario.
     * @throws ServerErrorException Si ocurre un error en el servidor.
     */
    public boolean existe(String email, String passwd) throws ServerErrorException {
        boolean existe = false;
        ResultSet rs = null;
        try {
            conn = pool.getConnection();
            stmt = conn.prepareStatement(comprobacionEmail);
            stmt.setString(1, email);
            stmt.setString(2, passwd);
            rs = stmt.executeQuery();

            if (rs.next()) {
                existe = true;
            }
        } catch (TimeOutException | SQLException ex) {
            // Manejar la excepción o registrarla adecuadamente
            Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerErrorException("Error en la base de datos");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    stmt.close();
                }
                pool.returnConnection(conn);
            } catch (TimeOutException ex) {
                // Manejar la excepción o registrarla adecuadamente
                Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                // Manejar la excepción o registrarla adecuadamente
                Logger.getLogger(DaoImplementacion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return existe;
    }
}
