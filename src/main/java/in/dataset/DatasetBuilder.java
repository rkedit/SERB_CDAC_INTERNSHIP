package in.dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.*;

import in.dao.DataAccessObject;
import in.datatype.BugMap;
import in.featureextractor.FeatureExtractionTask;

public class DatasetBuilder {

    private static ArrayList<File> fetchSchedulableFiles(
    		ArrayList<File> filePaths) {
        ArrayList<File> schedulableFilePaths = new ArrayList<File>();
        System.out.println("scheduling");
        while (!filePaths.isEmpty()) {
            File filePath = filePaths.remove(0);
            System.out.println(filePath);
            String ext = FileIterator.getExt(filePath.toString());
            if (ext != null && (ext.equals("c") || ext.equals("cpp") || 
            		ext.equals("cc") || ext.equals("java")
                    || ext.equals("py")))
                schedulableFilePaths.add(filePath);
        }
        return schedulableFilePaths;
    }

    private static void scheduleFileThreads(
    		ArrayList<File> filePaths) {
        BlockingQueue<Runnable> fileBlockingQueue = 
        		new ArrayBlockingQueue<Runnable>(50);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20,
                5000, TimeUnit.MILLISECONDS,
                fileBlockingQueue,
                new ThreadPoolExecutor.CallerRunsPolicy());
     // we fetch the only the C, C++, Java and
        // Python files
        ArrayList<File> schedulableFiles = fetchSchedulableFiles(filePaths);
        System.out.println("Submitting tasks");
        // submitting the tasks
        for (int i = 0; i < schedulableFiles.size(); i++) {
        	FeatureExtractionTask t = new FeatureExtractionTask(schedulableFiles.get(i), 
            		"train", FileIterator.getExt(schedulableFiles.get(i).toString()));
            executor.submit(t);
        }
        executor.shutdown();
        System.out.printf("Initiated the thread pool executor shutdown...");
    }

    private static void processCodeFiles(String folderNm) 
    		throws InterruptedException {
        System.out.println("In process Code Files");

        if (folderNm != null) {
            LinkedList<File> projectPaths = FileIterator.extractProjectPaths(
            		folderNm);
            if (projectPaths != null && !projectPaths.isEmpty()) {
                ArrayList<File> filePaths = new ArrayList<File>();
                // extractFilePaths(ProjectPaths);
                ArrayList<File> subFilePaths = null;
                // corresponding to every project
                File projectPath = null;
                while (!projectPaths.isEmpty()) {
                    projectPath = projectPaths.removeFirst();
                    subFilePaths = FileIterator.extractFilePaths(projectPath);
                    filePaths.addAll(subFilePaths);
                }
                // At this point we have the FilePaths ArrayList containing
                //the paths of all the available files in all the projects
                // Next we need to schedule each on a thread (in turns, 
                //not all at a time) from a thread pool
                scheduleFileThreads(filePaths);
            } else
                return;
        } else
            return;
    }

    private static void processBugReports(String folderName) {
    	// function to store the csv data of bug reports (similar to defect information
        // collector)
        File dir = new File(folderName); // pass the argument as "Data" 
        //using /*java Test Data*/
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            // for each pathname in pathname array
            String fileName = null;
            for (File path : directoryListing) {
                // prints file and directory paths
                fileName = path.getName();
                // read the files with this
                DataAccessObject.insertBugInfo(fileName);
            }
        } else
            System.out.println("No files present");
    }
