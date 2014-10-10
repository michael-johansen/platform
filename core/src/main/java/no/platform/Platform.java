package no.platform;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

/**
 * User: Michael Johansen
 * Date: 01.10.2014
 * Time: 18:19
 */
public class Platform implements PersistenceConfigurer {
    private Map<String, Type> typePropertyMap = new HashMap<>();
    private List<Consumer<Type>> onTypeRegisteredHandlers = new ArrayList<>();
    private List<Consumer<Item>> onSaveHandlers = new ArrayList<>();
    private Map<Type, Map<String, Item>> itemStorage = new HashMap<>();
    private final Map<PersistenceConfigurer, BiFunction<Type, String, Optional<Item>>> loadHandlers = new HashMap<>();
    private PersistenceConfigurer defaultPersistence = this;

    public Platform() {
        configurePersistence(this);
    }

    public Platform.TypeBuilder addType(String name) {
        return new TypeBuilder(name);
    }

    public Item createItem(String name) {
        if (!typePropertyMap.containsKey(name)) {
            throw new IllegalArgumentException("Item " + name + " is not defined");
        }
        return typePropertyMap.get(name).createItem();
    }

    private BiConsumer<String, Object> getPropertyAssignmentValidator(String name) {
        return (propertyName, value) -> {
            if (typePropertyMap.containsKey(name)) {
                Map<String, Class<?>> propertyMap = typePropertyMap.get(name).getPropertyClasses();
                if (propertyMap.containsKey(propertyName)) {
                    Class<?> propertyClass = propertyMap.get(propertyName);
                    if (value != null && !propertyClass.isAssignableFrom(value.getClass())) {
                        throw new IllegalArgumentException(String.format(
                                "Value%s is not assignable to property %s of type %s for type %s",
                                value,
                                propertyName,
                                propertyClass.getName(),
                                name
                        ));
                    }
                } else {
                    throw new IllegalArgumentException(String.format(
                            "Undefined property %s on type %s",
                            propertyName,
                            name
                    ));
                }
            }
        };
    }

    public void addOnTypeRegistered(Consumer<Type> consumer) {
        onTypeRegisteredHandlers.add(consumer);
    }

    public void addOnSaveHandler(Consumer<Item> consumer) {
        onSaveHandlers.add(consumer);
    }

    public Optional<Item> loadItem(String typeName, String itemKey) {
        return loadItem(defaultPersistence, typeName, itemKey);
    }

    public Optional<Item> loadItem(PersistenceConfigurer persistenceConfigurer, String typeName, String itemKey) {
        return Optional.ofNullable(loadHandlers.get(persistenceConfigurer))
                .map((loadHandler) -> loadHandler.apply(typePropertyMap.get(typeName), itemKey))
                .map(i -> i.get());
    }

    public void saveItem(Item item) {
        onSaveHandlers.forEach(handler -> handler.accept(item));
        itemStorage.computeIfAbsent(item.getType(), (key) -> new HashMap<>()).put(item.getKey(), item);
    }

    public Platform configurePersistence(PersistenceConfigurer persistenceConfigurer) {
        persistenceConfigurer.configurePersistence(this);
        return this;
    }

    @Override
    public void configurePersistence(Platform platform) {
        addLoadHandler(this, (type, id) -> loadItem(type, id));
    }

    private Optional<Item> loadItem(Type type, String id) {
        return ofNullable(itemStorage.get(type)).map((i) -> i.get(id));
    }

    public void addLoadHandler(PersistenceConfigurer key, BiFunction<Type, String, Optional<Item>> loadHandler) {
        loadHandlers.put(key, loadHandler);
    }

    public void setDefaultPersistence(PersistenceConfigurer defaultPersistence) {
        this.defaultPersistence = defaultPersistence;
    }

    public class TypeBuilder {
        private final String name;
        private final Map<String, Class<?>> propertyTypes;

        public TypeBuilder(String name) {
            this.name = name;
            this.propertyTypes = new HashMap<>();
            this.propertyTypes.put(Item.KEY, String.class);
            this.propertyTypes.put(Item.TYPE, Type.class);
        }

        public TypeBuilder(String name, Map<String, Class<?>> propertyTypes) {
            this(name);
            this.propertyTypes.putAll(propertyTypes);
        }

        public TypeBuilder addProperty(String propertyName, Class<?> aClass) {
            propertyTypes.put(propertyName, aClass);
            return new TypeBuilder(this.name, propertyTypes);
        }

        public Platform register() {
            typePropertyMap.computeIfPresent(name, (key, value) -> {
                throw new IllegalArgumentException("Type " + key + " already defined.");
            });
            apply();
            Platform.this.onTypeRegisteredHandlers.forEach(handler -> handler.accept(typePropertyMap.get(name)));
            return Platform.this;
        }

        public Platform apply() {
            typePropertyMap.put(name, new Type(
                    propertyTypes,
                    name,
                    getPropertyAssignmentValidator(name),
                    Platform.this::saveItem
            ));
            return Platform.this;
        }
    }

}
