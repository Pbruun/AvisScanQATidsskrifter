package dk.kb.avischk.qa.web;

import java.io.File;
import java.io.FileNotFoundException;

public class ContentLocationResolver {

    private static String httpContentBase;
    private static String imageContentBase;
    private static String iipsrvBase;
    
    public static void setHttpContentBase(String base) {
        httpContentBase = base;
    }
    
    public static String getHttpContentBase() {
        return httpContentBase;
    }

    public static String getImageContentBase() {
        return imageContentBase;
    }

    public static void setImageContentBase(String imageContentBase) {
        ContentLocationResolver.imageContentBase = imageContentBase;
    }

    public static String getIipsrvBase() {
        return iipsrvBase;
    }

    public static void setIipsrvBase(String iipsrvBase) {
        ContentLocationResolver.iipsrvBase = iipsrvBase;
    }
    
    public static String getContent(String relPath, String type) throws FileNotFoundException {
        String content = null;
        String path = imageContentBase + "/" + relPath;
        checkFile(path);
        
        switch (type) {
        case "pdf":
            content = httpContentBase + relPath;
            break;
        case "tiff":
            content = httpContentBase + relPath;
            break;
        case "jpg":
            content = httpContentBase + relPath;
            break;
        case "jp2":
            content = iipsrvBase + path + ".dzi";
            break;
        default:
            content = null;
        }
        
        return content;
    }
    
    protected static void checkFile(String path) throws FileNotFoundException {
        File f = new File(path);
        if( ! f.exists()) {
            throw new FileNotFoundException("Failed to find file at '" + path + "'");
        }
    }
}
