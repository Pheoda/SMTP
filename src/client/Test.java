package client;

import connection.TCPConnection;
import server.Client;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Test {

    Client client;
    ArrayList<String> destinataires = new ArrayList<String>();
    private String display;

    void start() {

        Scanner sc = new Scanner(System.in);
        String from, to, subject;
        ArrayList<String> message = new ArrayList<String>();
        StringBuilder mail = new StringBuilder();

        // 220 Service ready
        //waitForServerAnswer();

        // Identification
        System.out.print("Identification : ");
        from = sc.nextLine();
        mail.append("FROM: ");
        mail.append(from);

        // Destinataire
        System.out.print("Destinataire : ");
        to = sc.nextLine();
        mail.append("\nTO: ");
        mail.append(to);
        String[] splited = to.split("\\s+");
        Arrays.stream(splited).forEach(address -> {
            this.destinataires.add(address);
        });

        // Date
        mail.append("\nDate: ");
        mail.append(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
        message.add("Date: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));

        // Objet
        System.out.print("Objet : ");
        subject = sc.nextLine();
        mail.append("\nSubject: ");
        mail.append(subject);
        message.add("Subject: " + subject);
        message.add(" ");

        String line = "";
        // Message
        System.out.println("Message : ");
        do {
            line = sc.nextLine();
            mail.append(line);
            mail.append("\n");
            message.add(line);
        } while (!line.equals(""));


        System.out.println(mail);
        System.out.println("Envoi du mail...");

        for (String destinataire : this.destinataires) {
            if (!destinataire.contains("@")) {
                System.out.println("Not found address");
            } else {
                String nom_domaine = destinataire.substring(destinataire.indexOf("@"));
                if (nom_domaine.equals("@free.fr")) {
                    try {
                        this.client = new Client(InetAddress.getByName("134.214.116.127"), 2000);
                        waitForServerAnswer();
                        // Connexion with the server
                        this.client.sendMessage("EHLO");

                        waitForServerAnswer();

                        this.client.sendMessage("MAIL FROM: " + from);

                        waitForServerAnswer();

                        this.client.sendMessage("RCPT TO: " + destinataire);

                        waitForServerAnswer();

                        this.client.sendMessage("DATA");

                        waitForServerAnswer();

                        message.add(".\n");
                        message.forEach(s -> {
                            this.client.sendMessage(s);
                        });

                        System.out.println();

                        waitForServerAnswer();

                        System.out.println(this.display);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (nom_domaine.equals("@gmail.com")) {
                    try {
                        this.client = new Client(InetAddress.getByName("134.214.116.193"), 4000);
                        waitForServerAnswer();
                        // Connexion with the server
                        this.client.sendMessage("EHLO");

                        waitForServerAnswer();

                        this.client.sendMessage("MAIL FROM: " + from);

                        waitForServerAnswer();

                        this.client.sendMessage("RCPT TO: " + destinataire);

                        waitForServerAnswer();

                        this.client.sendMessage("DATA");

                        waitForServerAnswer();

                        message.add(".\n");
                        message.forEach(s -> {
                            this.client.sendMessage(s);
                        });

                        System.out.println();

                        waitForServerAnswer();

                        System.out.println(this.display);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Not found address");
                }
            }
        }
    }

    private void waitForServerAnswer() {
        String[] serverResponse;

        serverResponse = this.client.readCommand();

        char replyCode = serverResponse[0].charAt(0);
        if (replyCode != '2' && replyCode != '3') {
            System.out.print("Error : ");
            for (String s : serverResponse)
                System.out.print(s + " ");
            System.out.println();
            System.out.println();
            this.display = "Send Failed";
        } else {
            System.out.println("Server replied : " + String.join(" ", serverResponse));
            this.display = "Send Succeeded !";
        }
    }
}
