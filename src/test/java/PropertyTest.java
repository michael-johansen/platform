import no.platform.Item;
import no.platform.Platform;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

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
        Item person = createPerson("firstName", "lastName");
        assertThat(person, is(notNullValue()));
        // assert PropertyInformation
    }

    @Test
    public void defineProperties() throws Exception {
        Item person = createPerson("firstName", "lastName").set("firstName", "Donald").set("lastName", "Duck");

        assertThat(person.get("firstName"), is("Donald"));
        assertThat(person.get("lastName"), is("Duck"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePropertyValueTypeOnAssignment() throws Exception {
        createPerson("firstName", "lastName").set("firstName", 18);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantAssignToUndefinedProperty() throws Exception {
        createPerson("firstName", "lastName").set("age", 18);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantCreateUnregisteredType() throws Exception {
        platform.addType("Person");
        platform.createItem("Person");
    }

    private Item createPerson(String... properties) {
        Platform.TypeBuilder typeBuilder = platform.addType("Person");
        asList(properties).forEach((property) -> typeBuilder.addProperty(property, String.class));
        typeBuilder.register();

        return platform.createItem("Person");
    }
}
