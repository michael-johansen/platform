package platform;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * User: Michael Johansen
 * Date: 01.10.2014
 * Time: 18:22
 */
public class Item {
    private Map<String, Object> propertyMap = new HashMap<>();
    private final BiConsumer<String, Object> propertyAssignmentValidator;

    public Item(BiConsumer<String, Object> propertyAssignmentValidator) {
        this.propertyAssignmentValidator = propertyAssignmentValidator;
    }

    public <T> Item set(String name, T value) {
        propertyAssignmentValidator.accept(name, value);
        propertyMap.put(name, value);
        return this;
    }

    public <T> T get(String name) {
        return (T) propertyMap.get(name);
    }
}
