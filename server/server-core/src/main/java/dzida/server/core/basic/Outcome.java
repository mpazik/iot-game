package dzida.server.core.basic;

import java.util.Optional;
import java.util.function.Consumer;

public interface Outcome<T> {

    static <T> Outcome<T> ok(T value) {
        return new ValidOutcome<>(value);
    }

    static <T> Outcome<T> error(Error error) {
        return new ErrorOutcome<>(error);
    }

    static <T> Outcome<T> error(String errorMessage) {
        return new ErrorOutcome<>(new Error(errorMessage));
    }

    static <T> Outcome<T> fromOptional(Optional<T> playerScore, Error error) {
        return playerScore.map(Outcome::ok).orElseGet(() -> new ErrorOutcome<>(error));
    }

    void consume(Consumer<T> onValid, Consumer<Error> onError);

    Optional<T> toOptional();

    Result toResult();

    final class ValidOutcome<T> implements Outcome<T> {
        private final T value;

        ValidOutcome(T value) {
            this.value = value;
        }

        @Override
        public void consume(Consumer<T> onValid, Consumer<Error> onError) {
            onValid.accept(value);
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.of(value);
        }

        @Override
        public Result toResult() {
            return Result.ok();
        }

    }

    final class ErrorOutcome<T> implements Outcome<T> {
        private final Error error;

        ErrorOutcome(Error error) {
            this.error = error;
        }

        @Override
        public void consume(Consumer<T> onValid, Consumer<Error> onError) {
            onError.accept(error);
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        @Override
        public Result toResult() {
            return Result.error(error);
        }
    }

}