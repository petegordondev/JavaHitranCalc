import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static File dirIn = new File(System.getProperty("user.dir") + "/data/HitranData/");
    private static File dirOut = new File(System.getProperty("user.dir") + "/data/Output/");
    private static File[] directoryListing = dirIn.listFiles();
    private static int gasTally = new File(System.getProperty("user.dir") + "/data/HitranData/").list().length;

    // Values.
    private static final int KAPPA_LO = 500;
    private static final int KAPPA_HI = 750;
    private static final double DELTA_KAPPA = 0.0603/2;


    public static void main(String[] args) {
        // Process gases.
        if (directoryListing != null) {
            for (File child : directoryListing) {
                processGas(child);
            }
        }
    }

    private static double ILNZ(int kappa, double kappa0, double gamma){
        return 0;
    }

    private static void processGas(File inputFile) {

        // Create output file.
        if(!dirOut.exists()){
            //noinspection ResultOfMethodCallIgnored
            dirOut.mkdir();}

        String name = inputFile.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }

        File outputFile = new File(dirOut, name + ".csv");

        // Load data file.
        List<Double[]> hitranData = new DataFileHelper(inputFile).toStringArray();

        // Process.
        // Integrate each line within bandwidth.
        // Divide the result by 2*deltakappa to get the average intensity.
        // Assign result to the midway value kappa+deltakappa.



        // Write to output file.
//        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
//                new FileOutputStream(outputFile), "UTF-8"))) {
//            for (int kappa = KAPPA_LO; kappa <= KAPPA_HI; kappa++){
//
//                writer.write(kappa + ", " + result);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}

class DataFileHelper {

    private final File file;

    DataFileHelper(File file) {
        this.file = file;
    }

    List<Double[]> toStringArray(){
        return read();
    }

    private List<Double[]> read(){
        List<Double[]> l = new ArrayList<>();
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
                    // Parse line for values.
                    Double[] values = new Double[7];
                    line = line.replaceAll("\\s+","");
                    line = line.replaceAll("\\u0000","");
                    Scanner scanner = new Scanner(line);
                    scanner.useDelimiter(",");
                    for (int i = 0; i < 7; i++){
                        values[i] = scanner.nextDouble();
                    }
                    // System.out.println(values[0].toString());
                    // Add line of values to ArrayList.
                    l.add(values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return l;
    }

}
