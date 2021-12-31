package de.moinFT.main.listener.message;

import com.vdurmont.emoji.EmojiParser;
import de.moinFT.main.DatabaseConnection;
import de.moinFT.main.FileIn;
import de.moinFT.main.Functions;
import de.moinFT.main.RKICorona;
import de.moinFT.utils.DBChannelArray;
import de.moinFT.utils.DBRoleArray;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import static de.moinFT.main.Functions.getUserHighestRole;
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
        commandTimeout = DBServer.getServer(ServerID).getCommandTimeoutTimestamp();
        UserMessage = event.getMessage();
        UserMessageContent = UserMessage.getContent().toLowerCase();
        Prefix = DBServer.getServer(ServerID).getPrefix();

        if (UserMessage.isServerMessage()) {
            TextChannel channelMessage = event.getChannel().asTextChannel().get();

            TextChannel textChannel;

            DBChannelArray DBAdminChannel = DBServer.getServer(ServerID).getChannels().getChannel("admin");
            if (DBAdminChannel != null) {
                long adminChannelID = DBAdminChannel.getChannelID();
                textChannel = Server.getChannelById(adminChannelID).get().asTextChannel().get();
            } else {
                textChannel = channelMessage;
            }

            if (!UserMessage.getAuthor().isWebhook() && !UserMessage.getAuthor().isBotUser()) {
                if (UserMessageContent.startsWith(Prefix)) {
                    //ServerAdmin
                    if (UserMessage.getAuthor().isServerAdmin() || UserMessage.getAuthor().isBotOwner()) {
                        if (UserMessageContent.startsWith(Prefix + "toggle-bot-permission")) {
                            User user = getFirstUser_FromMessage(1, UserMessage);

                            if (user != null) {
                                long userID = user.getId();
                                boolean botPermission = DBServer.getServer(ServerID).getUsers().getUser(userID).getBotPermission();

                                if (userID != UserMessage.getAuthor().getId()) {
                                    UserMessage.addReaction(EmojiParser.parseToUnicode(":ok_hand:"));

                                    int adminChannelID = DBServer.getServer(ServerID).getChannels().getChannel("admin").getID();

                                    if (adminChannelID != -1) {
                                        ServerChannel adminChannel = Server.getChannelById(DBServer.getServer(ServerID).getChannels().getChannel(adminChannelID).getChannelID()).get();

                                        if (!botPermission) {
                                            new ServerChannelUpdater(adminChannel).addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build()).update();
                                        } else {
                                            new ServerChannelUpdater(adminChannel).addPermissionOverwrite(user, new PermissionsBuilder().setUnset(PermissionType.READ_MESSAGES).build()).update();
                                        }
                                    }

                                    DBServer.getServer(ServerID).getUsers().getUser(userID).updateBotPermission(!botPermission);
                                    DatabaseConnection.SQL_Execute("UPDATE user SET botPermission = '" + !botPermission + "' WHERE serverID = '" + ServerID + "' AND userID = '" + userID + "'");
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
                    if (DBServer.getServer(ServerID).getUsers().getUser(UserMessage.getAuthor().getId()).getBotPermission()) {
                        if (UserMessageContent.startsWith(Prefix + "info-bot-permission")) {
                            MessageBuilder message = new MessageBuilder();
                            message.append("Bot-Berechtigungen", MessageDecoration.CODE_LONG);

                            Iterator<User> users = Server.getMembers().iterator();
                            StringBuilder messageContent = new StringBuilder();

                            messageContent.append("Username");
                            messageContent.append(Functions.createSpaces(20 - ("Username").length()));
                            messageContent.append("Nickname");
                            messageContent.append(Functions.createSpaces(20 - ("Nickname").length()));
                            messageContent.append("Bot-Berechtigungen");
                            messageContent.append(Functions.createSpaces(22 - ("Bot-Berechtigungen").length()));
                            messageContent.append("Admin-Berechtigungen");
                            message.append(messageContent.toString(), MessageDecoration.CODE_LONG);
                            messageContent = new StringBuilder();

                            while (users.hasNext()) {
                                User user = users.next();

                                if (DBServer.getServer(ServerID).getUsers().getUser(user.getId()).getBotPermission()) {
                                    messageContent.append("\n");
                                    messageContent.append(user.getName());
                                    messageContent.append(Functions.createSpaces(20 - user.getName().length()));

                                    if (!user.getDisplayName(Server).equals(user.getName())) {
                                        messageContent.append(user.getDisplayName(Server));
                                        messageContent.append(Functions.createSpaces(22 - user.getDisplayName(Server).length()));
                                    } else {
                                        messageContent.append(Functions.createSpaces(22));
                                    }

                                    messageContent.append(EmojiParser.parseToUnicode(":white_check_mark:"));
                                    messageContent.append(Functions.createSpaces(20));

                                    if (DBServer.getServer(ServerID).getUsers().getUser(user.getId()).getIsAdmin()) {
                                        messageContent.append(EmojiParser.parseToUnicode(":white_check_mark:"));
                                    } else {
                                        messageContent.append(EmojiParser.parseToUnicode(":x:"));
                                    }
                                }
                            }
                            message.append(messageContent.toString(), MessageDecoration.CODE_LONG);
                            textChannel.sendMessage(message.getStringBuilder().toString());

                            Functions.messageDelete(UserMessage, 500);
                        } else if (UserMessageContent.startsWith(Prefix + "info-user-set")) {
                            String pageCommand = "";
                            if (UserMessageContent.length() > 14) {
                                pageCommand = UserMessageContent.substring(15);
                            }

                            MessageBuilder message = new MessageBuilder();
                            message.append("User-Setup", MessageDecoration.CODE_LONG);

                            int userCount = Server.getMemberCount();
                            Iterator<User> users = Server.getMembers().iterator();
                            StringBuilder messageContent = new StringBuilder();

                            messageContent.append("Username");
                            messageContent.append(Functions.createSpaces(20 - ("Username").length()));
                            messageContent.append("Nickname");
                            messageContent.append(Functions.createSpaces(20 - ("Nickname").length()));
                            messageContent.append("Bot-Berechtigungen");
                            messageContent.append(Functions.createSpaces(22 - ("Bot-Berechtigungen").length()));
                            messageContent.append("Admin-Berechtigungen");
                            messageContent.append(Functions.createSpaces(22 - ("Admin-Berechtigungen").length()));
                            messageContent.append("Hoechste Rolle");
                            message.append(messageContent.toString(), MessageDecoration.CODE_LONG);

                            int userPerPage = 16;

                            int pages = userCount / userPerPage;
                            if (userCount % userPerPage != 0) {
                                pages++;
                            }

                            int userStartIndex = 0;
                            int pageNumber = 1;
                            if (pages != 1 && !pageCommand.equals("")) {
                                try {
                                    pageNumber = parseInt(pageCommand.split(" ")[1]);
                                } catch (Exception e) {
                                    System.out.println("Error: No pageNumber found in th message!");
                                }
                                userStartIndex = (pageNumber - 1) * 16;
                            }

                            messageContent = new StringBuilder();
                            int count = 0;
                            while (users.hasNext()) {
                                User user = users.next();
                                if ((userStartIndex - 1) < count && (userStartIndex + userPerPage) > count) {
                                    messageContent.append("\n").append(user.getName());
                                    messageContent.append(Functions.createSpaces(20 - user.getName().length()));

                                    if (!user.getDisplayName(Server).equals(user.getName())) {
                                        messageContent.append(user.getDisplayName(Server));
                                        messageContent.append(Functions.createSpaces(22 - user.getDisplayName(Server).length()));
                                    } else {
                                        messageContent.append(Functions.createSpaces(22));
                                    }

                                    if (DBServer.getServer(ServerID).getUsers().getUser(user.getId()).getBotPermission()) {
                                        messageContent.append(EmojiParser.parseToUnicode(":white_check_mark:"));
                                    } else {
                                        messageContent.append(EmojiParser.parseToUnicode(":x:"));
                                    }

                                    messageContent.append(Functions.createSpaces(20));

                                    if (DBServer.getServer(ServerID).getUsers().getUser(user.getId()).getIsAdmin()) {
                                        messageContent.append(EmojiParser.parseToUnicode(":white_check_mark:"));
                                    } else {
                                        messageContent.append(EmojiParser.parseToUnicode(":x:"));
                                    }

                                    messageContent.append(Functions.createSpaces(20));

                                    Iterator<Role> roles = user.getRoles(Server).iterator();
                                    Role highestRole = null;
                                    while (roles.hasNext()) {
                                        Role role = roles.next();
                                        if (highestRole == null) {
                                            highestRole = role;
                                        } else {
                                            if (highestRole.getPosition() < role.getPosition()) {
                                                highestRole = role;
                                            }
                                        }
                                    }

                                    if (highestRole != null) {
                                        messageContent.append(highestRole.getName());
                                    } else {
                                        messageContent.append(EmojiParser.parseToUnicode(":x:"));
                                    }
                                }

                                if ((userStartIndex + userPerPage) < count) {
                                    break;
                                }
                                count++;
                            }
                            message.append(messageContent.toString(), MessageDecoration.CODE_LONG);
                            message.append("Seite: " + pageNumber + "\t(Seiten: 1 - " + pages + ")", MessageDecoration.CODE_LONG);
                            textChannel.sendMessage(message.getStringBuilder().toString());

                            Functions.messageDelete(UserMessage, 500);
                        } else if (UserMessageContent.startsWith(Prefix + "info-channel-set")) {
                            MessageBuilder message = new MessageBuilder();
                            message.append("Channel-Setup", MessageDecoration.CODE_LONG);

                            Iterator<ServerChannel> channels = Server.getChannels().iterator();
                            StringBuilder messageContent = new StringBuilder();

                            messageContent.append("Channel-Name");
                            messageContent.append(Functions.createSpaces(25 - ("Channel-Name").length()));
                            messageContent.append("Channel-Type");
                            messageContent.append(Functions.createSpaces(25 - ("Channel-Type").length()));
                            messageContent.append("Channel-Setup Name");
                            message.append(messageContent.toString(), MessageDecoration.CODE_LONG);
                            messageContent = new StringBuilder();

                            while (channels.hasNext()) {
                                ServerChannel channel = channels.next();

                                if (channel.getType() != ChannelType.CHANNEL_CATEGORY) {
                                    messageContent.append("\n");
                                    messageContent.append(channel.getName());
                                    messageContent.append(Functions.createSpaces(25 - (channel.getName().length())));

                                    String channelType = "";
                                    if (channel.getType() == ChannelType.SERVER_TEXT_CHANNEL) {
                                        channelType = "TextChannel";
                                    } else if (channel.getType() == ChannelType.SERVER_VOICE_CHANNEL) {
                                        channelType = "VoiceChannel";
                                    }

                                    messageContent.append(channelType);
                                    messageContent.append(Functions.createSpaces(25 - (channelType.length())));

                                    if (!DBServer.getServer(ServerID).getChannels().getChannel(channel.getId()).getChannelName().equals("")) {
                                        messageContent.append(DBServer.getServer(ServerID).getChannels().getChannel(channel.getId()).getChannelName());
                                    } else {
                                        messageContent.append(EmojiParser.parseToUnicode(":x:"));
                                    }
                                } else {
                                    if (messageContent.toString().length() != 0) {
                                        message.append(messageContent.toString(), MessageDecoration.CODE_LONG);
                                    }
                                    message.append(channel.getName(), MessageDecoration.CODE_LONG);
                                    messageContent = new StringBuilder();
                                }
                            }
                            message.append(messageContent.toString(), MessageDecoration.CODE_LONG);
                            textChannel.sendMessage(message.getStringBuilder().toString());

                            Functions.messageDelete(UserMessage, 500);
                        } else if (UserMessageContent.startsWith(Prefix + "info-role-set")) {
                            MessageBuilder message = new MessageBuilder();
                            message.append("Role-Setup", MessageDecoration.CODE_LONG);

                            Iterator<Role> roles = Server.getRoles().iterator();
                            StringBuilder messageContent = new StringBuilder();

                            messageContent.append("Role-Name");
                            messageContent.append(Functions.createSpaces(25 - ("Role-Name").length()));
                            messageContent.append("Role-Type");
                            messageContent.append(Functions.createSpaces(25 - ("Role-Type").length()));
                            messageContent.append("Role-Setup Name");

                            message.append(messageContent.toString(), MessageDecoration.CODE_LONG);
                            messageContent = new StringBuilder();

                            while (roles.hasNext()) {
                                Role role = roles.next();
                                messageContent.append("\n");
                                messageContent.append(role.getName());
                                messageContent.append(Functions.createSpaces(25 - (role.getName().length())));

                                String roleType = DBServer.getServer(ServerID).getRoles().getRole(role.getId()).getRoleType();
                                if (!roleType.equals("")) {
                                    messageContent.append(roleType);
                                } else {
                                    messageContent.append(EmojiParser.parseToUnicode(":x:"));
                                }

                                messageContent.append(Functions.createSpaces(25 - (roleType.length())));

                                String roleSetupName = DBServer.getServer(ServerID).getRoles().getRole(role.getId()).getRoleName();
                                if (!roleSetupName.equals("")) {
                                    messageContent.append(roleSetupName);
                                } else {
                                    messageContent.append(EmojiParser.parseToUnicode(":x:"));
                                }
                            }
                            message.append(messageContent.toString(), MessageDecoration.CODE_LONG);
                            textChannel.sendMessage(message.getStringBuilder().toString());

                            Functions.messageDelete(UserMessage, 500);
                        } else if (UserMessageContent.startsWith(Prefix + "clear")) {
                            int MessValue = 0;

                            try {
                                MessValue = parseInt(UserMessageContent.substring(7)) + 1;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                channelMessage.bulkDelete(channelMessage.getMessages(MessValue).get());
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
                                if (highestRole.getPosition() >= addRole.getPosition() || UserMessage.getUserAuthor().get().isBotOwner()) {
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
                            boolean error = false;
                            String prefix = "";

                            try {
                                prefix = UserMessageContent.split(" ")[1];
                                if (prefix.equals("_")) {
                                    prefix = "";
                                }
                            } catch (Exception e) {
                                System.out.println("Error: No prefix found in the message!");
                                errorMessageWithReaction(", es wurde kein Prefix in der Nachricht gefunden!");
                                error = true;
                            }

                            if (!error) {
                                UserMessage.addReaction(EmojiParser.parseToUnicode(":ok_hand:"));
                                DBServer.getServer(ServerID).updateMusicBotPrefix(prefix);
                                DatabaseConnection.SQL_Execute("UPDATE server SET musicBotPrefix = '" + prefix + "' WHERE serverID = '" + ServerID + "'");
                            }

                            Functions.messageDelete(UserMessage, 2500);
                        } else if (UserMessageContent.startsWith(Prefix + "channel-set")) {
                            boolean error = false;
                            long channelID = 0;
                            String channelName = "";

                            try {
                                channelID = UserMessage.getMentionedChannels().get(0).asTextChannel().get().getId();
                            } catch (Exception e) {
                                System.out.println("Error: No channelMention found in the message!");
                                errorMessageWithReaction(", es wurde keine ChannelMention in der Nachricht gefunden!");
                                error = true;
                            }

                            if (!error) {
                                try {
                                    channelName = UserMessageContent.split(" ")[2];
                                    if (channelName.equals("_")) {
                                        channelName = "";
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error: No channelName found in the message!");
                                    errorMessageWithReaction(", es wurde kein ChannelName in der Nachricht gefunden!");
                                    error = true;
                                }
                            }

                            if (!error) {
                                if (!channelName.equals("")) {
                                    DBChannelArray channelArray = DBServer.getServer(ServerID).getChannels();

                                    if (channelArray.getChannel(channelID).getChannelName().equals(channelName)) {
                                        error = true;
                                        System.out.println("Error: ChannelName exist already in DB!");
                                        errorMessageWithReaction(", der ChannelName ist schon vergeben! (Tipp: " + Prefix + "info-channel-set)");
                                    }
                                }
                            }

                            if (!error) {
                                UserMessage.addReaction(EmojiParser.parseToUnicode(":ok_hand:"));
                                DBServer.getServer(ServerID).getChannels().getChannel(channelID).updateChannelName(channelName);
                                DatabaseConnection.SQL_Execute("UPDATE channel SET channelName = '" + channelName + "' WHERE serverID = '" + ServerID + "' AND channelID = '" + channelID + "'");
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
                                if (!roleName.equals("") && !roleType.equals("")) {
                                    DBRoleArray rolesArray = DBServer.getServer(ServerID).getRoles();

                                    if (rolesArray.getRole(roleID).getRoleName().equals(roleName)) {
                                        error = true;
                                        System.out.println("Error: RoleName exist already in DB!");
                                        errorMessageWithReaction(", der RoleName ist schon vergeben! (Tipp: " + Prefix + "info-role-set)");
                                    }
                                }
                            }

                            if (!error) {
                                UserMessage.addReaction(EmojiParser.parseToUnicode(":ok_hand:"));
                                DBServer.getServer(ServerID).getRoles().getRole(roleID).updateRoleType(roleType);
                                DBServer.getServer(ServerID).getRoles().getRole(roleID).updateRoleName(roleName);
                                DatabaseConnection.SQL_Execute("UPDATE role SET roleType = '" + roleType + "', roleName = '" + roleName + "' WHERE serverID = '" + ServerID + "' AND roleID = '" + roleID + "'");
                            }

                            Functions.messageDelete(UserMessage, 2500);
                        }
                    }
                    normalCommands();
                } else if (UserMessageContent.startsWith(DBServer.getServer(ServerID).getMusicBotPrefix())) {
                    if (DBServer.getServer(ServerID).getChannels().getChannel("musicbot") != null) {
                        long musicbotChannelID = DBServer.getServer(ServerID).getChannels().getChannel("musicbot").getChannelID();
                        if (UserMessage.getChannel().getId() == musicbotChannelID) {
                            Functions.messageDelete(UserMessage, 45000);
                        }
                    }
                }
            } else if (!UserMessage.getAuthor().isYourself() && UserMessage.getAuthor().isBotUser()) {
                if (DBServer.getServer(ServerID).getChannels().getChannel("musicbot") != null) {
                    long musicbotChannelID = DBServer.getServer(ServerID).getChannels().getChannel("musicbot").getChannelID();
                    if (UserMessage.getChannel().getId() == musicbotChannelID) {
                        Functions.messageDelete(UserMessage, 45000);
                    }
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

                for (Role userRole : UserMessage.getUserAuthor().get().getRoles(Server)) {
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

            if (User != null) {
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
            }

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

                DBServer.getServer(ServerID).updateCommandTimeoutTimestamp(timestamp);
                DatabaseConnection.SQL_Execute("UPDATE server SET commandTimeout = '" + timestamp + "' WHERE serverID = '" + ServerID + "'");
            } else {
                UserMessage.addReaction(EmojiParser.parseToUnicode(":no_entry:"));
            }

            Functions.messageDelete(UserMessage, 2500);
        } else if (UserMessageContent.startsWith(Prefix + "corona")) {
            UserMessage.getChannel().sendMessage("Aktueller Inzidenzwert (Nienburg (Weser)): " + RKICorona.incidenceValue());

            Functions.messageDelete(UserMessage, 500);
        } else if (UserMessageContent.startsWith(Prefix + "help")) {
            getHelpMessage(DBServer.getServer(ServerID).getUsers().getUser(UserMessage.getAuthor().getId()).getBotPermission());
        }
    }

    private void getHelpMessage(boolean admin) {
        Functions.messageDelete(UserMessage, 500);

        String StringHelp = FileIn.read("/json/help.json");
        JSONObject JSONHelp = new JSONObject(StringHelp).getJSONObject("attributes");

        String colorInfoString = getColorInfoString();
        TextChannel textChannel = UserMessage.getChannel();

        String messageContent;
        MessageBuilder message = null;

        if (admin) {
            if (UserMessageContent.equalsIgnoreCase(Prefix + "help-all") || UserMessageContent.equalsIgnoreCase(Prefix + "help-set")) {
                int AdminChannelID_Cache = DBServer.getServer(ServerID).getChannels().getChannel("admin").getID();
                if (AdminChannelID_Cache > -1) {
                    long AdminChannelID = DBServer.getServer(ServerID).getChannels().getChannel(AdminChannelID_Cache).getChannelID();
                    textChannel = Server.getChannelById(AdminChannelID).get().asTextChannel().get();
                }
            }

            if (UserMessageContent.equalsIgnoreCase(Prefix + "help-all")) {
                message = new MessageBuilder();

                message.append("Bot-Befehle", MessageDecoration.CODE_LONG);

                message.append("Normale-Befehle (Mit Bot-Berechtigungen)", MessageDecoration.CODE_LONG);
                messageContent = createHelpMessage(JSONHelp.getJSONArray("botPerm"), 45);
                message.append(messageContent, MessageDecoration.CODE_LONG);

                message.append("Admin-Befehle (Mit Bot-Berechtigungen)", MessageDecoration.CODE_LONG);
                messageContent = createHelpMessage(JSONHelp.getJSONArray("botPermAdmin"), 45);
                message.append(messageContent, MessageDecoration.CODE_LONG);
            } else if (UserMessageContent.equalsIgnoreCase(Prefix + "help-set")) {
                message = new MessageBuilder();

                message.append("Bot-Befehle (Bot Variablen setzen)", MessageDecoration.CODE_LONG);
                messageContent = createHelpMessage(JSONHelp.getJSONArray("botVarSet"), 50);
                message.append(messageContent, MessageDecoration.CODE_LONG);
            }
        }

        JSONArray normalHelp = JSONHelp.getJSONArray("normalHelp");
        if (message == null) {
            message = new MessageBuilder();
            message.append("Bot-Befehle", MessageDecoration.CODE_LONG);
        } else {
            message.append("Normale-Befehle (Ohne Bot-Brechtigungen)", MessageDecoration.CODE_LONG);
            normalHelp.remove(0);
        }

        messageContent = createHelpMessage(normalHelp, 30);

        if (colorInfoString.length() != 0) {
            messageContent = messageContent + "\ncolor [color]                 " + colorInfoString;
            messageContent = messageContent + "\ncolor                         Farbe entfernen";
        }

        message.append(messageContent, MessageDecoration.CODE_LONG);
        textChannel.sendMessage(message.getStringBuilder().toString());

        Functions.messageDelete(UserMessage, 500);
    }

    private String createHelpMessage(JSONArray json, int space) {
        StringBuilder messageContent = new StringBuilder();

        for (int x = 0; x < json.length(); x++) {
            JSONObject temp = json.getJSONObject(x);
            messageContent.append("\n");

            String messageCommand = Prefix + temp.getString("command");
            String messageHelp = temp.getString("message");

            messageContent.append(messageCommand);
            messageContent.append(Functions.createSpaces(space - messageCommand.length()));
            messageContent.append(messageHelp);
        }
        return messageContent.toString();
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