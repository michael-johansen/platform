package platform;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
* Created by Michael on 06/10/2014.
*/
public class Type {
    private final Map<String, Class<?>> propertyClasses;

    public Type(Map<String, Class<?>> propertyClasses) {
        this.propertyClasses = unmodifiableMap(propertyClasses);
    }

    public Map<String, Class<?>> getPropertyClasses() {
        return propertyClasses;
    }
}
