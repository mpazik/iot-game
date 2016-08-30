package dzida.server.app.serialization;


import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MessageSerializerTest {

    @Test
    public void exceptionIsThrownIfAnotherClassWithSameNameIsRegistered() {
        assertThatThrownBy(() -> {
            MessageSerializer.create(ImmutableSet.of(Message.class, Base.Message.class));
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