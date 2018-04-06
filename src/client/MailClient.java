package client;

import connection.TCPConnection;
import server.Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MailClient extends TCPConnection {

    public MailClient(InetAddress ia, int port) throws IOException {
        super(new Socket(ia, port));
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Adresse IP serveur : ");
        String ip = sc.nextLine();
        System.out.print("Port serveur : ");
        int port = sc.nextInt();
        try {
            MailClient c = new MailClient(InetAddress.getByName(ip), port);
            new Thread(c).start();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        String from, to, subject;
        StringBuilder message = new StringBuilder();
        StringBuilder mail = new StringBuilder();

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

        // Date
        mail.append("\nDate: ");
        mail.append(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));

        // Objet
        System.out.print("Objet : ");
        subject = sc.nextLine();
        mail.append("\nSubject: ");
        mail.append(subject);

        String line = "";
        // Message
        System.out.println("Message : ");
        do {
            mail.append("\n");
            mail.append(line);
            message.append("\n");
            message.append(line);
            line = sc.nextLine();
        } while (!line.equals(""));


        System.out.println(mail);
        System.out.println("Envoi du mail...");

        // Connexion with the server
        sendMessage("EHLO");

        waitForServerAnswer();

        sendMessage("MAIL FROM: " + from);

        waitForServerAnswer();

        sendMessage("RCPT TO: " + to);

        waitForServerAnswer();

        sendMessage("DATA: ");

        waitForServerAnswer();

        message.append("\n.\n");
        sendMessage(message.toString());

        waitForServerAnswer();

        System.out.println("Mail success !");
    }

    private void waitForServerAnswer() {
        String[] serverResponse;

        serverResponse = readCommand();

        if (!(serverResponse[0].charAt(0) == '2')) {
            System.out.print("Error : ");
            for (String s : serverResponse)
                System.out.print(s + " ");
            quit();
        }
    }

    private void quit() {
        Thread.currentThread().interrupt();
    }
}
