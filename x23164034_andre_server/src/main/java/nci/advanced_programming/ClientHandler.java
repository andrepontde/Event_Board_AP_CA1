package nci.advanced_programming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
                    command = eventDesc[0].toLowerCase();
                    
                    // Check for quit command
                    if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("stop")) {
                        System.out.println(Thread.currentThread().getName() + " - Client requested disconnect");
                        postman.println("TERMINATE \n closing connection");
                        keepRunning = false;
                        break;
                    }
                    
                    // Process valid commands
                    if (command.equals("add") || command.equals("remove") || command.equals("list") || command.equals("import") || command.equals("addnolist")) {
                        if (eventDesc.length >= 4) {
                            // Trim spaces from date and time fields
                            s_event = new Sch_Events(eventDesc[1].trim(), eventDesc[2].trim(), eventDesc[3]);
                            
                            // Call methods OUTSIDE synchronized block
                            switch (command) {
                                case "add":
                                    try {
                                        postman.println("Event added successfully " + add(s_event));
                                    } catch (InvalidCommandException e) {
                                        postman.println("Error: " + e.getMessage());
                                        System.out.println("InvalidCommandException for client " + clientN + ": " + e.getMessage());
                                    }
                                    break;
                                //Case addnolist created to make a cleaner add function that does not return every event after completition for the 
                                //Import function on the client side
                                case "addnolist":
                                    try {
                                        postman.println("Event added successfully " + addNoList(s_event));
                                    } catch (InvalidCommandException e) {
                                        postman.println("Error: " + e.getMessage());
                                        System.out.println("InvalidCommandException for client " + clientN + ": " + e.getMessage());
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

    private String add(Sch_Events s_event) throws InvalidCommandException {
        // Validate event data before adding
        if (s_event.getDate() == null || s_event.getDate().trim().isEmpty()) {
            throw new InvalidCommandException("Invalid event: Date cannot be empty");
        }
        if (!isValidDateFormat(s_event.getDate().trim())) {
            throw new InvalidCommandException("Invalid event: Date must be in format 'DD/MM/YYYY', 'DD-MM-YYYY', or 'DD month YYYY' (e.g., '25/10/2024', '26-10-2026', or '12 november 2025')");
        }
        if (s_event.getTime() == null || s_event.getTime().trim().isEmpty()) {
            throw new InvalidCommandException("Invalid event: Time cannot be empty");
        }
        if (!isValidTimeFormat(s_event.getTime().trim())) {
            throw new InvalidCommandException("Invalid event: Time must be in format H AM/PM or H:MM AM/PM (e.g., '6 pm', '7:30 am', '12 am')");
        }
        if (s_event.getDesc() == null || s_event.getDesc().trim().isEmpty()) {
            throw new InvalidCommandException("Invalid event: Description cannot be empty");
        }
        
        // Synchronize only when modifying the shared list
        synchronized (sharedEvents) {
            // Check for duplicate events (same date, time, and description)
            for (Sch_Events existingEvent : sharedEvents) {
                if (existingEvent.getDate().trim().equals(s_event.getDate().trim()) &&
                    existingEvent.getTime().trim().equals(s_event.getTime().trim()) &&
                    existingEvent.getDesc().trim().equals(s_event.getDesc().trim())) {
                    throw new InvalidCommandException(
                        "Duplicate event detected: An event with the same date, time, and description already exists");
                }
            }
            
            // Add the event to the shared list
            sharedEvents.add(s_event);
            System.out.println("Event added: " + s_event.getEvent());
            
            return getAllEvents();
        }
    }

    //this is just a repeated method but with no list generation at the end!!
    private Sch_Events addNoList(Sch_Events s_event) throws InvalidCommandException {
        // Validate event data before adding
        if (s_event.getDate() == null || s_event.getDate().trim().isEmpty()) {
            throw new InvalidCommandException("Invalid event: Date cannot be empty");
        }
        if (!isValidDateFormat(s_event.getDate().trim())) {
            throw new InvalidCommandException("Invalid event: Date must be in format 'DD/MM/YYYY', 'DD-MM-YYYY', or 'DD month YYYY' (e.g., '25/10/2024', '26-10-2026', or '12 november 2025')");
        }
        if (s_event.getTime() == null || s_event.getTime().trim().isEmpty()) {
            throw new InvalidCommandException("Invalid event: Time cannot be empty");
        }
        if (!isValidTimeFormat(s_event.getTime().trim())) {
            throw new InvalidCommandException("Invalid event: Time must be in format H AM/PM or H:MM AM/PM (e.g., '6 pm', '7:30 am', '12 am')");
        }
        if (s_event.getDesc() == null || s_event.getDesc().trim().isEmpty()) {
            throw new InvalidCommandException("Invalid event: Description cannot be empty");
        }
        
        // Synchronize only when modifying the shared list
        synchronized (sharedEvents) {
            // Check for duplicate events (same date, time, and description)
            for (Sch_Events existingEvent : sharedEvents) {
                if (existingEvent.getDate().trim().equals(s_event.getDate().trim()) &&
                    existingEvent.getTime().trim().equals(s_event.getTime().trim()) &&
                    existingEvent.getDesc().trim().equals(s_event.getDesc().trim())) {
                    throw new InvalidCommandException(
                        "Duplicate event detected: An event with the same date, time, and description already exists");
                }
            }
            
            // Add the event to the shared list
            sharedEvents.add(s_event);
            System.out.println("Event added: " + s_event.getEvent());
            
            return s_event;
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
            //Stream new list to build new message, sorted by time
            e_matches.stream()
                .sorted((e1, e2) -> Integer.compare(timeToMinutes(e1.getTime()), timeToMinutes(e2.getTime())))
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
                    .sorted((e1, e2) -> Integer.compare(timeToMinutes(e1.getTime()), timeToMinutes(e2.getTime())))
                    .forEach(event -> listMsg.append(event.getEvent()));
                    
                return listMsg.toString();    
            }else{
                return "No shared events to show (list is empty)";
            }
            
        }
    }
    
    /**
     * Converts 12-hour time format to minutes since midnight for proper comparison
     * Handles formats like "6 pm", "7:30 am", "12 am"
     */
    private int timeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return 0;
        }
        
        String time = timeStr.trim().toUpperCase();
        int hours = 0;
        int minutes = 0;
        
        try {
            boolean isPM = time.contains("PM");
            String timePart = time.replace("AM", "").replace("PM", "").trim();
            
            if (timePart.contains(":")) {
                // Format: H:MM AM/PM
                String[] parts = timePart.split(":");
                hours = Integer.parseInt(parts[0]);
                minutes = Integer.parseInt(parts[1]);
            } else {
                // Format: H AM/PM
                hours = Integer.parseInt(timePart);
                minutes = 0;
            }
            
            // Convert to 24-hour format
            if (isPM && hours != 12) {
                hours += 12;
            } else if (!isPM && hours == 12) {
                hours = 0;
            }
            
            return hours * 60 + minutes;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // If parsing fails, return 0 (will sort to beginning)
            return 0;
        }
    }
    
    
    //  Validates if the given time string is in a proper time format.
    //  Accepts 12-hour format with AM/PM: H AM/PM, H:MM AM/PM
    //  Examples: "6 pm", "7:30 am", "12 am", "11:45 PM"
    //  Reference: GeeksforGeeks (n.d.) Validate a time format (HHMMSS) using Regex in Java. 
    //  Available at: https://www.geeksforgeeks.org/java/validate-a-time-format-hhmmss-using-regex-in-java/ 
    //  (Accessed: 18 November 2025).
     
    private boolean isValidTimeFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }
        
        // Pattern for 12-hour format: H AM/PM or H:MM AM/PM (1:00 AM to 12:59 PM) - case insensitive
        // Allows optional space before AM/PM and supports both H and H:MM formats
        Pattern pattern12Hour = Pattern.compile("^(1[0-2]|0?[1-9])(:[0-5][0-9])?\\s?(AM|PM)$", Pattern.CASE_INSENSITIVE);
        
        return pattern12Hour.matcher(time).matches();
    }
    
    
    // Validates if the given date string is in a proper date format.
    // Accepts formats like: DD/MM/YYYY, DD-MM-YYYY, or DD month YYYY
    // Examples: "25/10/2024", "26-10-2026", "12 november 2025", "5 jan 2024"
    // Reference: Baeldung (n.d.) Java Date Regular Expressions. Available at: 
    // https://www.baeldung.com/java-date-regular-expressions (Accessed: 18 November 2025).
   
    private boolean isValidDateFormat(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        
        String dateStr = date.trim();
        
        //Pattern for DD/MM/YYYY format (e.g., 25/10/2024, 5/1/2024)
        Pattern patternSlash = Pattern.compile("^(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[0-2])/([12][0-9]{3})$");
        
        //Pattern for DD-MM-YYYY format (e.g., 26-10-2026, 5-1-2024)
        Pattern patternDash = Pattern.compile("^(0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[0-2])-([12][0-9]{3})$");
        
        //Pattern for DD month YYYY format (e.g., 12 november 2025, 5 jan 2024)
        //Supports full month names and common abbreviations
        Pattern patternMonth = Pattern.compile(
            "^(0?[1-9]|[12][0-9]|3[01])\\s+(january|february|march|april|may|june|july|august|september|october|november|december|" +
            "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\s+([12][0-9]{3})$", 
            Pattern.CASE_INSENSITIVE
        );
        
        return patternSlash.matcher(dateStr).matches() || 
               patternDash.matcher(dateStr).matches() || 
               patternMonth.matcher(dateStr).matches();
    }
}
