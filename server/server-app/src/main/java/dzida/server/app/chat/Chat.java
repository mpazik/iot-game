package dzida.server.app.chat;

import com.google.common.base.Strings;
import dzida.server.app.instance.Instance;
import dzida.server.app.user.EncryptedLoginToken;
import dzida.server.app.user.LoginToken;
import dzida.server.app.user.UserTokenVerifier;
import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.Connector;
import dzida.server.core.basic.connection.ServerConnection;
import dzida.server.core.basic.connection.VerifyingConnectionServer;
import dzida.server.core.basic.entity.Key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Chat implements VerifyingConnectionServer<String, String> {
    private final UserTokenVerifier userTokenVerifier;

    private final Map<String, List<String>> channelConnections;
    private final Map<String, Consumer<String>> messageTargets;

    public Chat() {
        userTokenVerifier = new UserTokenVerifier();

        channelConnections = new HashMap<>();
        messageTargets = new HashMap<>();
    }

    private static boolean isChannelName(String name) {
        return !Strings.isNullOrEmpty(name) && name.charAt(0) == '#';
    }

    @Override
    public Result verifyConnection(String userToken) {
        Optional<LoginToken> loginToken = userTokenVerifier.verifyToken(new EncryptedLoginToken(userToken));
        if (!loginToken.isPresent()) {
            return Result.error("User token is not valid");
        }
        return Result.ok();
    }

    @Override
    public void onConnection(Connector<String> connector, String userToken) {
        Optional<LoginToken> loginToken = userTokenVerifier.verifyToken(new EncryptedLoginToken(userToken));
        if (!loginToken.isPresent()) {
            connector.onClose();
            return;
        }
        String nick = loginToken.get().nick;
        messageTargets.put(nick, connector::onMessage);
        ChatConnection chatConnection = new ChatConnection(nick);
        connector.onOpen(chatConnection);
    }

    public void createInstanceChannel(Key<Instance> instanceKey) {
        String channelName = chanelNameFromInstanceKey(instanceKey);
        channelConnections.put(channelName, new ArrayList<>());
        messageTargets.put(channelName, message -> channelConnections.get(channelName)
                .forEach(nick -> messageTargets.get(nick).accept("CHANNEL " + channelName + " " + message))
        );
    }

    public void closeInstanceChannel(Key<Instance> instanceKey) {
        String channelName = chanelNameFromInstanceKey(instanceKey);
        messageTargets.get(channelName).accept("CLOSED");
        channelConnections.remove(channelName);
        messageTargets.remove(channelName);
    }

    public String chanelNameFromInstanceKey(Key<Instance> instanceKey) {
        return "#" + instanceKey.getValue();
    }

    private final class ChatConnection implements ServerConnection<String> {
        private final String userNick;

        private ChatConnection(String userNick) {
            this.userNick = userNick;
        }

        @Override
        public void send(String data) {
            String[] dataSplit = data.split(" ", 2);
            String command = dataSplit[0];
            String args = dataSplit[1];
            switch (command) {
                case "JOIN": {
                    joinToChannel(args);
                    break;
                }
                case "MSG": {
                    String[] split = args.split(" ", 2);
                    String target = split[0];
                    String message = split[1];
                    sendMessage(target, message);
                    break;
                }
                case "QUIT": {
                    quiteFromChannel(args);
                    break;
                }
                case "LIST": {
                    listChannelPlayers(args);
                    break;
                }
            }
        }

        private void listChannelPlayers(String channelName) {
            if (!channelNameValidation(channelName)) {
                return;
            }
            List<String> channelUsers = channelConnections.get(channelName);
            messageTargets.get(userNick).accept("CHANNEL " + channelName + " LIST " + channelUsers.size() + " " + String.join(",", channelUsers));
        }

        private void quiteFromChannel(String channelName) {
            if (!channelNameValidation(channelName)) {
                return;
            }
            channelConnections.get(channelName).remove(userNick);
            messageTargets.get(channelName).accept("QUITED " + userNick);
        }

        private boolean channelNameValidation(String channelName) {
            if (!isChannelName(channelName)) {
                response("ERROR channel name: " + channelName + ", is incorrect");
                return false;
            }
            if (!channelConnections.containsKey(channelName)) {
                response("ERROR channel: " + channelName + ", does not exist.");
                return false;
            }
            return true;
        }

        private void response(String message) {
            messageTargets.get(userNick).accept(message);
        }

        private void sendMessage(String target, String message) {
            if (Strings.isNullOrEmpty(target)) {
                response("ERROR message target has to be defined");
                return;
            }
            if (Strings.isNullOrEmpty(message)) {
                response("ERROR can not send empty message");
                return;
            }
            if (!messageTargets.containsKey(target)) {
                response("ERROR target:" + target + " is not defined");
                return;
            }
            messageTargets.get(target).accept("MSG " + userNick + " " + message);
        }

        public void joinToChannel(String channelName) {
            if (!isChannelName(channelName)) {
                response("ERROR channel name: " + channelName + ", is in correct");
                return;
            }
            if (!channelConnections.containsKey(channelName)) {
                response("ERROR channel: " + channelName + ", does not exist.");
            }
            channelConnections.get(channelName).add(userNick);
            messageTargets.get(channelName).accept("JOINED " + userNick);
        }

        @Override
        public void close() {
            messageTargets.remove(userNick);
            channelConnections.forEach((channelName, users) -> users.remove(userNick));
        }
    }

}
