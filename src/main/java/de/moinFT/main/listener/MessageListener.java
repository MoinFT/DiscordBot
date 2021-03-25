package de.moinFT.main.listener;

import com.vdurmont.emoji.EmojiParser;
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
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.time.ZoneId;
import java.util.Date;
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
    private long commandTimeout = 0;
    private Message UserMessage = null;
    private String UserMessageContent = "";
    private String Prefix = "";

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Server = event.getServer().get();
        ServerID = Server.getId();
        commandTimeout = DBServer.getServer(ServerID).getCommandTimeout();
        UserMessage = event.getMessage();
        UserMessageContent = event.getMessageContent().toLowerCase();
        Prefix = DBServer.getServer(ServerID).getPrefix();

        if (UserMessage.isServerMessage()) {
            if (!UserMessage.getUserAuthor().get().isYourself() && !UserMessage.getUserAuthor().get().isBot()) {
                if (UserMessageContent.startsWith(Prefix)) {
                    //ServerAdmin
                    if (event.getMessageAuthor().isServerAdmin()) {
                        if (UserMessageContent.startsWith(Prefix + "toggle-bot-permission")) {
                            User user = getFirstUser_FromMessage(1, UserMessage);

                            if (user != null) {
                                long userID = user.getId();
                                boolean botPermission = DBServer.getServer(ServerID).getUsers().getUser(userID).getBotPermission();

                                if (userID != UserMessage.getUserAuthor().get().getId()) {
                                    UserMessage.addReaction(EmojiParser.parseToUnicode(":ok_hand:"));

                                    int adminChannelID = DBServer.getServer(Server.getId()).getChannels().getID("admin");

                                    if (adminChannelID != -1) {
                                        ServerChannel adminChannel = Server.getChannelById(DBServer.getServer(ServerID).getChannels().getChannelID(adminChannelID)).get();

                                        if (!botPermission) {
                                            new ServerChannelUpdater(adminChannel).addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build()).update();
                                        } else {
                                            new ServerChannelUpdater(adminChannel).addPermissionOverwrite(user, new PermissionsBuilder().setUnset(PermissionType.READ_MESSAGES).build()).update();
                                        }
                                    }

                                    DBServer.getServer(ServerID).getUsers().getUser(userID).updateBotPermission(!botPermission);
                                    DatabaseConnection.DBUpdateItem(Server.getId() + "_User", DBServer.getServer(ServerID).getUsers().getUser(userID).getDB_ID(), "`botPermission` = '" + !botPermission + "'");
                                } else {
                                    errorMessageWithReaction(", du kannst dir nicht selbst die Rechte entziehen!");
                                }
                            } else {
                                errorMessageWithReaction(", es wurde keine UserMention in der Nachricht gefunden!");
                            }

                            Functions.messageDelete(UserMessage, 2500);
                        }
                    }

                    //BotPermission
                    if (DBServer.getServer(ServerID).getUsers().getUser(UserMessage.getUserAuthor().get().getId()).getBotPermission()) {
                        if (UserMessageContent.startsWith(Prefix + "info-bot-permission")) {
                            TextChannel textChannel;

                            int AdminChannelID_Cache = DBServer.getServer(Server.getId()).getChannels().getID("admin");
                            if (AdminChannelID_Cache > -1) {
                                long AdminChannelID = DBServer.getServer(Server.getId()).getChannels().getChannelID(AdminChannelID_Cache);
                                textChannel = Server.getChannelById(AdminChannelID).get().asTextChannel().get();
                            } else {
                                textChannel = UserMessage.getChannel().asTextChannel().get();
                            }

                            Iterator<User> users = Server.getMembers().iterator();
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle("Bot-Berechtigungen");

                            while (users.hasNext()) {
                                User user = users.next();

                                if (DBServer.getServer(ServerID).getUsers().getUser(user.getId()).getBotPermission()){
                                    embed.addInlineField(user.getName(),user.getDisplayName(Server));
                                    embed.addInlineField(":white_check_mark:","Ja");
                                    embed.addInlineField("\u200B", "\u200B");
                                }
                            }

                            textChannel.sendMessage(embed);

                            Functions.messageDelete(UserMessage, 500);
                        } else if (UserMessageContent.startsWith(Prefix + "clear")) {
                            int MessValue = 0;

                            try {
                                MessValue = parseInt(UserMessageContent.substring(7)) + 1;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                event.getChannel().bulkDelete(event.getChannel().getMessages(MessValue).get());
                            } catch (InterruptedException | ExecutionException e) {
                                System.out.println("Error: Messages couldn't be deleted (check Permissions)!");
                                errorMessageWithReaction(", die Nachrichten konnten nicht gelöscht werden! (Bot Berechtigungen prüfen)");
                                e.printStackTrace();
                            }
                        } else if (UserMessageContent.startsWith(Prefix + "add-role")) {
                            Role highestRole = getUserHighestRole(Server, UserMessage.getUserAuthor().get());
                            Role addRole = getFirstRole_FromMessage(UserMessage);
                            User addUser = getFirstUser_FromMessage(2, UserMessage);

                            if (addRole != null && addUser != null) {
                                if (highestRole.getPosition() >= addRole.getPosition()) {
                                    UserMessage.addReaction(EmojiParser.parseToUnicode(":ok_hand:"));
                                    Server.addRoleToUser(addUser, addRole);
                                } else {
                                    errorMessageWithReaction(", du kannst keine Rolle vergeben die mehr Rechte hat als du!");
                                }
                            } else {
                                errorMessageWithReaction(", es wurde keine RoleMention und/oder keine UserMention in der Nachricht gefunden!");
                            }

                            Functions.messageDelete(UserMessage, 2500);
                        } else if (UserMessageContent.startsWith(Prefix + "remove-role")) {
                            Role highestRole = getUserHighestRole(Server, UserMessage.getUserAuthor().get());
                            Role removeRole = getFirstRole_FromMessage(UserMessage);
                            User removeUser = getFirstUser_FromMessage(2, UserMessage);

                            if (removeRole != null && removeUser != null) {
                                if (highestRole.getPosition() >= removeRole.getPosition()) {
                                    UserMessage.addReaction(EmojiParser.parseToUnicode(":ok_hand:"));
                                    Server.removeRoleFromUser(removeUser, removeRole);
                                } else {
                                    errorMessageWithReaction(", du kannst keine Rolle entfernen die mehr Rechte hat als du!");
                                }
                            } else {
                                errorMessageWithReaction(", es wurde keine RoleMention und/oder keine UserMention in der Nachricht gefunden!");
                            }

                            Functions.messageDelete(UserMessage, 2500);
                        } else if (UserMessageContent.startsWith(Prefix + "set-musicbot-prefix")) {
                            String prefix = "";

                            try {
                                prefix = UserMessageContent.split(" ")[1];
                                if (prefix.equals("_")) {
                                    prefix = "";
                                }
                            } catch (Exception e) {
                                System.out.println("Error: No prefix found in the message!");
                                errorMessageWithReaction(", es wurde kein Prefix in der Nachricht gefunden!");
                            }

                            DBServer.getServer(ServerID).updateMusicBotPrefix(prefix);
                            DatabaseConnection.DBUpdateItem("server", DBServer.getServer(ServerID).getDB_ID(), "`musicBotPrefix` = '" + prefix + "'");

                            Functions.messageDelete(UserMessage, 2500);
                        } else if (UserMessageContent.startsWith(Prefix + "channel-set")) {
                            boolean error = false;
                            long channelID = 0;

                            try {
                                channelID = UserMessage.getMentionedChannels().get(0).asTextChannel().get().getId();
                            } catch (Exception e) {
                                errorMessageWithReaction(", es wurde keine ChannelMention in der Nachricht gefunden!");
                                error = true;
                            }

                            if (!error) {
                                String channelName = "";

                                try {
                                    channelName = UserMessageContent.split(" ")[2];
                                    if (channelName.equals("_")) {
                                        channelName = "";
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error: No channelName found in the message!");
                                    errorMessageWithReaction(", es wurde kein ChannelName in der Nachricht gefunden!");
                                }

                                DBServer.getServer(ServerID).getChannels().updateChannelName(channelID, channelName);
                                DatabaseConnection.DBUpdateItem(ServerID + "_Channel", DBServer.getServer(ServerID).getChannels().getDB_ID(channelID), "`channelName` = '" + channelName + "'");
                            }

                            Functions.messageDelete(UserMessage, 2500);
                        } else if (UserMessageContent.startsWith(Prefix + "role-set")) {
                            long roleID = 0;
                            String roleType = "";
                            String roleName = "";

                            boolean error = false;

                            try {
                                roleID = UserMessage.getMentionedRoles().get(0).getId();
                            } catch (Exception e) {
                                System.out.println("Error: No roleMention found in the message!");
                                errorMessageWithReaction(", es wurde keine ChannelMention in der Nachricht gefunden!");
                                error = true;
                            }

                            if (!error) {
                                try {
                                    roleType = UserMessageContent.split(" ")[2];
                                    if (roleType.equals("_")) {
                                        roleType = "";
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error: No roleType found in the message!");
                                    errorMessageWithReaction(", es wurde kein RoleType in der Nachricht gefunden!");
                                    error = true;
                                }
                            }

                            if (!error) {
                                try {
                                    roleName = UserMessageContent.split(" ")[3];
                                    if (roleName.equals("_")) {
                                        roleName = "";
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error: No roleName found in the message!");
                                    errorMessageWithReaction(", es wurde kein RoleName in der Nachricht gefunden!");
                                    error = true;
                                }
                            }

                            if (!error) {
                                DBServer.getServer(ServerID).getRoles().getRole(roleID).updateRoleType(roleType);
                                DBServer.getServer(ServerID).getRoles().getRole(roleID).updateRoleName(roleName);
                                DatabaseConnection.DBUpdateItem(Server.getId() + "_Role", DBServer.getServer(ServerID).getRoles().getRole(roleID).getDB_ID(), "`roleType` = '" + roleType + "'");
                                DatabaseConnection.DBUpdateItem(Server.getId() + "_Role", DBServer.getServer(ServerID).getRoles().getRole(roleID).getDB_ID(), "`roleName` = '" + roleName + "'");
                            }

                            Functions.messageDelete(UserMessage, 2500);
                        } else if (UserMessageContent.startsWith(Prefix + "help")) {
                            getHelpMessage(true);
                        }
                    }

                    //NormalUser
                    normalCommands();
                } else if (UserMessageContent.startsWith(DBServer.getServer(Server.getId()).getMusicBotPrefix())) {
                    int musicbotChannelID = DBServer.getServer(Server.getId()).getChannels().getID("musicbot");
                    if (UserMessage.getChannel().getId() == DBServer.getServer(Server.getId()).getChannels().getChannelID(musicbotChannelID)) {
                        Functions.messageDelete(UserMessage, 90000);
                    }
                }
            } else if (!UserMessage.getUserAuthor().get().isYourself() && UserMessage.getUserAuthor().get().isBot()) {
                int musicbotChannelID = DBServer.getServer(Server.getId()).getChannels().getID("musicbot");
                if (UserMessage.getChannel().getId() == DBServer.getServer(Server.getId()).getChannels().getChannelID(musicbotChannelID)) {
                    Functions.messageDelete(UserMessage, 90000);
                }
            } else {
                Functions.messageDelete(UserMessage, 45000);
            }
        }
    }

    private void normalCommands() {
        if (UserMessageContent.startsWith(Prefix + "color")) {
            int DBRoleCount = DBServer.getServer(ServerID).getRoles().count();

            if (DBRoleCount > 0) {
                ListIterator<Role> userRoles = UserMessage.getUserAuthor().get().getRoles(Server).listIterator();

                while (userRoles.hasNext()) {
                    Role userRole = userRoles.next();
                    for (int i = 0; i < DBRoleCount; i++) {
                        if (DBServer.getServer(ServerID).getRoles().getRole(i).getRoleType().equalsIgnoreCase("color")) {
                            if (userRole.getId() == DBServer.getServer(ServerID).getRoles().getRole(i).getRoleID()) {
                                UserMessage.getUserAuthor().get().removeRole(userRole);
                            }
                        }
                    }
                }

                if (UserMessageContent.length() > 7) {
                    String messValue = UserMessageContent.substring(7);

                    for (int i = 0; i < DBRoleCount; i++) {
                        if (DBServer.getServer(ServerID).getRoles().getRole(i).getRoleName().equalsIgnoreCase(messValue) && DBServer.getServer(ServerID).getRoles().getRole(i).getRoleType().equalsIgnoreCase("color")) {
                            Role role = Server.getRoleById(DBServer.getServer(ServerID).getRoles().getRole(i).getRoleID()).get();

                            UserMessage.getUserAuthor().get().addRole(role);
                        }
                    }
                }
            }

            Functions.messageDelete(UserMessage, 500);
        } else if (UserMessageContent.startsWith(Prefix + "info-user")) {
            User User = getFirstUser_FromMessage(1, UserMessage);

            String joinDateString = User.getJoinedAtTimestamp(Server).get().atZone(ZoneId.systemDefault()).toString().split("T")[0];
            String joinDay = joinDateString.split("-")[2];
            String joinMonth = joinDateString.split("-")[1];
            String joinYear = joinDateString.split("-")[0];
            String joinDate = joinDay + "." + joinMonth + "." + joinYear;

            String botPermission;
            if (DBServer.getServer(ServerID).getUsers().getUser(User.getId()).getBotPermission()) {
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

            UserMessage.getChannel().sendMessage(embed);

            Functions.messageDelete(UserMessage, 500);
        } else if (UserMessageContent.startsWith(Prefix + "info-server")) {
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

            UserMessage.getChannel().sendMessage(embed);

            Functions.messageDelete(UserMessage, 500);
        } else if (UserMessageContent.startsWith(Prefix + "m-a") || UserMessageContent.startsWith(Prefix + "mute-all")) {
            long timestamp = new Date().getTime();
            ServerVoiceChannel voiceChannel = null;
            boolean error = false;

            try {
                voiceChannel = UserMessage.getUserAuthor().get().getConnectedVoiceChannel(Server).get();
            } catch (Exception e) {
                System.out.println("Error: The MessageAuthor isn't connected to a VoiceChannel!");
                error = true;

            }

            if ((timestamp - commandTimeout) > 5000 && !error) {
                UserMessage.addReaction(EmojiParser.parseToUnicode(":ok_hand:"));

                Iterator<User> users = voiceChannel.getConnectedUsers().iterator();

                User user = users.next();
                if (!user.isMuted(Server)) {
                    user.mute(Server);
                    while (users.hasNext()) {
                        user = users.next();
                        user.mute(Server);
                    }
                } else {
                    user.unmute(Server);
                    while (users.hasNext()) {
                        user = users.next();
                        user.unmute(Server);
                    }
                }

                DBServer.getServer(ServerID).updateCommandTimeout(timestamp);
                DatabaseConnection.DBUpdateItem("server", DBServer.getServer(ServerID).getDB_ID(), "`commandTimeout` = '" + timestamp + "'");
            } else {
                UserMessage.addReaction(EmojiParser.parseToUnicode(":no_entry:"));
            }

            Functions.messageDelete(UserMessage, 2500);
        } else if (UserMessageContent.equalsIgnoreCase(Prefix + "help")) {
            getHelpMessage(false);
        }
    }

    private void getHelpMessage(boolean admin) {
        String colorInfoString = getColorInfoString();
        TextChannel textChannel = UserMessage.getChannel();

        EmbedBuilder embed = null;

        if (UserMessageContent.equalsIgnoreCase(Prefix + "help")) {
            embed = new EmbedBuilder();

            embed.setTitle("Bot-Befehle");
            embed.addField(Prefix + "help", "Diese Liste");
            embed.addField(Prefix + "info-user [UserMention]", "Zeigt Informationen über den User [UserMention] an.");
            embed.addField(Prefix + "info-server", "Zeigt Informationen über den Server an.");
            embed.addField(Prefix + "m-a", "Alle Personnen in seinem VoiceChannel stummschalten (Cooldown: 5 Sekunden)");

            if (colorInfoString.length() != 0) {
                embed.addField(Prefix + "color [color]", colorInfoString);
                embed.addField(Prefix + "color", "Farbe entfernen");
            }

            Functions.messageDelete(UserMessage, 500);
        } else if (admin) {
            int AdminChannelID_Cache = DBServer.getServer(Server.getId()).getChannels().getID("admin");
            if (AdminChannelID_Cache > -1) {
                long AdminChannelID = DBServer.getServer(Server.getId()).getChannels().getChannelID(AdminChannelID_Cache);
                textChannel = Server.getChannelById(AdminChannelID).get().asTextChannel().get();
            } else {
                textChannel = UserMessage.getChannel().asTextChannel().get();
            }

            if (UserMessageContent.equalsIgnoreCase(Prefix + "help-all")) {
                embed = new EmbedBuilder();
                embed.setTitle("Bot-Befehle (Mit Bot-Berechtigungen)");
                embed.addField("Normale-Befehle", "(Mit Bot-Berechtigungen)");
                embed.addField(Prefix + "help-all", "Diese Liste");
                embed.addField(Prefix + "help-set", "Bot-Befehle (Bot Variablen setzen)");
                embed.addField(Prefix + "clear [Wert]", "Loescht [Wert] Nachrichten aus einem TextChannel");
                embed.addField(Prefix + "add-role [UserMention] [RoleMention]", "Gibt dem Nutzer [UserMention] die Rolle [RoleMention]");
                embed.addField(Prefix + "remove-role [UserMention] [RoleMention]", "Nimmt dem Nutzer [UserMention] die Rolle [RoleMention]");
                embed.addField("\u200B", "\u200B");
                embed.addField("Admin-Befehle", "(Mit Bot-Berechtigungen)");
                embed.addField(Prefix + "toggle-permission-bot [UserMention]", "Gibt/Nimmt dem Nutzer (UserID) die Berechtigung die Befehle mit Bot Berechtigungen");
            } else if (UserMessageContent.equalsIgnoreCase(Prefix + "help-set")) {
                embed = new EmbedBuilder();
                embed.setTitle("Bot-Befehle (Bot Variablen setzen)");
                embed.addField(Prefix + "help-set", "Diese Liste");
                embed.addField(Prefix + "channel-set [channelMention] [channelName]", "Fuegt einem Channel [channelMention] einen Namen [channelName] hinzu. \n Fuer Admin (Beispiel): !channel-set #admin admin");
                embed.addField(Prefix + "role-set [roleMention] [roleType] [roleName]", "Fuegt einer Rolle [roleMention] einen Typ [roleType] und \n einen Namen [roleName] hinzu. \n Fuer eine Farbe (Beispiel): !role-set @yellow color yellow");
            }

            if (embed != null) {
                embed.addField("\u200B", "\u200B");
                embed.addField("Normale-Befehle", "(Ohne Bot-Brechtigungen)");
                embed.addField(Prefix + "info-user [UserMention]", "Zeigt Informationen über den User [UserMention] an.");
                embed.addField(Prefix + "info-server", "Zeigt Informationen über den Server an.");
                embed.addField(Prefix + "m-a", "Alle Personnen in seinem VoiceChannel stummschalten (Cooldown: 5 Sekunden)");

                if (colorInfoString.length() != 0) {
                    embed.addField(Prefix + "color [color]", colorInfoString);
                    embed.addField(Prefix + "color", "Farbe entfernen");
                }
            }
            Functions.messageDelete(UserMessage, 500);
        }
        textChannel.sendMessage(embed);
    }

    private void errorMessageWithReaction(String errorMessage) {
        UserMessage.addReaction(EmojiParser.parseToUnicode(":no_entry:"));

        Message replyMessage = Functions.replyMessage(UserMessage, errorMessage);
        if (replyMessage != null) {
            Functions.messageDelete(replyMessage, 5000);
        }
    }

    //Get colorInfoString from DB role information
    private String getColorInfoString() {
        int roleCountDB = DBServer.getServer(ServerID).getRoles().count();
        int colorCount = 0;

        StringBuilder colorInfoString = new StringBuilder("Farben: ");

        for (int i = 0; i < roleCountDB; i++) {
            if (DBServer.getServer(ServerID).getRoles().getRole(i).getRoleType().equals("color")) {
                if (colorInfoString.toString().equals("Farben: ")) {
                    colorInfoString.append(DBServer.getServer(ServerID).getRoles().getRole(i).getRoleName());
                } else {
                    colorInfoString.append(", ").append(DBServer.getServer(ServerID).getRoles().getRole(i).getRoleName());
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
                roleID = parseLong(UserMessageContent.split(" ")[1]);
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
                userID = parseLong(UserMessageContent.split(" ")[indexOfUserMention]);
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