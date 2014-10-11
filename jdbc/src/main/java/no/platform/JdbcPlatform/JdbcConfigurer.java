package no.platform.JdbcPlatform;

import no.platform.Item;
import no.platform.PersistenceConfigurer;
import no.platform.Platform;
import no.platform.Type;

import java.sql.*;
import java.util.*;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static no.platform.JdbcPlatform.FunctionalSqlUtils.*;

/**
 * Created by Michael on 08/10/2014.
 */
public class JdbcConfigurer implements PersistenceConfigurer {
    public static final String PROPERTY_DELIMETER = ", ";
    private final Connection connection;

    public JdbcConfigurer(Connection connection) {
        this.connection = connection;
    }

    private void saveHandler(Item item) {
        String sql = String.format(
                "INSERT INTO %s SET %s",
                quote(item.getTypeName()),
                join(PROPERTY_DELIMETER, item.getProperties().entrySet().stream()
                        .map(this::toSetPropertyStatement)
                        .collect(toList()))
        );

        NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection, sql);
        Iterator<? extends Map.Entry<String, ?>> iterator = item.getProperties().entrySet().iterator();
        for (int i = 1; i <= item.getProperties().size(); i++) {
            Map.Entry<String, ?> entry = iterator.next();
            namedPreparedStatement.setString(entry.getKey(), String.valueOf(entry.getValue()));
        }
        namedPreparedStatement.execute();
    }

    public String toSetPropertyStatement(Map.Entry<String, ?> entry) {
        return quote(entry.getKey()) + "=" + ":" + entry.getKey();
    }

    private void createTable(Type type) {
        acceptSql(get(connection::createStatement)::execute, String.format(
                "CREATE TABLE %s (%s)",
                quote(type.getName()),
                join(PROPERTY_DELIMETER, type.getPropertyClasses().keySet().stream()
                        .filter((key) -> !key.startsWith("_"))
                        .map((property) -> quote(property) + " VARCHAR")
                        .collect(toList()))
        ));
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
        ResultSet resultSet = acceptSql(get(connection::createStatement)::executeQuery, String.format(
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
        return String.join(PROPERTY_DELIMETER, selectedProperties.stream()
                .map(entry -> entry.getKey())
                .map(JdbcConfigurer::quote)
                .collect(toList()));
    }


    private Object getValue(ResultSet resultSet, String name, Class<?> propertyClass) {
        Object resultSetString = accept(resultSet::getString, name);
        if (!propertyClass.isAssignableFrom(String.class)) {
            throw new IllegalArgumentException("Can't convert properties yet: " + name + " to " + propertyClass);
        }
        return resultSetString;
    }
}
