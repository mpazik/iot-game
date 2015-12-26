package dzida.server.core.basic;

public interface Outcome<T> {

    boolean isValid();

    default boolean isInValid() {
        return !isValid();
    }

    static <T> Outcome<T> ok(T value) {
        return new ValidOutcome<>(value);
    }

    static <T> Outcome<T> error(Error error) {
        return new ErrorOutcome<>(error);
    }

    T getValue();

    final class ValidOutcome<T> implements Outcome<T> {
        private final T value;

        ValidOutcome(T value) {
            this.value = value;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        public T getValue() {
            return value;
        }

    }

    final class ErrorOutcome<T> implements Outcome<T> {
        private final Error error;

        ErrorOutcome(Error error) {
            this.error = error;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public T getValue() {
            throw new UnsupportedOperationException("Error Outcome does not contain any value");
        }

        public Error getError() {
            return error;
        }
    }

}