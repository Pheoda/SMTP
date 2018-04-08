package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {

    private ServerSocket server;
    private User[] listUsers;
    private static final int CONNECTION = 6;

    public Server(int port, User[] list) {
        try {
            this.listUsers = list;
            server = new ServerSocket(port, CONNECTION);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        boolean loop = true;

        while (loop) {
            try {
                Socket connection;
                connection = server.accept();
                new Thread(new ServerConnection(connection, listUsers)).start();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                loop = false;
            }
        }
    }

}