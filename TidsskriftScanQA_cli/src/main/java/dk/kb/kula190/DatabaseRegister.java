package dk.kb.kula190;

import dk.kb.kula190.generated.Failure;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.generated.Reference;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedParsingEvent;
import dk.kb.util.json.JSON;
import dk.kb.util.yaml.YAML;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

//Not multithreaded...
public class DatabaseRegister extends DecoratedEventHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Driver jdbcDriver;
    private final String jdbcURL;
    private final String jdbcUser;
    private final String jdbcPassword;
    private final String initialBatchState;
    private final String finishedBatchState;
    private final String acknowledgmentFile;
    private final List<Failure> checkerFailures;

    private final List<Failure> registeredFailures;
    private BasicDataSource dataSource;

    public DatabaseRegister(ResultCollector resultCollector,
                            Driver jdbcDriver,
                            String jdbcURL,
                            String jdbcUser,
                            String jdbcPassword,
                            String initialBatchState,
                            String finishedBatchState,
                            String acknowledgmentFile,
                            List<Failure> checkerFailures) {
        super(resultCollector);
        this.jdbcDriver = jdbcDriver;
        this.jdbcURL = jdbcURL;
        this.jdbcUser = jdbcUser;
        this.jdbcPassword = jdbcPassword;
        this.acknowledgmentFile = acknowledgmentFile;
        this.checkerFailures = checkerFailures;
        this.registeredFailures = new ArrayList<>();
        this.initialBatchState = initialBatchState;
        this.finishedBatchState = finishedBatchState;
    }

    @Override
    public void batchBegins(DecoratedNodeParsingEvent event,
                            String newspaper,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) throws IOException {
        dataSource = new BasicDataSource();

        if (jdbcUser != null) {
            dataSource.setUsername(jdbcUser);
        }

        if (jdbcPassword != null) {
            dataSource.setPassword(jdbcPassword);
        }
        dataSource.setUrl(jdbcURL);

        dataSource.setDefaultReadOnly(false);
        dataSource.setDefaultAutoCommit(false);

        dataSource.setRemoveAbandonedTimeout(60); // 60 sec
        dataSource.setMaxWaitMillis(60000); // 1 min
        dataSource.setMaxTotal(2); // Change to 10 when running as WAR

        dataSource.setDriver(jdbcDriver);


        try (Connection connection = dataSource.getConnection()) {
            updateBatchState(newspaper, roundTrip, startDate, endDate, connection, initialBatchState, 0, null);
        } catch (SQLException e) {
            throw new IOException("Failure in registering batch state "
                                  + initialBatchState
                                  + " for batch "
                                  + batchName.get(), e);
        }

        Path ackFile = Paths.get(event.getLocation(), acknowledgmentFile);
        log.debug("Finished handling of batch {} so writing acknowledgmentFile {}", batchName.get(), ackFile);
        FileUtils.touch(ackFile.toFile());
        log.info("Finished handling of batch {} so wrote acknowledgmentFile {}", batchName.get(), ackFile);
    }

    private void updateBatchState(String newspaper,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate,
                                  Connection connection,
                                  String state,
                                  int numProblems,
                                  String failureMessage) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                                                                               INSERT INTO
                                                                                    batch(batchid,
                                                                                          roundtrip,
                                                                                          start_date,
                                                                                          end_date,
                                                                                          delivery_date,
                                                                                          problems,
                                                                                          state,
                                                                                          num_problems,
                                                                                          username,
                                                                                          lastmodified)
                                                                               VALUES (?,?,?,?,?,?,?,?,?,now())
                                                                               """)) {
            int param = 1;
            //Batch ID
            preparedStatement.setString(param++, batchName.get());
            //Avis ID
//            preparedStatement.setString(param++, newspaper);
            // roundtrip
            preparedStatement.setInt(param++, Integer.parseInt(roundTrip));
            //start date
            preparedStatement.setDate(param++, Date.valueOf(startDate));
            //end date
            preparedStatement.setDate(param++, Date.valueOf(endDate));
            //delivery date
            preparedStatement.setDate(param++, Date.valueOf(LocalDate.now()));
            //failureMessage
            preparedStatement.setString(param++, Optional.ofNullable(failureMessage).orElse(""));
            //state
            preparedStatement.setString(param++, state);
            //numProblems
            preparedStatement.setInt(param++, numProblems);
            //Username
            preparedStatement.setString(param++, System.getenv("USER"));

            boolean result = preparedStatement.execute();
        }
        connection.commit();
    }

    @Override
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String newspaper,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) throws IOException {
        newspaperFolder(event, newspaper);
        try (Connection connection = dataSource.getConnection()) {

            List<Failure> batchFailures = new ArrayList<>(checkerFailures);
            batchFailures.removeAll(registeredFailures);


            String failuresMessage = orEmpty(batchFailures);

            updateBatchState(newspaper,
                             roundTrip,
                             startDate,
                             endDate,
                             connection,
                             finishedBatchState,
                             checkerFailures.size(),
                             failuresMessage);

        } catch (SQLException e) {
            throw new IOException("Failure in registering batch state "
                                  + finishedBatchState
                                  + " for batch "
                                  + batchName.get(), e);
        } finally {
            try {
                if (dataSource != null) {
                    dataSource.close();
                }
            } catch (Exception e) {
                // ignore errors during shutdown, we cant do anything about it anyway
                log.error("shutdown failed", e);
            }
        }
    }

    private String orEmpty(List<?> batchFailures) {
        if (batchFailures.isEmpty()) {
            return "";
        }
        return JSON.toJson(batchFailures, true);
    }

    private boolean matchThisPage(Reference reference, DecoratedParsingEvent event) {
        return Objects.equals(event.getAvis(), reference.getAvis())
               && Objects.equals(event.getEditionDate().toString(),
                                 reference.getEditionDate())
               && Objects.equals(event.getUdgave(), reference.getUdgave())
               && Objects.equals(event.getSectionName(), reference.getSectionName())
               && Objects.equals(event.getPageNumber(), reference.getPageNumber());

    }

    @Override
    public void pdfFile(DecoratedAttributeParsingEvent event,
                        String newspaper,
                        LocalDate editionDate,
                        String edition,
                        String section,
                        Integer pageNumber) throws IOException {
        List<Failure> failuresForThisPage = checkerFailures.stream()
                                                           .filter(failure -> matchThisPage(failure.getReference(),
                                                                                            event))
                                                           .toList();
        registeredFailures.addAll(failuresForThisPage);

        String failuresMessage = orEmpty(failuresForThisPage);

        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO newspaperarchive(orig_relpath, format_type, edition_date, single_page, page_number, "
                    + "avisid, avistitle, shadow_path, section_title, edition_title, delivery_date, side_label, "
                    + "fraktur, problems, batchid) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                    + "ON CONFLICT (orig_relpath) DO UPDATE SET problems = excluded.problems")) {
                int param = 1;
                //orig_relpath
                preparedStatement.setString(param++,
                                            event.getLocation().substring(event.getLocation().indexOf(newspaper)));
                //format_type
                preparedStatement.setString(param++, "pdf");
                //edition_date
                preparedStatement.setDate(param++, Date.valueOf(editionDate));
                //single_page
                preparedStatement.setBoolean(param++, true);
                //page_numer
                preparedStatement.setInt(param++, pageNumber);
                //avis_id
                preparedStatement.setString(param++, newspaper);
                //avis_title,
                preparedStatement.setString(param++, CaseUtils.toCamelCase(newspaper, true));
                //shadow_path
                preparedStatement.setString(param++, event.getName());
                //section_title
                preparedStatement.setString(param++, section);
                //edition_title
                preparedStatement.setString(param++, edition);
                //delivery_date
                preparedStatement.setDate(param++, new Date(new File(event.getLocation()).lastModified()));
                //side_label
                preparedStatement.setString(param++, "");
                //fraktur
                preparedStatement.setBoolean(param++, true);

                //problems
                preparedStatement.setString(param++, failuresMessage);

                //Batch-reference
                preparedStatement.setString(param++, batchName.get());

                boolean result = preparedStatement.execute();
            }
            connection.commit();

        } catch (SQLException e) {
            throw new IOException("Failure in registering page " + event + " for batch " + batchName.get(), e);
        }
    }

    @Override
    public void newspaperFolder(DecoratedNodeParsingEvent event,
                                String avis) throws IOException {
        File path = new File(event.getLocation());
        File[] newspapers = path.listFiles((dir, name) -> new File(dir, name).isDirectory());
        if (newspapers != null) {
            for (File newspaper : newspapers) {
                File[] files = newspaper.listFiles((dir, name) -> new File(dir, name).isDirectory());
                assert files != null;
                long newspaperSize = files[0].length();
                DayOfWeek dayOfWeek = DayOfWeek.from(event.getEditionDate());
                if (newspaperSize > 0) {

                    int[] daysOfWeek = {0, 0, 0, 0, 0, 0, 0};
                    daysOfWeek[dayOfWeek.getValue() - 1] += 1;
                    try (Connection connection = dataSource.getConnection()) {

                        try (PreparedStatement preparedStatement = connection.prepareStatement(
                                """
                                INSERT INTO deliverypattern (avisid,monday,tuesday,wednesday,thursday,friday,saturday,sunday,batchcount) 
                                VALUES(?,?,?,?,?,?,?,?,1)
                                ON CONFLICT (avisid) 
                                DO 
                                UPDATE  
                                SET monday = deliverypattern.monday + ?,tuesday = deliverypattern.tuesday + ?, wednesday = deliverypattern.wednesday + ?, thursday = deliverypattern.thursday + ?, friday = deliverypattern.friday + ?, saturday = deliverypattern.saturday + ?, sunday = deliverypattern.sunday +?, batchcount = deliverypattern.batchcount + 1
                                WHERE deliverypattern.avisid = ?
                                """)) {
                            int param = 1;
                            //orig_relpath
                            preparedStatement.setString(param++, newspaper.getName());
                            for (int j : daysOfWeek) {
                                preparedStatement.setInt(param++, j);
                            }

                            for (int j : daysOfWeek) {
                                preparedStatement.setInt(param++, j);
                            }
                            preparedStatement.setString(param++, newspaper.getName());
                            preparedStatement.execute();

                        }
                        connection.commit();
                        checkDeliveryFrequency(event, newspaper.getName(), dayOfWeek, true);
                    } catch (SQLException e) {
                        throw new IOException("Failure in registering page " +
                                              event +
                                              " for batch " +
                                              newspaper.getName(), e);
                    }


                } else {
                    checkDeliveryFrequency(event, newspaper.getName(), dayOfWeek, false);
                }
            }


        }
        checkExpectedDeliveries(event, newspapers);


    }

    private void checkDeliveryFrequency(DecoratedNodeParsingEvent event,
                                        String newspaper, DayOfWeek date, Boolean hasPages) {
        Double frequency;
        int batchCount;
        int day;
        try (Connection connection = dataSource.getConnection()) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    SELECT * FROM deliverypattern WHERE avisid = ?
                    """)) {
                int param = 1;
                preparedStatement.setString(param++, newspaper);
                try (ResultSet res = preparedStatement.executeQuery()) {
                    while (res.next()) {
                        day = res.getInt(date.getValue() + 1);
                        batchCount = res.getInt("batchcount");

                        if (batchCount > 10) {
                            frequency = day / (double) batchCount;
                            if (!hasPages) {
                                checkWithinRange(event,
                                                 FailureType.DELIVERY_PATTERN_ERROR,
                                                 frequency,
                                                 0.0,
                                                 0.7,
                                                 "Newspaper usually has a release on this date");
                            } else {
                                checkAtLeast(event,
                                             FailureType.DELIVERY_PATTERN_ERROR,
                                             frequency,
                                             0.12,
                                             "Newspaper release was outside the standard delivery pattern.");
                            }

                        }
                    }

                }


            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    private void checkExpectedDeliveries(DecoratedNodeParsingEvent event, File[] newspapers) {
        try {
            File filepath = new File(Thread.currentThread()
                                               .getContextClassLoader()
                                               .getResource("ExpectedDelivery.yaml")
                                               .toURI());
            YAML expectedNewspapers = YAML.resolveLayeredConfigs(String.valueOf(filepath));
            List<String> xmlNewspapers = expectedNewspapers.getList("newspapers");
            List<String> newspapersList = new ArrayList<>(Arrays.stream(newspapers).map(File::getName).toList());
            checkTrue(event, FailureType.DELIVERY_PATTERN_ERROR, "Batch do not contain expected newspapers",
                      newspapersList.containsAll(xmlNewspapers));
            newspapersList.removeAll(xmlNewspapers);
            checkTrue(event,FailureType.DELIVERY_PATTERN_ERROR,"Batch contained more newspapers than expected"+newspapersList.toArray(),newspapersList.isEmpty());

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
