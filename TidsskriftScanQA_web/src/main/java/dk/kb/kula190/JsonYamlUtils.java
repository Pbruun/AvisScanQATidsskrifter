package dk.kb.kula190;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dk.kb.util.yaml.YAML;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;

public class JsonYamlUtils {
    
    
    public static String yamlToString(YAML yaml) {
        return new Yaml().dump(yaml);
    }
    
    public static String json2Yaml(String jsonString) throws IOException {
        // parse JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        JsonNode jsonNodeTree = objectMapper.readTree(jsonString);
        // save it as YAML
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }
    
    public static String yaml2Json(String yamlString) throws IOException {
        // parse YAML
        YAMLMapper objectMapper = new YAMLMapper();
        objectMapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        JsonNode yamlNodeTree = objectMapper.readTree(yamlString);
        // save it as JSON
        return new ObjectMapper().writeValueAsString(yamlNodeTree);
    }
    
}
