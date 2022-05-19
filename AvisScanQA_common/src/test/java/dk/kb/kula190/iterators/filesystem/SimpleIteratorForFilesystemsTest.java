package dk.kb.kula190.iterators.filesystem;


import dk.kb.kula190.iterators.AbstractTests;
import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.filesystem.transparent.TransparintingFileSystemIterator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;


public class SimpleIteratorForFilesystemsTest extends AbstractTests {
    
    private TreeIterator iterator;
    
    @Override
    public TreeIterator getIterator() throws IOException {
        if (iterator == null) {
            //File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            File file = new File(System.getenv("HOME")+"/Projects/AvisScanQA/data/dl_20220409_rt1/aarhusstiftstidende/pages");
            
            System.out.println(file);
            iterator = new TransparintingFileSystemIterator(file,
                                                            file.getParentFile(),
                                                            List.of("MIX", "TIFF", "PDF", "ALTO"),
                                                            //actual files named as modersmaalet_19060703_udg01_1.sektion_0004.mix.xml
                                                            List.of(
                                                                    //Part to remove to generate the edition name
                                                                    //will output modersmaalet_19060703_udg01
                                                                    "_[^_]+_\\d{4}\\.\\w+(\\.xml)?$"
        
                                                                    //Part to remove to generate the section name
                                                                    //will output modersmaalet_19060703_udg01_1.sektion
                                                                    , "_[^_]+$", //filename.split(editionRegexp)[0];
        
                                                                    //Part to remove to generate the page name
                                                                    //will output modersmaalet_19060703_udg01_1.sektion_0004
                                                                    "\\.[^_]+$" //filename.split(pageRegexp)[0];
                                                                   ),
                    
                                                            "checksums.txt",
                                                            List.of("transfer_acknowledged","transfer_complete"));
        }
        return iterator;
        
    }
    
    
    @Test
    public void testIterator() throws Exception {
        try (PrintStream out = new PrintStream("batch.xml")) {
            super.testIterator(out, null);
        }
    }
    
    @Test
    @Disabled
    public void testIteratorWithSkipping() throws Exception {
        try (PrintStream out = new PrintStream("batch.xml")) {
            super.testIteratorWithSkipping(out, null);
        }
    }
}
