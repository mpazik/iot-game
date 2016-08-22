package dzida.server.app.user;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import dzida.server.app.Configuration;
import dzida.server.core.basic.Outcome;
import dzida.server.core.basic.Result;
import dzida.server.core.basic.entity.Id;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserService {
    private final PasswordHash passwordHash;
    private final JWTSigner loginTokenSigner;
    private final JWTSigner reissueTokenSigner;
    private final JWTVerifier reissueTokenVerifier;

    private final Map<Id<User>, String> userNicks;
    private final Map<Id<User>, String> userPasswords;

    public UserService() {
        passwordHash = new PasswordHash();
        loginTokenSigner = new JWTSigner(Configuration.getLoginTokenSecret());
        reissueTokenSigner = new JWTSigner(Configuration.getReissueTokenSecret());
        reissueTokenVerifier = new JWTVerifier(Configuration.getReissueTokenSecret());

        this.userNicks = new HashMap<>();
        this.userPasswords = new HashMap<>();

        if (Configuration.isDevMode()) {
            register("test", "test");
            register("qwe", "qwe");
        }
    }

    public Result register(String nick, String password) {
        if (hadNonAlphaCharacter(nick)) {
            return Result.error("Nick name can only contains alphanumeric characters.");
        }
        Optional<Id<User>> userIdByNick = getUserIdByNick(nick);
        if (userIdByNick.isPresent()) {
            return Result.error("User with " + nick + " is already registered.");
        }
        String hashedPassword = passwordHash.createHash(password);
        Id<User> userId = generateNewUserId(nick);
        userNicks.put(userId, nick);
        userPasswords.put(userId, hashedPassword);

        return Result.ok();
    }

    public boolean hadNonAlphaCharacter(String nick) {
        return nick.matches("^.*[^a-zA-Z0-9 ].*$");
    }

    public Outcome<EncryptedReissueToken> login(String nick, String password) {
        Optional<Id<User>> userIdByNick = getUserIdByNick(nick);
        if (!userIdByNick.isPresent()) {
            return Outcome.error("User not found.");
        }
        Id<User> userId = userIdByNick.get();
        String validHashedPassword = getUserPassword(userId);
        if (!passwordHash.validatePassword(password, validHashedPassword)) {
            return Outcome.error("Password is incorrect.");
        }
        return Outcome.ok(createReissueToken(userId));
    }

    public Outcome<EncryptedLoginToken> reissueLoginToken(EncryptedReissueToken encryptedReissueToken) {
        Optional<Id<User>> userIdOpt = decryptReissueToken(encryptedReissueToken);
        if (!userIdOpt.isPresent()) {
            return Outcome.error("Reissue token is not valid");
        }
        Id<User> userId = userIdOpt.get();
        return Outcome.ok(reissueLoginToken(userId, userNicks.get(userId)));
    }

    private Id<User> generateNewUserId(String nick) {
        return new Id<>(nick.hashCode());
    }

    private EncryptedReissueToken createReissueToken(Id<User> userId) {
        long issuedAt = System.currentTimeMillis() / 1000L;
        long expiration = issuedAt + (60 * 60 * 24 * 7); // one weeks
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("exp", expiration);
        claims.put("iat", issuedAt);
        claims.put("sub", Long.toString(userId.getValue()));

        return new EncryptedReissueToken(reissueTokenSigner.sign(claims));
    }

    private Optional<Id<User>> decryptReissueToken(EncryptedReissueToken encryptedReissueToken) {
        try {
            Map<String, Object> claims = reissueTokenVerifier.verify(encryptedReissueToken.value);
            Object subject = claims.get("sub");
            int userId = Integer.parseInt((String) subject);
            return Optional.of(new Id<>(userId));
        } catch (JWTVerifyException | IllegalStateException | NumberFormatException e) {
            return Optional.empty();
        } catch (NoSuchAlgorithmException | IOException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private EncryptedLoginToken reissueLoginToken(Id<User> userId, String nick) {
        long issuedAt = System.currentTimeMillis() / 1000L;
        long expiration = issuedAt + (60 * 10); // 10 minutes
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("exp", expiration);
        claims.put("iat", issuedAt);
        claims.put("sub", Long.toString(userId.getValue()));
        claims.put("nick", nick);

        return new EncryptedLoginToken(loginTokenSigner.sign(claims));
    }

    private String getUserPassword(Id<User> userId) {
        return userPasswords.get(userId);
    }

    private Optional<Id<User>> getUserIdByNick(String nick) {
        return userNicks.entrySet().stream()
                .filter(entry -> entry.getValue().equals(nick))
                .map(Map.Entry::getKey)
                .findAny();
    }

}


