import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static File dir = new File(System.getProperty("user.dir") + "/data/HitranData/");
    private static File[] directoryListing = dir.listFiles();
    private static int gasTally = new File(System.getProperty("user.dir") + "/data/HitranData/").list().length;


    public static void main(String[] args) {
// Process gases.
        if (directoryListing != null) {
            for (File child : directoryListing) {
                processGas(child);
            }
        }
    }

    private static void processGas(File file) {
        // Load file.
        List<String> hitranData = new DataFileHelper(file).toStringArray();
        // Process.

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
