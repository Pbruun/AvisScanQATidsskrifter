package dk.kb.avischk.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2 {
    
    private static final Logger log = LoggerFactory.getLogger(H2.class);
    private static final String CREATE_TABLES_DDL_FILE = "ddl/avischk-db-schema.sql";
    private static final String CONTENT_DDL_FILE = "ddl/small.sql";
    
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String test_classes_path = new File(Thread.currentThread().getContextClassLoader().getResource("logback-test.xml").getPath()).getParentFile().getAbsolutePath();
    private static final String URL = "jdbc:h2:./h2/avischk-web-qa;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    
    
    
    
    /*
     * Delete database file if it exists. Create database with tables
     */
    
    private static void createEmptyDBFromDDL() throws Exception {
        // Delete if exists
        Utils.doDelete(new File(test_classes_path + "/h2"));
        
        try {
            Class.forName(H2_DRIVER); // load the driver
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
        
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            File ddlFile = Utils.getFile(CREATE_TABLES_DDL_FILE);
            log.info("Running DDL script:" + ddlFile.getAbsolutePath());
            
            if (!ddlFile.exists()) {
                log.error("DDL script not found:" + ddlFile.getAbsolutePath());
                throw new RuntimeException("DDL Script file not found:" + ddlFile.getAbsolutePath());
            }
            
            connection.createStatement().execute("RUNSCRIPT FROM '" + ddlFile.getAbsolutePath() + "'");
    
            connection.createStatement().execute("RUNSCRIPT FROM '" +  Utils.getFile(CONTENT_DDL_FILE).getAbsolutePath() + "'");
            
            connection.createStatement().execute("SHUTDOWN");
        }
        
    }
    
    public static void beforeClass() throws Exception {
        createEmptyDBFromDDL();
        //AlmaPickupNumbersStorage.initialize(DRIVER, URL, USERNAME, PASSWORD);
        
    }
    
    public static void main(String[] args) throws Exception {
        beforeClass();
    }
    
    public static void afterClass() {
        // No reason to delete DB data after test, since we delete it before each test.
        // This way you can open the DB in a DB-browser after a unittest and see the result.
        //AlmaPickupNumbersStorage.shutdown();
    }
}
