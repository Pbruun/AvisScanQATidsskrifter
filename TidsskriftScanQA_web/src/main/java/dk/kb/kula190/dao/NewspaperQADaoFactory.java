package dk.kb.kula190.dao;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.util.Properties;

public class NewspaperQADaoFactory {
    private final static Logger log = LoggerFactory.getLogger(NewspaperQADaoFactory.class);
    
    private static boolean initialized = false;
    private static ComboPooledDataSource connectionPool;
    private static NewspaperQADao daoInstance = null;
    
    public static synchronized NewspaperQADao getDAOInstance(String jdbcConnectionString, String jdbcUser,
            String jdbcPassword, String driver) throws PropertyVetoException {
        if(! initialized) {
            log.info("Initializing NewspaperQADaoFactory");
            initialized = true;
            
            Properties p = new Properties(System.getProperties());
            p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
            p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); // or any other
            System.setProperties(p);
            
            connectionPool = new ComboPooledDataSource();
            connectionPool.setDriverClass(driver);
            connectionPool.setJdbcUrl(jdbcConnectionString);
            connectionPool.setUser(jdbcUser);
            connectionPool.setPassword(jdbcPassword);
        }
        if(daoInstance == null) {
            daoInstance = new NewspaperQADao(connectionPool);
        }
        return daoInstance;
    }
    
}