//to be checked
    private static ArrayList<String> extractFileNm(String content) {
        ArrayList<String> fileNames = new ArrayList<String>();
        Integer conLen = content.length();
        String fileNm = null;
        StringBuilder sb = null;
        Integer ind = 0;
        Integer prevInd = 0;
        Integer newInd = -1;
        String ext = null;
        ind = content.indexOf('.');
        String tempCon = content;
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        if (ind > 0) {
            // let's find all the occurrences first
            while (ind > 0) {
                if (prevInd == 0)
                    prevInd = ind + prevInd;
                else
                    prevInd = ind + prevInd + 1;
                indexes.add(prevInd);
                tempCon = tempCon.substring(ind + 1);
                ind = tempCon.indexOf('.');
            }
            if (indexes.size() > 0) {
                for (int i = 0; i < indexes.size(); i++) {
                    ind = indexes.get(i);
                    if (conLen > ind + 1 && (content.charAt(ind + 1) == 'c' || 
                    		content.charAt(ind + 1) == 'j'
                            || content.charAt(ind + 1) == 'p')) // atleast one character exists after this .
                    {
                        char ch1 = content.charAt(ind + 1);
                        sb = new StringBuilder();
                        sb.append(ch1);
                        if (conLen > ind + 2 && (content.charAt(ind + 2) == 'a' || 
                        		content.charAt(ind + 2) == 'p' || 
                        		content.charAt(ind + 2) == 'c' || 
                        		content.charAt(ind + 2) == 'y')) {
                            char ch2 = content.charAt(ind + 2);
                            sb.append(ch2);

                            if (conLen > ind + 3 && (content.charAt(ind + 3) == 'v' || 
                            		content.charAt(ind + 3) == 'p')) {
                                char ch3 = content.charAt(ind + 3);
                                sb.append(ch3);
                                if (conLen > ind + 4 && (content.charAt(ind + 4) 
                                		== 'a')) {
                                    char ch4 = content.charAt(ind + 4);
                                    sb.append(ch4);
                                    ext = sb.toString();
                                } else if ((conLen <= ind + 5 || content.charAt(ind + 4) == ' ')
                                        && (content.charAt(ind + 1) == 'c' && content.charAt(ind + 2) == 'p'
                                        && content.charAt(ind + 3) == 'p'))
                                    ext = sb.toString();
                                else
                                    continue;
                            } else if ((conLen <= ind + 4 || content.charAt(ind + 3) == ' ')
                                    && ((content.charAt(ind + 1) == 'p' && content.charAt(ind + 2) == 'y')
                                    || (content.charAt(ind + 1) == 'c' && content.charAt(ind + 2) == 'c'))) {
                                ext = sb.toString();
                            } else
                                continue;
                        } else if ((conLen <= ind + 3 || content.charAt(ind + 2) == ' ')
                                && content.charAt(ind + 1) == 'c')
                            ext = sb.toString();
                        else
                            continue;
                    } else
                        continue;

                    if (ext.equalsIgnoreCase("java") || ext.equalsIgnoreCase("py") || ext.equalsIgnoreCase("c") ||
                    		ext.equalsIgnoreCase("cpp") || ext.equalsIgnoreCase("cc")) {
                        // fetch the complete filename
                        newInd = ind - 1;
                        if (newInd >= 0) {
                            sb = new StringBuilder();
                            while (newInd >= 0) {
                                char ch = content.charAt(newInd);
                                int flag = 0;
                                switch (ch) {
                                    case '(':
                                        flag = 1;
                                        break;
                                    case ')':
                                        flag = 1;
                                        break;
                                    case '[':
                                        flag = 1;
                                        break;
                                    case ']':
                                        flag = 1;
                                        break;
                                    case '-':
                                        flag = 1;
                                        break;
                                    case ',':
                                        flag = 1;
                                        break;
                                    case ' ':
                                        flag = 1;
                                        break;
                                    case '>':
                                        flag = 1;
                                        break;
                                    case '<':
                                        flag = 1;
                                        break;
                                    case '.':
                                        flag = 1;
                                        break;
                                    case ';':
                                        flag = 1;
                                        break;
                                    case '\\':
                                        flag = 1;
                                        break;
                                    // we don't need full path as it is not available for all & it later creates a
                                    // problem in fetching complete path
                                    case '/':
                                        flag = 1;
                                        break;
                                    case '\"':
                                        flag = 1;
                                        break;
                                    case ':':
                                        flag = 1;
                                        break;
                                    case '+':
                                        flag = 1;
                                        break;
                                    case '?':
                                        flag = 1;
                                        break;
                                    case '*':
                                        flag = 1;
                                        break;
                                    case '{':
                                        flag = 1;
                                        break;
                                    case '$':
                                        flag = 1;
                                        break;
                                    case '0':
                                        flag = 1;
                                        break;
                                    case '1':
                                        flag = 1;
                                        break;
                                    case '2':
                                        flag = 1;
                                        break;
                                    case '3':
                                        flag = 1;
                                        break;
                                    case '4':
                                        flag = 1;
                                        break;
                                    case '5':
                                        flag = 1;
                                        break;
                                    case '6':
                                        flag = 1;
                                        break;
                                    case '7':
                                        flag = 1;
                                        break;
                                    case '8':
                                        flag = 1;
                                        break;
                                    case '9':
                                        flag = 1;
                                        break;
                                }
                                if (flag == 0) {
                                    if (ch != ' ') {
                                        sb.append(ch);
                                        newInd = newInd - 1; // fetch the next character
                                    } else
                                        break;
                                } else
                                    break;
                            }
                            // all characters appended
                            sb = sb.reverse();
                            fileNm = sb.toString();
                            fileNm = fileNm.concat(".").concat(ext);
                            // System.out.println(fileNm);
                            fileNames.add(fileNm);
                        }
                    } // if ext match ends
                    else
                        continue;

                } // for ends
            }
        } // if ends
        // removing the duplicate entries due to multiple occurrences in a file
        Set<String> hs = new HashSet<>();
        hs.addAll(fileNames);
        fileNames.clear();
        fileNames.addAll(hs);
        return fileNames;
    }

    private static HashMap<Integer, BugMap> formMapping(Integer bugEngineId, String bugId, ArrayList<String> fileNms,
                                                        String source, HashMap<Integer, BugMap> link) {
        Integer recordId = null;
        if (link == null) {
            link = new HashMap<Integer, BugMap>();
            recordId = 0;
        } else
            recordId = link.size();

        recordId = recordId + 1;
        for (int i = 0; i < fileNms.size(); i++) {
            String fileNm = fileNms.get(i);
            // create a BugMap object storing the mapping information
            BugMap obj = new BugMap(bugEngineId.toString(), bugId, fileNm, source);
            if (fileNm != null)
                link.put(recordId + i, obj); // first record
        }
        return link;
    }

    private static HashMap<Integer, BugMap> extractPatchLinkedFile(Integer bugEngineId, File filePath,
                                                                   HashMap<Integer, BugMap> link) { // extracts the bugIds and the fileNames from the patches and returns the
        // mapping
        // the function is called for each file in the patch folder
        String bugId = filePath.getName();
        System.out.println(bugId);
        Integer ind = bugId.indexOf('_');
        bugId = bugId.substring(0, ind);
        String fileContent = null;
        //FeatureExtractionTask t = new FeatureExtractionTask();
        fileContent = FileIterator.readFile(filePath);
        ArrayList<String> linkedFiles = extractFileNm(fileContent); // extracts all the filenames found in this
        // particular patch file and returns as array
        link = formMapping(bugEngineId, bugId, linkedFiles, "patch", link);
        return link;
    }

    public static HashMap<Integer, BugMap> findPatchLinkedFiles(String patchFolder) {// patchFolder is the bugIds folder
        // for each patch file, a mapping is formed and returned
        HashMap<Integer, BugMap> mapping = null;
        File dir = new File(patchFolder);
        Integer bugEngineId = 0;
        File[] subFolders = dir.listFiles(); // will list all 3 sub folders
        String bugEngine = null;
        //Integer undIndex = null;
        String patchFileName = null;
        if (subFolders != null) {
            // for each pathname in pathname array
            for (File subFolder : subFolders) {
                if (subFolder.isDirectory()) {
                    // present in one of the eclipsePaho, apache or gcc folder
                    bugEngine = subFolder.getName();
                    if (bugEngine.charAt(0) == 'g') {
                        bugEngineId = 1;
                        System.out.println("g");
                    } else if (bugEngine.charAt(0) == 'e') {
                        bugEngineId = 3;
                        System.out.println("e");
                    } else if (bugEngine.charAt(0) == 'a') {
                        bugEngineId = 2;
                        System.out.println("a");
                    } else if (bugEngine.charAt(0) == 'p') {
                        bugEngineId = 4;
                        System.out.println("p");
                    } else {
                        System.out.println("Wrong Folder");
                    }
                    File[] subDirectoryListing = subFolder.listFiles();
                    // now we will move into the patch folders present as the subfolders in these
                    // respective folders
                    for (File subSubFolder : subDirectoryListing) {
                        if (subSubFolder.isDirectory() && subSubFolder.getName().equals("patches")) {
                            // we are within this patches folder which contains all the code patches
                            // extracted as text files
                            // just a check to take care of some ambiguous fileNames
                            File[] patchFileListing = subSubFolder.listFiles();
                            for (File patchFile : patchFileListing) {
                                patchFileName = patchFile.getName();
                                System.out.println(patchFileName);
                                mapping = extractPatchLinkedFile(bugEngineId, patchFile, mapping);
                            }
                        }
                    }
                }
            }
        } else
            System.out.println("No files present");
        // removing duplicate mappings
        // removing the duplicate entries due to multiple occurrences of a file name in
        // a patch
        return mapping;
    }

    public static HashMap<Integer, BugMap> findSummaryLinkedFiles() {
        HashMap<Integer, BugMap> mapping = null;
        return mapping;
    }

    public static HashMap<Integer, BugMap> findCommentLinkedFiles(String commentFolder) {
        HashMap<Integer, BugMap> mapping = null;
        return mapping;
    }

    public static void getLinkedBugIds() {
        DataAccessObject dao = new DataAccessObject();
        // fetch the bug ids whose patches are available
        dao.fetchPatchBugIds();

    }

    public static void establishCodeBugMapping(String bugIdPatchFolder) // , String commentFolder)
    {
        HashMap<Integer, BugMap> mapping = null;
        HashMap<Integer, BugMap> patchMaps = null;
        HashMap<Integer, BugMap> summaryMaps = null;
        HashMap<Integer, BugMap> commentMaps = null;
        ArrayList<String> bugIds = new ArrayList<String>();
        // extract the linkages between bugIds and FileNames through patches summary and
        // comments
        // Through Patches
        patchMaps = findPatchLinkedFiles(bugIdPatchFolder); // patches folder
        /*
         * //Through Summaries summaryMaps = findSummaryLinkedFiles(); //from the
         * database //Through Comments commentMaps =
         * findCommentLinkedFiles(commentFolder); //comments folder if(summaryMaps!=null
         * || commentMaps!=null) { //merge the hashmaps } else {
         */
        // insert the mapping
        DataAccessObject dao = new DataAccessObject();
        // after that we extract patches corresponding to these bugId using python
        if (patchMaps != null && patchMaps.size() > 0) { // inserting the maps of bugIds and the extracted fileNames (from patches) in
            // the form of a mapping table
            dao.insertBugMap(patchMaps);
        }
        System.out.println("All Done!");
        // }
    }

    public static void getLinkedFiles(Integer queryFlag, String language, String type) {
        DataAccessObject dao = new DataAccessObject();
        HashMap<Integer, BugMap> mapping = null; // will also be required while linking with metadata
        HashMap<String, Integer> featureIdMap = null; // mapping of feature names to unique ids
        HashMap<String, Integer> fileNameIdMap = null; // mapping of filenames and Is
        HashMap<String, Integer> productIdMap = null; // mapping of filenames and Is
        HashMap<String, Integer> constructIdMap = null; // mapping of filenames and Is
        HashMap<String, Integer> linkedFileIdMap = null; // mapping of filenames and Is

        HashMap<String, Float> featureValMap = null;
        String fileName = "FullNameMapping.txt";
        // fetch complete file names and bugIds corresponding to them
        // you can pass on the language you want to
        // String tableName="SubRecords";
        // fileNameIdMap = dao.insertFileIdMap(tableName);

        // fileNameIdMap=dao.getFileIdMap();
        // System.out.println("fetched the FileId map"+fileNameIdMap.size());
        // productIdMap = dao.insertProductIdMap();
        // productIdMap = dao.getProductIdMap();
        // System.out.println("fetched the product id map"+productIdMap.size());
        constructIdMap = dao.getConstructIdMap();
        System.out.println("fetched the ConstructId map");

        // mapping = dao.getBugFileMapping(fileName, productIdMap);
        // System.out.println("fetched mapping");

        // System.out.println("fetched the BugfileId map");
        // mapping also represents a hashMap containing unique ids for bugId<-> FileName
        // pairs <-> hASHMAP ID
        // Now lets assign ids to all kind of features possible
        // featureIdMap = dao.getFeatureIdMap();
        // linkedFileIdMap=dao.insertLinkedFileIdMap();
        // linkedFileIdMap=dao.getLinkedFileIdMap();
        // A file may be linked to multiple bugIds, but features of that file would
        // remain same
        // Later we can link this linkedFileIdMap with the CombinedLinkedFileIdentifier
        // to find the other related meta info like product, bugId etc.
        featureValMap = dao.getFeatures(constructIdMap, queryFlag, language, type);
        System.out.println("fetched the featureId map");

        // scope left for fetching corresponding bugIds and source from the same file to
        // perform clustering of bugIds or provide corresponding met info
        // we get features corresponding to each of this unique pair as a unique row
        // feature vector
        /*
         * featureValMap = dao.getFeatures(mapping, featureIdMap, fileNameIdMap,
         * projectIdMap); //changed System.out.println("fetched the feature map");
         * System.out.println("Size: "+featureValMap.size());
         */
    }

    public static void main(String[] args) throws Exception {
        // arg[0] contains the input folder determined by the task to be done
        // Data folder contains code files and Bug_Data folder contains data extracted
        // from various bug reports
        if (true) // can put gcc-master instead of Data to test
        {
            System.out.println(args[0]);
            processCodeFiles(args[0]);
        }
        else
            System.out.println("Wrong Data Input");
    }// main function ends
}// class ends
