package dzida.server.core.entity;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.Consumer;

public class CommandHandler {
    private final List<EntityDescriptor> entityDescriptors;
    private final List<Consumer<EntityChangesWithType<?>>> changeListeners;

    public CommandHandler(
            ImmutableList<EntityDescriptor> entityDescriptors,
            ImmutableList<Consumer<EntityChangesWithType<?>>> changeListeners) {
        this.entityDescriptors = entityDescriptors;
        this.changeListeners = changeListeners;
    }

    public void handle(Command command) {
        entityDescriptors.forEach(entityDescriptor -> process(entityDescriptor, command));
    }

    private void process(EntityDescriptor<?> entityDescriptor, Command command) {
        CommandProcessor.Result<?> result = entityDescriptor.getCommandProcessor().process(command);
        //noinspection unchecked
        result.getChanges().stream()
                .map(entry -> new EntityChangesWithType(entry.entityId, entityDescriptor.getEntityType(), entry.changes))
                .forEach(entry -> changeListeners.forEach(listener -> listener.accept(entry)));

        // todo shouldn't be in the same transaction. May cause stack overflow.
        result.getCommands().stream().forEach(this::handle);
    }
}

