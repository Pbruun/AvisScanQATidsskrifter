package dk.kb.kula190.dao;

import dk.kb.kula190.model.Batch;
import dk.kb.kula190.model.SlimBatch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static dk.kb.kula190.dao.DaoUtils.toOffsetDateTime;

public class DaoBatchHelper {
    static List<SlimBatch> getLatestBatches(String avisID, Connection conn) throws SQLException {
        List<SlimBatch> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select *, na.avisid "
                + "from batch b, newspaperarchive na "
                + "where b.batchid = na.batchid AND na.avisid = ? AND "
                + "b.lastmodified >= ALL(SELECT lastmodified FROM batch WHERE batchid=b.batchid)")) {
            ps.setString(1, avisID);

            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    final SlimBatch e = readSlimBatch(conn, res);
                    results.add(e);
                }
            }
            return results;
        }
    }

    /**
     * Gets (distinct) states for all batches for the given avisID.
     * Used to figure out if all batches (of an avisID) is "finished" or some are still in progress
     * @param avisID the avis id to check for
     * @param conn the connection
     * @return List of distinct states for the batches of an avis ID
     * @throws SQLException
     */
    static List<String> getBatchStatesForAvisID(String avisID, Connection conn) throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                """
                SELECT DISTINCT(state)
                FROM batch b
                WHERE b.lastmodified >= ALL(SELECT lastmodified FROM batch WHERE batchid=b.batchid)
                """)) {
            //ps.setString(1, avisID);

            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    String e = res.getString("state");
                    results.add(e);
                }
            }
            return results;
        }
    }
    static LocalDate getLatestDeliveryDate(String avisID, Connection conn) throws SQLException {
        String result = null;
        try (PreparedStatement ps = conn.prepareStatement(
                """
                SELECT MAX(delivery_date) AS dDate
                FROM batch
                """)) {
            //ps.setString(1, avisID);

            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    result = res.getString("dDate");
                }
            }
            return LocalDate.parse(result);
        }
    }


    static Batch getLatestBatch(String batchID, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select *, na.avisid"
                + "from batch b, newspaperarchive na "
                + "where batchid = ? AND b.batchid = na.batchid and "
                + "      b.lastmodified >= ALL(SELECT lastmodified FROM batch WHERE batchid=b.batchid) limit 1")) {
            ps.setString(1, batchID);

            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return readBatch(conn, res);
                }
            }
        }
        return null;
    }


    static SlimBatch getLatestSlimBatch(String batchID, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select * "
                + "from batch b "
                + "where batchid = ? and "
                + "      b.lastmodified >= ALL(SELECT lastmodified FROM batch WHERE batchid=b.batchid) limit 1" +
                " ")) {
            ps.setString(1, batchID);

            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return readSlimBatch(conn, res);
                }
            }
        }
        return null;
    }
    static SlimBatch getLatestSlimBatch(String batchID, String newspaperID, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select *, na.avisid "
                + "from batch b, newspaperarchive na "
                + "where b.batchid = ? AND na.avisid = ? and "
                + "      b.lastmodified >= ALL(SELECT lastmodified FROM batch WHERE batchid=b.batchid) limit 1")) {
            ps.setString(1, batchID);
            ps.setString(2, newspaperID);

            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return readSlimBatch(conn, res);
                }
            }
        }
        return null;
    }
    static List<SlimBatch> getAllBatches(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT *, na.avisid "
                                                          +
                                                          " FROM batch b1, newspaperarchive na"
                                                          +
                                                          " WHERE lastmodified >= ALL(SELECT lastmodified FROM batch " +
                                                          "b2 WHERE b1.batchid = b2.batchid)" +
                                                          " AND b1.batchid = na.batchid")) {
            List<SlimBatch> list = new ArrayList<>();
            try (ResultSet res = ps.executeQuery()) {

                while (res.next()) {
                    list.add(readSlimBatch(conn, res));
                }

            }
            return list;
        }
    }

    static void setBatchState(String batchID, String state, String username, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO batch(batchid, roundtrip, start_date, end_date, delivery_date, problems, state, " +
                "num_problems, username, lastmodified)  "
                +
                " ( SELECT batchid, roundtrip, start_date, end_date, delivery_date, problems, ?, " +
                "num_problems, ?, now() "
                +
                " FROM batch "
                +
                " WHERE batchid=? AND "
                +
                " lastmodified >= ALL("
                +
                "     SELECT lastmodified FROM batch WHERE batchid=?)"
                +
                "      )")) {
            int param = 1;
            ps.setString(param++, state);
            ps.setString(param++, username);
            ps.setString(param++, batchID);
            ps.setString(param++, batchID);
            ps.execute();
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        }
    }


    private static Batch readBatch(Connection conn, ResultSet res) throws SQLException {
        String batchID = res.getString("batchid");
        return new Batch().batchid(batchID)
                          .avisid(res.getString("avisid"))
                          .roundtrip(res.getInt("roundtrip"))

                          .notes(DaoNoteHelper.getBatchLevelNotes(batchID, conn))
                          .numNotes(DaoNoteHelper.getNumNotes(batchID, conn))

                          .startDate(res.getDate("start_date").toLocalDate())
                          .endDate(res.getDate("end_date").toLocalDate())
                          .deliveryDate(res.getDate("delivery_date").toLocalDate())

                          .problems(res.getString("problems"))
                          .numProblems(res.getInt("num_problems"))

                          .state(res.getString("state"))
                          .username(res.getString("username"))
                          .lastModified(toOffsetDateTime(res.getTimestamp("lastmodified")));
    }


    private static SlimBatch readSlimBatch(Connection conn, ResultSet res) throws SQLException {
        String batchID = res.getString("batchid");
        return new SlimBatch().batchid(batchID)
                              .avisid(res.getString("avisid"))
                              .roundtrip(res.getInt("roundtrip"))

                              // .notes(DaoNoteHelper.getBatchLevelNotes(batchID,conn))
                              .numNotes(DaoNoteHelper.getNumNotes(batchID, conn))

                              .startDate(res.getDate("start_date").toLocalDate())
                              .endDate(res.getDate("end_date").toLocalDate())
                              .deliveryDate(res.getDate("delivery_date").toLocalDate())

                              // .problems(res.getString("problems"))
                              .numProblems(res.getInt("num_problems"))

                              .state(res.getString("state"))
                              .username(res.getString("username"))
                              .lastModified(toOffsetDateTime(res.getTimestamp("lastmodified")));
    }

    static <T extends SlimBatch> Stream<LocalDate> batchDays(T batch) {
        return LongStream.rangeClosed(0, ChronoUnit.DAYS.between(batch.getStartDate(), batch.getEndDate()))
                         .mapToObj((long t) -> batch.getStartDate().plusDays(t));
    }
}
