package in.dataset;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *This class helps in performing various file iteration functions, fetching the sub-folder paths, 
 * extracting the file extensions, etc.
 */
public class FileIterator
{
	/**
	 * The function inserts all the absolute paths of a project in a projectQueue
	 * and returns this projectQueue as a LinkedList<File> object 
	 * <p>
	 * The function returns null in case the input folder is not a valid directory path or the input folder is empty
	 * and prints a message in both these scenarios
	 * 
	 * @param folderNm absolute path of the folder containing the repositories to be processed
	 * @return the collection of all the absolute paths of project repositories present in the input folder as a LinkedList<File> object
	 * @see java.util.LinkedList<File>
	 * @see java.io.File#isDirectory()
	 * @see java.io.File#listFiles()
	 */
	public static LinkedList<File> extractProjectPaths(String folderNm)
	{ 
		/**
		 * folderNm contains a collection of repositories (or directories)
		 * projectPaths is the list of absolute paths of all the directories or folders present within folderNm
		 */
		File dir = new File(folderNm); //convert the String type to File type
		LinkedList<File> projectPaths = null;
		if(dir.isDirectory())
		{
			System.out.println("<<here");
			//list all the abstract path names present in dir
			File[] directoryListing = dir.listFiles();
			if (directoryListing != null) // i.e. the folder does contain some projects
			{
				projectPaths = new LinkedList<File>();	//initializing the list
				//iterate through all the files or folders present
				for (File project : directoryListing)
				{
					System.out.println(project);
					// a check to ensure that only folder names are encountered
					if (project.isDirectory())
						projectPaths.add(project);
				}
				if (projectPaths == null || projectPaths.isEmpty())
					projectPaths.add(dir);
			}
			else
				System.out.println("The input directory is empty");
		}
		else
			System.out.println("Input entered is not a valid directory path");
		System.out.println(projectPaths);
		return projectPaths;	//returns null in case of else or in case of an empty directory
	}

	/**
	 *	The function is used to fetch absolute paths of all the files present in the input directory (or folder). 
	 *	It returns the extracted file paths in the form of an ArrayList<File> object.
	 * <p>
	 * The function iterates through all the sub-folders present in the input folder, thus fetching the complete list 
	 * of source files present in the current input folder.
	 * 
	 * @param projectPath an open source repository which contains a collection of source files 
	 * @return an ArrayList<File> type object containing the complete list of files present in the input folder
	 * @see java.util.LinkedList<File>
	 * @see java.util.ArrayList<File>
	 * @see java.io.File#isDirectory()
	 * @see java.io.File#listFiles()
	 * @see java.io.File#isFile()
	 */
	public static ArrayList<File> extractFilePaths(File projectPath)
	{ 
		/**
		 * I/P is a Project folder path
		 *Function Inserts the paths of all the files present in this folder in an
		 *ArrayList type FileList and
		 *Inserts all the SubFolder names in a subFolder linked list
		 *It is then recursively called for all the subfolders, whose paths are stored
		 *in a subFolder arraylist
		*/
		if (projectPath != null)
		{
			LinkedList<File> subFolderList = null;		//list of all subFolders present in the input folder 
			ArrayList<File> fileList = null;	//list to store the absolute file paths
			ArrayList<File> subFolderFileList = null; //list of files present in a certain subFolder
			File[] directoryListing = projectPath.listFiles();
			if (directoryListing != null) // i.e. the project does contain some files/folders
			{
				subFolderList = new LinkedList<File>();
				fileList = new ArrayList<File>();
				for (File path : directoryListing) //iterate through the complete list of paths
				{
					// check if the element is a folder or a file
					if (path.isDirectory()) // if it is folder
					{// store in the SubFolder queue
						subFolderList.add(path); 
					} else if (path.isFile()) // if it is a file
					{ // store in the FileQueue
						fileList.add(path);
					} else
						continue;
				}
				while (!subFolderList.isEmpty())
				{
					File subFolderName = subFolderList.removeFirst();
					/**
					 * creating a new list to fetch all the files in this
					 * SubFolder as ArrayList doesn't work for appending in
					 * the same list
					 */
					subFolderFileList = new ArrayList<File>(); 
					subFolderFileList = extractFilePaths(subFolderName);
					fileList.addAll(subFolderFileList);
				}
			}
			return fileList;
		} else
			return null;
	}

	/**
	 * This function is used to extract the extension of a source code file from its fileName.
	 * For instance, for an input file name as /home/user/repo1/foo.java,
	 * the function would return java.
	 * 
	 * @param fullFileName	absolute path of a file
	 * @return the extension of the input file
	 * @see java.lang.String#lastIndexOf(char)
	 * @see java.lang.String#substring(int)
	 */
	public static String getExt(String fullFileName)
	{
		/**
		 * This function extracts the extension of a code file from its fileName
		 * fullFileName	contains the absolute path of a source file
		 */
		int lastInd = fullFileName.lastIndexOf('.');	//find the index of '.'
		if(lastInd > 0)
			return fullFileName.substring(lastInd+1);
		else
		{
			System.out.println("A folder or an informatory file (README or a LICENCE file).");
			return null; // for files with no extension e.g. readme, LICENCE
		}
	}
	/**
	 * The function is used to read  a source file. The function returns the complete file content 
	 * contained in a String object. The absolute path of the file to be read is provided as 
	 * input in the form of a File object. 
	 * 
	 * @param filePath	a File object containing the absolutre path of the file to be read
	 * @return	the complete file content in the form of a String object.
	 * @see java.io.BufferedReader#BufferedReader(java.io.Reader)
	 * @see java.lang.System#lineSeparator()
	 * @see java.io.BufferedReader#readLine()
	 * @see java.lang.StringBuilder
	 * @see java.lang.StringBuilder#toString() 
	 * @see java.io.IOException
	 */
	public static String readFile(File filePath)
	{
    	/**
    	 * Function to read a text file
    	 * The file content read is returned as a String
    	 */
		String fileContent=null;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader (filePath));
			StringBuilder sb = new StringBuilder();
			String line;
			line = br.readLine();
			while (line != null) 
			{
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			br.close();
			fileContent = sb.toString();
		} 
		catch (IOException e) { e.printStackTrace(); }
		return fileContent;
	}
	
}
