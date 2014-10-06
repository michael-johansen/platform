package platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * User: Michael Johansen
 * Date: 01.10.2014
 * Time: 18:19
 */
public class Platform {
    private Map<String, Type> typePropertyMap = new HashMap<>();
    private List<Consumer<Type>> onTypeRegisteredHandlers = new ArrayList<>();

    public Platform.TypeBuilder addType(String name) {
        return new TypeBuilder(name);
    }

    public Item createItem(String name) {
        if (!typePropertyMap.containsKey(name)) {
            throw new IllegalArgumentException("Item " + name + " is not defined");
        }
        return new Item((propertyName, value) -> {
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
        });
    }

    public void addOnTypeRegistered(Consumer<Type> consumer) {
        onTypeRegisteredHandlers.add(consumer);
    }

    public class TypeBuilder {
        private final String name;
        private final Map<String, Class<?>> propertyTypes;

        public TypeBuilder(String name) {
            this.name = name;
            this.propertyTypes = new HashMap<>();
        }

        public TypeBuilder(String name, Map<String, Class<?>> propertyTypes) {
            this(name);
            propertyTypes.putAll(propertyTypes);
        }

        public TypeBuilder addProperty(String propertyName, Class<?> aClass) {
            propertyTypes.put(propertyName, aClass);
            return new TypeBuilder(this.name, propertyTypes);
        }

        public Platform register() {
            Type type = new Type(propertyTypes);
            typePropertyMap.put(name, type);
            Platform.this.onTypeRegisteredHandlers.forEach(handler -> handler.accept(type));
            return Platform.this;
        }
    }

}
