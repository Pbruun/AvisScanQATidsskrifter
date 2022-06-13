package dk.kb.kula190.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class DaoUtils {
    static int setNullable(PreparedStatement ps, int param, String value) throws SQLException {
        if (value == null) {
            ps.setNull(param++, Types.VARCHAR);
        } else {
            ps.setString(param++, value);
        }
        return param;
    }
    
    static int setNullable(PreparedStatement ps, int param, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(param++, Types.INTEGER);
        } else {
            ps.setInt(param++, value);
        }
        return param;
    }
    
    static int setNullable(PreparedStatement ps, int param, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(param++, Types.DATE);
        } else {
            ps.setDate(param++, Date.valueOf(value));
        }
        return param;
    }
    
    public static LocalDate nullableDate(String date) {
        date = nullable(date);
        if (date != null) {
            return LocalDate.parse(date);
        }
        return null;
    }
    
    public static Integer nullableInteger(String integer) {
        integer = nullable(integer);
        if (integer != null) {
            return Integer.parseInt(integer);
        }
        return null;
    }
    
    public static String nullable(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("null")) {
            return null;
        }
        return value;
    }
    
    static Optional<Integer> nullableInt(int integer) {
        if (integer == 0){
            return Optional.empty();
        } else {
            return Optional.of(integer);
        }
    }
    
    static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return OffsetDateTime.ofInstant(timestamp.toInstant(),
                                        ZoneId.systemDefault());
    }
    
    static Integer readNullableInt(ResultSet res, String column) throws SQLException {
        Integer page_number = res.getInt(column);
        if (res.wasNull()){
            page_number = null;
        }
        return page_number;
    }
    
    static LocalDate readNullableDate(ResultSet res, String column) throws SQLException {
        return Optional.ofNullable(res.getDate(column)).map(Date::toLocalDate).orElse(null);
    }
}
