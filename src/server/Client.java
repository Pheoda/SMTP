package server;

import connection.TCPConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends TCPConnection {

    private boolean skipCommand;

    public Client(InetAddress ia, int port) throws IOException {
        super(new Socket(ia, port));

        skipCommand = false;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Adresse IP serveur : ");
        String ip = sc.nextLine();
        System.out.print("Port serveur : ");
        int port = sc.nextInt();
        try {
            Client c = new Client(InetAddress.getByName(ip), port);
            new Thread(c).start();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printString(String[] str) {
        for (String s : str) {
            System.out.print(s + " ");
        }
    }

    private void printEmail() {
        String[] str_tab;
        boolean mailFinished = false;
        do {
            str_tab = readCommand();
            if (str_tab.length > 0) {
                printString(str_tab);
                System.out.println();

                skipCommand = str_tab[0].equals("-ERR");
                mailFinished = str_tab[0].equals(".") || skipCommand;
            }
        } while (!mailFinished);
    }

    @Override
    public void run() {
        boolean loop = true;
        String[] response = null;
        Scanner sc = new Scanner(System.in);
        while (loop) {
            if (!skipCommand) {
                response = readCommand();
                printString(response);
            } else
                skipCommand = false;


            try {
                String cmd = sc.nextLine();
                byte[] data = (cmd + "\r\n").getBytes();
                out.write(data);
                out.flush();

                String[] cmd_tab = cmd.split("\\s+");

                switch (cmd_tab[0].toUpperCase()) {
                    case "RETR":
                        printEmail();
                        break;
                    case "QUIT":
                        loop = false;
                        break;
                    default:
                        break;
                }

            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}