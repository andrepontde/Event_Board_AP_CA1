package nci.advanced_programming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
            // BufferedReader 'in' - reads text data COMING FROM the server
            BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
            // PrintWriter 'out' - writes text data TO the server (auto-flush enabled)
            PrintWriter out = new PrintWriter(link.getOutputStream(), true);

            // Set up stream for reading keyboard input from the console
            BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in));
            
            System.out.println("Connected to server! Type 'quit' or 'exit' to disconnect.");
            System.out.println("Message format: command;date;time;place");
            System.out.println("Example: add;2025-10-28;14:30;Conference Room A\n");
            
            // Keep connection alive - loop until user types "quit"
            boolean keepRunning = true;
            while (keepRunning) {
                String message;
                String response;
                
                try {
                    // Prompt the user to type a message
                    System.out.print("Enter command: ");
                    message = userEntry.readLine();
                    
                    // Check if user input is null (EOF/Ctrl+D)
                    if (message == null) {
                        System.out.println("\nInput stream closed. Disconnecting...");
                        out.println("quit; null; null; null;");
                        break;
                    }
                    
                    // Check if user wants to quit
                    if (message.trim().equalsIgnoreCase("quit") || message.trim().equalsIgnoreCase("exit")) {
                        System.out.println("Disconnecting from server...");
                        
                        // Send quit command to server
                        out.println(message.trim());
                        
                        // Read server's goodbye response
                        response = in.readLine();
                        if (response != null) {
                            System.out.println("SERVER> " + response);
                        }
                        
                        keepRunning = false;
                        break;
                    }

                    if (message.trim().equals("list")){
                        out.println( "list; null; null; null;");
                    }else{
                        // Step 3: SEND THE MESSAGE TO THE SERVER
                        // This is where the actual message transmission happens!
                        // Format: command;date;time;place (semicolon-delimited string)
                        // out.println() writes the message to the output stream, which sends it over the network
                        out.println(message);        
                    }
                    
                    
                    // Step 3: RECEIVE THE RESPONSE FROM THE SERVER
                    // Wait for the server to send back a response and read it
                    response = in.readLine();
                    
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
