package de.moinFT.main.listener.message.SlashCommand;

import com.vdurmont.emoji.EmojiParser;
import de.moinFT.main.Functions;
import de.moinFT.utils.BotRoleType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;

import static de.moinFT.main.Main.DBServer;

public class SlashCommand_Show {
    private static final Logger log = LogManager.getLogger(SlashCommand_Show.class.getName());

    public SlashCommand_Show(Server server, SlashCommandInteraction slashCommandInteraction) {
        long serverID = server.getId();
        SlashCommandInteractionOption firstOption;
        String logInfos = "Server: " + server.getName() + " (" + server.getId() + ") | User: " + slashCommandInteraction.getUser().getDiscriminatedName() + " (" + slashCommandInteraction.getUser().getId() + ")\n";

        if (slashCommandInteraction.getFirstOption().isPresent()) {
            firstOption = slashCommandInteraction.getFirstOption().get();
        } else {
            slashCommandInteraction.createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL)
                    .setContent("Der gesendete SlashCommand ist ungültig!")
                    .respond();

            log.error("SlashCommand was send without firstOption.");
            return;
        }

        switch (firstOption.getName()) {
            //Displays all users with their nickname and bot-permission if they have bot-permission
            case "bot-permission": {
                MessageBuilder message = new MessageBuilder();
                message.append("Bot-Berechtigungen", MessageDecoration.CODE_LONG);

                Iterator<User> users = server.getMembers().iterator();
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

                    if (DBServer.getServer(serverID).getUsers().getUser(user.getId()).getBotPermission()) {
                        messageContent.append("\n");
                        messageContent.append(user.getName());
                        messageContent.append(Functions.createSpaces(20 - user.getName().length()));

                        if (!user.getDisplayName(server).equals(user.getName())) {
                            messageContent.append(user.getDisplayName(server));
                            messageContent.append(Functions.createSpaces(22 - user.getDisplayName(server).length()));
                        } else {
                            messageContent.append(Functions.createSpaces(22));
                        }

                        messageContent.append(EmojiParser.parseToUnicode(":white_check_mark:"));
                        messageContent.append(Functions.createSpaces(20));

                        if (DBServer.getServer(serverID).getUsers().getUser(user.getId()).getIsAdmin()) {
                            messageContent.append(EmojiParser.parseToUnicode(":white_check_mark:"));
                        } else {
                            messageContent.append(EmojiParser.parseToUnicode(":x:"));
                        }
                    }
                }
                message.append(messageContent.toString(), MessageDecoration.CODE_LONG);

                slashCommandInteraction.createImmediateResponder()
                        .setContent(message.getStringBuilder().toString())
                        .respond();

                log.info(logInfos + "\t\t\tCommand: show bot-permission");
            }
            break;
            //Displays all users with their nickname and highest role and bot-permission
            case "users": {
                int pageNumber;
                if (!firstOption.getOptions().isEmpty()) {
                    pageNumber = firstOption.getOptions().get(0).getIntValue().get();
                } else {
                    pageNumber = 1;
                }

                MessageBuilder message = new MessageBuilder();
                message.append("User-Setup", MessageDecoration.CODE_LONG);

                int userCount = server.getMemberCount();
                Iterator<User> users = server.getMembers().iterator();
                StringBuilder messageContent = new StringBuilder();

                messageContent.append("Username");
                messageContent.append(Functions.createSpaces(20 - ("Username").length()));
                messageContent.append("Nickname");
                messageContent.append(Functions.createSpaces(20 - ("Nickname").length()));
                messageContent.append("Bot-Berechtigungen");
                messageContent.append(Functions.createSpaces(22 - ("Bot-Berechtigungen").length()));
                messageContent.append("Admin-Berechtigungen");
                messageContent.append(Functions.createSpaces(22 - ("Admin-Berechtigungen").length()));
                messageContent.append("Höchste Rolle");
                message.append(messageContent.toString(), MessageDecoration.CODE_LONG);

                int userPerPage = 16;

                int pages = userCount / userPerPage;
                if (userCount % userPerPage != 0) {
                    pages++;
                }

                int userStartIndex = (pageNumber - 1) * 16;

                messageContent = new StringBuilder();
                int count = 0;
                while (users.hasNext()) {
                    User user = users.next();
                    if ((userStartIndex - 1) < count && (userStartIndex + userPerPage) > count) {
                        messageContent.append("\n").append(user.getName());
                        messageContent.append(Functions.createSpaces(20 - user.getName().length()));

                        if (!user.getDisplayName(server).equals(user.getName())) {
                            messageContent.append(user.getDisplayName(server));
                            messageContent.append(Functions.createSpaces(22 - user.getDisplayName(server).length()));
                        } else {
                            messageContent.append(Functions.createSpaces(22));
                        }

                        if (DBServer.getServer(serverID).getUsers().getUser(user.getId()).getBotPermission()) {
                            messageContent.append(EmojiParser.parseToUnicode(":white_check_mark:"));
                        } else {
                            messageContent.append(EmojiParser.parseToUnicode(":x:"));
                        }

                        messageContent.append(Functions.createSpaces(20));

                        if (DBServer.getServer(serverID).getUsers().getUser(user.getId()).getIsAdmin()) {
                            messageContent.append(EmojiParser.parseToUnicode(":white_check_mark:"));
                        } else {
                            messageContent.append(EmojiParser.parseToUnicode(":x:"));
                        }

                        messageContent.append(Functions.createSpaces(20));

                        Role highestRole = Functions.getUserHighestRole(server, user);

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

                slashCommandInteraction.createImmediateResponder()
                        .setContent(message.getStringBuilder().toString())
                        .respond();

                log.info(logInfos + "\t\t\tCommand: show users");
            }
            break;
            case "user": {
                User user = firstOption.getOptions().get(0).getUserValue().get();

                String joinDateString = user.getJoinedAtTimestamp(server).get().atZone(ZoneId.systemDefault()).toString().split("T")[0];
                String joinDay = joinDateString.split("-")[2];
                String joinMonth = joinDateString.split("-")[1];
                String joinYear = joinDateString.split("-")[0];
                String joinDate = joinDay + "." + joinMonth + "." + joinYear;

                String botPermission;
                if (DBServer.getServer(serverID).getUsers().getUser(user.getId()).getBotPermission()) {
                    botPermission = "Ja";
                } else {
                    botPermission = "Nein";
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setAuthor(user)
                        .setTitle("User Info")
                        .addInlineField("Nutzername:", user.getName())
                        .addInlineField("\u200B", "\u200B")
                        .addInlineField("Nickname:", user.getDisplayName(server))
                        .addInlineField("Ist Mitglied seit:", joinDate)
                        .addInlineField("\u200B", "\u200B")
                        .addInlineField("Bot-Berechtigungen", botPermission);

                slashCommandInteraction.createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .addEmbed(embed)
                        .respond();

                log.info(logInfos + "\t\t\tCommand: show user | User: " + user.getDiscriminatedName() + " (" + user.getId() + ") ");
            }
            break;
            //Displays all channel with their name, type and
            case "channels": {
                MessageBuilder message = new MessageBuilder();
                message.append("Channel-Setup", MessageDecoration.CODE_LONG);

                Iterator<ServerChannel> channels = server.getChannels().iterator();
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

                        if (!DBServer.getServer(serverID).getChannels().getChannel(channel.getId()).getChannelName().equals("")) {
                            messageContent.append(DBServer.getServer(serverID).getChannels().getChannel(channel.getId()).getChannelName());
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

                slashCommandInteraction.createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent(message.getStringBuilder().toString())
                        .respond();

                log.info(logInfos + "\t\t\tCommand: show channel");
            }
            break;
            case "roles": {
                if (slashCommandInteraction.getFirstOption().isPresent()) {
                    firstOption = slashCommandInteraction.getFirstOption().get();
                } else {
                    slashCommandInteraction.createImmediateResponder()
                            .setFlags(MessageFlag.EPHEMERAL)
                            .setContent("Der gesendete SlashCommand ist ungültig!")
                            .respond();

                    log.error("SlashCommand was send without secondOption.");
                    return;
                }

                MessageBuilder message = new MessageBuilder();
                message.append("Role-Setup", MessageDecoration.CODE_LONG);

                Iterator<Role> roles = server.getRoles().iterator();
                StringBuilder messageContent = new StringBuilder();

                messageContent.append("Role-Name");
                messageContent.append(Functions.createSpaces(25 - ("Role-Name").length()));
                messageContent.append("Role-Type");
                messageContent.append(Functions.createSpaces(25 - ("Role-Type").length()));
                messageContent.append("Role-Setup Name");

                message.append(messageContent.toString(), MessageDecoration.CODE_LONG);
                messageContent = new StringBuilder();

                switch (firstOption.getOptions().get(0).getIntValue().get()) {
                    case 1: {
                        while (roles.hasNext()) {
                            Role role = roles.next();
                            messageContent.append("\n");
                            messageContent.append(role.getName());
                            messageContent.append(Functions.createSpaces(25 - (role.getName().length())));

                            BotRoleType roleType = DBServer.getServer(serverID).getRoles().getRole(role.getId()).getRoleType();
                            if (roleType != BotRoleType.UNKNOWN) {
                                messageContent.append(BotRoleType.getString(roleType));
                                messageContent.append(Functions.createSpaces(25 - (BotRoleType.getString(roleType).length())));
                            } else {
                                messageContent.append(EmojiParser.parseToUnicode(":x:"));
                                messageContent.append(Functions.createSpaces(22));
                            }

                            String roleSetupName = DBServer.getServer(serverID).getRoles().getRole(role.getId()).getRoleName();
                            if (!roleSetupName.equals("")) {
                                messageContent.append(roleSetupName);
                            } else {
                                messageContent.append(EmojiParser.parseToUnicode(":x:"));
                            }
                        }

                        log.info(logInfos + "\t\t\tCommand: show roles all");
                    }
                    break;
                    case 2: {
                        while (roles.hasNext()) {
                            Role role = roles.next();
                            BotRoleType roleType = DBServer.getServer(serverID).getRoles().getRole(role.getId()).getRoleType();
                            if (roleType == BotRoleType.COLOR) {
                                messageContent.append("\n");
                                messageContent.append(role.getName());
                                messageContent.append(Functions.createSpaces(25 - (role.getName().length())));
                                messageContent.append(BotRoleType.getString(roleType));
                                messageContent.append(Functions.createSpaces(25 - (BotRoleType.getString(roleType).length())));

                                String roleSetupName = DBServer.getServer(serverID).getRoles().getRole(role.getId()).getRoleName();
                                if (!roleSetupName.equals("")) {
                                    messageContent.append(roleSetupName);
                                } else {
                                    messageContent.append(EmojiParser.parseToUnicode(":x:"));
                                }
                            }
                        }

                        log.info(logInfos + "\t\t\tCommand: show roles color");
                    }
                    break;
                    case 3: {
                        while (roles.hasNext()) {
                            Role role = roles.next();
                            BotRoleType roleType = DBServer.getServer(serverID).getRoles().getRole(role.getId()).getRoleType();
                            if (roleType == BotRoleType.USER) {
                                messageContent.append("\n");
                                messageContent.append(role.getName());
                                messageContent.append(Functions.createSpaces(25 - (role.getName().length())));
                                messageContent.append(BotRoleType.getString(roleType));
                                messageContent.append(Functions.createSpaces(25 - (BotRoleType.getString(roleType).length())));

                                String roleSetupName = DBServer.getServer(serverID).getRoles().getRole(role.getId()).getRoleName();
                                if (!roleSetupName.equals("")) {
                                    messageContent.append(roleSetupName);
                                } else {
                                    messageContent.append(EmojiParser.parseToUnicode(":x:"));
                                }
                            }
                        }

                        log.info(logInfos + "\t\t\tCommand: show roles user");
                    }
                    break;
                }

                message.append(messageContent.toString(), MessageDecoration.CODE_LONG);

                slashCommandInteraction.createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent(message.getStringBuilder().toString())
                        .respond();
            }
            break;
            case "server": {
                ZonedDateTime createDateString = server.getCreationTimestamp().atZone(ZoneId.systemDefault());
                String createDay = "0" + createDateString.getDayOfMonth();
                String createMonth = "0" + createDateString.getMonth().getValue();
                int createYear = createDateString.getYear();
                String createDate = createDay.substring(createDay.length() - 2) + "." + createMonth.substring(createDay.length() - 2) + "." + createYear;

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Server Info")
                        .addInlineField("Server Besitzer:", server.getOwner().get().getDisplayName(server))
                        .addInlineField("\u200B", "\u200B")
                        .addInlineField("Wurde erstellt am:", createDate)
                        .addInlineField("Anzahl Mitglieder:", String.valueOf(server.getMemberCount()))
                        .addInlineField("\u200B", "\u200B")
                        .addInlineField("Anzahl Mitglieder (Online):", String.valueOf(Functions.membersOnlineCount(server)));

                slashCommandInteraction.createImmediateResponder()
                        .addEmbed(embed)
                        .respond();

                log.info(logInfos + "\t\t\tCommand: show server");
            }
            break;
        }
    }
}
