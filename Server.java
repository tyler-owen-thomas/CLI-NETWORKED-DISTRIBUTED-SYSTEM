package to2498gre.ac.uk;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Server {


    private final ServerSocket serverSocket;
        public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;

    }
    public void startServer() {
        try {
            // Listening for incoming user connections on port specified by Server.
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                UserManager userManager = new UserManager(socket);
                Thread thread = new Thread(userManager);
                thread.start();

            }
        } catch (IOException e) {
            /** code  adapted from https://www.codejava.net/java-se/networking/how-to-create-a-chat-console-application-in-java-using-socket */
            System.out.println("Error in the server: " + e.getMessage());
            /** End of adapted code */
            closeServerSocket();
        }
    }

    // Close the server socket.
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Run the program.
    public static void main(String[] args) throws IOException {
        // Asking the server what port they want the connection on.
        Scanner scanner = new Scanner(System.in);
        /** "while valid" code adapted from https://stackoverflow.com/questions/20075940/catching-an-inputmismatchexception-until-it-is-correct */
        boolean valid = false;
        while (!valid) {
            System.out.println("Enter a port number: ");
            // To make sure that the server port only excepts numbers.
            try {
                int portNumber = scanner.nextInt();
                System.out.println("Server is hosting on port: " + portNumber);
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Server server = new Server(serverSocket);
                server.startServer();
                valid = true;
            } catch (InputMismatchException x) {
                System.out.println("Please enter a number.");
                scanner.next();
            }
        }
    }
}


// Tyler Owen-Thomas