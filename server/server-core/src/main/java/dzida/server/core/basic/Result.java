package dzida.server.core.basic;

import java.util.function.Consumer;

public interface Result {

    static Result ok() {
        return new ValidResult();
    }

    static Result error(Error error) {
        return new ErrorResult(error);
    }

    void consume(Runnable onValid, Consumer<Error> onError);

    final class ValidResult implements Result {

        ValidResult() {
        }

        @Override
        public void consume(Runnable onValid, Consumer<Error> onError) {
            onValid.run();
        }

    }

    final class ErrorResult implements Result {
        private final Error error;

        ErrorResult(Error error) {
            this.error = error;
        }

        @Override
        public void consume(Runnable onValid, Consumer<Error> onError) {
            onError.accept(error);
        }
    }

}