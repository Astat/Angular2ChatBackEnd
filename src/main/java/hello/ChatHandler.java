package hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHandler extends TextWebSocketHandler {

    private Map<WebSocketSession, String> sessions = new ConcurrentHashMap<>();

    private static final String SYSTEM_NAME = "Dieu";

    @Autowired
    MessageFactory factory;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage wsMessage) {
        Message m = factory.from(wsMessage);
        if (!sessions.containsKey(session)) {
            connectUser(session, m);
        } else {
            String from = sessions.get(session);
            Message message = new Message(m.getText(), from);
            broadCast(message);
        }
    }

    private void connectUser(WebSocketSession session, Message m) {
        String name = m.getText();

        if (StringUtils.isEmpty(name)) {
            Message alreadyUsed = new Message("Pseudo obligatoire", SYSTEM_NAME);
            send(session, alreadyUsed);
            close(session);
            return;
        }

        if (sessions.containsValue(name) || SYSTEM_NAME.equalsIgnoreCase(name)) {
            Message alreadyUsed = new Message("Pseudo déjà attribué", SYSTEM_NAME);
            send(session, alreadyUsed);
            close(session);
            return;
        }

        sessions.put(session, name);

        notifyUpdateList();

        Message welcome = new Message("Connecté en tant que " + m.getText(), SYSTEM_NAME);
        send(session, welcome);
    }

    private void notifyUpdateList() {
        String connected = factory.getList(sessions.values());
        Message connectedList = new Message(connected, SYSTEM_NAME);
        connectedList.setAction(Message.Action.LIST);
        broadCast(connectedList);
    }

    private void send(WebSocketSession s, Message m) {
        try {
            s.sendMessage(factory.export(m));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void close(WebSocketSession s) {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void broadCast(Message m) {
        sessions.forEach((s, n) -> {
            try {
                String output = new ObjectMapper().writeValueAsString(m);
                s.sendMessage(new TextMessage(output));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        notifyUpdateList();
    }
}