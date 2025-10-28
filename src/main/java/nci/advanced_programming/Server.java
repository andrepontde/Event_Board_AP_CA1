package nci.advanced_programming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
                Thread t = new Thread(new ClientHandling(clientConnections, link));
                t.setName("Client n." + clientConnections);
                t.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    class ClientHandling implements Runnable {

        Socket link;
        private int clientN;

        public ClientHandling(int clientN, Socket link) {
            this.clientN = clientN;
            this.link = link;
        }

        @Override
        public void run() {
            BufferedReader in = null;
            PrintWriter out = null;
            
            try {
                // Set up I/O streams once at the start
                in = new BufferedReader(new InputStreamReader(link.getInputStream()));
                out = new PrintWriter(link.getOutputStream(), true);
                
                System.out.println(Thread.currentThread().getName() + " connected - handling client number: " + clientN);
                
                // Keep connection alive - loop until client sends "quit"
                boolean keepRunning = true;
                while (keepRunning) {
                    String message = null;
                    Sch_Events s_event = null;
                    String command = null;
                    
                    try {
                        // Read message from client
                        message = in.readLine();
                        
                        // Check if client disconnected or sent null
                        if (message == null) {
                            System.out.println(Thread.currentThread().getName() + " - Client disconnected");
                            break;
                        }
                        
                        System.out.println("Message received from client " + clientN + ": " + message);
                        
                        // Parse the command
                        String[] eventDesc = message.split(";");
                        command = eventDesc[0];
                        
                        // Check for quit command
                        if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit")) {
                            System.out.println(Thread.currentThread().getName() + " - Client requested disconnect");
                            out.println("Connection closing.");
                            keepRunning = false;
                            break;
                        }
                        
                        // Process valid commands
                        if (command.equals("add") || command.equals("remove") || command.equals("list") || command.equals("import")) {
                            if (eventDesc.length >= 4) {
                                s_event = new Sch_Events(eventDesc[1], eventDesc[2], eventDesc[3]);
                                
                                // Call methods OUTSIDE synchronized block
                                switch (command) {
                                    case "add":
                                        try {
                                            add(s_event);
                                            out.println("Event added successfully");
                                        } catch (ClientInconsistencyException e) {
                                            out.println("Error: " + e.getMessage());
                                            System.out.println("ClientInconsistencyException for client " + clientN + ": " + e.getMessage());
                                        }
                                        break;
                                    case "remove":
                                        remove(s_event);
                                        out.println("Event removed successfully");
                                        break;
                                    case "list":
                                        out.println(list(s_event));
                                        break;
                                    case "import":
                                        e_import(s_event);
                                        out.println("Event imported successfully");
                                        break;
                                }
                            } else {
                                out.println("Error: Invalid command format. Expected format: command;param1;param2;param3");
                            }
                        } else {
                            out.println("Error: Unknown command '" + command + "'. Valid commands: add, remove, list, import, quit");
                        }
                        
                    } catch (Exception e) {
                        System.out.println("Error processing message from client " + clientN + ": " + e.getMessage());
                        keepRunning = false;
                        if (out != null) {
                            out.println("Error: " + e.getMessage());
                        }
                    }
                }
                
            } catch (IOException e) {
                System.out.println("Connection error with client " + clientN + ": " + e.getMessage());
            } finally {
                // Clean up - close the connection
                try {
                    if (link != null && !link.isClosed()) {
                        link.close();
                        System.out.println(Thread.currentThread().getName() + " - Connection closed");
                    }
                } catch (IOException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        }


        private void add (Sch_Events s_event) throws ClientInconsistencyException {
            // Validate event data before adding
            if (s_event.getDate() == null || s_event.getDate().trim().isEmpty()) {
                throw new ClientInconsistencyException("Invalid event: Date cannot be empty");
            }
            if (s_event.getTime() == null || s_event.getTime().trim().isEmpty()) {
                throw new ClientInconsistencyException("Invalid event: Time cannot be empty");
            }
            if (s_event.getDesc() == null || s_event.getDesc().trim().isEmpty()) {
                throw new ClientInconsistencyException("Invalid event: Description cannot be empty");
            }
            
            // Synchronize only when modifying the shared list
            synchronized (sharedEvents) {
                // Check for duplicate events (same date, time, and description)
                for (Sch_Events existingEvent : sharedEvents) {
                    if (existingEvent.getDate().trim().equals(s_event.getDate().trim()) &&
                        existingEvent.getTime().trim().equals(s_event.getTime().trim()) &&
                        existingEvent.getDesc().trim().equals(s_event.getDesc().trim())) {
                        throw new ClientInconsistencyException(
                            "Duplicate event detected: An event with the same date, time, and description already exists");
                    }
                }
                
                sharedEvents.add(s_event);
                System.out.println("Event added: " + s_event.getEvent());
            }
        }
        
        private void remove (Sch_Events s_event){
            synchronized (sharedEvents) {
                sharedEvents.remove(s_event);
                System.out.println("Event removed: " + s_event);
            }
        }
        
        //Have to be sorted by time!!
        private String list (Sch_Events s_event){
            String date = s_event.date;
            List<Sch_Events> e_matches = new ArrayList<>();
            StringBuilder listMsg = new StringBuilder();
            synchronized (sharedEvents) {
            sharedEvents.stream()
                .filter(event -> event.date.equals(date))
                .forEach(event -> e_matches.add(event));
            }
            e_matches.stream()
                .forEach(event -> listMsg.append(event.getEvent()));

            return listMsg.toString();
        }
        
        private void e_import (Sch_Events s_event){
            // Import logic here
            synchronized (sharedEvents) {
                // Add your import logic
                System.out.println("Importing event: " + s_event);
            }
        }
    }


    

}
