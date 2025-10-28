package nci.advanced_programming;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//Author: André Pont De Anda
//Student ID: x23164034
public class Server {

    private static ServerSocket servSock;
    private static final int PORT = 1234;
    private static int clientConnections = 0;
    private static final List<Sch_Events> sharedEvents = new ArrayList<>();

    public Server() {
        System.out.println("Opening port...\n");

        try {
            servSock = new ServerSocket(PORT);
        } catch (IOException var2) {
            System.out.println("Unable to attach to port!");
            System.exit(1);
        }

        while (true) {
            run();
        }
    }

    private void run() {
        Socket link = null;

        while (true) {
            try {
                System.out.println("ready to receive new message");
                link = servSock.accept();
                ++clientConnections;
                Thread t = new Thread(new ClientHandler(clientConnections, link, sharedEvents));
                t.setName("Client n." + clientConnections);
                t.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
