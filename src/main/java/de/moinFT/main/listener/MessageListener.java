package de.moinFT.main.listener;

import de.moinFT.main.DatabaseConnection;
import de.moinFT.main.Functions;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.time.ZoneId;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

import static de.moinFT.main.Functions.*;
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
        Prefix = DBServer.getServer(ServerID).getPrefix();

        if (Message.isServerMessage()) {
            if (!Message.getUserAuthor().get().isYourself() && !Message.getUserAuthor().get().isBot()) {
                if (MessageContent.startsWith(Prefix)) {
                    if (DBServer.getServer(ServerID).getUsers().getBotPermission(Message.getUserAuthor().get().getId())) {
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
                        } else if (MessageContent.startsWith(Prefix + "toggle-bot-permission")) {
                            User user = getFirstUser_FromMessage(1, Message);

                            if (user != null) {
                                long userID = user.getId();
                                boolean botPermission = DBServer.getServer(ServerID).getUsers().getBotPermission(userID);

                                if (userID != Message.getUserAuthor().get().getId()) {
                                    int adminChannelID = DBServer.getServer(Server.getId()).getChannels().getID("admin");

                                    if (adminChannelID != -1) {
                                        ServerChannel adminChannel = Server.getChannelById(DBServer.getServer(ServerID).getChannels().getChannelID(adminChannelID)).get();

                                        if (!botPermission) {
                                            new ServerChannelUpdater(adminChannel).addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build()).update();
                                        } else {
                                            new ServerChannelUpdater(adminChannel).addPermissionOverwrite(user, new PermissionsBuilder().setUnset(PermissionType.READ_MESSAGES).build()).update();
                                        }
                                    }

                                    DBServer.getServer(ServerID).getUsers().updateBotPermission(userID, !botPermission);
                                    DatabaseConnection.DBUpdateItem(Server.getId() + "_User", DBServer.getServer(ServerID).getUsers().getDB_ID(userID), "`botPermission` = '" + !botPermission + "'");
                                }
                            }

                            Message.delete();
                        } else if (MessageContent.startsWith(Prefix + "add-role")) {
                            Role highestRole = getUserHighestRole(Server, Message.getUserAuthor().get());

                            Role addRole = getFirstRole_FromMessage(Message);
                            User addUser = getFirstUser_FromMessage(2, Message);

                            if (addRole != null && addUser != null) {
                                if (highestRole.getPosition() >= addRole.getPosition()) {
                                    Server.addRoleToUser(addUser, addRole);
                                }
                            }

                            Message.delete();
                        } else if (MessageContent.startsWith(Prefix + "remove-role")) {
                            Role highestRole = getUserHighestRole(Server, Message.getUserAuthor().get());

                            Role removeRole = getFirstRole_FromMessage(Message);
                            User removeUser = getFirstUser_FromMessage(2, Message);

                            if (removeRole != null && removeUser != null) {
                                if (highestRole.getPosition() >= removeRole.getPosition()) {
                                    Server.removeRoleFromUser(removeUser, removeRole);
                                }
                            }

                            Message.delete();
                        } else if (MessageContent.startsWith(Prefix + "set-musicbot-prefix")) {
                            String prefix = "";

                            try {
                                prefix = MessageContent.split(" ")[1];
                                if (prefix.equals("_")) {
                                    prefix = "";
                                }
                            } catch (Exception e) {
                                System.out.println("Error: No prefix found in the message!");
                            }

                            DBServer.getServer(ServerID).updateMusicBotPrefix(prefix);
                            DatabaseConnection.DBUpdateItem("server", DBServer.getServer(ServerID).getDB_ID(), "`musicBotPrefix` = '" + prefix + "'");

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
                                if (roleType.equals("_")) {
                                    roleType = "";
                                }
                            } catch (Exception e) {
                                System.out.println("Error: No roleType found in the message!");
                            }

                            try {
                                roleName = MessageContent.split(" ")[3];
                                if (roleName.equals("_")) {
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
                        } else if (MessageContent.equalsIgnoreCase(Prefix + "help-all") || MessageContent.equalsIgnoreCase(Prefix + "help-set")) {
                            getHelpMessage(true);
                        } else {
                            normalCommands();
                        }
                    } else {
                        normalCommands();
                    }
                } else if (MessageContent.startsWith(DBServer.getServer(Server.getId()).getMusicBotPrefix())) {
                    int musicbotChannelID = DBServer.getServer(Server.getId()).getChannels().getID("musicbot");
                    if (Message.getChannel().getId() == DBServer.getServer(Server.getId()).getChannels().getChannelID(musicbotChannelID)) {
                        Functions.messageDelete(Message, 5000);
                    }
                }
            } else if (!Message.getUserAuthor().get().isYourself() && Message.getUserAuthor().get().isBot()) {
                Functions.messageDelete(Message, 2500);
            } else {
                Functions.messageDelete(Message, 45000);
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
        } else if (MessageContent.startsWith(Prefix + "info-user")) {
            User User = getFirstUser_FromMessage(1, Message);

            String joinDateString = User.getJoinedAtTimestamp(Server).get().atZone(ZoneId.systemDefault()).toString().split("T")[0];
            String joinDay = joinDateString.split("-")[2];
            String joinMonth = joinDateString.split("-")[1];
            String joinYear = joinDateString.split("-")[0];
            String joinDate = joinDay + "." + joinMonth + "." + joinYear;

            String botPermission;
            if (DBServer.getServer(ServerID).getUsers().getBotPermission(User.getId())) {
                botPermission = "Ja";
            } else {
                botPermission = "Nein";
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor(User)
                    .setTitle("User Info")
                    .addInlineField("Username:", User.getName())
                    .addInlineField("\u200B", "\u200B")
                    .addInlineField("Nickname:", User.getDisplayName(Server))
                    .addInlineField("Ist Member seit:", joinDate)
                    .addInlineField("\u200B", "\u200B")
                    .addInlineField("Bot-Berechtigungen", botPermission);

            Message.getChannel().sendMessage(embed);

            Message.delete();
        } else if (MessageContent.startsWith(Prefix + "info-server")) {
            String createDateString = Server.getCreationTimestamp().atZone(ZoneId.systemDefault()).toString().split("T")[0];
            String createDay = createDateString.split("-")[2];
            String createMonth = createDateString.split("-")[1];
            String createYear = createDateString.split("-")[0];
            String createDate = createDay + "." + createMonth + "." + createYear;

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Server Info")
                    .addInlineField("Server Besitzer:", Server.getOwner().get().getDisplayName(Server))
                    .addInlineField("\u200B", "\u200B")
                    .addInlineField("Ist existent seit:", createDate)
                    .addInlineField("Anzahl Member:", String.valueOf(Server.getMemberCount()))
                    .addInlineField("\u200B", "\u200B")
                    .addInlineField("Anzahl Member (Online):", String.valueOf(Functions.membersOnlineCount(Server)));

            Message.getChannel().sendMessage(embed);

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
        } else if (MessageContent.equalsIgnoreCase(Prefix + "help")) {
            getHelpMessage(false);
        }
    }

    private void getHelpMessage(boolean admin) {
        if (MessageContent.equalsIgnoreCase(Prefix + "help")) {
            String colorInfoString = getColorInfoString();

            TextChannel textChannel = Message.getChannel();
            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle("Bot-Befehle");
            embed.addField(Prefix + "help", "Diese Liste");
            embed.addField(Prefix + "info-user [UserMention]", "Zeigt Informationen über den User [UserMention] an.");
            embed.addField(Prefix + "info-server", "Zeigt Informationen über den Server an.");
            embed.addField(Prefix + "m-a", "Alle Personnen in seinem VoiceChannel stummschalten (Cooldown: 5 Sekunden)");

            if (colorInfoString.length() != 0) {

                embed.addField(Prefix + "color [color]", colorInfoString);
                embed.addField(Prefix + "color", "Farbe entfernen");
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

            if (MessageContent.equalsIgnoreCase(Prefix + "help-all")) {
                embed = new EmbedBuilder();
                embed.setTitle("Bot-Befehle (Mit Bot-Berechtigungen)");
                embed.addField("Admin-Befehle", "\u200B");
                embed.addField(Prefix + "help-all", "Diese Liste");
                embed.addField(Prefix + "help-set", "Bot-Befehle (Bot Variablen setzen)");
                embed.addField(Prefix + "clear [Wert]", "Loescht [Wert] Nachrichten aus einem TextChannel");
                embed.addField(Prefix + "add-role [UserMention] [RoleMention]", "Gibt dem Nutzer [UserMention] die Rolle [RoleMention]");
                embed.addField(Prefix + "remove-role [UserMention] [RoleMention]", "Nimmt dem Nutzer [UserMention] die Rolle [RoleMention]");
                embed.addField(Prefix + "toggle-permission-bot [UserMention]", "Gibt/Nimmt dem Nutzer (UserID) die Berechtigung die Befehle mit Bot Berechtigungen");
            } else if (MessageContent.equalsIgnoreCase(Prefix + "help-set")) {
                embed = new EmbedBuilder();
                embed.setTitle("Bot-Befehle");
                embed.addField("Admin-Befehle (Bot Variablen setzen)", "\u200B");
                embed.addField(Prefix + "help-set", "Diese Liste");
                embed.addField(Prefix + "channel-set [channelMention] [channelName]", "Fuegt einem Channel [channelMention] einen Namen [channelName] hinzu. \n Fuer Admin (Beispiel): !channel-set #admin admin");
                embed.addField(Prefix + "role-set [roleMention] [roleType] [roleName]", "Fuegt einer Rolle [roleMention] einen Typ [roleType] und \n einen Namen [roleName] hinzu. \n Fuer eine Farbe (Beispiel): !role-set @yellow color yellow");
            }

            if (embed != null) {
                String colorInfoString = getColorInfoString();

                embed.addField("\u200B", "\u200B");
                embed.addField("Normale-Befehle", "\u200B");
                embed.addField(Prefix + "help", "Diese Liste");
                embed.addField(Prefix + "info-user [UserMention]", "Zeigt Informationen über den User [UserMention] an.");
                embed.addField(Prefix + "info-server", "Zeigt Informationen über den Server an.");
                embed.addField(Prefix + "m-a", "Alle Personnen in seinem VoiceChannel stummschalten (Cooldown: 5 Sekunden)");

                if (colorInfoString.length() != 0) {

                    embed.addField(Prefix + "color [color]", colorInfoString);
                    embed.addField(Prefix + "color", "Farbe entfernen");
                }

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
                    colorInfoString.append(", ").append(DBServer.getServer(ServerID).getRoles().getRoleName(i));
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
    private User getFirstUser_FromMessage(int indexOfUserMention, Message message) {
        long userID = 0;

        if (!message.getMentionedUsers().isEmpty()) {
            return message.getMentionedUsers().get(0);
        } else {
            try {
                userID = parseLong(MessageContent.split(" ")[indexOfUserMention]);
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