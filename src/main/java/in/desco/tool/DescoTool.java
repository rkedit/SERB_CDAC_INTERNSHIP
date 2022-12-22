package in.desco.tool;

/**
 * <b>Import statements:</b> we import various Input/ Output (io) and util
 * collections of java. Further, the FeatureExtractionTask and the
 * datatype classes to store the measure values associated with the
 * lexical properties of various programming constructs.
 *
 * @see in.datatype.CodeInfo
 * @see in.datatype.StatProp
 * @see in.featureextractor.FeatureExtractionTask
 */

import in.datatype.CodeInfo;
import in.datatype.PredResult;
import in.datatype.StatProp;
import in.featureextractor.FeatureExtractionTask;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * The <b>DescoTool</b> class is the main class responsible for performing the
 * defect estimation on an input test file content. It comprises of the complete
 * set of defect estimation methods essential to perform defect estimation in
 * various scenarios. It uses the FeatureExtractionTask class to extract the raw
 * features from the input test file content.
 *
 * @see in.featureextractor.FeatureExtractionTask
 */
public class DescoTool {

    private static HashMap<String, Integer> constructsList;
    private static HashMap<Integer, String> bugTypeProbabilities;
    private static String backEndPath;
    private static HashMap<Integer, String> scnToModel;
    private Integer taskScenId = null;
    private String finalLabel = null;
    private String fileContent = null;
    private String fileExt = null;

    Map<Integer, PredResult> table = null;

    /**
     * Constructor to initialize the static data members and static methods of the
     * class. It initializes the backEndFolder path and the hash maps containing the
     * mappings of construct Ids to construct names, model paths to scenario Ids and
     * model to accuracy values.
     *
     * @param backEndFolderPath path of the backEnd folder storing the necessary
     *                          Machine Learning (ML) models and code files
     * @throws IOException
     */
    public DescoTool(String backEndFolderPath) throws IOException {

        backEndPath = backEndFolderPath;
        initializeConstructList();
        initializeModelPaths();
        getBugTypeProbabilities();
    }

    /**
     * A function that returns a response while prediction of different bug
     * characteristics. In case of the very first defect estimation scenario (i.e.
     * scenId =1), the response is <b>Defective</b> in case the finalLabel is
     * <b>1</b> or <b>Unpredictable</b> if the finalLabel is <b>0</b>.
     * <p>
     * For the rest of the scenarios (i.e. the scenId >1), If the finalLabel of the
     * test source file, provided as input, equals "1", the response is returned as
     * Yes else it is marked as No.
     * <p>
     * This means that a finalLabel which equals one depicts a positive likelihood
     * for the presence of the - specific bug characteristic for which the function
     * is called.
     *
     * @return a String type object containing the response
     */
    private String getLabelResponse() {
        if (finalLabel.equalsIgnoreCase(Constants.ONE_LABEL))
            if (taskScenId > 1)
                return ":Yes";
            else// in case scenId = 1
                return ":Defective";
        else// in case of 0
            if (taskScenId > 1)
                return ":No";
            else// in case scenId = 1
                return ":Unpredictable";
    }


    /**
     * A function that computes the statistical measure values of the lexical
     * measure values provided as input. The input given to this function is a
     * HashMap containing the mapping of featureIds to their specific ArrayLists.
     * The ArrayList specify the list of measure values obtained in different
     * scenarios.
     * <p>
     * For instance, when taking about <b>if_count</b> in a file, we get different
     * measure values corresponding to different parent nodes. The function works by
     * applying various statistical measure (such as max, min, avg, stdDev, etc.) on
     * each of such ArrayList and building the corresponding set of values as a
     * StatProp type object. The function further builds a mapping of the featureIds
     * to the respective StatProp thus obtained and returns the same.
     *
     * @param features a HashMap containing the mapping of featureIds to their
     *                 specific ArrayLists
     * @return a HashMap containing the mapping of the featureIds to their
     * respective StatProp computed in the function
     */
    private HashMap<Integer, StatProp> buildFeatureValueTuple(HashMap<Integer,
            ArrayList<Float>> features) {

        HashMap<Integer, StatProp> proconFeatures = new HashMap<Integer, StatProp>();
        Set<String> featureMetrics = constructsList.keySet();
        ArrayList<Float> measureVals = null;

        for (String k : featureMetrics) {
            Integer featureId = constructsList.get(k);
            if (features.containsKey(featureId)) {

                measureVals = features.get(featureId);
                double[] mv = Arrays.stream(measureVals.toArray())
                        .mapToDouble(value -> ((Float)value).doubleValue())
                        .toArray();
                DescriptiveStatistics ds = new DescriptiveStatistics(mv);
                StatProp statVals = new StatProp(
                        ds.getMax(),
                        ds.getMax(),
                        ds.getMean(),
                        ds.getStandardDeviation());
                proconFeatures.put(featureId, statVals);
            }
        }
        return proconFeatures;
    }

