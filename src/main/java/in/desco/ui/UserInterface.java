/**
 * <b>in.desco.ui package:</b>
 * This package contains the user-interface files of the Desco tool.
 */

package in.desco.ui;

/**
 * <b>Import Statements:</b>
 * Since the class deals with interacting with the user and calling the DescoTool with the
 * specified input parameters, various Input/ Output imports are added to it.
 *
 *  @see java.io.File
 *  @see java.io.IOException
 *  @see  java.util.Scanner
 *  @see  in.desco.tool.DescoTool
 *  @see in.dataset.FileIterator
 */


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import in.desco.tool.DescoTool;

/**
 * This class contains functions which help in fetching the inputs from the user
 * and then calling the Desco tool with the specified input parameters. The user
 * herein specifies the path of a test file which needs to be tested for the
 * defectiveness, and the location of the backEndFolder containing the necessary
 * Machine Learning (ML) models to perform the defect estimates. It uses the
 * FileIterator class to perform various file operations and the DescoTool class
 * to perform the defect estimation.
 *
 * @see in.desco.tool.DescoTool
 */
public class UserInterface {

    /**
     * This function helps in interacting with the user to fetch various input
     * parameters required for performing the defect estimation process.
     * <p>
     * It performs the following functions:<br>
     * 1) Fetches the path of the test file and the backEndFolder from the user <br>
     * 2) Reads the file content of the test file located at the path specified in
     * Step 1 <br>
     * 3) Calls a DescoTool instance with the input parameters fetched in Step 1 and
     * 2 to perform the defect estimation
     *
     * @param args command line arguments (not used currently).
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        args = new String[]{"/Users/auser/work/ritu_work/backEndFolder", "/Users/auser/work/ritu_work/desco/src/main/java/in/desco/ui/UserInterface.java"};
        if (args.length < 2) {
            System.out.println("\nUsage: java -jar " + findJar(UserInterface.class) +
                    " <back-end folder path> <input file path>\n");
            return;
        }
        DescoTool tool = new DescoTool(args[0]);
        File inpFile = new File(args[1]);
        // String fileContent = FileIterator.readFile(inpFile);
        String lang = args[1].substring(args[1].lastIndexOf(".")+1);
        // tool.performDefectEstimation(fileContent, ext);

        System.out.println("Results:\n" +
                tool.performDefectEstimation(inpFile, lang));
    }

    public static String findJar(Class<?> context) throws IllegalStateException {
        URL location = context.getResource('/' + context.getName().replace(".", "/") + ".class");
        String jarPath = location.getPath();
        if (jarPath.contains(".jar")) {
            return jarPath.substring("file:".length(), jarPath.lastIndexOf("!"));
        } else {
            return jarPath;
        }
    }

}
