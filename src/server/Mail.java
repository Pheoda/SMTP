package server;

public class Mail {
    private String message;
    private int size;

    public Mail(String message, int size) {
        this.message = message;
        this.size = size;
    }

    public String getMessage() {
        return message;
    }

    public int getSize() {
        return size;
    }
}