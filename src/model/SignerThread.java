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

public class SignerThread extends Thread {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Socket soc;
    private Sign sign;
    private Encapsulator encap;
    private User user;

    public SignerThread(Socket soc) {
        this.soc = soc;
    }

    @Override
    public void run() {
        try {
            ois = new ObjectInputStream(soc.getInputStream());
           

            DaoFactory daofact = new DaoFactory();
            sign = daofact.getDao();

            encap = (Encapsulator) ois.readObject();

            switch (encap.getMessage()) {
                case SIGNIN_REQUEST:
                    user = sign.getExecuteSignIn(encap.getUser());
                    encap.setUser(user);
                    encap.setMessage(MessageType.OK_RESPONSE);
                    break;
                case SIGNUP_REQUEST:
                    sign.getExecuteSignUp(encap.getUser());
                    encap.setMessage(MessageType.OK_RESPONSE);
                    break;
                default:
                    encap.setMessage(MessageType.ERROR_RESPONSE);
                    break;
            }
            oos = new ObjectOutputStream(soc.getOutputStream());
            oos.writeObject(encap);
        } catch (IOException | ClassNotFoundException e) {
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
                if (ois != null) ois.close();
                if (oos != null) oos.close();
                soc.close();
            } catch (IOException ex) {
                Logger.getLogger(SignerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