    /**
     * The function transforms the features extracted into the actual format of
     * PROCON dataset. It now combines the construct names with the measure types to
     * build the actual features and obtain the feature values for the same. It
     * calls a private data member function buildFeatureValueTuple to compute the
     * statistical measure values on the obtained set of values, and returns the
     * same.
     *
     * @param featuresFromInput raw measure values associated with the lexical
     *                          properties of the occurrences of various constructs
     * @return a HashMap containing the mapping of the featureIds to their
     * respective StatProp computed in the function
     * @see #buildFeatureValueTuple(HashMap)
     */
    public HashMap<Integer, StatProp> refineFeatures(
            HashMap<CodeInfo, Integer> featuresFromInput) {

        Set<CodeInfo> keys = featuresFromInput.keySet();
        HashMap<Integer, ArrayList<Float>> fileFeatures = new HashMap<Integer, ArrayList<Float>>();
        for (CodeInfo p : keys) {
            String constructNm = p.getConstructNm();
            String measureType = p.getMeasureType();
            String featureMetric = constructNm.concat("_").concat(measureType);

            Integer featureId = constructsList.get(featureMetric);
            if (featureId != null) {
                int measureVal = featuresFromInput.get(p);
                // we need to add all constructs for our file case
                ArrayList<Float> metricValues = fileFeatures.get(featureId);
                if (metricValues == null) {
                    metricValues = new ArrayList<>();
                    fileFeatures.put(featureId, metricValues);
                }
                metricValues.add((float) measureVal);
            }

        } // for ends
        return buildFeatureValueTuple(fileFeatures);
    }

    /**
     * Function to build a mapping between the construct names and unique
     * identifiers assigned to them by reading the considered construct names from a
     * text file. This function is used only once during the initialization by the
     * constructor.
     *
     * @throws IOException
     */
    private synchronized void initializeConstructList() throws IOException {
        if (constructsList == null || constructsList.isEmpty()) {

            System.out.println("Reading constructs");
            constructsList = new HashMap<>(); // initializing the static variable
            List<String> lines = Files.readAllLines(Paths.get(backEndPath,
                    Constants.CONSTRUCT_FILE));
            for (int i = 0; i < lines.size(); i++) {
                constructsList.put(lines.get(i), i + 1);
            }
            System.out.println("Initialized constructs list to:\n" + constructsList);
        } else {
            System.out.println("Reusing the existing constructs list instance.");
        }
    }

    /**
     * The function returns the file length of the test file. It uses the fileId to
     * locate the specific value within the proconFeatures obtained for the file
     *
     * @param proconFeatures
     * @return
     */
    private Double getFileLength(HashMap<Integer, StatProp> proconFeatures) {
        Integer fileLengthId = constructsList.get(Constants.FILE_LENGTH);
        StatProp fileValues = proconFeatures.get(fileLengthId);
        return fileValues.getMaxValue();
    }

    /**
     * This function reads the proconFeatures and transform them into a suitable
     * input format for ML models. The function inserts the feature values in an
     * ArrayList by arranging them sequentially in the ascending order of their
     * feature Ids.
     *
     * @param proconFeatures a HashMap containing the mapping of featureIds and
     *                       featureValue tuples.
     * @return an ArrayList containing the final featureSet ready to be provided as
     * input to a ML model.
     */
    private ArrayList<String> transformFeaturesOfTestFile(HashMap<Integer,
            StatProp> proconFeatures) {

        Set<String> featureMetrics = constructsList.keySet();
        ArrayList<String> testFeatures = new ArrayList<String>();
        // we need to divide all feature values by file length and exclude the file
        // length as a feature
        // get file_length Id
        Integer fileLengthId = constructsList.get(Constants.FILE_LENGTH);
        Double fileLength = getFileLength(proconFeatures); // fetch file length
        for (String k : featureMetrics) {
            Integer featureId = constructsList.get(k);
            if (proconFeatures.containsKey(featureId)) {
                if (featureId != fileLengthId) {
                    StatProp statObj = proconFeatures.get(featureId);
                    // get the 4 stat measure Vals
                    Double measureVal = statObj.getMaxValue()/fileLength;
                    testFeatures.add(measureVal.toString());
                    measureVal = statObj.getMinValue()/fileLength;
                    testFeatures.add(measureVal.toString());
                    measureVal = statObj.getAvgValue()/fileLength;
                    testFeatures.add(measureVal.toString());
                    measureVal = statObj.getStdDevValue()/fileLength;
                    testFeatures.add(measureVal.toString());
                } else
                    continue;
            } else {
                // we insert 4 0.0 values
                for (int y = 0; y < 4; y++) {
                    testFeatures.add(Constants.zeroVal.toString());
                }
            }
        }
        return testFeatures;
    }

