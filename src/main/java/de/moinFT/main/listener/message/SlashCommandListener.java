package de.moinFT.main.listener.message;

import com.vdurmont.emoji.EmojiParser;
import de.moinFT.main.Functions;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.util.Date;
import java.util.Iterator;

import static de.moinFT.main.Main.DBServer;
import static de.moinFT.main.Main.ServerUserRequest;

public class SlashCommandListener implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
        Server server;
        long serverID;

        SlashCommandInteractionOption firstOption = null;
        SlashCommandInteractionOption secondOption = null;

        if (!slashCommandInteraction.getOptions().isEmpty()) {
            firstOption = slashCommandInteraction.getOptions().get(0);
        }

        if (firstOption != null) {
            if (!slashCommandInteraction.getOptions().get(0).getOptions().isEmpty()) {
                secondOption = firstOption.getOptions().get(0);
            }
        }

        if (slashCommandInteraction.getServer().isPresent()) {
            server = slashCommandInteraction.getServer().get();
            serverID = server.getId();
        } else {
            slashCommandInteraction.createImmediateResponder()
                    .setContent("FEHLER: Server wurde nicht gefunden!")
                    .respond();

            return;
        }

        if (slashCommandInteraction.getCommandName().equals("bot-permission")) {
            if (firstOption == null) {
                slashCommandInteraction.createImmediateResponder()
                        .setContent("FEHLER: SlashCommand wurde nicht gefunden!")
                        .respond();

                return;
            }

            switch (firstOption.getName()) {
                case "set": {
                    if (secondOption == null) {
                        slashCommandInteraction.createImmediateResponder()
                                .setContent("FEHLER: SlashCommand wurde nicht gefunden!")
                                .respond();

                        return;
                    }

                    User user = secondOption.getUserValue().get();

                    //Send respond on the request
                    slashCommandInteraction.createImmediateResponder()
                            .setContent("Setzt Bot-Berechtigungen für: " + user.getDiscriminatedName())
                            .addComponents(
                                    ActionRow.of(Button.success("botPermissionAllow", "Erlauben"),
                                            Button.danger("botPermissionDeny", "Ablehnen")))
                            .respond();

                    ServerUserRequest.setData("botPermission", server, user, new Date().getTime());
                }
                break;
            }
        } else if (slashCommandInteraction.getCommandName().equals("show")) {
            if (firstOption == null) {
                slashCommandInteraction.createImmediateResponder()
                        .setContent("FEHLER: SlashCommand wurde nicht gefunden!")
                        .respond();

                return;
            }

            switch (firstOption.getName()) {
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
                }
                break;
            }
        } else if (slashCommandInteraction.getCommandName().equals("ping")) {
            slashCommandInteraction.createImmediateResponder()
                    .setContent("Ping")
                    .respond();
        }
    }
}
