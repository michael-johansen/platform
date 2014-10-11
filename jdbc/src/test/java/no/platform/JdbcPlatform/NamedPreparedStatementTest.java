package no.platform.JdbcPlatform;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class NamedPreparedStatementTest {

    private Connection connection;


    @Before
    public void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:~/test;AUTO_SERVER=TRUE");
        connection.createStatement().execute("DROP ALL OBJECTS");
        connection.createStatement().execute("CREATE TABLE \"Person\" (\"name\" VARCHAR, \"key\" VARCHAR)");
        connection.createStatement().execute("INSERT INTO \"Person\" SET \"name\"='John', \"key\"='1'");
        connection.createStatement().execute("INSERT INTO \"Person\" SET \"name\"='Mary', \"key\"='2'");
    }

    @Test
    public void canFindJohn() throws Exception {
        NamedPreparedStatement statement = new NamedPreparedStatement(connection, "SELECT * FROM \"Person\" WHERE \"key\"=:key AND \"name\"=:name");
        statement.setString("key", "1");
        statement.setString("name", "John");

        List<String> names = statement.executeQuery(rs -> rs.getString("name"));

        assertThat(names, is(notNullValue()));
        assertThat(names.size(), is(1));
        assertThat(names.iterator().next(), is("John"));
    }
    @Test
    public void canListAll() throws Exception {
        NamedPreparedStatement statement = new NamedPreparedStatement(connection, "SELECT * FROM \"Person\" ORDER BY \"key\" ASC");

        List<String> names = statement.executeQuery(rs -> rs.getString("name"));

        assertThat(names, is(notNullValue()));
        assertThat(names.size(), is(2));
        Iterator<String> iterator = names.iterator();
        assertThat(iterator.next(), is("John"));
        assertThat(iterator.next(), is("Mary"));
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }
}