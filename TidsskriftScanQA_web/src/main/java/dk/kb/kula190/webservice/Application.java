package dk.kb.kula190.webservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import dk.kb.kula190.api.impl.DefaultApiServiceImpl;
import dk.kb.kula190.dao.NewspaperQADao;
import dk.kb.kula190.dao.NewspaperQADaoFactory;
import dk.kb.util.yaml.NotFoundException;
import dk.kb.util.yaml.YAML;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.ApplicationPath;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class Application extends javax.ws.rs.core.Application {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final YAML serviceConfig;
    private final String batchesFolder;
    private final NewspaperQADao dao;
    private final String configFile;
    
    public Application() {
        log.info("Initializing WebQA service v{}", getClass().getPackage().getImplementationVersion());
        
        try {
            InitialContext ctx = new InitialContext();
            configFile    = (String) ctx.lookup("java:/comp/env/application-config");
            serviceConfig = YAML.resolveLayeredConfigs(configFile);
        } catch (IOException | NamingException e) {
            throw new RuntimeException("Failed to lookup settings", e);
        }
        try {
            dao = NewspaperQADaoFactory.getDAOInstance(getConfigString("avischk-web-qa.jdbc-connection-string"),
                                                       getConfigString("avischk-web-qa.jdbc-user"),
                                                       getConfigString("avischk-web-qa.jdbc-password"),
                                                       getConfigString("avischk-web-qa.jdbc-driver"));
        } catch (PropertyVetoException e) {
            throw new RuntimeException("Database connection driver issue", e);
        }
        batchesFolder = StringSubstitutor.replace(getConfigString("avischk-web-qa.batchesFolder"), System.getenv());
        
    }
    
    
    private String getConfigString(String path) {
        try {
            return serviceConfig.getString(path);
        } catch (NotFoundException e) {
            throw new NotFoundException("Failed to get value from configs " + configFile, path, e);
        }
    }
    
    public NewspaperQADao getDao() {
        return dao;
    }
    
    public String getBatchesFolder() {
        return batchesFolder;
    }
    
    public Set<Class<?>> getClasses() {
        return new HashSet<>(Arrays.asList(DefaultApiServiceImpl.class, ServiceExceptionMapper.class));
    }
    
    
    @Override
    public Set<Object> getSingletons() {
        return Set.of(getJsonProviderWithDateTimes());
    }
    
    public static JacksonJaxbJsonProvider getJsonProviderWithDateTimes() {
        // see https://github.com/FasterXML/jackson-modules-java8
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        
        return new JacksonJaxbJsonProvider(mapper,
                                           JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
    }
    
    
    public YAML getConfig() {
        return serviceConfig;
    }
}

