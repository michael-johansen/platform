package no.platform.JdbcPlatform;

import java.sql.SQLException;

/**
* Created by Michael on 11/10/2014.
*/
public interface SqlBiConsumer<R, S> {
    void accept(S s, R r) throws SQLException;
}
