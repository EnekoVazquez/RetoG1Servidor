/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import exception.CredentialErrorException;
import exception.ServerErrorException;
import exception.UserAlreadyExistsException;
import exception.UserNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 *
 * @author Eneko.
 */
public class DaoImplementacion implements Sign {

    private Connection conn;
    private PreparedStatement stmt;
    private static Pool pool;
    private ResourceBundle configBD;
    private final String SignUpResUser = "INSERT INTO res.users (id, company_id, partner_id, create_date, login, password, create_uid, write_uid, write_date, notification_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private final String SignUpResPart = "INSERT INTO res_partner (name,write_uid, create_uid, street ,zip ,city, phone, active) VALUES ( ?, 1, 1, ?, ?, ?, ?, 'true')";
    private final String SignUpData3 = " ";
    private static final Logger LOG = Logger.getLogger(DaoImplementacion.class.getName());

    public void connectionBD() {
        //Configuración estándar para conectarnos a nuestra base de datos
        this.pool = pool.getPool();

    }

    @Override
    public User getExecuteSignUp(User user) throws ServerErrorException, UserAlreadyExistsException {
        return null;

    }

    @Override
    public User getExecuteSignIn(User user) throws ServerErrorException, CredentialErrorException, UserNotFoundException {
        return null;

    }
}
