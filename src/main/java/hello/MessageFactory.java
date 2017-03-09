package hello;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Collection;

@Component
public class MessageFactory {

    private ObjectMapper m = new ObjectMapper();

    public Message from(String json) {
        try {
            return m.readValue(json, Message.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Message from(TextMessage message){
        return from(message.getPayload());
    }

    public TextMessage export(Message message) {
        try {
            return new TextMessage(m.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getList(Collection<String> list) {
        try {
            return m.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
