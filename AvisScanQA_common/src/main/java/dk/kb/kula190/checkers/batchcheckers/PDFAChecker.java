package dk.kb.kula190.checkers.batchcheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;


public class PDFAChecker extends DecoratedEventHandler {
    public PDFAChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }

    @Override
    public void pdfFile(DecoratedAttributeParsingEvent event,
                        String newspaper,
                        LocalDate editionDate,
                        String edition,
                        String section,
                        Integer pageNumber) {
        VeraGreenfieldFoundryProvider.initialise();
        PDFAFlavour flavour = PDFAFlavour.byFlavourId("1b");
        try (PDFAParser parser = Foundries.defaultInstance()
                                          .createParser(new FileInputStream(event.getLocation()), flavour)) {
            PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false);
            ValidationResult result = validator.validate(parser);

            if(result.isCompliant()){
                checkTrue(event,
                          FailureType.INVALID_PDF_ERROR,
                          "PDF File did not adhere to 2b standard",
                          result.isCompliant());
                System.out.println("was compliant");
            }
        } catch (EncryptedPdfException | ModelParsingException | ValidationException | IOException e) {
            getResultCollector().addFailure(event,
                                            FailureType.INVALID_PDF_ERROR,
                                            event.getLocation(),
                                            "Error doing check on pdf flavour");
        }
    }

}
