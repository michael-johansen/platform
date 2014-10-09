import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import platform.Item;
import platform.JdbcPlatform.JdbcConfigurer;
import platform.Platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Michael on 08/10/2014.
 */
public class JdbcTest {


    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:");
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testName() throws Exception {
        JdbcConfigurer persistenceConfigurer = new JdbcConfigurer(connection);

        Platform platformA = new Platform();
        platformA.configurePersistence(persistenceConfigurer);
        platformA.setDefaultPersistence(persistenceConfigurer);
        platformA.addType("Person").addProperty("name", String.class).register();

        Platform platformB = new Platform();
        platformB.configurePersistence(persistenceConfigurer);
        platformB.setDefaultPersistence(persistenceConfigurer);
        platformB.addType("Person").addProperty("name", String.class).apply();

        Item save = platformA.createItem("Person").set("name", "SomeName").save();
        Optional<Item> loadItem = platformB.loadItem("Person", save.getKey());

        assertThat("loadItem.isPresent() is(true)", loadItem.isPresent(), is(true));
        assertThat(loadItem.get().get("name"), is(save.<String>get("name")));
    }
}
