package nci.advanced_programming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//Author: André Pont De Anda
//Student ID: x23164034
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final int clientN;
    private final List<Sch_Events> sharedEvents;

    public ClientHandler(int clientN, Socket socket, List<Sch_Events> sharedEvents) {
        this.clientN = clientN;
        this.socket = socket;
        this.sharedEvents = sharedEvents;
    }


    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter postman = null;
        
        try {
            // Set up I/O streams once at the start
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            postman = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println(Thread.currentThread().getName() + " connected - handling client number: " + clientN);
            
            // Keep connection alive - loop until client sends "quit"
            boolean keepRunning = true;
            while (keepRunning) {
                String message;
                Sch_Events s_event;
                String command;
                
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
                    if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("stop")) {
                        System.out.println(Thread.currentThread().getName() + " - Client requested disconnect");
                        postman.println("TERMINATE \n closing connection");
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
                                        postman.println("Event added successfully " + add(s_event));
                                    } catch (ClientInconsistencyException e) {
                                        postman.println("Error: " + e.getMessage());
                                        System.out.println("ClientInconsistencyException for client " + clientN + ": " + e.getMessage());
                                    }
                                    break;
                                case "remove":
                                    if (remove(s_event)){
                                        postman.println("Event removed successfully " + getAllEvents());
                                        System.out.println("Event removed successfully");    
                                    }else{
                                        postman.println("Event was not found/removed");
                                        System.out.println("Event not removed or found");
                                    }
                                    break;
                                case "list":
                                    postman.println(list(s_event));
                                    break;
                            }
                        } else {
                            postman.println("Error: Invalid command format. Expected format: command;param1;param2;param3");
                        }
                    } else {
                        postman.println("Error: Unknown command '" + command + "'. Valid commands: add, remove, list, import, stop");
                    }
                    
                } catch (Exception e) {
                    System.out.println("Error processing message from client " + clientN + ": " + e.getMessage());
                    keepRunning = false;
                    if (postman != null) {
                        postman.println("Error: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.out.println("Connection error with client " + clientN + ": " + e.getMessage());
        } finally {
            // Clean up - close the connection
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    System.out.println(Thread.currentThread().getName() + " - Connection closed");
                }
            } catch (IOException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private String add(Sch_Events s_event) throws ClientInconsistencyException {
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
            
            // Add the event to the shared list
            sharedEvents.add(s_event);
            System.out.println("Event added: " + s_event.getEvent());
            
            return getAllEvents();
        }
    }
    
    private boolean remove(Sch_Events s_event) {
        synchronized (sharedEvents) {
            boolean erased = false;

            for (int i = 0; i < sharedEvents.size(); i++){
                Sch_Events temp = sharedEvents.get(i);

                if (temp.date.equals(s_event.date) && 
                temp.time.equalsIgnoreCase(s_event.time) &&
                temp.desc.equals(s_event.desc)) {
                    sharedEvents.remove(i);
                    erased = true;
                    break;    
                }
            }
            return erased;
        }
    }
    
    private String list(Sch_Events s_event) {
        String date = s_event.date;
        List<Sch_Events> e_matches = new ArrayList<>();
        StringBuilder listMsg = new StringBuilder();
        //Only show events that match the date by filtering with a stream from the shared Events list
        synchronized (sharedEvents) {
            if(!sharedEvents.isEmpty()){
                sharedEvents.stream()
                    .filter(event -> event.date.equals(date))
                    .forEach(event -> e_matches.add(event));
            }else{
                return "Event list is empty";
            }
        }

        if (!e_matches.isEmpty()) {
            //Stream new list to build new message
            e_matches.stream()
                .forEach(event -> listMsg.append(event.getEvent()));    
            return listMsg.toString();
        }else{
            return "No matches found for the specified date";
        }
        
        
    }
    
    private String getAllEvents(){
        //Helper method to print every event currently on memory
        synchronized (sharedEvents) {
            StringBuilder listMsg = new StringBuilder();
            if (!sharedEvents.isEmpty()) {
                sharedEvents.stream()
                    .forEach(event -> listMsg.append(event.getEvent()));
                    
                return listMsg.toString();    
            }else{
                return "No shared events to show (list is empty)";
            }
            
        }
    }
}
