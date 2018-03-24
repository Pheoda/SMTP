package server;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
    private ArrayList<User> currentlisteDestinataires = new ArrayList<>();
    private ArrayList<String> currentMessage = new ArrayList<>();

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

            System.err.println(command + " " + currentState);

            switch (currentState) {
                case INIT:
                    this.currentUser = null;
                    this.currentMessage.clear();
                    this.currentlisteDestinataires.clear();
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
                    } else if (command.equals("QUIT"))
                        quit();
                    else
                        unrecognizedCommand();
                    break;
                case CONNECTED:
                    if (command.equals("MAIL")) {
                        // TEST FROM ?
                        if (commandLine.length > 1 && commandLine[1].toUpperCase().equals("FROM:")) {
                            if (commandLine.length > 2) {
                                // mail valide
                                sendMessage("250 Sender OK");
                                currentState = State.TO;
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
                    break;

                case TO:
                    if (commandLine.length == 3 && command.equals("RCPT") && commandLine[1].equals("TO:")) {
                        User tmp_user = null;
                        for (User u : listUsers) {
                            if (u.getAddress().equals(commandLine[2])) {
                                tmp_user = u;
                                break;
                            }
                        }
                        if (tmp_user != null) {
                            this.currentlisteDestinataires.add(tmp_user);
                            sendMessage("250 OK");
                        } else
                            sendMessage("550 No such user here");
                    } else if (command.equals("DATA")) {
                        this.currentState = State.DATA;
                        sendMessage("354 Start mail input");
                    } else if (command.equals("RSET"))
                        reset();
                    else if (command.equals("QUIT"))
                        quit();
                    else
                        unrecognizedCommand();
                    break;
                case DATA:
                    this.currentMessage.add(String.join(" ", commandLine));
                    if (commandLine.length > 0 && commandLine[0].equals(".")) {
                        this.sendMail();
                        this.currentState = State.INIT;
                    }
                    sendMessage("");
                    break;
            }
        }
    }

    private void sendMail() {
        this.currentlisteDestinataires.forEach(destinataire -> {
            Path path = Paths.get(destinataire.getUsername() + ".txt");
            try {
                Files.write(path, ("\n\nFrom: " + currentUser.getUsername() + "<" + currentUser.getAddress() + ">").getBytes(), StandardOpenOption.APPEND);
                Files.write(path, ("\nTo: " + destinataire.getUsername() + "<" + destinataire.getAddress() + ">").getBytes(), StandardOpenOption.APPEND);
                Files.write(path, ("\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.currentMessage.forEach(msg -> {
                try {
                    Files.write(path, ("\n" + msg).getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            try {
                Files.write(path, ("\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
