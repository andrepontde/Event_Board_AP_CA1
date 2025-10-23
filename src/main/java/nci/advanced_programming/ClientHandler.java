package nci.advanced_programming;

import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable{
    private final Socket socket;
    private List<Sch_Events> sharedEvents;

    ClientHandler(Socket socket, List<Sch_Events> sharedEvents) {
        this.socket = socket;
        this.sharedEvents = sharedEvents;
    }


    @Override
    public void run() {
        
        
    }

    private void addEvent(){

    }

}
