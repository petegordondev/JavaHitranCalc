import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static File dirIn = new File(System.getProperty("user.dir") + "/data/HitranData/");
    private static File dirOut = new File(System.getProperty("user.dir") + "/data/Output/");
    private static File[] directoryListing = dirIn.listFiles();
    private static int gasTally = new File(System.getProperty("user.dir") + "/data/HitranData/").list().length;


    public static void main(String[] args) {
// Process gases.
        if (directoryListing != null) {
            for (File child : directoryListing) {
                processGas(child);
            }
        }
    }

    private static void processGas(File inputFile) {

        // Create output file.
//        if(!dirOut.exists()){
//            dirOut.mkdir();}

        String name = inputFile.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }

        File outputFile = new File(dirOut, name + ".csv");
//        if(!outputFile.exists()){
//            try {
//                outputFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        // Load data file.
        List<String> hitranData = new DataFileHelper(inputFile).toStringArray();

        // Process.
        // outputFile.setWritable(true);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), "utf-8"))) {
            writer.write("something");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DataFileHelper {

    private final File file;

    DataFileHelper(File file) {
        this.file = file;
    }

    List<String> toStringArray(){
        return read();
    }

    private List<String> read(){
        List<String> l = new ArrayList<>();
        // Read lines from file.
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String line;
        try {
            if (in != null) {
                while ((line = in.readLine()) != null) {
                    // Add each line to ArrayList.
                    l.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return l;
    }

}
