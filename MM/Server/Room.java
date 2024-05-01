package MM.Server;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import MM.Common.Constants;

public class Room implements AutoCloseable {
    // protected static Server server;// used to refer to accessible server
    // functions
    private String name;
    private List<ServerThread> clients = new ArrayList<ServerThread>();

    private boolean isRunning = false;
    // Commands
    private final static String COMMAND_TRIGGER = "/";
    private final static String CREATE_ROOM = "createroom";
    private final static String JOIN_ROOM = "joinroom";
    private final static String DISCONNECT = "disconnect";
    private final static String LOGOUT = "logout";
    private final static String LOGOFF = "logoff";
    private final static String ROLL = "roll";
    private final static String FLIP = "flip";
    private final static String MUTE = "mute";
    private final static String UN_MUTE = "unmute";

    private Logger logger = Logger.getLogger(Room.class.getName());

    public Room(String name) {
        this.name = name;
        isRunning = true;
    }

    private void info(String message) {
        logger.info(String.format("Room[%s]: %s", name, message));
    }

    public String getName() {
        return name;
    }

    protected synchronized void addClient(ServerThread client) {
        if (!isRunning) {
            return;
        }
        client.setCurrentRoom(this);
        client.sendJoinRoom(getName());// clear first
        if (clients.indexOf(client) > -1) {
            info("Attempting to add a client that already exists");
        } else {
            clients.add(client);
            // connect status second
            sendConnectionStatus(client, true);
            syncClientList(client);
        }

    }

    protected synchronized void removeClient(ServerThread client) {
        if (!isRunning) {
            return;
        }
        clients.remove(client);
        // we don't need to broadcast it to the server
        // only to our own Room
        if (clients.size() > 0) {
            // sendMessage(client, "left the room");
            sendConnectionStatus(client, false);
        }
        checkClients();
    }

    /***
     * Checks the number of clients.
     * If zero, begins the cleanup process to dispose of the room
     */
    private void checkClients() {
        // Cleanup if room is empty and not lobby
        if (!name.equalsIgnoreCase(Constants.LOBBY) && clients.size() == 0) {
            close();
        }
    }

    /***
     * Helper function to process messages to trigger different functionality.
     * 
     * @param message The original message being sent
     * @param client  The sender of the message (since they'll be the ones
     *                triggering the actions)
     */
    private boolean processCommands(String message, ServerThread client) {
        
        boolean wasCommand = false;
        try {
            if (message.startsWith("@")) {
                sendPrivateMessage(message, message, client);
                wasCommand = true;
            } else if (message.startsWith(COMMAND_TRIGGER)) {
                String[] comm = message.split(COMMAND_TRIGGER);
                String part1 = comm[1];
                String[] comm2 = part1.split(" ");
                String command = comm2[0];
                String roomName;
                wasCommand = true;
                switch (command) {
                    case CREATE_ROOM:
                        roomName = comm2[1];
                        Room.createRoom(roomName, client);
                        break;
                    case JOIN_ROOM:
                        roomName = comm2[1];
                        Room.joinRoom(roomName, client);
                        break;
                    case DISCONNECT:
                    case LOGOUT:
                    case LOGOFF:
                        //room.disconnectClient(client, this);
                        break;
                    case ROLL:
                        String result = new RollandFlip().handleRollCommand(comm2);
                        broadcast(result);
                        break;
                    case FLIP:
                        String res = new RollandFlip().handleFlipCommand();
                        broadcast(res);
                        break;
                    case MUTE:
                        if (comm2.length > 1){
                            String userToMute = comm2[1];
                            muteUser(userToMute, client);
                        }
                        break;
                    case UN_MUTE:
                        if (comm2.length > 1){
                            String user = comm2[1];
                            unMuteUser(user, client);
                        }
                        break;
                        case "pm":
                        if (comm2.length > 2) {
                            String userToSend = comm2[1];
                            String privateMessage = message.substring(message.indexOf(" ", message.indexOf(" ") + 1) + 1); //bryan madewell bm47 IT114 chatroom spring 2024
                            sendPrivateMessage(userToSend, privateMessage, client);
                        } else {
                            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "Invalid private message format. Usage: /pm (username) to send a private message");
                        }
                        break;
                    default:
                        wasCommand = false;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wasCommand;
    }
        

    private void sendPrivateMessage(String userToSend, String message, ServerThread sender) {
        // Remove "@" symbol from the username if present
        if (userToSend.startsWith("@")) {
            userToSend = userToSend.substring(1);
        }
    
        for (ServerThread client : clients) {
            if (client.getClientName().equalsIgnoreCase(userToSend)) {
                client.sendMessage(sender.getClientId(), "PM from " + sender.getClientName() + ": " + message);
                return;
            }
        }
        sender.sendMessage(Constants.DEFAULT_CLIENT_ID, "User @" + userToSend + " not found in the room.");
    }
    

private void broadcast(String msg){
    for (ServerThread client : clients) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, msg);
    }
}

//bm47 IT114 Spring 2024 milestone 4

private void muteUser(String userToMute, ServerThread muter) {
    for (ServerThread client : clients) {
        if (client.getClientName().equals(userToMute)) {
            if (!client.isMuted()) {
                client.setMuted(true);
                client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have been muted by " + muter.getClientName());
                logger.info(userToMute + " has been muted by " + muter.getClientName());
            } else {
                logger.info(userToMute + " is already muted.");
            }
            return;
        }
    }
    logger.info("User " + userToMute + " not found in the room.");
}

private void unMuteUser(String user, ServerThread muter) {
    for (ServerThread client : clients) {
        if (client.getClientName().equals(user)) {
            client.setMuted(false);
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have been unmuted by " + muter.getClientName());
            logger.info(user + " has been unmuted by " + muter.getClientName());
            return;
        }
    }
    logger.info("User " + user + " not found in the room.");
}

