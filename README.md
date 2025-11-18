# Event Board Application

A multi-threaded client-server application for managing scheduled events, built with Java using socket programming.

## Author
**André Pont De Anda**  
Student ID: x23164034

## Project Overview

This Event Board application consists of a server that manages a shared collection of scheduled events and multiple clients that can connect simultaneously to perform various operations. The system uses TCP sockets for communication and implements thread-safe operations to handle concurrent client connections.

## Features

- **Multi-threaded Server**: Handles multiple client connections simultaneously
- **Event Management**: Add, remove, and list scheduled events
- **Data Validation**: Robust validation for date and time formats using regular expressions
- **Import Functionality**: Import events from external URLs
- **Thread-Safe Operations**: Synchronized access to shared event collection
- **Duplicate Detection**: Prevents duplicate events from being added

## Supported Date Formats

- `DD/MM/YYYY` (e.g., 25/10/2024, 5/1/2024)
- `DD-MM-YYYY` (e.g., 26-10-2026, 5-1-2024)
- `DD month YYYY` (e.g., 12 november 2025, 5 jan 2024)

## Supported Time Formats

- **24-hour format**: `HH:MM` (e.g., 14:30, 09:15)
- **12-hour format**: `H:MM AM/PM` (e.g., 2:30 PM, 9:15 AM)

## Project Structure

```
Event_Board_AP_CA1/
├── x23164034_andre_server/          # Server application
│   ├── src/main/java/nci/advanced_programming/
│   │   ├── Server.java              # Main server class
│   │   ├── ClientHandler.java       # Handles individual client connections
│   │   ├── Sch_Events.java          # Event data model
│   │   ├── InvalidCommandException.java # Custom exception class
│   │   └── Main.java                # Server entry point
│   └── pom.xml                      # Maven configuration
├── x23164034_andre_client/          # Client application
│   ├── src/main/java/nci/advanced_programming/
│   │   ├── Client.java              # Client implementation
│   │   └── Main.java                # Client entry point
│   └── pom.xml                      # Maven configuration
└── README.md                        # This file
```

## Commands

### Client Commands

- `add;date;time;description` - Add a new event
- `remove;date;time;description` - Remove an existing event
- `list;date;any;any` - List all events for a specific date
- `import` - Import events from default URL (http://apem.andrepont.dev/events.txt)
- `import;custom_url` - Import events from a custom URL
- `quit` / `exit` / `stop` - Disconnect from server

### Example Usage

```
add;25/12/2025;14:30;Christmas Meeting
add;26-12-2025;2:30 PM;Boxing Day Event
add;27 december 2025;09:00;Year-end Review
list;25/12/2025;any;any
remove;25/12/2025;14:30;Christmas Meeting
```

## How to Run

### Prerequisites
- Java 8 or higher
- Maven (optional, for building)

### Running the Server

1. Navigate to the server directory:
   ```bash
   cd x23164034_andre_server
   ```

2. Compile and run:
   ```bash
   mvn compile exec:java -Dexec.mainClass="nci.advanced_programming.Main"
   ```
   
   Or if using compiled classes:
   ```bash
   java -cp target/classes nci.advanced_programming.Main
   ```

### Running the Client

1. Navigate to the client directory:
   ```bash
   cd x23164034_andre_client
   ```

2. Compile and run:
   ```bash
   mvn compile exec:java -Dexec.mainClass="nci.advanced_programming.Main"
   ```
   
   Or if using compiled classes:
   ```bash
   java -cp target/classes nci.advanced_programming.Main
   ```

## Technical Details

### Server Configuration
- **Port**: 1234
- **Connection Type**: TCP Socket
- **Threading Model**: One thread per client connection
- **Data Storage**: In-memory ArrayList (thread-safe operations)

### Validation
- **Date Validation**: Uses regular expressions to validate multiple date formats
- **Time Validation**: Supports both 12-hour and 24-hour time formats
- **Duplicate Detection**: Prevents events with identical date, time, and description

### References
- Baeldung (n.d.) Java Date Regular Expressions. Available at: https://www.baeldung.com/java-date-regular-expressions (Accessed: 18 November 2025).
- GeeksforGeeks (n.d.) Validate a time format (HHMMSS) using Regex in Java. Available at: https://www.geeksforgeeks.org/java/validate-a-time-format-hhmmss-using-regex-in-java/ (Accessed: 18 November 2025).

## Error Handling

The application includes comprehensive error handling for:
- Invalid command formats
- Invalid date/time formats
- Duplicate events
- Network connection issues
- Malformed import data

## License

This project is part of an academic assignment for Advanced Programming coursework.