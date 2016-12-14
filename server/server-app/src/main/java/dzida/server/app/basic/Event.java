package dzida.server.app.basic;


import org.jetbrains.annotations.NotNull;

public interface Event {

    @NotNull
    static String getMessageTypeFromClass(Class<?> event) {
        return event.getSimpleName();
    }
}
