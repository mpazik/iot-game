package dzida.server.app.user;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import dzida.server.app.Configuration;
import dzida.server.app.basic.entity.Id;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;
import java.util.Optional;

public class UserTokenVerifier {
    private final JWTVerifier tokenVerifier;

    public UserTokenVerifier() {
        tokenVerifier = new JWTVerifier(Configuration.getLoginTokenSecret());
    }

    public Optional<LoginToken> verifyToken(EncryptedLoginToken encryptedLoginToken) {
        try {
            Map<String, Object> claims = tokenVerifier.verify(encryptedLoginToken.value);
            Object subject = claims.get("sub");
            Object nick = claims.get("nick");
            int userId = Integer.parseInt((String) subject);
            return Optional.of(new LoginToken(new Id<>(userId), (String) nick));

        } catch (JWTVerifyException | IllegalStateException | NumberFormatException e) {
            return Optional.empty();
        } catch (NoSuchAlgorithmException | IOException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
