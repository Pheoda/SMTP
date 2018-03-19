package server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TCPConnection implements Runnable {

    public final static int CR = 13;
    public final static int LF = 10;

    boolean run = true;

    public enum State {AUTHORIZATION, TRANSACTION}

    protected Socket socket;
    protected InputStream in;
    protected OutputStream out;
    protected BufferedInputStream bufIn;

    protected State state;

    public TCPConnection(Socket connexion) {
        socket = connexion;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            bufIn = new BufferedInputStream(in);
            state = State.AUTHORIZATION;
        } catch (IOException ex) {
            Logger.getLogger(TCPConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected String[] readCommand() {
        int character = -1;
        boolean end = false, crReceived = false;
        String request = "";
        do {
            try {
                character = bufIn.read();

                request += (char) character;

                end = crReceived && character == LF;

                crReceived = (character == CR);

            } catch (IOException e) {
                run = false;
            }
        } while (character != -1 && !end);

        return request.split("\\s+");
    }


    protected void sendMessage(String message) {
        try {
            out.write((message + "\r\n").getBytes());
            System.out.println("Said : " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
