package server;

import java.net.Socket;

public class ServerConnection extends server.TCPConnection {

    private enum State {INIT, CONNECTED, FROM, TO, DATA}

    private State currentState;
    private boolean loop;

    private String[] commandLine;
    private String command;

    public ServerConnection(Socket connexion) {
        super(connexion);

        sendMessage("220 Service ready");

        currentState = State.INIT;
        loop = true;
    }

    private void quit() {
        sendMessage("221 Closing connection");

        loop = false;
    }

    private void reset() {
        sendMessage("250 Resetting");
        this.currentState = State.CONNECTED;
    }

    private void unrecognizedCommand() {
        sendMessage("500 Syntax error, command unrecognized");
    }

    private void uncorrectParameters() {
        sendMessage("501 Uncorrect parameters");
    }

    @Override
    public void run() {
        while (loop) {
            commandLine = readCommand();
            if (commandLine.length > 0)
                command = commandLine[0].toUpperCase();
            else
                command = "NULL";

            switch (currentState) {
                case INIT:
                    if (command.equals("EHLO")) {
                        sendMessage("250 OK");
                    } else
                        unrecognizedCommand();
                    break;
                case CONNECTED:
                    if (command.equals("MAIL")) {
                        // TEST FROM ?
                        if (commandLine.length > 1 && commandLine[1].toUpperCase().equals("FROM:")) {
                            // mail valide
                            sendMessage("250 Sender OK");
                            currentState = State.FROM;

                            // mail non valide
                            sendMessage("");
                        }
                        else
                            uncorrectParameters();
                    } else if (command.equals("RSET"))
                        reset();
                    else if (command.equals("QUIT"))
                        quit();
                    else
                        unrecognizedCommand();
                    break;
                case FROM:

                    break;
                case TO:

                    break;
                case DATA:

                    break;
            }
        }
    }
}
