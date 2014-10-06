package platform;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * User: Michael Johansen
 * Date: 01.10.2014
 * Time: 18:22
 */
public class Item {
    private Map<String, Object> propertyMap = new HashMap<>();
    private final BiConsumer<String, Object> propertyAssignmentValidator;
    private final Consumer<Item> saveHandler;
    private final String key;

    public Item(BiConsumer<String, Object> propertyAssignmentValidator,
                Consumer<Item> saveHandler,
                String key
    ) {
        this.propertyAssignmentValidator = propertyAssignmentValidator;
        this.saveHandler = saveHandler;
        this.key = key;
    }

    public Item(BiConsumer<String, Object> propertyAssignmentValidator,
                Consumer<Item> saveHandler
    ) {
        this(propertyAssignmentValidator, saveHandler, UUID.randomUUID().toString());
    }

    public <T> Item set(String name, T value) {
        propertyAssignmentValidator.accept(name, value);
        propertyMap.put(name, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) propertyMap.get(name);
    }

    public String getKey() {
        return key;
    }

    public void save() {
        saveHandler.accept(this);
    }
}
