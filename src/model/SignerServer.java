/**
 * Clase que representa el servidor de un sistema de firma electrónica.
 * Este servidor acepta conexiones de clientes y asigna un hilo de ejecución (SignerThread) a cada conexión entrante.
 * Implementa un mecanismo de detección de tecla para permitir al usuario detener el servidor presionando 'q' y luego Enter.
 * Utiliza un archivo de propiedades (configBD.properties) para configurar el puerto del servidor y el número máximo de usuarios permitidos.
 *
 * @author Eneko, Egoitz y Josu
 * @version 1.0
 */
package model;

import exception.MaxUsersException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servidor para un sistema de firma electrónica.
 */
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

    /**
     * Constructor que inicializa y configura el servidor. Crea un ServerSocket
     * en el puerto especificado en el archivo de propiedades. Inicia un hilo
     * para la detección de tecla que permite detener el servidor. Acepta
     * conexiones de clientes y crea hilos SignerThread para manejar cada
     * conexión. Controla el número máximo de usuarios permitidos.
     */
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
                    } else {
                        isServerRunning = false;
                        throw new MaxUsersException("Demasiados usuarios conectados. Espere su turno para acceder a la base de datos.");
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

    /**
     * Método principal que inicia el servidor.
     *
     * @param args Argumentos de la línea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        new SignerServer();
    }

    /**
     * Método synchronized que incrementa el contador de usuarios cuando se
     * establece una nueva conexión.
     *
     * @param signT Objeto SignerThread asociado a la conexión.
     */
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
