package dzida.server.app.rest;

import co.cask.http.HttpResponder;
import dzida.server.app.user.EncryptedLoginToken;
import dzida.server.app.user.EncryptedReissueToken;
import dzida.server.app.user.UserService;
import dzida.server.core.basic.Outcome;
import dzida.server.core.basic.Result;
import org.jboss.netty.handler.codec.http.HttpRequest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

public class UserResource extends AbstractResource {
    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @Path("login")
    @POST
    public void login(HttpRequest request, HttpResponder responder) {
        LoginRequest loginRequest = parseJsonRequest(request, LoginRequest.class);
        send(responder, login(loginRequest));
    }

    private Outcome<LoginResponse> login(LoginRequest loginRequest) {
        Outcome<EncryptedReissueToken> reissueTokenOutcome = userService.login(loginRequest.nick, loginRequest.password);
        if (!reissueTokenOutcome.isValid()) {
            return Outcome.error(reissueTokenOutcome);
        }

        Outcome<EncryptedLoginToken> loginTokenOutcome = userService.reissueLoginToken(reissueTokenOutcome.get());
        if (!loginTokenOutcome.isValid()) {
            System.err.println("User resource generated token that is not valid");
            return Outcome.error(loginTokenOutcome);
        }
        return Outcome.ok(new LoginResponse(reissueTokenOutcome.get().value, loginTokenOutcome.get().value));
    }

    @Path("reissue")
    @POST
    public void reissue(HttpRequest request, HttpResponder responder) {
        ReissueTokenRequest reissueTokenRequest = parseJsonRequest(request, ReissueTokenRequest.class);
        send(responder, reissue(reissueTokenRequest));
    }

    private Outcome<ReissueTokenResponse> reissue(ReissueTokenRequest reissueTokenRequest) {
        Outcome<EncryptedLoginToken> loginTokenOutcome = userService.reissueLoginToken(new EncryptedReissueToken(reissueTokenRequest.token));
        if (!loginTokenOutcome.isValid()) {
            System.err.println("User resource generated token that is not valid");
            return Outcome.error(loginTokenOutcome);
        }
        return Outcome.ok(new ReissueTokenResponse(loginTokenOutcome.get().value));
    }

    @Path("register")
    @POST
    public void register(HttpRequest request, HttpResponder responder) {
        RegisterRequest registerRequest = parseJsonRequest(request, RegisterRequest.class);
        Result registerResult = userService.register(registerRequest.nick, registerRequest.password);
        send(responder, registerResult);
    }

    private static final class LoginRequest {
        final String nick;
        final String password;

        private LoginRequest(String nick, String password) {
            this.nick = nick;
            this.password = password;
        }
    }

    private static final class LoginResponse {
        final String reissueToken;
        final String loginToken;

        private LoginResponse(String reissueToken, String loginToken) {
            this.reissueToken = reissueToken;
            this.loginToken = loginToken;
        }
    }

    private static final class ReissueTokenRequest {
        final String token;

        private ReissueTokenRequest(String token) {
            this.token = token;
        }
    }

    private static final class ReissueTokenResponse {
        final String loginToken;

        private ReissueTokenResponse(String loginToken) {
            this.loginToken = loginToken;
        }
    }

    private static final class RegisterRequest {
        final String nick;
        final String password;
        final String email;

        private RegisterRequest(String nick, String password, String email) {
            this.nick = nick;
            this.password = password;
            this.email = email;
        }
    }
}
