package no.platform.JdbcPlatform;

import java.sql.SQLException;

/**
* Created by Michael on 11/10/2014.
*/
public interface SqlSupplier<T> {
    T get() throws SQLException;
}
