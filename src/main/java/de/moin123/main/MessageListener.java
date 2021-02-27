package de.moin123.main;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class MessageListener implements MessageCreateListener {

    private Server Server = null;
    private Message Message = null;
    private String MessageContent = "";

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Server = event.getServer().get();
        Message = event.getMessage();
        MessageContent = event.getMessageContent().toLowerCase();

        if (event.isServerMessage()) {
            if (!event.getMessageAuthor().isBotUser()) {
                if (MessageContent.startsWith("!")) {
                    if (MessageContent.startsWith("!clear")) {
                        if (event.getMessageAuthor().isServerAdmin()) {
                            int MessValue = 0;

                            try {
                                MessValue = parseInt(MessageContent.substring(7)) + 1;
                            } catch (Exception e) {
                                event.getMessage().delete();
                            }

                            event.getChannel().bulkDelete((Iterable<Message>) event.getChannel().getMessages(MessValue));
                        } else {
                            event.deleteMessage();
                        }
                    } else if(MessageContent.startsWith("!add-role")){
                        Role addRole = getRole_FromMessage(Message);
                        User addUser = getUser_FromMessage(Message);

                        if(addRole != null && addUser != null) {
                            Server.addRoleToUser(addUser, addRole);
                        }

                        event.deleteMessage();
                    } else if(MessageContent.startsWith("!remove-role")){
                        Role removeRole = getRole_FromMessage(Message);
                        User removeUser = getUser_FromMessage(Message);

                        if(removeRole != null && removeUser != null) {
                            Server.removeRoleFromUser(removeUser, removeRole);
                        }

                        event.deleteMessage();
                    }
                }
            }
        }
    }

    //Get the first mentioned Role of the Message or the RoleID
    private Role getRole_FromMessage(Message message){
        long roleID = 0;

        if(!message.getMentionedRoles().isEmpty()){
            return message.getMentionedRoles().get(0);
        } else {
            try {
                roleID = parseLong(MessageContent.split(" ")[1]);
            } catch (Exception e) {
                System.out.println("The Message doesn't contain an RoleID!");
            }
        }

        try {
            return Server.getRoleById(roleID).get();
        } catch (Exception e) {
            System.out.println("Role doesn't exist! RoleID: " + roleID);
            return null;
        }
    }

    //Get the first mentioned User of the Message or the UserID
    private User getUser_FromMessage(Message message){
        long userID = 0;

        if(!message.getMentionedRoles().isEmpty()){
            return message.getMentionedUsers().get(0);
        } else {
            try {
                userID = parseLong(MessageContent.split(" ")[2]);
            } catch (Exception e) {
                System.out.println("The Message doesn't contain an UserID!");
            }
        }

        try {
            return Server.getMemberById(userID).get();
        } catch (Exception e) {
            System.out.println("User doesn't exist! UserID: " + userID);
            return null;
        }
    }
}
