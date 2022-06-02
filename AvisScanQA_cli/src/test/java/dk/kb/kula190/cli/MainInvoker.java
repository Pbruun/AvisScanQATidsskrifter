package dk.kb.kula190.cli;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainInvoker {
    
    
    private static final List<File>
            specificBatch
            = new ArrayList<File>(List.of(new File("/home/pabr/Projects/AvisScanQATidsskrifter/AvisScanQATidsskrifter/data/dl_20220301_rt1"),new File("/home/pabr/Projects/AvisScanQATidsskrifter/AvisScanQATidsskrifter/data/dl_20220409_rt1"),new File("/home/pabr/Projects/AvisScanQATidsskrifter/AvisScanQATidsskrifter/data/dl_20220502_rt1")));
    
    public static void main(String[] args) throws IOException, URISyntaxException {
        File batch1 = specificBatch.get(0);
        File batch2 = specificBatch.get(1);
        File batch3 = specificBatch.get(2);
        Main.main(batch3.getAbsolutePath());
    }
}
