/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eneko.
 */
public class SignerServer {

    /**
     * ResourceBundle para parámetros de configuración.
     */
    private static final ResourceBundle RETO1 = ResourceBundle.getBundle("model.configBD");

    /**
     * Puerto utilizado para la comunicación con el servidor.
     */
    private static final int PUERTO = Integer.parseInt(RETO1.getString("PORT"));

    /**
     * Max Users del servidor
     */
    private static final int MAX_USERS = Integer.parseInt(RETO1.getString("MaxUser"));

    private static final Logger LOGGER = Logger.getLogger(SignerServer.class.getName());

    /**
     * Instancia de Encapsulator para envolver la información del usuario y los
     * mensajes.
     */
    private Encapsulator encapsu;

    private Socket sokClient;

    private ServerSocket svSocket;

    private SignerThread signT;

    private static Integer user = 0;

    public SignerServer() {

        try {
            svSocket = new ServerSocket(PUERTO);

            while (true) {

                if (user < MAX_USERS) {

                    sokClient = svSocket.accept();

                    signT = new SignerThread(sokClient);
                    signT.start();
                    conexionCreada(signT);
                } else {

                    ObjectOutputStream oos = new ObjectOutputStream(sokClient.getOutputStream());
                    encapsu.setMessage(MessageType.MAX_USER);
                    oos.writeObject(encapsu);
                }

            }

        } catch (IOException ex) {
            Logger.getLogger(SignerServer.class.getName()).log(Level.SEVERE, null, ex);
            
        } finally {
            try {
                svSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(SignerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        new SignerServer();
    }

    public static synchronized void conexionCreada(SignerThread signT) {
        user++;
    }

    public static synchronized void borrarConexion(SignerThread signT) {
        user--;
    }
}
