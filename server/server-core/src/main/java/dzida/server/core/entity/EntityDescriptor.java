package dzida.server.core.entity;

public interface EntityDescriptor<T extends State<T>> {
    CommandProcessor<T> getCommandProcessor();
    EntityType<T> getEntityType();
}
