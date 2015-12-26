package dzida.server.core.basic;

import java.util.function.Consumer;

public interface Result {

    boolean isValid();

    default boolean isInValid() {
        return !isValid();
    }

    static Result ok() {
        return new ValidResult();
    }

    static Result error(Error error) {
        return new ErrorResult(error);
    }

    void forEach(Consumer<ValidResult> onValid, Consumer<ErrorResult> onError);

    final class ValidResult implements Result {

        ValidResult() {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void forEach(Consumer<ValidResult> onValid, Consumer<ErrorResult> onError) {
            onValid.accept(this);
        }

    }

    final class ErrorResult implements Result {
        private final Error error;

        ErrorResult(Error error) {
            this.error = error;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public void forEach(Consumer<ValidResult> onValid, Consumer<ErrorResult> onError) {
            onError.accept(this);
        }

        public Error getError() {
            return error;
        }
    }

}