package no.platform.JdbcPlatform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static no.platform.JdbcPlatform.FunctionalSqlUtils.*;

/**
 * Created by Michael on 11/10/2014.
 */
public class NamedPreparedStatement {
    public static final Pattern PATTERN = Pattern.compile(":\\w+");
    public static final String COLUMN_PREFIX = ":";
    private final Connection connection;
    private final Map<String, Integer> namePositionMapping = new HashMap<>();
    private final String transformedSql;
    private final PreparedStatement statement;


    public NamedPreparedStatement(Connection connection, String sql) {
        this.connection = connection;

        Function<String, String> transform = (in) -> {
            int position = 1;
            for (Matcher matcher = PATTERN.matcher(in); matcher.find(); in = matcher.replaceFirst("?"), matcher = PATTERN.matcher(in), position++) {
                namePositionMapping.put(matcher.group(), position);
            }
            return in;
        };
        transformedSql = transform.apply(sql);
        statement = accept(connection::prepareStatement, transformedSql);
    }

    public void setString(String column, String value) {
        System.out.println(column + "<-" + value);
        apply(statement::setString, namePositionMapping.get(COLUMN_PREFIX + column), value);
    }

    public void execute(){
        get(statement::execute);
    }

    public <T> List<T> executeQuery(SqlFunction<ResultSet, T> mapper) {
        List<T> elements = new ArrayList<>();
        ResultSet resultSet = get(statement::executeQuery);
        while (scroll(resultSet::next)) {
            elements.add(accept(mapper, resultSet));
        }
        return elements;
    }
}
