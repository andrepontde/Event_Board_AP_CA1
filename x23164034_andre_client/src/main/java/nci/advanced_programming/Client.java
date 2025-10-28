package nci.advanced_programming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

//Author: André Pont De Anda
//Student ID: x23164034
public class Client {

    private static InetAddress host;
    private static final int PORT = 1234;

    public Client() {
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Host ID not found!");
            System.exit(1);
        }
        run();
    }

    private static void run() {
        // Step 1: Declare socket variable to hold the TCP connection to the server
        Socket link = null;

        try {
            // Step 1: Create a socket connection to the server at the specified host and port
            // This establishes a TCP connection (performs the TCP handshake)
            link = new Socket(host, PORT);

            // Step 2: Set up input/output streams for communicating with the server
            // BufferedReader 'inPostman' - reads text data COMING FROM the server
            BufferedReader inPostman = new BufferedReader(new InputStreamReader(link.getInputStream()));
            // PrintWriter 'outPostman' - writes text data TO the server (auto-flush enabled)
            PrintWriter outPostman = new PrintWriter(link.getOutputStream(), true);

            // Set up stream for reading keyboard input from the console
            BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Connected to server! Type 'quit' or 'exit' to disconnect.");
            System.out.println("Message format: command;date;time;place");
            System.out.println("Example: add;2025-10-28;14:30;Conference Room A");
            System.out.println("Or type 'import' to transfer data from \nandrepont.dev/events.txt to the server\n");

            // Keep connection alive - loop until user types "quit" or "exit"
            boolean keepRunning = true;
            while (keepRunning) {
                String message;
                String response;

                try {
                    // Prompt the user to type a message
                    System.out.print("Enter command: ");
                    message = userEntry.readLine();

                    // Check if user input is null (EOF/Ctrl+C)
                    if (message == null) {
                        System.out.println("\nInput stream closed. Disconnecting...");
                        outPostman.println("quit; null; null; null;");
                        break;
                    }

                    // Check if user wants to quit
                    if (message.trim().equalsIgnoreCase("quit") || message.trim().equalsIgnoreCase("exit") || message.trim().equalsIgnoreCase("stop")) {
                        System.out.println("Disconnecting from server...");

                        // Send quit command to server
                        outPostman.println(message.trim());

                        // Read server's goodbye response
                        response = inPostman.readLine();
                        if (response != null) {
                            System.out.println("SERVER> " + response);
                        }

                        break;
                    } else if (message.trim().equalsIgnoreCase("import")) {
                        //Stream results from custom file inPostman my private server to send to local 
                        //server memory storage
                        URL url = new URL("http://apem.andrepont.dev/events.txt");
                        URLConnection conn = url.openConnection();
                        BufferedReader inImport = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        String line;
                        while ((line = inImport.readLine()) != null) {
                            outPostman.println("add; " + line);
                            System.out.println("Sent to server: " + line);
                            response = inPostman.readLine();
                            if (response != null) {
                                System.out.println("SERVER> " + response);
                            }
                        }
                        inImport.close();
                        System.out.println("Import finished\n");

                        // Continue to next iteration - don't read another response
                        continue;

                    } else {
                        //SEND THE MESSAGE TO THE SERVER
                        outPostman.println(message);

                    }

                    // RECEIVE THE RESPONSE FROM THE SERVER
                    // Wait for the server to send back a response and read it
                    response = inPostman.readLine();

                    // Check if server closed connection
                    if (response == null) {
                        System.out.println("\nServer closed the connection.");
                        break;
                    }

                    System.out.println("SERVER> " + response + "\n");

                } catch (IOException e) {
                    System.out.println("Error communicating with server: " + e.getMessage());
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Step 4: Clean up - close the connection to free up resources
            try {
                System.out.println("\n* Closing connection... *");
                if (link != null && !link.isClosed()) {
                    link.close();
                }
            } catch (IOException e) {
                System.out.println("Unable to disconnect/close!");
                System.exit(1);
            }
        }
    }

}
