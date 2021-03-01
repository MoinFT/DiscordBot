package de.moinFT.main;

import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

import static de.moinFT.main.Main.DBServer;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class MessageListener implements MessageCreateListener {

    private Server Server = null;
    private long ServerID = 0;
    private Message Message = null;
    private String MessageContent = "";

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Server = event.getServer().get();
        ServerID = Server.getId();
        Message = event.getMessage();
        MessageContent = event.getMessageContent().toLowerCase();

        if (event.isServerMessage()) {
            if (!event.getMessageAuthor().isBotUser()) {
                if (MessageContent.startsWith("!")) {
                    if (event.getMessageAuthor().isServerAdmin()) {
                        if (MessageContent.startsWith("!clear")) {
                            int MessValue = 0;

                            try {
                                MessValue = parseInt(MessageContent.substring(7)) + 1;
                            } catch (Exception e) {
                                event.deleteMessage();
                            }

                            try {
                                event.getChannel().bulkDelete(event.getChannel().getMessages(MessValue).get());
                            } catch (InterruptedException | ExecutionException e) {
                                System.out.println("Error: Messages couldn't be deleted (check Permissions)!");
                                e.printStackTrace();
                            }
                        } else if (MessageContent.startsWith("!add-role")) {
                            Role addRole = getRole_FromMessage(Message);
                            User addUser = getUser_FromMessage(Message);

                            if (addRole != null && addUser != null) {
                                Server.addRoleToUser(addUser, addRole);
                            }

                            event.deleteMessage();
                        } else if (MessageContent.startsWith("!remove-role")) {
                            Role removeRole = getRole_FromMessage(Message);
                            User removeUser = getUser_FromMessage(Message);

                            if (removeRole != null && removeUser != null) {
                                Server.removeRoleFromUser(removeUser, removeRole);
                            }

                            event.deleteMessage();
                        } else if (MessageContent.startsWith("!channel-set")) {
                            long channelID = Message.getMentionedChannels().get(0).asTextChannel().get().getId();
                            String channelName = "";

                            try{
                                channelName = MessageContent.split(" ")[2];
                                if (channelName.equals("_")){
                                    channelName = "";
                                }
                            } catch (Exception e) {
                                System.out.println("Error: No channelName found in the message!");
                            }

                            DBServer.getServer(ServerID).getChannels().updateChannelName(channelID, channelName);
                            DatabaseConnection.DBUpdateItem(Server.getId() + "_Channel", DBServer.getServer(ServerID).getChannels().getDB_ID(channelID), "`channelName` = '" + channelName + "'");

                            Message.delete();
                        }else if (MessageContent.startsWith("!role-set")) {
                            long roleID = Message.getMentionedRoles().get(0).getId();
                            String roleType = "";
                            String roleName = "";

                            try{
                                roleType = MessageContent.split(" ")[2];
                                if (roleType.equals("_")){
                                    roleType = "";
                                }
                            } catch (Exception e) {
                                System.out.println("Error: No roleType found in the message!");
                            }

                            try{
                                roleName = MessageContent.split(" ")[3];
                                if (roleName.equals("_")){
                                    roleName = "";
                                }
                            } catch (Exception e) {
                                System.out.println("Error: No roleName found in the message!");
                            }

                            DBServer.getServer(ServerID).getRoles().updateRoleType(roleID, roleType);
                            DBServer.getServer(ServerID).getRoles().updateRoleName(roleID, roleName);
                            DatabaseConnection.DBUpdateItem(Server.getId() + "_Role", DBServer.getServer(ServerID).getRoles().getDB_ID(roleID), "`roleType` = '" + roleType + "'");
                            DatabaseConnection.DBUpdateItem(Server.getId() + "_Role", DBServer.getServer(ServerID).getRoles().getDB_ID(roleID), "`roleName` = '" + roleName + "'");

                            Message.delete();
                        } else if (MessageContent.startsWith("!help-all")) {
                            TextChannel textChannel;
                            int AdminChannelID_Cache = DBServer.getServer(Server.getId()).getChannels().getID("admin");
                            if (AdminChannelID_Cache > -1) {
                                long AdminChannelID = DBServer.getServer(Server.getId()).getChannels().getChannelID(AdminChannelID_Cache);
                                textChannel = Server.getChannelById(AdminChannelID).get().asTextChannel().get();
                            } else {
                                textChannel = Message.getChannel().asTextChannel().get();
                            }

                            EmbedBuilder embed = new EmbedBuilder()
                                    .setTitle("Bot-Befehle")
                                    .addField("Admin-Befehle (Ohne Bot-Berechtigungen)", "\u200B")
                                    .addField("!help-all", "Diese Liste")
                                    .addField("!help-set", "Bot-Befehle (Bot Variablen setzen)")
                                    .addField("!clear [Wert]", "Loescht [Wert] Nachrichten aus einem TextChannel")
                                    .addField("\u200B", "\u200B")
                                    .addField("Admin-Befehle (Mit Bot-Berechtigungen)", "\u200B")
                                    .addField("!add-role [UserMention] [RoleMention]", "Gibt dem Nutzer [UserMention] die Rolle [RoleMention]")
                                    .addField("!remove-role [UserMention] [RoleMention]", "Nimmt dem Nutzer [UserMention] die Rolle [RoleMention]");

                            textChannel.sendMessage(embed);

                            Message.delete();
                        } else if (MessageContent.startsWith("!help-set")) {
                            TextChannel textChannel;
                            int AdminChannelDB_ID = DBServer.getServer(Server.getId()).getChannels().getDB_ID("admin");
                            if (AdminChannelDB_ID > -1) {
                                long AdminChannelID = DBServer.getServer(Server.getId()).getChannels().getChannelID(AdminChannelDB_ID);
                                textChannel = Server.getChannelById(AdminChannelID).get().asTextChannel().get();
                            } else {
                                textChannel = Message.getChannel().asTextChannel().get();
                            }

                            EmbedBuilder embed = new EmbedBuilder()
                                    .setTitle("Bot-Befehle (Bot Variablen setzen)")
                                    .addField("!help-set", "Diese Liste")
                                    .addField("!channel-set [channelMention] [channelName]", "Fuegt einem Channel [channelMention] einen Namen [channelName] hinzu. \n Fuer Admin (Beispiel): !channel-set #admin admin")
                                    .addField("!role-set [roleMention] [roleType] [roleName]", "Fuegt einer Rolle [roleMention] einen Typ [roleType] und \n einen Namen [roleName] hinzu. \n Fuer eine Farbe (Beispiel): !role-set @yellow color yellow");

                            textChannel.sendMessage(embed);

                            Message.delete();
                        } else {
                            normalCommands(Message);
                        }
                    } else {
                        normalCommands(Message);
                    }
                }
            }
        }
    }

    private void normalCommands(Message message) {
        if (MessageContent.startsWith("!color")) {
            int DBRoleCount = DBServer.getServer(ServerID).getRoles().count();

            if (DBRoleCount > 0) {
                ListIterator<Role> userRoles = message.getUserAuthor().get().getRoles(Server).listIterator();

                while (userRoles.hasNext()) {
                    Role userRole = userRoles.next();
                    for (int i = 0; i < DBRoleCount; i++) {
                        if(DBServer.getServer(ServerID).getRoles().getRoleType(i).equalsIgnoreCase("color")) {
                            if (userRole.getId() == DBServer.getServer(ServerID).getRoles().getRoleID(i)) {
                                message.getUserAuthor().get().removeRole(userRole);
                            }
                        }
                    }
                }

                if (MessageContent.length() > 7) {
                    String messValue = MessageContent.substring(7);

                    for (int i = 0; i < DBRoleCount; i++) {
                        if (DBServer.getServer(ServerID).getRoles().getRoleName(i).equalsIgnoreCase(messValue) && DBServer.getServer(ServerID).getRoles().getRoleType(i).equalsIgnoreCase("color")) {
                            Role role = Server.getRoleById(DBServer.getServer(ServerID).getRoles().getRoleID(i)).get();

                            Message.getUserAuthor().get().addRole(role);
                        }
                    }
                }
            }

            Message.delete();
        } else if (MessageContent.startsWith("!m-a")) {
            ServerVoiceChannel voiceChannel = null;
            try {
                voiceChannel = Message.getUserAuthor().get().getConnectedVoiceChannel(Server).get();
            } catch (Exception e) {
                System.out.println("Error: The MessageAuthor isn't connected to a VoiceChannel!");
            }

            if (voiceChannel != null) {
                Iterator<User> users = voiceChannel.getConnectedUsers().iterator();

                User user = users.next();
                if (!user.isMuted(Server)) {
                    while (users.hasNext()) {
                        user = users.next();
                        user.mute(Server);
                    }
                } else {
                    while (users.hasNext()) {
                        user = users.next();
                        user.unmute(Server);
                    }
                }
            }
            Message.delete();
        } else if (MessageContent.startsWith("!help")) {
            String colorInfoString = getColorInfoString();

            TextChannel textChannel = message.getChannel();
            EmbedBuilder embed;

            if (colorInfoString.length() != 0) {
                embed = new EmbedBuilder()
                        .setTitle("Bot-Befehle")
                        .addField("!help", "Diese Liste")
                        .addField("!m-a", "Alle Personnen in seinem VoiceChannel stummschalten (Cooldown: 5 Sekunden)")
                        .addField("!color [color]", colorInfoString)
                        .addField("!color", "Farbe entfernen");
            } else {
                embed = new EmbedBuilder()
                        .setTitle("Bot-Befehle")
                        .addField("!help", "Diese Liste")
                        .addField("!m-a", "Alle Personnen in seinem VoiceChannel stummschalten (Cooldown: 5 Sekunden)");
            }

            textChannel.sendMessage(embed);

            Message.delete();
        }
    }

    //Get colorInfoString from DB role information
    private String getColorInfoString() {
        int roleCountDB = DBServer.getServer(Server.getId()).getRoles().count();
        int colorCount = 0;

        StringBuilder colorInfoString = new StringBuilder("Farben: ");

        for (int i = 0; i < roleCountDB; i++) {
            System.out.println(DBServer.getServer(Server.getId()).getRoles().getRoleType(i));
            if (DBServer.getServer(Server.getId()).getRoles().getRoleType(i).equalsIgnoreCase("color")) {
                if (colorInfoString.toString().equals("Farben: ")) {
                    colorInfoString.append(DBServer.getServer(Server.getId()).getRoles().getRoleName(i));
                } else {
                    colorInfoString.append(",").append(DBServer.getServer(Server.getId()).getRoles().getRoleName(i));
                }
                colorCount++;
            }
        }

        System.out.println(colorCount);
        System.out.println(colorInfoString.toString());

        if (colorCount == 0) {
            return "";
        } else {
            return colorInfoString.toString();
        }
    }

    //Get the first mentioned Role of the Message or the RoleID
    private Role getRole_FromMessage(Message message) {
        long roleID = 0;

        if (!message.getMentionedRoles().isEmpty()) {
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
    private User getUser_FromMessage(Message message) {
        long userID = 0;

        if (!message.getMentionedUsers().isEmpty()) {
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