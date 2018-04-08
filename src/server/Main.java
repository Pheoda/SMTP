package server;

public class Main {

    public static void main(String[] args) {

        //Free
        Server s = new Server(2000, new User[]{
                new User("user", "pass", "user@free.fr")
        });
        new Thread(s).start();

        //Gmail
        Server s2 = new Server(4000, new User[]{
                new User("user2", "pass", "user2@gmail.com")
        });
        new Thread(s2).start();
    }
}
