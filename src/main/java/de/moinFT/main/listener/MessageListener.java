package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
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
    private String Prefix = "";

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Server = event.getServer().get();
        ServerID = Server.getId();
        Message = event.getMessage();
        MessageContent = event.getMessageContent().toLowerCase();
        Prefix = DBServer.getPrefix(ServerID);

        if (Message.isServerMessage()) {
            if (!Message.getUserAuthor().get().isBot()) {
                if (MessageContent.startsWith(Prefix)) {
                    if (event.getMessageAuthor().isServerAdmin()) {
                        if (MessageContent.startsWith(Prefix + "clear")) {
                            int MessValue = 0;

                            try {
                                MessValue = parseInt(MessageContent.substring(7)) + 1;
                            } catch (Exception e) {
                                Message.delete();
                            }

                            try {
                                event.getChannel().bulkDelete(event.getChannel().getMessages(MessValue).get());
                            } catch (InterruptedException | ExecutionException e) {
                                System.out.println("Error: Messages couldn't be deleted (check Permissions)!");
                                e.printStackTrace();
                            }
                        } else if (MessageContent.startsWith(Prefix + "add-role")) {
                            Role addRole = getFirstRole_FromMessage(Message);
                            User addUser = getFirstUser_FromMessage(Message);

                            if (addRole != null && addUser != null) {
                                Server.addRoleToUser(addUser, addRole);
                            }

                            Message.delete();
                        } else if (MessageContent.startsWith(Prefix + "remove-role")) {
                            Role removeRole = getFirstRole_FromMessage(Message);
                            User removeUser = getFirstUser_FromMessage(Message);

                            if (removeRole != null && removeUser != null) {
                                Server.removeRoleFromUser(removeUser, removeRole);
                            }

                            Message.delete();
                        } else if (MessageContent.startsWith(Prefix + "channel-set")) {
                            long channelID = Message.getMentionedChannels().get(0).asTextChannel().get().getId();
                            String channelName = "";

                            try {
                                channelName = MessageContent.split(" ")[2];
                                if (channelName.equals("_")) {
                                    channelName = "";
                                }
                            } catch (Exception e) {
                                System.out.println("Error: No channelName found in the message!");
                            }

                            DBServer.getServer(ServerID).getChannels().updateChannelName(channelID, channelName);
                            DatabaseConnection.DBUpdateItem(Server.getId() + "_Channel", DBServer.getServer(ServerID).getChannels().getDB_ID(channelID), "`channelName` = '" + channelName + "'");

                            Message.delete();
                        } else if (MessageContent.startsWith(Prefix + "role-set")) {
                            long roleID = Message.getMentionedRoles().get(0).getId();
                            String roleType = "";
                            String roleName = "";

                            try {
                                roleType = MessageContent.split(" ")[2];
                                if (roleType.equals("-")) {
                                    roleType = "";
                                }
                            } catch (Exception e) {
                                System.out.println("Error: No roleType found in the message!");
                            }

                            try {
                                roleName = MessageContent.split(" ")[3];
                                if (roleName.equals("-")) {
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
                        } else if (MessageContent.startsWith(Prefix + "help-all") || MessageContent.startsWith(Prefix + "help-set")) {
                            getHelpMessage(true);
                        } else {
                            normalCommands();
                        }
                    } else {
                        normalCommands();
                    }
                }
            }
        }
    }

    private void normalCommands() {
        if (MessageContent.startsWith(Prefix + "color")) {
            int DBRoleCount = DBServer.getServer(ServerID).getRoles().count();

            if (DBRoleCount > 0) {
                ListIterator<Role> userRoles = Message.getUserAuthor().get().getRoles(Server).listIterator();

                while (userRoles.hasNext()) {
                    Role userRole = userRoles.next();
                    for (int i = 0; i < DBRoleCount; i++) {
                        if (DBServer.getServer(ServerID).getRoles().getRoleType(i).equalsIgnoreCase("color")) {
                            if (userRole.getId() == DBServer.getServer(ServerID).getRoles().getRoleID(i)) {
                                Message.getUserAuthor().get().removeRole(userRole);
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
        } else if (MessageContent.startsWith(Prefix + "m-a")) {
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
        } else if (MessageContent.startsWith(Prefix + "help")) {
            getHelpMessage(false);
        }
    }

    private void getHelpMessage(boolean admin) {
        if (MessageContent.startsWith(Prefix + "help")) {
            String colorInfoString = getColorInfoString();

            TextChannel textChannel = Message.getChannel();
            EmbedBuilder embed;

            if (colorInfoString.length() != 0) {
                embed = new EmbedBuilder()
                        .setTitle("Bot-Befehle")
                        .addField(Prefix + "help", "Diese Liste")
                        .addField(Prefix + "m-a", "Alle Personnen in seinem VoiceChannel stummschalten (Cooldown: 5 Sekunden)")
                        .addField(Prefix + "color [color]", colorInfoString)
                        .addField(Prefix + "color", "Farbe entfernen");
            } else {
                embed = new EmbedBuilder()
                        .setTitle("Bot-Befehle")
                        .addField(Prefix + "help", "Diese Liste")
                        .addField(Prefix + "m-a", "Alle Personnen in seinem VoiceChannel stummschalten (Cooldown: 5 Sekunden)");
            }

            textChannel.sendMessage(embed);

            Message.delete();
        } else if (admin) {
            TextChannel textChannel;
            int AdminChannelID_Cache = DBServer.getServer(Server.getId()).getChannels().getID("admin");
            if (AdminChannelID_Cache > -1) {
                long AdminChannelID = DBServer.getServer(Server.getId()).getChannels().getChannelID(AdminChannelID_Cache);
                textChannel = Server.getChannelById(AdminChannelID).get().asTextChannel().get();
            } else {
                textChannel = Message.getChannel().asTextChannel().get();
            }

            EmbedBuilder embed = null;

            if (MessageContent.startsWith(Prefix + "help-all")) {
                embed = new EmbedBuilder()
                        .setTitle("Bot-Befehle")
                        .addField("Admin-Befehle (Ohne Bot-Berechtigungen)", "\u200B")
                        .addField(Prefix + "help-all", "Diese Liste")
                        .addField(Prefix + "help-set", "Bot-Befehle (Bot Variablen setzen)")
                        .addField(Prefix + "clear [Wert]", "Loescht [Wert] Nachrichten aus einem TextChannel")
                        .addField("\u200B", "\u200B")
                        .addField("Admin-Befehle (Mit Bot-Berechtigungen)", "\u200B")
                        .addField(Prefix + "add-role [UserMention] [RoleMention]", "Gibt dem Nutzer [UserMention] die Rolle [RoleMention]")
                        .addField(Prefix + "remove-role [UserMention] [RoleMention]", "Nimmt dem Nutzer [UserMention] die Rolle [RoleMention]");
            } else if (MessageContent.startsWith(Prefix + "help-set")) {
                embed = new EmbedBuilder()
                        .setTitle("Bot-Befehle (Bot Variablen setzen)")
                        .addField(Prefix + "help-set", "Diese Liste")
                        .addField(Prefix + "channel-set [channelMention] [channelName]", "Fuegt einem Channel [channelMention] einen Namen [channelName] hinzu. \n Fuer Admin (Beispiel): !channel-set #admin admin")
                        .addField(Prefix + "role-set [roleMention] [roleType] [roleName]", "Fuegt einer Rolle [roleMention] einen Typ [roleType] und \n einen Namen [roleName] hinzu. \n Fuer eine Farbe (Beispiel): !role-set @yellow color yellow");
            }

            if (embed != null) {
                textChannel.sendMessage(embed);
            }

            Message.delete();
        }
    }

    //Get colorInfoString from DB role information
    private String getColorInfoString() {
        int roleCountDB = DBServer.getServer(ServerID).getRoles().count();
        int colorCount = 0;

        StringBuilder colorInfoString = new StringBuilder("Farben: ");

        for (int i = 0; i < roleCountDB; i++) {
            if (DBServer.getServer(ServerID).getRoles().getRoleType(i).equals("color")) {
                if (colorInfoString.toString().equals("Farben: ")) {
                    colorInfoString.append(DBServer.getServer(ServerID).getRoles().getRoleName(i));
                } else {
                    colorInfoString.append(" ,").append(DBServer.getServer(ServerID).getRoles().getRoleName(i));
                }
                colorCount++;
            }
        }

        if (colorCount == 0) {
            return "";
        } else {
            return colorInfoString.toString();
        }
    }

    //Get the first mentioned Role of the Message or the RoleID
    private Role getFirstRole_FromMessage(Message message) {
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
    private User getFirstUser_FromMessage(Message message) {
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