    /**
     * This function writes the features (refined PROCON features) extracted from
     * the input test file as a text file. This file is later read by the python
     * script to perform the defect estimation.
     *
     * @param testFeatures an ArrayList containing the list of Float type feature
     *                     values ready to be provided as input to an ML model
     * @throws IOException
     */
    private void saveTestFeaturesToFile(ArrayList<String> testFeatures)
            throws IOException {
    	String testFeatureSet = String.join(" ", testFeatures);
    	File feat = Paths.get(backEndPath, Constants.TEST_FEATURES).toFile();
    	if (feat.exists()) {
            System.out.println("Features file exists, deleting.");
            feat.delete();
        }
        FileWriter fileWriter = new FileWriter(feat);
        fileWriter.write(testFeatureSet);
        fileWriter.close();
    }

    /**
     * This function is used to extract the features of the input test file. It
     * finally writes the features in the form of a text file Later, these features
     * are input to various defect estimation tasks
     *
     * @throws IOException
     */
    public void getFeatures() throws IOException {
        System.out.println("Feature extraction process started.\n" +
                "It may take 2-3 minutes to complete.");
        saveTestFeaturesToFile(transformFeaturesOfTestFile(
                refineFeatures(extractFeatures())));
        System.out.println("Features written into a file successfully.");
    }

    /**
     * A function that builds a map between the scenario Ids and the model Paths. To
     * build this map, it reads the model paths from a text file sequentially. The
     * model paths have been written into the text file in the same order as per the
     * scenario Ids. This function is called only once, during the initialization of
     * the static data member scnToModel by the constructor.
     * @throws IOException 
     */
    private synchronized void initializeModelPaths() throws IOException {
    	if (scnToModel == null || scnToModel.isEmpty()) {
            System.out.println("Reading model paths");
            scnToModel = new HashMap<>(); 
            List<String> lines = Files.readAllLines(Paths.get(backEndPath,
                    Constants.MODEL_PATHS));
            for (int i = 0; i < lines.size(); i++) {
                scnToModel.put(i + 1, lines.get(i));
            }
            System.out.println("Initialized scenario to model mapping to:\n" + scnToModel);
        } else {
            System.out.println("Reusing the existing model paths instance.");
        }
    }
    	

    /**
     * A function that fetches the accuracy values of the best performing ML model.
     * in each scenario and builds a map between the scenId and accuracy values.
     * This function is called only once, during the initialization of the static
     * data member bugTypeProbabilities by the constructor.
     * @throws IOException 
     */

    private synchronized void getBugTypeProbabilities() throws IOException {
    	if (bugTypeProbabilities == null || bugTypeProbabilities.isEmpty()) {
    		System.out.println("Reading the model accuracies. Models count = "
                    +scnToModel.size());
    		bugTypeProbabilities = new HashMap<>();
    		int id=1;
    		for (Integer scenId : scnToModel.keySet()) {
                String modelPath = scnToModel.get(scenId);
                String text = new String(Files.readAllBytes(Paths.get(backEndPath,
                        modelPath, Constants.ACC_SCORE_FILE)));
                text = text.trim().replace(' ', ':');
                bugTypeProbabilities.put(id++, text);
    		} //outer for ends
            System.out.println("Initialized model accuracies to:\n" +bugTypeProbabilities);
    	} else {
    		System.out.println("Reusing the existing accuracies instance.");
    	}
   }

    /**
     * A function that returns a specific bug type depending upon the input scenario
     * id (scenId).
     *
     * @return a String type object representing a a specific bug type corresponding
     * to the input scenId.
     */
    private String getBugType() {
        switch (taskScenId) {
            case 1:
                return "Overall Defectiveness";
            case 2:
                return "Can have defects tagged as enhancement to the software";
            case 3:
                return "Can have defects related to intended business functionality";
            case 4:
                return "Can have compilation/build errors";
            case 5:
                return "Can have defects related to related to resource usage (e.g. memory leakage, socket connection, etc.)";
            case 6:
                return "Can have defects related to security";
            case 7:
                return "maximum";
            case 8:
                return "least";
            case 9:
                return "medium";
            case 10:
                return "OSS operating system";
            case 11:
                return "Proprietary operating system";
            case 12:
                return "Insignificant";
            case 13:
                return "High";
            case 14:
                return "Low";
            case 15:
                return "Medium";
        }
        return null;
    }

