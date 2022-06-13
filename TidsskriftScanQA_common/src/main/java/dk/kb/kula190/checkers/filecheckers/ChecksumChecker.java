package dk.kb.kula190.checkers.filecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.DefaultTreeEventHandler;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

public class ChecksumChecker extends DefaultTreeEventHandler {
    
    public ChecksumChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        String computedMD5;
        try (InputStream data = event.getData()) {
            computedMD5 = DigestUtils.md5Hex(data);
            String givenMD5 = event.getChecksum();
            if (givenMD5 == null){
                addFailure(event, FailureType.CHECKSUM_MISSING_ERROR, "Appendix G – Checksums: File with missing checksum");
            } else {
                checkEquals(event,
                            FailureType.CHECKSUM_MISMATCH_ERROR,
                            "Appendix G – Checksums: File have checksum {actual} but should have checksum {expected}",
                            computedMD5,
                            givenMD5
                           );
            }
        } catch (IOException e) {
            addExceptionalFailure(event, e);
        }
    }
    
}
