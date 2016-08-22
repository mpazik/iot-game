package dzida.server.app.user;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import dzida.server.app.Configuration;
import dzida.server.core.basic.entity.Id;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;
import java.util.Optional;

public class UserTokenDecryptor {
    private final JWTVerifier tokenVerifier;

    public UserTokenDecryptor() {
        tokenVerifier = new JWTVerifier(Configuration.getLoginTokenSecret());
    }

    Optional<LoginToken> decryptToken(EncryptedLoginToken encryptedLoginToken) {
        try {
            Map<String, Object> claims = tokenVerifier.verify(encryptedLoginToken.value);
            Object subject = claims.get("sub");
            Object nick = claims.get("nick");
            if (subject == null || !(subject instanceof Integer) || nick == null || !(nick instanceof String)) {
                return Optional.empty();
            }
            return Optional.of(new LoginToken(new Id<>((Integer) subject), (String) nick));

        } catch (JWTVerifyException | IllegalStateException e) {
            return Optional.empty();
        } catch (NoSuchAlgorithmException | IOException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
