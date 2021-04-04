package de.moinFT.main.Server;

import de.moinFT.utils.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static de.moinFT.main.Main.DBServer;

public class PortListener extends Thread {
    private final ServerSocket serverSocket;

    public PortListener(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void run() {
        while (true) {
            if (serverSocket.getLocalPort() == Privates.CommandPort) {
                try {
                    System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
                    Socket server = serverSocket.accept();
                    System.out.println("Just connected to " + server.getRemoteSocketAddress());
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    String input = in.readUTF();

                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    if (input.equals(Privates.clientToken)) {
                        out.writeUTF("OK!");
                    } else {
                        out.writeUTF("NO CONNECTION!");
                    }

                    in = new DataInputStream(server.getInputStream());
                    input = in.readUTF();

                    long discordServerID;
                    String discordBotCommand;
                    String discordBotArguments;

                    try {
                        discordServerID = Long.parseLong(input.split("-")[0]);
                        discordBotCommand = input.split("-")[1];
                        discordBotArguments = input.split("-")[2];
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        break;
                    }

                    CommandManagement commandManagement = new CommandManagement(discordServerID, discordBotCommand, discordBotArguments);
                    if (commandManagement.command()) {
                        out = new DataOutputStream(server.getOutputStream());
                        out.writeUTF("CommandBuildSuccessful!");
                    } else {
                        out = new DataOutputStream(server.getOutputStream());
                        out.writeUTF("CommandBuildError!\nErrorCode: " + commandManagement.getError());
                    }

                    server.close();
                } catch (SocketTimeoutException s) {
                    System.out.println("Socket timed out!");
                    break;
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    break;
                }
            } else if(serverSocket.getLocalPort() == Privates.GetDataPort){
                try {
                    System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
                    Socket server = serverSocket.accept();
                    System.out.println("Just connected to " + server.getRemoteSocketAddress());
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    String input = in.readUTF();

                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    if (input.equals(Privates.clientToken)) {
                        out.writeUTF("OK!");
                    } else {
                        out.writeUTF("NO CONNECTION!");
                        break;
                    }

                    in = new DataInputStream(server.getInputStream());
                    input = in.readUTF();
                    if (input.equals("OK!")){
                        out.writeUTF(String.valueOf(DBServer.count()));
                    } else {
                        out.writeUTF("NO CONNECTION!");
                        break;
                    }

                    for (int i = 0; i < DBServer.count(); i++) {
                        DBServerArray discordServer = DBServer.getServer(i);
                        out.writeUTF( discordServer.getServerID() + "~" + discordServer.getDiscordServerName() + "~" + discordServer.getCommandTimeout() + "~" + discordServer.getPrefix() + "~" + discordServer.getMusicBotPrefix());
                    }

                    in = new DataInputStream(server.getInputStream());
                    input = in.readUTF();
                    if (!input.equals("OK!")){
                        out.writeUTF("NO CONNECTION!");
                        break;
                    }

                    for (int iteratorServer = 0; iteratorServer < DBServer.count(); iteratorServer++) {
                        DBChannelArray discordChannels = DBServer.getServer(iteratorServer).getChannels();
                        out.writeUTF(String.valueOf(discordChannels.count()));
                        for (int iteratorChannel = 0; iteratorChannel < discordChannels.count(); iteratorChannel++) {
                            DBChannelArray discordChannel = discordChannels.getChannel(iteratorChannel);
                            out.writeUTF(discordChannel.getChannelID() + "~" + discordChannel.getDiscordChannelName() + "~" + discordChannel.getChannelType() + "~" + discordChannel.getChannelName()+ " ");
                        }
                    }

                    in = new DataInputStream(server.getInputStream());
                    input = in.readUTF();
                    if (!input.equals("OK!")){
                        out.writeUTF("NO CONNECTION!");
                        break;
                    }

                    for (int iteratorServer = 0; iteratorServer < DBServer.count(); iteratorServer++) {
                        DBRoleArray discordRoles = DBServer.getServer(iteratorServer).getRoles();
                        out.writeUTF(String.valueOf(discordRoles.count()));
                        for (int iteratorRole = 0; iteratorRole < discordRoles.count(); iteratorRole++) {
                            DBRoleArray discordRole = discordRoles.getRole(iteratorRole);

                            String roleType = discordRole.getRoleType();
                            if (roleType.equals("")){
                                roleType = "-";
                            }

                            String roleName = discordRole.getRoleName();
                            if (roleName.equals("")){
                                roleName = "-";
                            }

                            out.writeUTF(discordRole.getRoleID() + "~" + discordRole.getDiscordRoleName() + "~" + roleType + "~" + roleName);
                        }
                    }

                    in = new DataInputStream(server.getInputStream());
                    input = in.readUTF();
                    if (!input.equals("OK!")){
                        out.writeUTF("NO CONNECTION!");
                        break;
                    }

                    for (int iteratorServer = 0; iteratorServer < DBServer.count(); iteratorServer++) {
                        DBUserArray discordUsers = DBServer.getServer(iteratorServer).getUsers();
                        out.writeUTF(String.valueOf(discordUsers.count()));
                        for (int iteratorUser = 0; iteratorUser < discordUsers.count(); iteratorUser++) {
                            DBUserArray discordUser = discordUsers.getUser(iteratorUser);
                            out.writeUTF(discordUser.getUserID() + "~" + discordUser.getDiscordUserName() + "~" + discordUser.getIsAdmin() + "~" + discordUser.getBotPermission());
                        }
                    }

                    server.close();
                } catch (SocketTimeoutException s) {
                    System.out.println("Socket timed out!");
                    break;
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
