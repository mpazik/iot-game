package dzida.server.core.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Publisher<T> {
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public void subscribe(Consumer<T> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Consumer<T> listener) {
        listeners.remove(listener);
    }

    public void notify(T data) {
        listeners.forEach(consumer -> consumer.accept(data));
    }
}
