package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private String address;

    private ArrayList<Mail> messages;

    private BufferedReader reader;

    public User(String username, String password, String address) {
        this.username = username;
        this.password = password;
        this.address = address;

        this.messages = new ArrayList<>();

        try {
            this.reader = new BufferedReader(new FileReader(this.address + ".txt"));
            String line;
            String currentMessage = "";
            int currentSize = 0;
            while((line = reader.readLine()) != null) {
                line += "\r\n";
                currentMessage += line;
                currentSize += line.length();
                if(line.equals(".\r\n")) {
                    messages.add(new Mail(currentMessage, currentSize));
                    currentMessage = "";
                    currentSize = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNbMessages() {
        return messages.size();
    }

    // Total size
    public int getSizeMessage()
    {
        int sum = 0;
        for (Mail m : messages) {
            sum += m.getSize();
        }
        return sum;
    }

    // Size of the message i
    public int getSizeMessage(int i) {
        return messages.get(i).getSize();
    }

    public String getMessage(int i) {
        return messages.get(i).getMessage();
    }

    public boolean userExists(String username, String password) {
        // Password needs to be MD5 in POP3S !
        return this.username.equals(username) && this.password.equals(password);
    }

    public String getUsername() {
        return username;
    }

    public String getAddress() {
        return address;
    }
}
