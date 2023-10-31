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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Josu.
 */
public class SignerThread extends Thread {

    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Socket soc;
    private Sign sign;
    private MessageType messT;
    private Encapsulator encap;
    private User user;

    public SignerThread() {

    }

    public SignerThread(Socket soc) {
        this.soc = soc;
    }

    /**
     * This method manages de information received and makes an action depending
     * of the information given
     *
     */
    @Override
    public void run() {
        int cont = 0;
        try {
            ois = new ObjectInputStream(soc.getInputStream());
            //LLamaos al método de la factoria por cada registro que hace el cliente
            DaoFactory daofact = new DaoFactory();
            sign = daofact.getDao();
            //Leemos los datos del encapsulador
            encap = (Encapsulator) ois.readObject();

            //Le decimos al hilo la accion que tiene que hacer dependiendo del mensaje que recibe
            switch (encap.getMessage()) {
                case SIGNIN_REQUEST:
                    user = sign.getExecuteSignIn(encap.getUser());
                    //Si no da fallos guardamos el usuario y estableciendo el mensaje
                    encap.setUser(user);
                    encap.setMessage(MessageType.OK_RESPONSE);

                    break;
                case SIGNUP_REQUEST:
                    sign.getExecuteSignUp(encap.getUser());
                    encap.setUser(user);
                    encap.setMessage(MessageType.OK_RESPONSE);

                    break;

            }

            oos = new ObjectOutputStream(soc.getOutputStream());
            oos.writeObject(encap);
            SignerServer.borrarConexion(this);

        } catch (IOException e) {
            encap.setMessage(MessageType.ERROR_RESPONSE);
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (ClassNotFoundException e) {
            encap.setMessage(MessageType.ERROR_RESPONSE);
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (UserNotFoundException e) {
            encap.setMessage(MessageType.USER_NOT_FOUND_RESPONSE);
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (ServerErrorException e) {
            encap.setMessage(MessageType.ERROR_RESPONSE);
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, e);
        } catch (UserAlreadyExistsException ex) {
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CredentialErrorException ex) {
            Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                
                SignerServer.borrarConexion(this);

                ois.close();
                oos.close();
                soc.close();
            } catch (IOException ex) {
                Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
