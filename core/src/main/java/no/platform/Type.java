package no.platform;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
* Created by Michael on 06/10/2014.
*/
public class Type {
    private final Map<String, Class<?>> propertyTypes;
    private final String name;
    private final BiConsumer<String, Object> propertyAssignmentValidator;
    private final Consumer<Item> saveItem;

    public Type(
            Map<String, Class<?>> propertyTypes,
            String name,
            BiConsumer<String, Object> propertyAssignmentValidator,
            Consumer<Item> saveItem) {

        this.propertyTypes = propertyTypes;
        this.name = name;
        this.propertyAssignmentValidator = propertyAssignmentValidator;
        this.saveItem = saveItem;
    }

    public Map<String, Class<?>> getPropertyClasses() {
        return propertyTypes;
    }

    public String getName() {
        return name;
    }

    public Item createItem() {
        return new Item(
                propertyAssignmentValidator,
                saveItem,
                this
        );
    }
}
