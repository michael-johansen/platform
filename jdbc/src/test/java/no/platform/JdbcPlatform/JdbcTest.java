package no.platform.JdbcPlatform;

import no.platform.Item;
import no.platform.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * This software is written by Michael on 08/10/2014.
 */
public class JdbcTest {


    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:~/test;AUTO_SERVER=TRUE");
        connection.createStatement().execute("DROP ALL OBJECTS");
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void canSaveAndLoadItem() throws Exception {
        JdbcStorage storage = new JdbcStorage(connection);

        Platform platformA = new Platform();
        platformA.configureStorage(storage);
        platformA.setDefaultStorage(storage);
        platformA.addType("Person").addProperty("name", String.class).register();

        Platform platformB = new Platform();
        platformB.configureStorage(storage);
        platformB.setDefaultStorage(storage);
        platformB.addType("Person").addProperty("name", String.class).apply();

        Item save = platformA.createItem("Person").set("name", "SomeName").save();
        Optional<Item> loadItem = platformB.loadItem("Person", save.getKey());

        assertThat("loadItem.isPresent() is(true)", loadItem.isPresent(), is(true));
        assertThat(loadItem.get().get("name"), is(save.<String>get("name")));
    }
}
