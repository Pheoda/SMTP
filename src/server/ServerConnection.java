package server;

import java.net.Socket;
import java.util.ArrayList;

public class ServerConnection extends server.TCPConnection {

    private enum State {INIT, CONNECTED, FROM, TO, DATA}

    private State currentState;
    private boolean loop;

    private User[] listUsers = {
            new User("user", "pass", "user@mail.com"),
            new User("user2", "pass", "user2@mail.com")
    };

    private User currentUser = null;
    private ArrayList<String> listeDestinataires = new ArrayList<>();

    private String[] commandLine;
    private String command;

    private ArrayList<String> currentMessage = new ArrayList<>();

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

            System.err.println(command + " " + currentState);

            switch (currentState) {
                case INIT:
                    if (commandLine.length == 3 && command.equals("EHLO")) {
                        for (User u : listUsers) {
                            if (u.userExists(commandLine[1], commandLine[2])) {
                                currentUser = u;
                                break;
                            }
                        }
                        // User reconnu ou non
                        if (currentUser != null) {
                            sendMessage("250 OK");
                            currentState = State.CONNECTED;
                        } else
                            sendMessage("-ERR mauvais user");
                    } else
                        unrecognizedCommand();
                    break;
                case CONNECTED:
                    if (command.equals("MAIL")) {
                        // TEST FROM ?
                        if (commandLine.length > 1 && commandLine[1].toUpperCase().equals("FROM:")) {
                            if (commandLine.length > 2) {
                                // mail valide
                                sendMessage("250 Sender OK");
                                currentState = State.FROM;
                            } else {
                                // mail non valide
                                sendMessage("mail non valide");
                            }
                        } else
                            uncorrectParameters();
                    } else if (command.equals("RSET"))
                        reset();
                    else if (command.equals("QUIT"))
                        quit();
                    else
                        unrecognizedCommand();
                    break;
                case FROM:
                    if (commandLine.length == 3 && command.equals("RCPT") && commandLine[1].equals("TO:")) {
                        String tmp_address = null;
                        for (User u : listUsers) {
                            if (u.getAddress().equals(commandLine[2])) {
                                tmp_address = u.getAddress();
                                break;
                            }
                        }
                        if (tmp_address != null) {
                            this.listeDestinataires.add(tmp_address);
                            sendMessage("250 OK");
                        } else
                            sendMessage("550 No such user here");
                    } else if (command.equals("DATA")) {
                        this.currentState = State.DATA;
                        sendMessage("354 Start mail input");
                    } else
                        unrecognizedCommand();
                    break;
                case TO:

                    break;
                case DATA:
                    System.out.println(commandLine[0]);
                    if (!commandLine[0].equals(".")) {
                        this.currentMessage.add(String.join(" ", commandLine));
                        System.out.println(this.currentMessage);
                    } else {
                        System.out.println("ici");
                        System.out.println(this.currentMessage);
                        this.currentState = State.CONNECTED;
                    }
                    break;
            }
        }
    }
}
