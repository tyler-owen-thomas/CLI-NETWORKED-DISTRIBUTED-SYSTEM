package to2498gre.ac.uk;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;

public class UserManager implements Runnable {

    // This Array list will list of all the threads handling users so that messages can be sent to user the thread is handling.
    public static ArrayList<UserManager> userManagers = new ArrayList<>();

    // To keep a list of users.
    public static ArrayList<String> userLists = new ArrayList<>();
    // Time stamp is created from current operating system time.
    /** Code adapted from https://mkyong.com/java/how-to-get-current-timestamps-in-java/ */
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    /** End of adapted code */

    // Socket handles the connection. The buffered reader and writer  is used for sending and receiving data.
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;
    //Random integer is created to ensure a unique username is always possible.
    /** Code adapted from https://stackoverflow.com/questions/20389890/generating-a-random-number-between-1-and-10-java */
    Random random = new Random();
    int uniqueID = random.nextInt(100 - 1 + 1) + 1;
    /** End of adapted code */

    // Creating the userManager from the socket the server gives.
    public UserManager(Socket socket) {
            try {
                this.socket = socket;
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                // When a user connects their username read.
                this.userName = bufferedReader.readLine();
                // If there is a duplicate name then a random number will be assigned.
                if (userLists.contains(userName)) {
                    String userName2 = userName + "(" + uniqueID + ")";
                    sendMessage("SERVER: New user had a duplicate name, random number assigned");
                    userName = userName2;
                }
                // Add the new user to the array, so they can receive messages from others.
                userManagers.add(this);
                // A message gets broadcast so that everyone can see who has joined.
                sendMessage("SERVER: " + userName + " has entered the chat!");
                // This adds the username to a different Array list so that the coordinator can be chosen.
                userLists.add(userName);
                System.out.println("The Members in this chatroom are" + ": " + userLists);
                // Try catch block is there for when all the users leave it will throw an index out of bounds error.
                try {
                    if (userLists != null) {
                        sendMessage(userLists.get(0) + " is the coordinator");
                        System.out.println("The coordinator is: " + userLists.get(0));
                    }
                } catch (IndexOutOfBoundsException exception) {
                    System.out.println("No users in chat, no active coordinator can be assigned.");

                }

            } catch (IOException e) {
                // Close everything
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }

    @Override
    public void run() {
        String messageFromClient;
        // This will keep listening for messages while a connection with the user is still established.
        while (socket.isConnected()) {
            try {
                // Reading what the user sent and then send it to every other user.
                messageFromClient = bufferedReader.readLine();
                sendMessage(timestamp + " " + messageFromClient);
            } catch (IOException e) {
                // Close everything.
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void sendMessage(String messageToSend) {
        for (UserManager userManager : userManagers) {
            try {
                // This is so that the user doesn't get sent their own message.
                if (userManager!=this) {
                    userManager.bufferedWriter.write(messageToSend);
                    userManager.bufferedWriter.newLine();
                    userManager.bufferedWriter.flush();
                } try {
                    // This is for the coordinator. Only the coordinator can see this code. If your username is the same as
                    // the index 0 (the first one) and user manager equals this then only that specific person can see the message.
                    if (userName == userLists.get(0) && userManager==this) {
                        if (messageToSend.contains("help")) {
                            userManager.bufferedWriter.write("Type \"groupinfo\" for information about the group.");
                            userManager.bufferedWriter.newLine();
                            userManager.bufferedWriter.flush();
                        }
                        if (messageToSend.contains("groupinfo")) {
                            String users = String.valueOf(userLists);
                            String hosts = String.valueOf(userManagers);
                            userManager.bufferedWriter.write(users + System.lineSeparator() + hosts);
                            userManager.bufferedWriter.newLine();
                            userManager.bufferedWriter.flush();
                        }
                    }
                }catch (IndexOutOfBoundsException e) {

                }
            } catch (IOException e) {
                // Close everything.
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // If the user disconnects remove the user from the list so a message isn't sent down a broken connection.
     void removeUserManager() {
        userManagers.remove(this);
        sendMessage("SERVER: " + userName + " has left the chat!");
        userLists.remove(userName);
        try {
            sendMessage(userLists.get(0) + " is the coordinator!");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("No users in chat, no active coordinator can be assigned.");
        }
        sendMessage("The remaining users are: " + userLists);
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

        // If the user disconnected or an error occurred this will remove them from the list to ensure that no message is broadcast.
        removeUserManager();
        try {
            System.out.println( userName + " has disconnected.");
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


}

// Tyler Owen-Thomas