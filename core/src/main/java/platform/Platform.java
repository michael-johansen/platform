package platform;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * User: Michael Johansen
 * Date: 01.10.2014
 * Time: 18:19
 */
public class Platform {
    private Map<String, Map<String, Class<?>>> typePropertyMap = new HashMap<>();

    public Platform.TypeBuilder addType(String name) {
        return new TypeBuilder(name);
    }

    public Item createItem(String name) {
        if(!typePropertyMap.containsKey(name)){
            throw new IllegalArgumentException("Item " + name +" is not defined");
        }
        return new Item((propertyName, value) -> {
            if (typePropertyMap.containsKey(name)) {
                Map<String, Class<?>> propertyMap = typePropertyMap.get(name);
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
                    throw  new IllegalArgumentException(String.format(
                            "Undefined property %s on type %s",
                            propertyName,
                            name
                    ));
                }
            }
        });
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
            typePropertyMap.put(name, unmodifiableMap(propertyTypes));
            return Platform.this;
        }
    }
}
