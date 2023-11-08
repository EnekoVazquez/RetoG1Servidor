/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import exception.MaxUsersException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignerServer {

    private static final ResourceBundle RETO1 = ResourceBundle.getBundle("model.configBD");
    private static final int PUERTO = Integer.parseInt(RETO1.getString("PORT"));
    private static final int MAX_USERS = Integer.parseInt(RETO1.getString("MaxUser"));
    private static final Logger LOGGER = Logger.getLogger(SignerServer.class.getName());

    private Encapsulator encapsu;
    private Socket sokClient;
    private ServerSocket svSocket;
    private SignerThread signT;
    private static Integer user = 0;
    private volatile boolean isServerRunning = true; // Variable para controlar el estado del servidor

    public SignerServer() {
        try {
            svSocket = new ServerSocket(PUERTO);

            // Hilo para la detección de tecla y detener el servidor
            Thread shutdownThread = new Thread(() -> {
                LOGGER.info("Presiona 'q' y luego Enter para detener el servidor.");
                Scanner scanner = new Scanner(System.in);
                while (isServerRunning) {
                    String input = scanner.nextLine();
                    if (input.equals("q")) {
                        isServerRunning = false;
                        break;
                    }
                }
                try {
                    svSocket.close(); // Cierra el servidor.
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            });

            shutdownThread.start();

            while (isServerRunning) {
                try {
                    if (user < MAX_USERS) {
                        sokClient = svSocket.accept();
                        signT = new SignerThread(sokClient);
                        signT.start();
                        conexionCreada(signT);
                    }else{
                        isServerRunning=false;
                        throw new MaxUsersException("demasiados usuarios conectados la base de datos espere su turno");
                        
                    }
                } catch (IOException e) {
                    // Maneja la excepción si se produce un error al aceptar la conexión.
                    if (!isServerRunning) {
                        break; // Si el servidor se está apagando, sale del bucle.
                    }
                } catch (MaxUsersException ex) {
                    Logger.getLogger(SignerServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        new SignerServer();
    }

    public static synchronized void conexionCreada(SignerThread signT) {
        user++;
    }
    /*
    //EGO: añadido si falla bye bye
    public static synchronized void borrarConexion(SignerThread signT) {
        user--;
    }
*/
}
