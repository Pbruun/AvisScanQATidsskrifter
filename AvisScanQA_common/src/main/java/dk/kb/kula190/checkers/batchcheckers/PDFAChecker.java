package dk.kb.kula190.checkers.batchcheckers;

import dk.kb.kula190.ResultCollector;
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
                        Integer pageNumber)
            throws IOException, ValidationException, EncryptedPdfException, ModelParsingException {
        VeraGreenfieldFoundryProvider.initialise();
        PDFAFlavour flavour = PDFAFlavour.fromString("2b");
        try(PDFAParser parser = Foundries.defaultInstance().createParser(new FileInputStream(event.getName()), flavour)) {
            PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false);
            ValidationResult result = validator.validate(parser);
            checkTrue(event,null,"PDF File did not adhere to 2b standard",result.isCompliant());
        }
    }
}
