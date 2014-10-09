package platform.JdbcPlatform;

import platform.Item;
import platform.PersistenceConfigurer;
import platform.Platform;
import platform.Type;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

/**
 * Created by Michael on 08/10/2014.
 */
public class JdbcConfigurer implements PersistenceConfigurer {
    private final Connection connection;

    public JdbcConfigurer(Connection connection) {
        this.connection = connection;
    }

    private void saveHandler(Item item) {
        // TODO: remove SQL injection posibility
        executeSql(call(connection::createStatement)::execute, String.format(
                "INSERT INTO %s SET %s",
                quote(item.getTypeName()),
                join(",", item.getProperties().entrySet().stream()
                        .filter((entry) -> !entry.getKey().startsWith("_"))
                        .map(this::toSetPropertyStatement)
                        .collect(toList()))
        ));
    }

    public String toSetPropertyStatement(Map.Entry<String, ?> entry) {
        return quote(entry.getKey()) + "=" + singleQuote(String.valueOf(entry.getValue()));
    }

    private void createTable(Type type) {
        executeSql(call(connection::createStatement)::execute, String.format(
                "CREATE TABLE %s (%s)",
                quote(type.getName()),
                join(",", type.getPropertyClasses().keySet().stream()
                        .filter((key) -> !key.startsWith("_"))
                        .map((property) -> quote(property) + " VARCHAR")
                        .collect(toList()))
        ));
    }

    private static String singleQuote(String text) {
        return "'" + text + "'";
    }

    private static String quote(String text) {
        return "\"" + text + "\"";
    }


    @Override
    public void configurePersistence(Platform platform) {
        platform.addOnTypeRegistered(this::createTable);
        platform.addOnSaveHandler(this::saveHandler);
        platform.addLoadHandler(this, this::loadHandler);
    }

    private Optional<Item> loadHandler(Type type, String itemKey) {
        List<Map.Entry<String, Class<?>>> selectedProperties = type.getPropertyClasses().entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("_"))
                .collect(toList());
        ResultSet resultSet = executeSql(call(connection::createStatement)::executeQuery, String.format(
                "SELECT %s FROM %s",
                getPropertiesList(selectedProperties),
                quote(type.getName())
        ));
        if (scroll(resultSet::first)) {
            if (!scroll(resultSet::isLast)) {
                throw new IllegalStateException("More than one row found for " + type.getName() + " with key " + itemKey);
            }
            Item item = type.createItem();
            selectedProperties.stream().forEach(
                    property -> item.set(property.getKey(), getValue(resultSet, property.getKey(), property.getValue()))
            );
            return Optional.of(item);
        }
        return Optional.empty();
    }

    private String getPropertiesList(List<Map.Entry<String, Class<?>>> selectedProperties) {
        return String.join(", ", selectedProperties.stream()
                .map(entry -> entry.getKey())
                .map(JdbcConfigurer::quote)
                .collect(toList()));
    }

    private interface ExecuteAction<T> {
        T execute(String sql) throws SQLException;
    }

    private interface Action<T> {
        T call() throws SQLException;
    }

    private <T> T execute(ExecuteAction<T> executeAction, String sql) {
        return call(() -> executeAction.execute(sql));
    }

    private <T> T executeSql(ExecuteAction<T> executeAction, String sql) {
        System.out.println(sql);
        return call(() -> executeAction.execute(sql));
    }

    private boolean scroll(Action<Boolean> action) {
        return call(() -> action.call());
    }

    private <T> T call(Action<T> action) {
        try {
            return action.call();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }


    private Object getValue(ResultSet resultSet, String name, Class<?> propertyClass) {
        Object resultSetString = execute(resultSet::getString, name);
        if (!propertyClass.isAssignableFrom(String.class)) {
            throw new IllegalArgumentException("Can't convert properties yet: " + name + " to " + propertyClass);
        }
        return resultSetString;
    }
}
