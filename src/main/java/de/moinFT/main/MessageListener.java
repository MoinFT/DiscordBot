package de.moinFT.main;

import de.moinFT.utils.Privates;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

import static de.moinFT.main.Main.DBServer;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class MessageListener implements MessageCreateListener {

    private Server Server = null;
    private String ServerID = "";
    private Message Message = null;
    private String MessageContent = "";

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Server = event.getServer().get();
        ServerID = String.valueOf(Server.getId());
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
                        } else if (MessageContent.startsWith("!help-all")) {
                            TextChannel AdminChannel = Server.getChannelById(Privates.AdminChannelID).get().asTextChannel().get();

                            EmbedBuilder embed = new EmbedBuilder()
                                    .setTitle("Bot-Befehle")
                                    .addField("Admin-Befehle (Ohne Bot-Berechtigungen)", "\u200B")
                                    .addField("!help-all", "Diese Liste")
                                    .addField("!clear [Wert]", "Loescht [Wert] Nachrichten aus einem TextChannel")
                                    .addField("\u200B", "\u200B")
                                    .addField("Admin-Befehle (Mit Bot-Berechtigungen)", "\u200B")
                                    .addField("!add-role [UserMention] [RoleMention]", "Gibt dem Nutzer [UserMention] die Rolle [RoleMention]")
                                    .addField("!remove-role [UserMention] [RoleMention]", "Nimmt dem Nutzer [UserMention] die Rolle [RoleMention]")
                                    .addField("\u200B", "\u200B")
                                    .addField("Normale Befehle", "\u200B")
                                    .addField("!color [color]", "Farben: black, cyan, purple, pink, yellow, red, gray, dark-gray, dark-blue")
                                    .addField("!color", "Farbe entfernen");

                            AdminChannel.sendMessage(embed);
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
                    for (int i = 0; i < DBRoleCount; i++) {
                        Role userRole = userRoles.next();
                        if (String.valueOf(userRole.getId()).equals(DBServer.getServer(ServerID).getRoles().getRoleID(i))) {
                            message.getUserAuthor().get().removeRole(userRole);
                        }
                    }
                }

                if (MessageContent.length() > 7) {
                    String messValue = MessageContent.substring(7);

                    for (int i = 0; i < DBRoleCount; i++) {
                        if (DBServer.getServer(ServerID).getRoles().getRoleName(i).equals(messValue) && DBServer.getServer(ServerID).getRoles().getRoleType(i).equals("color")) {
                            Role role = Server.getRoleById(DBServer.getServer(ServerID).getRoles().getRoleID(i)).get();

                            Message.getUserAuthor().get().addRole(role);
                        }
                    }
                }
            }

            Message.delete();
        } else if (MessageContent.startsWith("!m-a")) {
            ServerVoiceChannel voiceChannel = Message.getUserAuthor().get().getConnectedVoiceChannel(Server).get();
            if (voiceChannel != null) {
                Object[] userIDs = voiceChannel.getConnectedUserIds().toArray();

                if (!Server.getMemberById((long) userIDs[0]).get().isMuted(Server)) {
                    for (int i = 0; i < userIDs.length; i++) {
                        Server.getMemberById((long) userIDs[i]).get().mute(Server);
                    }
                } else {
                    for (int i = 0; i < userIDs.length; i++) {
                        Server.getMemberById((long) userIDs[i]).get().unmute(Server);
                    }
                }
            }
            Message.delete();
        } else if (MessageContent.startsWith("!help")) {
            TextChannel textChannel = message.getChannel();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Bot-Befehle")
                    .addField("!m-a", "Alle Personnen in seinem VoiceChannel stummschalten (Cooldown: 5 Sekunden)")
                    .addField("!color [color]", "Farben: black, cyan, purple, pink, yellow, red, gray, dark-gray, dark-blue")
                    .addField("!color", "Farbe entfernen");

            textChannel.sendMessage(embed);
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