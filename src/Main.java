import java.io.*;
import java.util.*;

public class Main {

    private static File dirIn = new File(System.getProperty("user.dir") + "/data/HitranData/");
    private static File dirOut = new File(System.getProperty("user.dir") + "/data/Output/");
    private static File[] directoryListing = dirIn.listFiles();

    // Values.
    private static final int RANGE_LO = 400;
    private static final int RANGE_HI = 4000;

    private static final float RES_COARSE = (float) (100/2);

    private static final float RANGE_RES_FINE = 200;
    private static final float RES_FINE = (float) (0.0603/2);


    public static void main(String[] args) {
        // Process gases.
        if (directoryListing != null) {
            for (File child : directoryListing) {
                processGas(child);
            }
        }
    }

    private static double lorentzIntegral(float kappa, float kappa0, float gamma){
        return (1/Math.PI) * Math.atan((kappa - kappa0)/gamma);
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

        System.out.println("Processing file: " + name);

        File outputFile = new File(dirOut, name + ".csv");

        // Load data file.
        List<LineStrength> hitranData = new DataFileHelper(inputFile).read();

        // Process.
        // Integrate each line within bandwidth.
        // Divide the result by 2*deltakappa to get the average intensity.
        // Assign result to the midway value kappa+deltakappa.


        // Get coarse map of features.

        HashMap<Float, Double> coarseMap = new LinkedHashMap<>();

        for (float kappa = RANGE_LO; kappa <= RANGE_HI; kappa += 2 * RES_COARSE) {
            double s = 0;
            for (LineStrength aHitranData : hitranData) {
                double v = lorentzIntegral(kappa + RES_COARSE, aHitranData.waveNumber, aHitranData.airWidth);
                v -= lorentzIntegral(kappa - RES_COARSE, aHitranData.waveNumber, aHitranData.airWidth);
                s += aHitranData.lineStrength * v;
            }
            s /= (2 * RES_COARSE);

            coarseMap.put(kappa, s);
        }

        // Identify feature locations.

        System.out.print("Identifying feature locations: ");

        List<Float> mapKeyIndex = new ArrayList<>();
        List<Float> features = new ArrayList<>();
        for (Map.Entry<Float, Double> entry : coarseMap.entrySet()) {
            mapKeyIndex.add(entry.getKey());
        }

        for (int i = 0; i < mapKeyIndex.size(); i++){

            float keyCurr = mapKeyIndex.get(i);
            double valCurr = coarseMap.get(keyCurr);
            if (i != 0 && i != mapKeyIndex.size() - 1){
                // Check neighbour values.
                float keyPrev = mapKeyIndex.get(i - 1);
                double valPrev = coarseMap.get(keyPrev);
                float keyNext = mapKeyIndex.get(i + 1);
                double valNext = coarseMap.get(keyNext);

                if (valCurr > valPrev && valCurr > valNext){
                    // Found a feature so record location.
                    features.add(keyCurr);
                    System.out.print(keyCurr + " cm-1, ");
                }
            }
        }

        System.out.println();

        // Render features in detail.
        // Write to output file.
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), "UTF-8"))) {
            for (Float feature : features){
                System.out.print("Rendering " + name + " " + feature + " cm-1 feature... ");
                for (float kappa = feature - RANGE_RES_FINE; kappa <= feature + RANGE_RES_FINE; kappa += 2 * RES_FINE){
                    double s = 0;
                    for (LineStrength aHitranData : hitranData) {
                        double v = lorentzIntegral(kappa + RES_FINE, aHitranData.waveNumber, aHitranData.airWidth);
                        v -= lorentzIntegral(kappa - RES_FINE, aHitranData.waveNumber, aHitranData.airWidth);
                        s += aHitranData.lineStrength * v;
                    }
                    s /= (2 * RES_FINE);

                    writer.write(kappa + ", " + s + "\n");
                }
                System.out.println("Complete");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class LineStrength {
    // Represents a HITRAN data point.

    final float waveNumber;
    final double lineStrength;
    final float airWidth;

    LineStrength(float waveNumber, double lineStrength, float airWidth) {

        this.waveNumber = waveNumber;
        this.lineStrength = lineStrength;
        this.airWidth = airWidth;
    }
}

class DataFileHelper {

    private final File file;

    DataFileHelper(File file) {
        this.file = file;
    }

    List<LineStrength> read(){
        List<LineStrength> l = new ArrayList<>();
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
                    // Remove annoying whitespace and null characters added by HITRAN.
                    line = line.replaceAll("\\s+","");
                    line = line.replaceAll("\\u0000","");

                    // Parse line for the necessary values.
                    Scanner scanner = new Scanner(line);
                    scanner.useDelimiter(",");
                    scanner.nextInt();
                    scanner.nextInt();
                    float waveNumber = scanner.nextFloat();
                    double lineStrength = scanner.nextDouble();
                    scanner.nextDouble();
                    float airWidth = scanner.nextFloat();

                    LineStrength values = new LineStrength(waveNumber, lineStrength, airWidth);

                    // Add values to ArrayList.
                    l.add(values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return l;
    }

}
