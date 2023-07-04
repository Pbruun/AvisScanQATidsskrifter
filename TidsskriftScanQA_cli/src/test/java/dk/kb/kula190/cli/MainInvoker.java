package dk.kb.kula190.cli;

import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.features.FeatureExtractorConfig;
import org.verapdf.features.FeatureObjectType;
import org.verapdf.features.tools.FeatureTreeNode;
import org.verapdf.gf.model.GFModelParser;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class MainInvoker {
    
    
    private static final List<File>
            specificBatch
            = new ArrayList<File>(List.of(new File("/home/pabr/Projects/AvisScanQATidsskrifter/AvisScanQATidsskrifter/data/dl_20210101_rt1"),new File("/home/pabr/Projects/AvisScanQATidsskrifter/AvisScanQATidsskrifter/data/dl_20220409_rt1"),new File("/home/pabr/Projects/AvisScanQATidsskrifter/AvisScanQATidsskrifter/data/dl_20220502_rt1")));
    
    public static void main(String... args) throws IOException, URISyntaxException {
        //testVera();
       Main.main("/home/pabr/Projects/AvisScanQATidsskrifter/AvisScanQATidsskrifter/data/dl_20210101_rt1");
    }
    public static void testVera(){
        VeraGreenfieldFoundryProvider.initialise();
        PDFAFlavour flavour = PDFAFlavour.byFlavourId("1b");
        PDFAFlavour.getFlavourIds();
        FeatureExtractorConfig featureExtractorConfig = new FeatureExtractorConfig() {
            @Override public boolean isFeatureEnabled(FeatureObjectType type) {
                return type != null;
            }

            @Override public boolean isAnyFeatureEnabled(EnumSet<FeatureObjectType> types) {
                return !types.isEmpty();
            }

            @Override public EnumSet<FeatureObjectType> getEnabledFeatures() {
                return null;
            }
        };
        try (GFModelParser parser = GFModelParser
                                            .createModelWithFlavour(new FileInputStream("/home/pabr/Projects/AvisScanQATidsskrifter/AvisScanQATidsskrifter/data/dl_20210101_rt1/aarhusstiftstidende/pages/veraPDF test suite 6-8-t01-fail-a.pdf"), flavour)) {
            PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false);
            ValidationResult result = validator.validate(parser);
            List<FeatureTreeNode>
                    testing = parser.getFeatures(featureExtractorConfig).getFeatureTreesForType(FeatureObjectType.EMBEDDED_FILE);
            List<TestAssertion> toBeTested = result.getTestAssertions();
            if(result.isCompliant()){
                System.out.println("was compliant");
            }
        } catch (EncryptedPdfException | ModelParsingException | ValidationException | IOException e) {
            System.out.println(e);
        }
    }
}
