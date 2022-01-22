package de.moinFT.main.listener.message;

import de.moinFT.main.FileIn;
import de.moinFT.main.Functions;
import de.moinFT.utils.BotRoleType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONArray;
import org.json.JSONObject;

import static de.moinFT.main.Functions.createHelpMessage;
import static de.moinFT.main.Main.DBServer;

public class MessageListener implements MessageCreateListener {
    private static final Logger log = LogManager.getLogger(MessageListener.class.getName());

    private Server Server = null;
    private long ServerID = 0;
    private Message UserMessage = null;
    private String UserMessageContent = "";
    private String Prefix = "";
    private String logInfos;

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        UserMessage = event.getMessage();
        UserMessageContent = UserMessage.getContent().toLowerCase();

        if (UserMessage.isServerMessage()) {
            Server = event.getServer().get();
            ServerID = Server.getId();
            Prefix = DBServer.getServer(ServerID).getPrefix();

            logInfos = "Server: " + Server.getName() + " (" + Server.getId() + ") | User: " + UserMessage.getAuthor().getDiscriminatedName() + " (" + UserMessage.getAuthor().getId() + ")\n";

            if (!UserMessage.getAuthor().isWebhook() && !UserMessage.getAuthor().isBotUser()) {
                if (UserMessageContent.startsWith(Prefix)) {
                    if (UserMessageContent.startsWith(Prefix + "help")) {
                        getHelpMessage(DBServer.getServer(ServerID).getUsers().getUser(UserMessage.getAuthor().getId()).getBotPermission());
                    }
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

    private void getHelpMessage(boolean admin) {
        Functions.messageDelete(UserMessage, 500);

        String StringHelp = FileIn.read("/json/help.json");
        JSONObject JSONHelp = new JSONObject(StringHelp).getJSONObject("attributes");

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

                log.info(logInfos + "\t\t\tCommand: help-all");
            } else if (UserMessageContent.equalsIgnoreCase(Prefix + "help-set")) {
                message = new MessageBuilder();

                message.append("Bot-Befehle (Bot Variablen setzen)", MessageDecoration.CODE_LONG);
                messageContent = createHelpMessage(JSONHelp.getJSONArray("botVarSet"), 50);
                message.append(messageContent, MessageDecoration.CODE_LONG);

                log.info(logInfos + "\t\t\tCommand: help-set");
            }
        }

        JSONArray normalHelp = JSONHelp.getJSONArray("normalHelp");
        if (message == null) {
            message = new MessageBuilder();
            message.append("Bot-Befehle", MessageDecoration.CODE_LONG);

            log.info(logInfos + "\t\t\tCommand: help");
        } else {
            message.append("Normale-Befehle (Ohne Bot-Brechtigungen)", MessageDecoration.CODE_LONG);
            normalHelp.remove(0);
        }

        messageContent = createHelpMessage(normalHelp, 20);

        if (DBServer.getServer(ServerID).getRoles().countRoleType(BotRoleType.COLOR) != 0) {
            messageContent = messageContent + "\n/color              Verändert die Farbe des eigenen Namen.";
        }

        message.append(messageContent, MessageDecoration.CODE_LONG);
        textChannel.sendMessage(message.getStringBuilder().toString());

        Functions.messageDelete(UserMessage, 500);
    }
}