    // Command helper methods
    private synchronized void syncClientList(ServerThread joiner) {
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread st = iter.next();
            if (st.getClientId() != joiner.getClientId()) {
                joiner.sendClientMapping(st.getClientId(), st.getClientName());
            }
        }
    }
    protected static void createRoom(String roomName, ServerThread client) {
        if (Server.INSTANCE.createNewRoom(roomName)) {
            //server.joinRoom(roomName, client);
            Room.joinRoom(roomName, client);
        } else {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s already exists", roomName));
        }
    }

    protected static void joinRoom(String roomName, ServerThread client) {
        if (!Server.INSTANCE.joinRoom(roomName, client)) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s doesn't exist", roomName));
        }
    }

    protected static List<String> listRooms(String searchString, int limit) {
        return Server.INSTANCE.listRooms(searchString, limit);
    }

    protected static void disconnectClient(ServerThread client, Room room) {
        client.setCurrentRoom(null);
        client.disconnect();
        room.removeClient(client);
    }
    // end command helper methods

    /***
     * Takes a sender and a message and broadcasts the message to all clients in
     * this room. Client is mostly passed for command purposes but we can also use
     * it to extract other client info.
     * 
     * @param sender  The client sending the message
     * @param message The message to broadcast inside the room
     */
    protected synchronized void sendMessage(ServerThread sender, String message) {
        if(sender.isMuted()){
            return;
        }
        if (!isRunning) {
            return;
        }
        info("Sending message to " + clients.size() + " clients");
        if (sender != null && processCommands(message, sender)) {
            // it was a command, don't broadcast
            return;
        }
    
        long from = (sender == null) ? Constants.DEFAULT_CLIENT_ID : sender.getClientId();
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread client = iter.next();
            // Check if the client is muted, if yes, skip sending the message
            if (client.isMuted()) {
                continue;
            }
            // Process the message for text formatting
            String formattedMessage = processTextFormatting(message);
    
            boolean messageSent = client.sendMessage(from, formattedMessage);// BRYAN MADEWELL bm47 IT114 CHATROOM PROJECT SPRING 2024
            if (!messageSent) {
                handleDisconnect(iter, client);
            }
        }
    }
    
    private String processTextFormatting(String message) {
        boolean isBold = false;
        boolean isItalic = false;
        boolean isUnderline = false;
        String color = ""; // Empty string represents the default chat color
    
        StringBuilder formattedMessage = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            char currentChar = message.charAt(i);
            if (currentChar == '&') {
                if (i + 1 < message.length()) {
                    char nextChar = message.charAt(i + 1);
                    if (nextChar == 'b') {
                        isBold = !isBold; 
                        formattedMessage.append(isBold ? "<b>" : "</b>");
                        i++; 
                    } else if (nextChar == 'i') {
                        isItalic = !isItalic; 
                        formattedMessage.append(isItalic ? "<i>" : "</i>");
                        i++; 
                    } else if (nextChar == 'u') {
                        isUnderline = !isUnderline; 
                        formattedMessage.append(isUnderline ? "<u>" : "</u>");
                        i++; 
                    } else if (nextChar == '1') {
                        color = "red"; // Set color to red
                        formattedMessage.append("<font color=\"red\">");
                        i++; 
                    } else if (nextChar == '2') {
                        color = "green"; // Set color to green
                        formattedMessage.append("<font color=\"green\">");
                        i++; 
                    } else if (nextChar == '3') {
                        color = "blue"; // Set color to blue
                        formattedMessage.append("<font color=\"blue\">");
                        i++; 
                    }
                    continue;
                }
            }
            formattedMessage.append(currentChar);

        }
            if (isBold) {
            formattedMessage.append("</b>");
        }
        if (isItalic) {
            formattedMessage.append("</i>");
        }
        if (isUnderline) {
            formattedMessage.append("</u>");
        }
        if (!color.isEmpty()) {
            formattedMessage.append("</font>");
        }
        return formattedMessage.toString();
    }
                                        // BRYAN MADEWELL bm47 IT114 CHATROOM PROJECT SPRING 2024


    protected synchronized void sendConnectionStatus(ServerThread sender, boolean isConnected) {
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread client = iter.next();
            boolean messageSent = client.sendConnectionStatus(sender.getClientId(), sender.getClientName(),
                    isConnected);
            if (!messageSent) {
                handleDisconnect(iter, client);
            }
        }
    }

    private void handleDisconnect(Iterator<ServerThread> iter, ServerThread client) {
        iter.remove();
        info("Removed client " + client.getClientName());
        checkClients();
        sendMessage(ServerConstants.FROM_ROOM, client.getClientName() + " disconnected");
    }

    public void close() {
        Server.INSTANCE.removeRoom(this);
        //server = null;
        isRunning = false;
        clients = null;
    }
    
}
