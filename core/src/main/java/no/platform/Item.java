package no.platform;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * User: Michael Johansen
 * Date: 01.10.2014
 * Time: 18:22
 */
public class Item {
    public static final String TYPE = "_type";
    public static final String KEY = "key";
    private Map<String, Object> propertyMap = new HashMap<>();
    private final BiConsumer<String, Object> propertyAssignmentValidator;
    private final Consumer<Item> saveHandler;

    public Item(BiConsumer<String, Object> propertyAssignmentValidator,
                Consumer<Item> saveHandler,
                String key,
                Type type
    ) {
        this.propertyAssignmentValidator = propertyAssignmentValidator;
        this.saveHandler = saveHandler;
        set(KEY, key);
        set(TYPE, type);
    }

    public Item(BiConsumer<String, Object> propertyAssignmentValidator,
                Consumer<Item> saveHandler,
                Type type
    ) {
        this(propertyAssignmentValidator, saveHandler, UUID.randomUUID().toString(), type);
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
    public Map<String, ?> getProperties() {
        return propertyMap.entrySet().stream()
                .filter(e->!e.getKey().startsWith("_"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        //return Collections.unmodifiableMap(propertyMap);
    }

    public String getKey() {
        return get(KEY);
    }

    public Type getType() {
        return get(TYPE);
    }

    public String getTypeName() {
        return getType().getName();
    }

    public Item save() {
        saveHandler.accept(this);
        return this;
    }
}
