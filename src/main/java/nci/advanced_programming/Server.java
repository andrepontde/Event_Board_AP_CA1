package nci.advanced_programming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static ServerSocket servSock;
    private static final int PORT = 1234;
    private static int clientConnections = 0;
    

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

    private static void run() {
        Socket link = null;

        while (true) {
            try {
                System.out.println("ready to receive new message");
                link = servSock.accept();
                // ++clientConnections;
                BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
                PrintWriter out = new PrintWriter(link.getOutputStream(), true);
                String message = in.readLine();
                System.out.println("Message received from client: " + clientConnections + "  " + message);
                out.println("Echo Message: " + message);
                



            } catch (IOException var12) {
                var12.printStackTrace();
            }
            // finally {
            //     try {
            //         System.out.println("\n* Closing connection... *");
            //         link.close();
            //     } catch (IOException var11) {
            //         System.out.println("Unable to disconnect!");
            //         System.exit(1);
            //     }

            // }
        }

    }

}

