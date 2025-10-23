package nci.advanced_programming;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Server serverInstance;
        Client clientInstance;

        Scanner in = new Scanner(System.in);
        String option = in.next();
        if(option.equals("s")){
            serverInstance = new Server();
        }else{
            clientInstance = new Client();
        }


    }
}