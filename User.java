package to2498gre.ac.uk;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

// A user will send a message to the server. Then the server will create a thread to communicate with the user.
// Each connection with a user will be added to an array list. A loop will be made so that any message sent gets sent to every other user.

public class User {
    // A user will have a socket to connect to the server
    // A user will also have a writer and reader so that messages will be able to send and read.
    private Socket socket;
    private BufferedReader readerBuff;
    private BufferedWriter writerBuff;
    private String userName;

    public User(Socket socket, String username) {
        try {
            this.socket = socket;
            this.userName = username;
            this.readerBuff = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writerBuff = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            // Close everything.
            closeEverything(socket, readerBuff, writerBuff);
        }
    }
    public void sendMessage() {
        try {
            // Sends the userName of the user.
            writerBuff.write(userName);
            writerBuff.newLine();
            writerBuff.flush();
            // Create a scanner for user input.
            Scanner scanner = new Scanner(System.in);
            // While a connection is ongoing with the server, this will keep the scanner active in order to send the message.
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                writerBuff.write(userName + ": " + messageToSend);
                writerBuff.newLine();
                writerBuff.flush();
            }
        } catch (IOException e) {
            // Close everything.
            closeEverything(socket, readerBuff, writerBuff);
        }
    }

//    A new thread for listing for a message.
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                // While a socket is connected (an active connection with the server) This will use a different thread to keep listening for messages.
                while (socket.isConnected()) {
                    try {
                        // Receive the messages sent from other users and output it to the console.
                        msgFromGroupChat = readerBuff.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        // Close everything.
                        closeEverything(socket, readerBuff, writerBuff);
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * code adapted from https://www.linuxquestions.org/questions/programming-9/are-there-any-ways-to-catch-ctrl-c-in-a-console-java-program-464904/
     */
    public void ShutDown() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Exited!");
            }
        });
        for (; ; ) ;
    }
    /**
     * End of adapted code
     */

    // Running the program.
    public static void main(String[] args) throws IOException {
        // Asking the user for a username and a socket connection.
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.println("Enter a port number: ");
        boolean valid = false;
        while (!valid) {
            try {
                int portNumber = scanner.nextInt();
                Socket socket = new Socket("localhost", portNumber);
                // Passing the socket and to give the user a username.
                User newUser = new User(socket, username);
                // A loop to read and send messages.
                newUser.listenForMessage();
                newUser.sendMessage();
                valid = true;
            } catch (ConnectException noConnection) {
                System.out.println("No connection is on this port, try again! ");

            }catch (InputMismatchException x) {
                System.out.println("Please enter a number.");
                scanner.next();
            }
        }
    }
}


// Tyler Owen-Thomas