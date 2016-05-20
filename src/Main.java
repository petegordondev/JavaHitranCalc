import java.io.*;
import java.util.*;

public class Main {

    private static File dirIn = new File(System.getProperty("user.dir") + "/data/HitranData/");
    private static File dirOut = new File(System.getProperty("user.dir") + "/data/Output/");
    private static File[] directoryListing = dirIn.listFiles();

    // Values.
    private static final int RANGE_LO = 600;
    private static final int RANGE_HI = 4000;

    private static final float RES_COARSE = (float) (100/2);

    private static final float RANGE_RES_FINE = 200;
    private static final float RES_FINE = (float) (0.03/2);


    public static void main(String[] args) {
        // Process gases.
        if (directoryListing != null) {
            for (File child : directoryListing) {
                processGas(child);
            }
        }
    }

    private static double lorentz(float nu, float nu0, float gamma){
        return (1/Math.PI) * gamma/(Math.pow((nu - nu0),2) + Math.pow(gamma,2));
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

        // Get coarse map of features.

        HashMap<Float, Double> coarseMap = new LinkedHashMap<>();

        for (float nu = RANGE_LO; nu <= RANGE_HI; nu += 2 * RES_COARSE) {
            double theta = 0;
            for (LineStrength aHitranData : hitranData) {
                double theta_nu = aHitranData.lineStrength * lorentz(nu, aHitranData.waveNumber, aHitranData.airWidth);
                theta += theta_nu;
            }
            coarseMap.put(nu, theta);
        }

        // Identify feature locations.

        System.out.print("\nFeature locations: ");

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
                }
            }
        }

        for (int i = 0; i < features.size(); i++){
            System.out.print(features.get(i) + " cm-1");
            if (i < features.size() - 1) System.out.print(", ");
        }
        System.out.print("\n\n");

        // Render features in detail.
        // Write to output file.
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), "UTF-8"))) {
            for (Float feature : features){
                long startTime = System.nanoTime();
                System.out.print("Rendering " + name + " " + feature + " cm-1 feature... ");
                for (float nu = feature - RANGE_RES_FINE/2; nu <= feature + RANGE_RES_FINE/2; nu += 2 * RES_FINE) {
                    double theta = 0;
                    for (LineStrength aHitranData : hitranData) {
                        double theta_nu = aHitranData.lineStrength * lorentz(nu, aHitranData.waveNumber, aHitranData.airWidth);
                        theta += theta_nu;
                    }

                    writer.write(nu + ", " + theta + "\n");
                }
                long endTime = System.nanoTime();
                long duration = (endTime - startTime)/1000000;
                System.out.println("Complete (" + duration + " ms)");
            }
            writer.close();
            System.out.print("\n\n");
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
        long startTime = System.nanoTime();
        List<LineStrength> l = new ArrayList<>();
        // Read lines from file.
        System.out.print("Loading HITRAN data from file... ");
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

        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;
        System.out.println("Complete (" + duration + " ms)");

        return l;
    }

}
