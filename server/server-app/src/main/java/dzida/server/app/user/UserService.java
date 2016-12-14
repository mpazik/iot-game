package dzida.server.app.user;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import dzida.server.app.Configuration;
import dzida.server.app.basic.Outcome;
import dzida.server.app.basic.Result;
import dzida.server.app.basic.entity.Id;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserService {
    private final UserStore userStore;
    private final PasswordHash passwordHash;
    private final JWTSigner loginTokenSigner;
    private final JWTSigner reissueTokenSigner;
    private final JWTVerifier reissueTokenVerifier;

    public UserService(UserStore userStore) {
        this.userStore = userStore;
        passwordHash = new PasswordHash();
        loginTokenSigner = new JWTSigner(Configuration.getLoginTokenSecret());
        reissueTokenSigner = new JWTSigner(Configuration.getReissueTokenSecret());
        reissueTokenVerifier = new JWTVerifier(Configuration.getReissueTokenSecret());
    }

    public Result register(String nick, String email, String password) {
        if (hadNonAlphaCharacter(nick)) {
            return Result.error("Nick name can only contains alphanumeric characters.");
        }
        Optional<Id<User>> userIdByNick = userStore.getUserIdByNick(nick);
        if (userIdByNick.isPresent()) {
            return Result.error("User with " + nick + " is already registered.");
        }
        String hashedPassword = passwordHash.createHash(password);
        return userStore.registerNewUser(nick, email, hashedPassword).toResult();
    }

    public boolean hadNonAlphaCharacter(String nick) {
        return nick.matches("^.*[^a-zA-Z0-9 ].*$");
    }

    public Outcome<EncryptedReissueToken> login(String nick, String password) {
        Optional<Id<User>> userIdByNick = userStore.getUserIdByNick(nick);
        if (!userIdByNick.isPresent()) {
            return Outcome.error("User not found.");
        }
        Id<User> userId = userIdByNick.get();
        String validHashedPassword = userStore.getUserPassword(userId);
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
        return Outcome.ok(reissueLoginToken(userId, userStore.getUserNick(userId)));
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

    public String getUserNick(Id<User> userId) {
        return userStore.getUserNick(userId);
    }
}


