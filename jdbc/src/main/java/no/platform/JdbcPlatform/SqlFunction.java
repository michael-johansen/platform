package no.platform.JdbcPlatform;

import java.sql.SQLException;

/**
* Created by Michael on 11/10/2014.
*/
public interface SqlFunction<S, T> {
    T apply(S s) throws SQLException;
}