    /**
     * A function that returns a bugType response by appending the input bug type
     * (bugType) obtained in case of a particular scenario, represented by a unique
     * scenario Id (scenId).
     *
     * @param bugType a particular bug type depending on the scenId.
     * @return
     */
    private String getScenResponse(String bugType) {
        switch (taskScenId) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return bugType;
            case 7:
            case 8:
            case 9:
                return "Can have defects involving ".concat(bugType).concat(" user activity (in the form of comments)");
            case 10:
            case 11:
                return "Can have defects that show on ".concat(bugType);
            case 12:
            case 13:
            case 14:
            case 15:
                return "Can have ".concat(bugType).concat(" priority defect(s)");
        }
        return null;
    }


    /**
     * A function that fetches the results for a defect estimation task and combines
     * the results of all such tasks in a tabular format. Each row of this table
     * (except the first one) represents an outcome obtained corresponding to a
     * defect estimation task.
     */
    private void collectResults() {
        String pred = getScenResponse(getBugType()).concat(getLabelResponse());
        table.put(taskScenId, new PredResult(pred,
                bugTypeProbabilities.get(taskScenId)));
    }


    /**
     * A function that runs the python script present at backEndPath location. This
     * python script performs the actual defect estimation tasks and returns the
     * achieved finalLabel.
     *
     * @param modelPath a String type object representing the path of the best
     *                  defect estimation ML models required to perform defect
     *                  estimation.
     * @throws IOException
     * @throws InterruptedException
     */
    private void runPredScript(String modelPath) throws IOException, InterruptedException {

        String filePath = Paths.get(backEndPath,
                Constants.PRED_TEST_FILE).toString();
        String[] cmd = {"python", filePath, modelPath, backEndPath};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(backEndPath));
        File out = File.createTempFile("desco_be_out_", ".log");
        pb.redirectOutput(Redirect.appendTo(out));
        File err = File.createTempFile("desco_be_err_", ".log");
        pb.redirectError(Redirect.appendTo(err));
        System.out.println("Output file: " + out.getAbsolutePath());
        Process p = pb.start();
        System.out.println("Started the command: " + pb.command());
        p.waitFor();
        String errMsg = new String(Files.readAllBytes(err.toPath()));
        finalLabel = new String(Files.readAllBytes(out.toPath())).trim();
        if (finalLabel == null || finalLabel.length() == 0) {
            System.out.println("Errors: " + errMsg);
            throw new IOException("Failed to run the back-end script.\n" + errMsg);
        }
        System.out.println("Output: '" + finalLabel+"'");

        Files.deleteIfExists(err.toPath());
        Files.deleteIfExists(out.toPath());

    }

    /**
     * This function calls the feature extraction task to extract the features from
     * the input file and initializes the values of featureHashmap with the obtained
     * features. featuresFromInput stores the values of various lexical properties
     * associated with the programming constructs.
     *
     * @return a HashMap storing the values of various lexical properties associated
     * with the programming constructs.
     */
    private HashMap<CodeInfo, Integer> extractFeatures() {
        // feature extraction task started
        FeatureExtractionTask t = new FeatureExtractionTask(fileContent, Constants.TEST_PHASE, fileExt);
        t.run();
        // features fetched are stored here
        return t.getHashmap();
    }

    /**
     * Performs predition on the supplied input file. It returns an
     * List<List<String>>, i.e a table, whose rows are indexed with
     * scenario Id and contain: { scenario, result, accVal, errVal }
     *
     * @param inputFile
     * @param lang Programming language in which input file is written.
     * @return results as an List<List<String>>
     */
    public Map<Integer, PredResult> performDefectEstimation(
            File inputFile, String lang) throws IOException {

        fileContent = new String(
                Files.readAllBytes(inputFile.toPath()));
        fileExt = lang;

        predictForAllScenarios();
        return table;
    }

    private void predictForAllScenarios() throws IOException {

        getFeatures();
        table = new HashMap<>();
        for (taskScenId = 1; taskScenId <= 15; taskScenId++) {
            if (taskScenId.equals(Constants.defectivenessCheckScen)) {
                System.out.println("Prediction Results:");
            }
            try {
                runPredScript(scnToModel.get(taskScenId));
                collectResults();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
