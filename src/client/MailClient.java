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
        boolean loop = true;

        while(loop) {
            System.out.print("Adresse IP serveur : ");
            String ip = sc.nextLine();
            System.out.print("Port serveur : ");
            int port = sc.nextInt();
            try {
                Client c = new Client(InetAddress.getByName(ip), port);
                new Thread(c).start();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                loop = false;
            }
        }
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        String from, to, subject, message = "";
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

        // Objet
        System.out.print("Objet : ");
        subject = sc.nextLine();
        mail.append("Subject: ");
        mail.append(subject);

        // Date
        mail.append("\nDate: ");
        mail.append(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));

        // Message
        System.out.println("Message : ");
        do {
            mail.append("\n");
            mail.append(message);
            message = sc.nextLine();
        }while(!message.equals(""));


    }
}
