package server;

import connection.TCPConnection;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ServerConnection extends TCPConnection {

    private enum State {INIT, CONNECTED, FROM, TO, DATA}

    private State currentState;

    private User[] listUsers;

    private String currentUser = null;
    private ArrayList<User> currentlisteDestinataires = new ArrayList<>();
    private ArrayList<String> listeNotFoundDestinataires = new ArrayList<>();
    private ArrayList<String> currentMessage = new ArrayList<>();

    private String[] commandLine;
    private String command;


    public ServerConnection(Socket connexion, User[] listUsers) {
        super(connexion);
        this.listUsers = listUsers;

        sendMessage("220 Service ready");

        currentState = State.INIT;
    }

    private void quit() {
        sendMessage("221 Closing connection");

        running = false;
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
        while (running) {
            commandLine = readCommand();
            if (commandLine.length > 0)
                command = commandLine[0].toUpperCase();
            else
                command = "NULL";

            System.out.println("received=" + String.join(" ", commandLine));
            switch (currentState) {
                case INIT:
                    this.currentUser = null;
                    this.currentMessage.clear();
                    this.currentlisteDestinataires.clear();
                    if (commandLine.length == 1 && command.equals("EHLO")) {
                        sendMessage("250 OK");
                        currentState = State.CONNECTED;
                    } else if (command.equals("QUIT"))
                        quit();
                    else
                        unrecognizedCommand();
                    break;
                case CONNECTED:
                    this.currentUser = null;
                    this.currentMessage.clear();
                    this.currentlisteDestinataires.clear();
                    if (command.equals("MAIL")) {
                        currentUser = commandLine[1];
                        sendMessage("250 Sender OK");
                        currentState = State.TO;
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
                        } else {
                            this.listeNotFoundDestinataires.add(commandLine[2]);
                            sendMessage("550 No such user here : " + commandLine[2]);
                        }
                    } else if (command.equals("DATA") && this.currentlisteDestinataires.size() > 0) {
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
                        if (this.listeNotFoundDestinataires.size() > 0) {
                            StringBuilder msg = new StringBuilder("Ces users n'existent pas :\n");
                            this.listeNotFoundDestinataires.forEach(destinataires -> {
                                msg.append(destinataires + "\n");
                            });
                            sendMessage(msg.toString());
                        }
                        this.currentState = State.CONNECTED;
                    }
                    break;
            }
        }
    }

    private void sendMail() {
        this.currentlisteDestinataires.forEach(destinataire -> {
            Path path = Paths.get(destinataire.getAddress() + ".txt");
            try {
                Files.write(path, ("\n\nFrom: " + currentUser + " <" + currentUser + ">").getBytes(), StandardOpenOption.APPEND);
                Files.write(path, ("\nTo: " + destinataire.getUsername() + " <" + destinataire.getAddress() + ">").getBytes(), StandardOpenOption.APPEND);
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
