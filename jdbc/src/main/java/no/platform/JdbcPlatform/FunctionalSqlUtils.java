package no.platform.JdbcPlatform;

import java.sql.SQLException;

/**
 * Created by Michael on 11/10/2014.
 */
public class FunctionalSqlUtils {
    public static <S, T> T accept(SqlFunction<S, T> sqlFunction, S s) {
        return get(() -> sqlFunction.apply(s));
    }

    public static <R, S> void apply(SqlBiConsumer<R, S> sqlFunction, S s, R r) {
        consume((in) -> sqlFunction.accept(in, r), s);
    }

    public static <S, T> T acceptSql(SqlFunction<S, T> sqlFunction, S s) {
        System.out.println(s);
        return accept(sqlFunction, s);
    }

    public static boolean scroll(SqlSupplier<Boolean> sqlSupplier) {
        return get(() -> sqlSupplier.get());
    }

    public static <T> void consume(SqlConsumer<T> sqlConsumer, T t) {
        get(() -> {
            sqlConsumer.accept(t);
            return null;
        });
    }

    public static <T> T get(SqlSupplier<T> sqlSupplier) {
        try {
            return sqlSupplier.get();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
