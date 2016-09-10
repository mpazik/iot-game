package dzida.server.app.user;

import org.junit.Test;

public class PasswordHashTest {

    @Test
    public void name() {
        System.out.println(new PasswordHash().createHash("asd"));

    }
}