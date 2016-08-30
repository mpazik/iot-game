package dzida.server.app.serialization;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MessageSerializerTest {
    @Test
    public void exceptionIsThrownIfSameClassIsRegisteredTwice() {
        assertThatThrownBy(() -> {
            new MessageSerializer.Builder()
                    .registerMessage(Message.class)
                    .registerMessage(Message.class)
                    .build();
        }).hasMessageContaining("Duplicated");
    }

    @Test
    public void exceptionIsThrownIfAnotherClassWithSameNameIsRegistered() {
        assertThatThrownBy(() -> {
            new MessageSerializer.Builder()
                    .registerMessage(Message.class)
                    .registerMessage(Base.Message.class)
                    .build();
        }).hasMessageContaining("the old class name can not be changed");
    }

    class Message {
        final int number;

        Message(int number) {
            this.number = number;
        }
    }

    class Base {
        class Message {
            final String text;

            Message(String text) {
                this.text = text;
            }
        }
    }
}