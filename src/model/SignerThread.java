/**
 * Clase que representa un hilo de ejecución para manejar las solicitudes de clientes en el sistema de firma electrónica.
 * Este hilo recibe un objeto Encapsulator, interpreta su mensaje y realiza las operaciones correspondientes utilizando
 * la interfaz Sign y las implementaciones concretas de DaoFactory.
 *
 * @author Eneko, Egoitz y Josu
 * @version 1.0
 */
package model;

import exception.CredentialErrorException;
import exception.ServerErrorException;
import exception.UserAlreadyExistsException;
import exception.UserNotFoundException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hilo de ejecución para manejar las solicitudes de clientes en el sistema de
 * firma electrónica.
 */
public class SignerThread extends Thread {

    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Socket soc;
    private Sign sign;
    private Encapsulator encap;
    private User user;
    private static final Logger LOGGER = Logger.getLogger(SignerThread.class.getName());

    /**
     * Constructor que recibe un socket para la comunicación con el cliente.
     *
     * @param soc Socket de comunicación con el cliente.
     */
    public SignerThread(Socket soc) {
        this.soc = soc;
    }

    /**
     * Método principal del hilo de ejecución. Realiza la lectura de objetos
     * desde el flujo de entrada, interpreta el mensaje recibido y realiza las
     * operaciones correspondientes. Maneja excepciones relacionadas con las
     * operaciones de firma electrónica y la comunicación con el cliente.
     */
    @Override
    public void run() {
        try {
            ois = new ObjectInputStream(soc.getInputStream());

            DaoFactory daofact = new DaoFactory();
            sign = daofact.getDao();

            encap = (Encapsulator) ois.readObject();

            switch (encap.getMessage()) {
                case SIGNIN_REQUEST:
                    LOGGER.info("Realizando operación de SIGN IN");
                    user = sign.getExecuteSignIn(encap.getUser());
                    encap.setUser(user);
                    encap.setMessage(MessageType.OK_RESPONSE);
                    break;
                case SIGNUP_REQUEST:
                    LOGGER.info("Realizando operación de SIGN UP");
                    sign.getExecuteSignUp(encap.getUser());
                    encap.setMessage(MessageType.OK_RESPONSE);
                    break;
                default:
                    encap.setMessage(MessageType.ERROR_RESPONSE);
                    break;
            }

        } catch (UserNotFoundException e) {
            encap.setMessage(MessageType.USER_NOT_FOUND_RESPONSE);
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (ServerErrorException e) {
            encap.setMessage(MessageType.ERROR_RESPONSE);
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (UserAlreadyExistsException ex) {
            encap.setMessage(MessageType.USER_ALREADY_EXISTS_RESPONSE);
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CredentialErrorException ex) {
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException e) {
            encap.setMessage(MessageType.ERROR_RESPONSE);
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                oos = new ObjectOutputStream(soc.getOutputStream());
                oos.writeObject(encap);
                //SignerServer.borrarConexion(this);.
                if (ois != null) {
                    ois.close();
                }
                if (oos != null) {
                    oos.close();
                }
                soc.close();
            } catch (IOException ex) {
                Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
