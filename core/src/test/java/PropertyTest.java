import org.junit.Before;
import org.junit.Test;
import no.platform.Item;
import no.platform.Platform;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * User: Michael Johansen
 * Date: 01.10.2014
 * Time: 18:18
 */
public class PropertyTest {

    private Platform platform;

    @Before
    public void setUp() throws Exception {
        platform = new Platform();

    }

    @Test
    public void defineType() throws Exception {
        Item person = createTypeAndItem("Person", "firstName", "lastName");
        assertThat(person, is(notNullValue()));
        // assert PropertyInformation
    }

    @Test
    public void defineProperties() throws Exception {
        Item person = createTypeAndItem("Person", "firstName", "lastName").set("firstName", "Donald").set("lastName", "Duck");

        assertThat(person.get("firstName"), is("Donald"));
        assertThat(person.get("lastName"), is("Duck"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePropertyValueTypeOnAssignment() throws Exception {
        createTypeAndItem("Person", "firstName", "lastName").set("firstName", 18);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantAssignToUndefinedProperty() throws Exception {
        createTypeAndItem("Person", "firstName", "lastName").set("age", 18);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantCreateUnregisteredType() throws Exception {
        platform.addType("Person");
        platform.createItem("Person");
    }

    @Test(expected = CallbackException.class)
    public void onRegisterHandlers() throws Exception {
        platform.addOnTypeRegistered((Type) -> {
            throw new CallbackException();
        });
        createTypeAndItem("Person");
    }

    @Test
    public void persistAndLoad() {
        Item person = createTypeAndItem("Person", "firstName", "lastName").set("firstName", "Donald").set("lastName", "Duck");
        person.save();

        assertThat(person.getKey(), is(notNullValue()));
        Optional<Item> loaded = platform.loadItem("Person", person.getKey());

        assertThat(loaded.isPresent(), is(true));
        assertThat(loaded.get().<String>get("firstName"), is(person.<String>get("firstName")));
        assertThat(loaded.get().<String>get("lastName"), is(person.<String>get("lastName")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsOnDuplicateRegistration() throws Exception {
        createTypeAndItem("Person");
        createTypeAndItem("Person");
    }

    private Item createTypeAndItem(String name, String... properties) {
        Platform.TypeBuilder typeBuilder = platform.addType(name);
        asList(properties).forEach((property) -> typeBuilder.addProperty(property, String.class));
        typeBuilder.register();

        return platform.createItem("Person");
    }

    private static class CallbackException extends RuntimeException {
    }
}
