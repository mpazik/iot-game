package dzida.server.core.entity;

import com.google.common.collect.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A component that transforms command to the events.
 * The only possible side effect of processing commands is generating other commands.
 */
public interface CommandProcessor<T extends State<T>> {

    /**
     * This function should be side effect free.
     */
    Result<T> process(Command command);

    final class Result<T extends State<T>> {
        private final Collection<EntityChanges<T>> changes;
        private final List<Command> commands;

        private Result(Collection<EntityChanges<T>> changes, List<Command> commands) {
            this.changes = changes;
            this.commands = commands;
        }

        /**
         * @return returns events in order of creation.
         */
        public Collection<EntityChanges<T>> getChanges() {
            return changes;
        }

        /**
         * @return returns commands in order that determine theirs processing.
         */
        public List<Command> getCommands() {
            return commands;
        }

        public static <T extends State<T>> Result<T> change(EntityId<T> entityId, Change<T> change) {
            return new Result<>(ImmutableList.of(EntityChanges.change(entityId, change)), ImmutableList.of());
        }

        public static <T extends State<T>> Result<T> command(Command command) {
            return new Result<>(ImmutableList.of(), ImmutableList.of(command));
        }

        public static <T extends State<T>> Result<T> empty() {
            return new Result<>(ImmutableList.of(), ImmutableList.of());
        }

        public static <T extends State<T>> Builder<T> builder() {
            return new Builder<>();
        }

        public static final class Builder<T extends State<T>> {
            private HashMap<EntityId<T>, List<Change<T>>> changes = new HashMap<>();
            private ArrayList<Command> commands = new ArrayList<>();

            private Builder() {
            }

            public Result.Builder<T> addChange(EntityId<T> id, Change<T> change) {
                if (!changes.containsKey(id)) {
                    changes.put(id, new ArrayList<>());
                }
                changes.get(id).add(change);
                return this;
            }

            public Result.Builder<T> addCommand(Command command) {
                commands.add(command);
                return this;
            }

            public Result<T> build() {
                Collection<EntityChanges<T>> listChanges = changes.entrySet()
                        .stream()
                        .map(entry -> new EntityChanges<>(entry.getKey(), ImmutableList.copyOf(entry.getValue())))
                        .collect(Collectors.toList());
                return new Result<>(listChanges, ImmutableList.copyOf(commands));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result<?> that = (Result<?>) o;
            return Objects.equals(changes, that.changes) &&
                    Objects.equals(commands, that.commands);
        }

        @Override
        public int hashCode() {
            return Objects.hash(changes, commands);
        }

        @Override
        public String toString() {
            return "CommandProcessingReslut{" +
                    "events=" + changes +
                    ", commands=" + commands +
                    '}';
        }
    }

}

