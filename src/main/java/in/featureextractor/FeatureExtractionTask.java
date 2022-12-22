package in.featureextractor;

import in.dao.*;
import in.datatype.*;
import in.dataset.FileIterator;
import in.lexpar.*;
import java.io.File;
import java.util.HashMap;
import java.util.Set;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import in.desco.tool.Constants;

/**
 *<b>Import statements:</b> The import statements include various 
 * auto-generated classes from ANTLR, FeatureExtractor classes 
 * corresponding to various languages, datatype classes, and java 
 * io and util collections.
 *@see in.lexpar
 *@see java.util
 * @see java.io
 *@see in.datatype
 */

/**
 * FeatureExtractionTask represents a Thread performing feature extraction
 * on an input source file. This class also contains various delegating 
 * functions called from different FeatureExractor classes of different
 * programming languages. The end results returned is a HashMap<CodeInfo,
 * Integer>, which stores the measure values corresponding to lexical 
 * properties of various programming constructs. The Integer field of 
 * this HashMAp stores this measure value, while the CodeInfo field stores
 * the description of the associated construct, measure, parent, etc.
 * @see datatype.CodeInfo
 *
 */
public class FeatureExtractionTask implements Runnable {

    //private data members
    private File filePath;
    private String fileName;
    private String fileContent;
    private String projectName;
    private String phase;

    //public data members of the class
    private int globalCId;    //global variable to keep a track of construct Ids globally
    private int globalTypId;    //global variable to keep a track of Type Ids globally
    int funcId;    //global variable to keep a track of Function Ids globally
    int structId;    //global variable to keep a track of Structure Ids globally
    int classId;
    int pId;
  /**
   * a HashMap containing measure values corresponding to lexical properties 
   * of various programming constructs. The measure values are stored 
   * corresponding to all occurrences of every programming construct. 
   * For instance, when storing the <b>length</b>measure value corresponding
   * to the programming construct <b>if</b>, we store it for all the
   * occurrences of <b>if</b> construct within the source file. 
   */
    private HashMap<CodeInfo, Integer> hm;	 
    private String fileExt;

    /**
     * public member function used to return the private HashMap hm.
     * @return a HashMap containing measure values corresponding to lexical
     * properties of various programming constructs.
     */
    public HashMap<CodeInfo, Integer> getHashmap() {
    	return this.hm;}

    /**
     * Function to increment the Construct Id. A globalCId keeps a global
     * check of construct Ids being assigned. The function fetches the
     * current value of globalCId, increments it and returns the same.
     * @return an integer value, representing a unique id for the calling 
     * construct.
     */
    int incrCId() {
        this.globalCId = this.globalCId + 1;
        return this.globalCId;
    }

    /**
     * Function to increment the Construct Type Id. A globalTypeId keeps
     * a global check of construct Type Id being assigned. A type id basically
     * represents the identifier assigned on the basis of the type of programmng
     * construct. For instance, every <b>class</b> will now have the same 
     * <b>constructTypId</b>, say <b>1001</b>.  The function fetches the current
     * value of globalCId, increments it and returns the same.
     * @return an integer value, representing a unique id for the calling construct.
     */
    private int incrGlblTypId() {
        this.globalTypId = this.globalTypId + 1;
        return this.globalTypId;
    }


    /**
     * The function is used to fetch the type Id corresponding to an input construct
     * name. The function checks if the input construct name already exists in the
     * collection. If it does, it returns its type, else it increments the globalTypId
     * and returns the same.
     * @param constructNm	a String type object storing the name of the construct,
     * whose type Id needs to be fetched
     * @return a type Id for the input construct name
     * @see #incrGlblTypId()
     * @see CodeInfo#getTypId()
     */
    int incrTypId(String constructNm) {
    	//we set the class type Id as 1001 and function Id as 1000
    	if(constructNm.equals("cu"))
    		return Constants.cuStaticId;
    	if(constructNm.equals("class"))
    		return Constants.classStaticId;
    	else if(constructNm.equals("function"))
    		return Constants.funcStaticId;
    	else
    	{
    		// Function to increment the Construct Type Ids
    		Set<CodeInfo> keys = this.getHashmap().keySet();    //fetch the keys set of the hashmap
    		String constructName = null;
    		Integer Id = null;
    		for (CodeInfo p : keys) {//fetch all the exiting construct types
    			constructName = p.getConstructNm();	//fetch the construct name of the current key
    			if (constructName.equalsIgnoreCase(constructNm)) {	//compare the construct names
    				//if the construct names match
    				Id = p.getTypId();	//fetch the type Id of the matched construct name
    				break;
    			}//IF ENDS
    		}//for ends
    		if (Id == null) //first occurrence
    			Id = this.incrGlblTypId();	//increment the global type Id and fetch the same
    		return Id;	//return the type Id of the input construct
    	}//outer else ends
    }

    /**
     * This function is used to increment the occurrence count of a CodeInfo
     * object with input parameters. The function checks for the existence of
     * the input object in the hash map containing features. If the object 
     * already exists, its count is fetched, incremented and updated. Otherwise,
     * a new object is created and its count is stored.
     * @param obj a CodeInfo object whose occurrence count needs to be incremented
     * @see in.datatype.CodeInfo#isEqual(HashMap)
     */
    public void incrOccCount(int cId, int typId, String constructNm, int pId) {
    	int val = 0;
    	//build the object with input parameters
    	CodeInfo obj = new CodeInfo(cId, typId, constructNm, pId, "count");
		
        CodeInfo robj;
        robj = obj.isEqual(this.hm);    //Find if an entry exists corresponding to the calling object
        //robj now stores a matching object (if a match is found) or the current object (obj) (is no match is found in the hashmap)
        if (!robj.equals(obj))    //is a match was found in the hashmap
        {
            val = this.hm.get(robj);    //get the occurrence count of the object
        }
        val = val + 1;    // Increment Occurrence count
        this.hm.put(robj, val);    //update (or store in case of first occurrence) the occurrence count of the construct
    }

    /**
     * Function to insert the information about depth, count and length of
     * the construct
     * @param constructNm	a String type object storing the construct name of
     * the construct whose information is to be stored
     * @param depth an int type object storing the depth of the construct in
     * the parse tree of the source code
     * @param text a String type object storing the text of the calling object
     * @param pId an int type object storing the parent Id (construct Id of the 
     * parent node) of the calling object
     */
    public int insertCountDepthLength(String constructNm, String text, int pId)
    {
    	int cId;
    	
    	if(this.structId>0 && (constructNm.equals("struct") || constructNm.equals("union")))
    		cId = this.structId;
    	else
    		cId = this.incrCId();
    	int typId = this.incrTypId(constructNm);
        this.incrOccCount(cId, typId, constructNm, pId);
        //build the object for storing the depth property
        CodeInfo obj = new CodeInfo(cId, typId, constructNm, pId, "depth");
        //System.out.println("till here insertCountDepthLength");
        //this.hm.put(obj, depth); //depth stored in hash map
        //inserting length
        //build the object for storing the length property
        obj = new CodeInfo(cId, typId, constructNm, pId, "length");
        this.hm.put(obj, text.trim().length()); //length stored in hash map
        //System.out.println("Now returning cId");
        return cId;
    }
    
    /**
     * Function to fetch operand Id for an operand provided as input. The
     * function checks from the existing collection for the pre-existence
     * of the input operand construct. If it exists, its constructId is
     * fetched and returned, else the globalCId is incremented
     * (using incrCId) and returned.
     * 
     * @param operandNm a String type object storing the name of the operand
     * whose operand Id needs to be fetched
     * @return an int type object storing the operand Id for the input operand name
     * @see CodeInfo#getCId()
     * @see CodeInfo#getConstructNm()
     */
    private int getOpId(String operandNm) {
    	//Fetching all the keys from the HashMap
        Set<CodeInfo> keys = this.hm.keySet(); 
        String constructName = null;
        Integer constructId = null;
      //iterating over all the keys
        for (CodeInfo p : keys) 
        {
            constructName = p.getConstructNm();
            //if the construct name matches, fetch the corresponding key
            if (constructName.equalsIgnoreCase(operandNm))  
            {// get construct Id for the specific construct name
                constructId = p.getCId(); 
                break;
            }
        }
      //if no entry present
        if (constructId == null)    
        {//Increment the Construct Id
            constructId = this.incrCId();    
        }
        //return the constructId
        return constructId;
    }

    
    /**
     * public member function to check the count of various operator type present
     * in a particular input. For instance, the input could be a function body,
     * a class body, a code block, an iterative statement, etc. The input parameter
     * s represents this input text body. The input here is completely free from
     * blank spaces or newline characters.
     * <p>
     * The function iterates through each character of the input text to discover
     * various operators and keeps storing their respective counts. The function
     * uses the insertOpCount function to store the operand counts.
     * 
     * @param s a String object storing the complete text of the calling construct,
     * within which the operator occurrences need to be checked. 	
     * @param prtId	an int type object storing the parent Id of the current set of 
     * operands.
     * @see #insertOpCount(String, int)
     */
    void opCountCheck(String s, int prtId)
    {
       int len = s.length();    //find the length of the input body. For instance, function body, class body, etc.
        String constructName = null;
        int flag;
        char ch1, ch2, ch3;
        ch2 = '$';
        ch3 = '$';
       //we identify different constructs and assign construct names
        for (int i = 0; i < len; i++)    //check corresponding to every position in the input string
        {
            ch1 = s.charAt(i);
            if ((i + 1) < len)
                ch2 = s.charAt(i + 1);
            if ((i + 2) < len)
                ch3 = s.charAt(i + 2);
            flag = 0;
            if (ch1 == '+' && ch2 != '+' && ch2 != '=' && ch2 != ')') //Making sure that it is not a prefix or postfix operator or shortform assignment operator
                constructName = "plusOp";
            else if (ch1 == '-' && ch2 != '-' && ch2 != '=') //Making sure that it is not a prefix or postfix operator or shortform assignment operator
                constructName = "minusOp";
            else if (ch1 == '*' && ch2 != '=' && ((ch2 != ' ' && ch3 != ' ') && ch2 != '\n'))//making sure that t is not a shorthand assignment notation
                constructName = "mulOp";
            else if (ch1 == '/' && ch2 != '=')//check for shorthand assignment
                constructName = "divOp";
            else if (ch1 == '=' && ch2 != '=') //check for equality conditional operator
                constructName = "asgnOp";
            else if (ch1 == '%' && ch2 != '=') //short hand notation check
                constructName = "modOp";
            else if (ch1 == '+' && ch2 == '+' && (ch3 == ';' || ch3 == ')')) //a++;
                constructName = "postIncrOp";
            else if (ch1 == '-' && ch2 == '-' && ch3 == ';') //a--;
                constructName = "postDecrOp";
            else if (ch1 == '+' && ch2 == '+' && ch3 != ';') //++a;
                constructName = "preIncrOp";
            else if (ch1 == '-' && ch2 == '-' && ch3 != ';') //--a;
                constructName = "preDecrOp";
            else if (ch1 == '+' && ch2 == '=') // a += 1
                constructName = "cmpndSumAsgnOp";
            else if (ch1 == '-' && ch2 == '=') // a -= 1
                constructName = "cmpndMinusAsgnOp";
            else if (ch1 == '/' && ch2 == '=') // a /= 1
                constructName = "cmpndDivisionAsgnOp";
            else if (ch1 == '%' && ch2 == '=') // a %= 1
                constructName = "cmpndModAsgnOp";
            else if (ch1 == '*' && ch2 == '=') // a *= 1
                constructName = "cmpndMultAsgnOp";
            else if (ch1 == '<' && ch2 == '<' && ch3 == '=') // <<a;
                constructName = "cmpndLeftShiftAsgnOp";
			else if (ch1 == '&' && ch2 == '=')
                constructName = "cmpndBitwiseAndAsgnOp";
            else if (ch1 == '|' && ch2 == '=')
                constructName = "cmpndBitwiseOrAsgnOp";
            else if (ch1 == '^' && ch2 == '=')
                constructName = "cmpndBitwiseXorAsgnOp";
            else if (ch1 == '-' && ch2 == '>')
                constructName = "arrowOp";
            else if (ch1 == '&' && ch2 == '&')
                constructName = "logicalAndOp";
            else if (ch1 == '|' && ch2 == '|')
                constructName = "logicalOrOp";
            else if (ch1 == '^' && ch2 != '=')
                constructName = "caretOp";
            else if (ch1 == '~')
                constructName = "negOp";
            else if (ch1 == '!' && ch2 != '=')
                constructName = "notOp";
            else if (ch1 == '=' && ch2 == '=')
                constructName = "equalOp";
            else if (ch1 == '!' && ch2 == '=')
                constructName = "notEqualOp";
            else if (ch1 == '<' && ch2 != '>' && ch2 != '<' && ch2 != '=' && ch2 != 'E') //E for EOF
                constructName = "lessOp";
            else
                flag = 1;
            if (flag == 0)
            	this.incrOccCount(this.getOpId(constructName), this.incrTypId(constructName), constructName, prtId);    //increment the operand count
        }// for ends
    }// function ends

    

    /**
     * This function inserts the length of the code block.  
     * It just counts the characters present in total in the code block body. 
     * It further checks for the presence of operator
     * occurrences within the calling code block and stores the same using 
     * opCountCheck.
     * 
     * @param pId	an int type object storing the parent Id of the current 
     * code block
     * @param length	an int type object storing the length of the current 
     * code block
     * @param s a String type object storing the complete text of the calling
     * code block construct 
     * @see #opCountCheck(String, int)
     */
    private void insertCodeBlockLength(int pId, int length, String s) {
        int constructId = this.incrCId(); //increment construct id and fetch the same
        //increment type id and fetch the same - > this.incrTypId(constructName)
        //build the object
        CodeInfo obj = new CodeInfo(constructId, this.incrTypId("codeBlocks"), "codeBlocks", pId, "length");
        this.hm.put(obj, length); //insert the length value
        this.opCountCheck(s, constructId); //operands within this code block or parentId=constructId of this block
    }

    /**
     * This function is used to count the sub-code blocks present 
     * within a code block. we assume that a code block cannot be more
     * than this (2000 characters) length
     * @param s  a String type object storing the complete text of the 
     * calling code block construct.
     * @param pId	an int type object storing the parent Id of the current
     * code block.
     */
    private void codeBlocksWithinConstructLength(String s, int pId) {
        int[] strtPos = new int[2000];  
        int strtPosCount = 0;
        int[] endPos = new int[2000];
        int endPosCount = 0;
        int length = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '{')
                strtPos[strtPosCount++] = i;
            else if (s.charAt(i) == '}')
                endPos[endPosCount++] = i;
        }
        int k = 0, l = 0;

        while (k != strtPosCount && l != endPosCount) {
            if (strtPos[k + 1] < endPos[l]) {
                while ((strtPos[k + 1] < endPos[l]) && k < strtPosCount - 1) {
                    k = k + 1;
                    if (k == strtPosCount - 1)
                        break;
                }
                if (endPos[l] > strtPos[k])
                    length = endPos[l] - strtPos[k];
                else
                    length = strtPos[k] - endPos[l];

                this.insertCodeBlockLength(pId, length, s);
                //OVERWITING THESE INDEXES AND DECREASING THE COUNTS OF ELEMENTS
                for (int y = k; y < strtPosCount - 1; y++)
                    strtPos[y] = strtPos[y + 1];
                strtPosCount--;

                for (int y = l; y < endPosCount - 1; y++)
                    endPos[y] = endPos[y + 1];
                endPosCount--;

                k--; //starting from one index back due to overwriting

                if (strtPosCount == 1) {
                    //only last outer block left
                    if (endPos[0] > strtPos[0])
                        length = endPos[0] - strtPos[0];
                    else
                        length = strtPos[0] - endPos[0];

                    this.insertCodeBlockLength(pId, length, s); //insert the length of this sub-code block
                    break;
                }
            }//outer if ends
            else {
                if (endPos[l] > strtPos[k])
                    length = endPos[l] - strtPos[k];
                else
                    length = strtPos[k] - endPos[l];

                this.insertCodeBlockLength(pId, length, s); //insert the length of this sub-code block
                if (k != 0)
                    k--;
                for (int y = k; y < strtPosCount - 1; y++)
                    strtPos[y] = strtPos[y + 1];
                strtPosCount--;
                for (int y = l; y < endPosCount - 1; y++)
                    endPos[y] = endPos[y + 1];
                endPosCount--;
            }//else ends
        }//while ends
    }//function ends

    /**
     * This function is created to insert the code block information.
     * @param s	a String type object storing the complete text of the
     * calling code block construct.
     * @param pId	an int type object storing the parent Id of the
     * current code block.
     * @param depth	an int type object storing the depth of the current
     * code block construct within a parse tree.
     * @see #insertCountDepthLength(String, int, String, int)
     * @see #codeBlocksWithinConstructLength(String, int)
     */
    void insertCodeBlockInfo(String s, int pId, int depth) {
    	int codeBlocks = s.split("[{]").length -1;
        //build the object
    	this.insertCountDepthLength("codeBlocks", s, pId);
        if (codeBlocks > 0) {    //if more code blocks present
            this.codeBlocksWithinConstructLength(s, pId);
        }
        
	}

   

    /**
     * This function stores the length of the input source file.
     * The total  count of characters is used as the file length. 
     * This is required while normalizing the counts. 
     * and various measures of constructs.
     * 
     * @param fileLength an int type object representing the file
     * length of the calling file type construct.
     */
    private void insertFileLength(int fileLength) {
        /**
         * This function stores the length of the input source file
         * The total  count of characters is used as the file length
         * We need this while normalizing the counts and various measures
         * of constructs.
         */
       //file has no parent. So pId is made 0
        //build the object
        CodeInfo obj = new CodeInfo(this.incrCId(), this.incrTypId("file"), "file", 0, "length");
        this.hm.put(obj, fileLength); //store file length
    }

    /**
     * This function is used to assign different construct names by
     * using node definitions while walking the parse tree.
     *  
     * @param node a String type object representing the name of the
     * calling construct.
     * @return a String type object representing the name of the required
     * construct name.
     */
    String findConstructName(String node) {
       if(node.contains("do"))
        	return "do_while";
        else if(node.contains("decorated"))
        	return "decorated";
        else if(node.contains("while"))
        	return "while";
        else if(node.contains("with"))
        	return "with_stmt";
        else if(node.contains("for"))
        	return "for";
        else if(node.contains("if"))
        	return "if";
        else if(node.contains("switch"))
        	return "switch";
        else if(node.contains("switch"))
        	return "static";
        else if(node.contains("struct"))
        	return "struct";
        else if(node.contains("synch"))
        	return "synchronized";
        else if(node.contains("dec"))
        	return "decorated";
        else if(node.contains("return"))
        	return "return";
        else if (node.contains("go"))
            return "goto";
        else if(node.contains("continue"))
        	return "continue";
        else if(node.contains("catch"))
        	return "catch";
        else if(node.contains("break"))
        	return "break";
        else if(node.contains("extern"))
        	return "extern";
        else if(node.contains("enum"))
        	return "enum";
        else if(node.contains("typedef"))
        	return "typedef";
        else if(node.contains("try"))
        	return "try";
        else if(node.contains("throw"))
        	return "throw";
        else if(node.contains("union"))
        	return "union";
        else
        	return null;
      }

    /**
     * Function to store the information about the iterative and selection
     * statements.
     * 
     * @param orgConstructName a String type object, represents a category
     * of construct name. For instance, select_stmt, iter_stat, etc.
     * @param depth an int type object storing the depth of the calling 
     * construct within a parse tree.
     * @param s a String type object storing the complete text body of the
     * calling construct.	
     * @see #insertCountDepthLength(String, int, String, int)
     * @see #insertCodeBlockInfo(String, int, int)
     * @see #initParentId()
     */
    void insertIter_SelectInfo(String orgConstructName, int depth, String s) {
        /**
         * orgConstructName represents a category construct name here. For instance,
         * select_stmt, iter_stat, etc.
         * constructName would give the specific construct's name within this category.
         * For instance, if, for, switch, etc.
         */
    	this.initParentId();
    	this.insertCountDepthLength(orgConstructName, s, this.pId);
    	//storing the specific construct info. For instance, if construct under selection statements.
    	String constructName = this.findConstructName(s);
    	int cId = this.insertCountDepthLength(constructName, s, this.pId);
        this.opCountCheck(s, cId);
        //finding code blocks
        this.insertCodeBlockInfo(s, cId, depth);
    }


    

    /**
     * Function to return the private data member fileName
     * @return a String type object storing the required fileName
     */
    public String getFileName() {
        return this.fileName;
    }
    
    /**
     * Function to fetch the project name from its complete path name. Project
     * name is same as the repository name. All the repositories are with the
     * Data folder. For instance, a repository path could be  Data/repo1/...,
     * and the returned project name would be repo1.
     * 
     * @param FilePath a String type object storing an absolute path name
     * @return a String type object containing the project name extracted from
     * the input path name.
     */
    private String fetchProjectName(String FilePath) {
        int strtInd = FilePath.indexOf('\\');
        String str = FilePath.substring(strtInd + 1);
        int endInd = str.indexOf('\\');
        str = FilePath.substring(strtInd, endInd);
        return str;
    }

    /**
     * Constructor used while building the dataset. 
     * @param FilePath a File type object containing the path of the file
     * being processed 
     * @param inputPhase a String type object storing the phase of processing.
     * For instance, test and train.
     * @param ext a String type object storing the extension of the file being
     * processed.
     */
    public FeatureExtractionTask(File FilePath, String inputPhase, String ext) {
        /**
         * constructor to initialize various variables
         * public static data member "HashMap" used to store the information of various
         * constructs
         */
        this.hm = new HashMap<CodeInfo, Integer>();
        this.filePath = FilePath;
        //System.out.println("here:"+filePath.toString());
        if (inputPhase.equalsIgnoreCase(Constants.TRAIN_PHASE)) {
            this.fileName = FilePath.getName();
            //FilePath.
            this.projectName = this.fetchProjectName(FilePath.toString());
            this.fileExt = FileIterator.getExt(fileName);
        } else    //testing phase
        {
            this.fileName = FilePath.toString();
            this.fileExt = ext;
        }
        this.phase = inputPhase;
    }

    /**
     * default constructor
     */
    public FeatureExtractionTask() {

    }

    /**
     * Constructor to initialize certain private data members. This is used in
     * case of testing phase. 
     * @param fileContent a String type object storing the fileContent, which 
     * needs to be checked for defectiveness
     * @param inputPhase a String type object storing the processing phase. 
     * For instance, test and train.
     * @param ext a String type object storing the extension of the file being processed.
     */
    public FeatureExtractionTask(String fileContent, String inputPhase, String ext) {
        this.hm = new HashMap<CodeInfo, Integer>();
        this.fileContent = fileContent;
        this.phase = inputPhase;
        this.fileExt = ext;
    }

    
    /**
     * Function to return the private data member filePath
     * @return a File type object storing the required absolute path of the file
     * being processed.
     */
    public File getFilePath() {
        //function to return the private variable filePath
        return this.filePath;
    }

    /**
     * function to display the project name, filname and complete file path
     * before starting the file processing
     */
    private void displayTask() {
        System.out.println("Project Name: " + this.projectName + " File Name: " + this.fileName + "\n Complete Path: " + this.filePath.toString());
    }

    /**
     * Function to call the C Feature Extractor and fetch the features.
     * The features fetched are retained in the CFeatureExtractor class 
     * object 'extractor'
     * @param tree a ParseTree class object storing the parse tree of the 
     * source file being processed.
     * @see in.featureextractor.CFeatureExtractor
     */


    /**
     * Function to call the Java Feature Extractor and fetch the features.
     * The features fetched are retained in the JavaFeatureExtractor class
     * object 'extractor'.
     * @param tree a ParseTree class object storing the parse tree of the
     * source file being processed.
     * @see in.featureextractor.JavaFeatureExtractor
     */
    private void getJavaExtractor(ParseTree tree) {
    	JavaFeatureExtractor extractor = new JavaFeatureExtractor(this);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(extractor, tree);
    }

    /**
     * Function to call the CPP Feature Extractor and fetch the features.
     * The features fetched are retained in the CPPFeatureExtractor class
     * object 'extractor'.
     * @param tree a ParseTree class object storing the parse tree of the 
     * source file being processed.
     * @see in.featureextractor.CPPFeatureExtractor
     */


    /**
     * Function to call the Python Feature Extractor and fetch the features.
     * The features fetched are retained in the PythonFeatureExtractor class
     * object 'extractor'.
     * @param tree a ParseTree class object storing the parse tree of the
     * source file being processed.
     * @see in.featureextractor.PythonFeatureExtractor
     */


 
    /**
     * Function to perform the parsing of the input source file content
     * of C programming language.
     * @param fileContent a String type object storing the content of a source file
     * @return a CParser class object representing the reference of the parser
     * containing the tokens and parse tree of the input content.
     */

    /**
     * Function to perform the parsing of the input source file content of
     * Java programming language.
     * @param fileContent a String type object storing the content of a
     * source file
     * @return a JavaParser class object representing the reference of the
     * parser containing the tokens and parse tree of the input content.
     */
    private JavaParser getJavaParser(String fileContent) {//Parsing function for Java source files
        ANTLRInputStream input = new ANTLRInputStream(fileContent);
        // create a lexer 
        JavaLexer lexer = new JavaLexer(input);
        //remove lexer warnings
        lexer.removeErrorListeners();

        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // create a parser that feeds off the tokens buffer
        JavaParser parser = new JavaParser(tokens);
        //remove parser warnings
        parser.removeErrorListeners();

        return parser;
    }

    /**
     * Function to perform the parsing of the input source file content
     * of C++ programming language.
     * @param fileContent a String type object storing the content of a
     * source file
     * @return a CPPParser class object representing the reference of
     * the parser containing the tokens and parse tree of the input content.
     */



    /**
     * Function to perform the parsing of the input source file content of
     * Python programmin language.
     * @param fileContent a String type object storing the content of a
     * source file
     * @return a PythonParser class object representing the reference of
     * the parser containing the tokens and parse tree of the input content.
     */



    /**
     * Function to execute the C feature extraction process on the input
     * fileContent.
     * @param fileContent a String type object storing the file content
     * to be processed.
     */


    /**
     * Function to execute the Java feature extraction process on the input
     * fileContent.
     * @param fileContent a String type object storing the file content to be processed.
     */
    private void extractJava_features() {
    //function to execute the Java feature extraction process
        this.insertFileLength(this.fileContent.length());
        // get the parseTree
        JavaParser parser = this.getJavaParser(this.fileContent);
        ParseTree tree = parser.compilationUnit(); // begin parsing at init rule
        this.getJavaExtractor(tree); //perform feature extraction
    }

    /**
     * Function to execute the C++ feature extraction process on the input
     * fileContent.
     * @param fileContent a String type object storing the file content to
     * be processed.
     */


    /**
     * Function to execute the Python feature extraction process on the
     * input fileContent.
     * @param fileContent a String type object storing the file content
     * to be processed.
     */


    /**
     * The function checks the extension of the input file and calls
     * the corresponding feature extractor
     * @param fileContent a String type object storing the file content
     * to be processed.
     */
    private void callCorrespondingFeatureExtractor() {
        String ext = this.fileExt;	//fetching the file extension
        if (ext.equalsIgnoreCase("c") || ext.equalsIgnoreCase("cc") || ext.equalsIgnoreCase("java") || ext.equalsIgnoreCase("py")) {
        	if (ext.equals("java")) {
                this.extractJava_features();
            } else {
                throw new UnsupportedOperationException("Unsupported file extension "+this.fileExt);
            }
        }
        else {
            throw new UnsupportedOperationException("Unsupported file extension "+this.fileExt);
        }
    }


    /**
     * Function to execute the parallel processing thread task
     */
    @Override public void run() {
    	if (this.phase.equals("train")) {
            this.displayTask();
            this.fileContent = FileIterator.readFile(this.filePath);
            System.out.println("Working on file: " +this.filePath.getName());
        }
        this.callCorrespondingFeatureExtractor();
        if (this.phase.equals("train")) {
            DataAccessObject dao = new DataAccessObject(this.filePath.toString(), this.hm);
            //we do not require dataset population in testing phase
            dao.insertData(); // we insert the features in the database
        }
    }
    
    /*
     *  As one exits a function, the parent Id is set to the parent
     * class or the root node (i.e. cu)
     *  and the funcId flag is set to 0
     */
    public void exitFunc()
    {
		if(this.classId!=0 )	//within a class
			this.pId=this.classId;
		else
			this.pId=1;	//construct id of cu
		this.funcId=0;
	}
    
    /**
	  * As we exit a class, we set the classId to 0 
	  * and set the parent as CU 
	  */
    public void exitClass()
	{
		 this.pId = 1; //construct Id of CU
		 this.classId=0;
	}
	
    /**
     * The function stores the information related to programming
     * variables.
     * @param text	a String type object storing the complete text
     * of the calling object.
     * @param depth an int type object storing the depth of the calling
     * construct within a parse tree.
     * @see #insertCountDepthLength(String, int, String, int)
     * @see #initParentId()
     */
    public void enterInitDeclarator(String text)
	{
		this.initParentId();
		// check if the variable is initialized and declared at the same time
		int index = text.indexOf('=');
		// we extract only variable name by removing the extra part 
        //from a variable initialization statement
		if (index>0)	
			text = text.substring(0, index).trim();
		//else we take the variable name as it is
		this.insertCountDepthLength("var", text, this.pId);
	}

	/**
	 * The function stores the try and catch block information.
	 * @param constructName a String type object storing the 
     * constructname of the calling node.
	 * @param text a String type object storing the complete text
     * of the calling object.
	 * @param depth an int type object storing the depth of the calling
     * construct within a parse tree.
	 * @see #initParentId()
	 * @see #insertCountDepthLength(String, int, String, int)
	 * @see #opCountCheck(String, int)
	 */
    public void enterTryCatchBlock(String constructName, String text, int depth)
	{
		//initialize the parent id
		this.initParentId();
		//storing the count, depth and length information
		int cId = this.insertCountDepthLength(constructName, text, this.pId);
		//store the information of the operands within this try catch block
		this.opCountCheck(text, cId);
	}

	/**
	 * The function stores the class information.
	 * 
	 * @param text a String type object storing the complete text of
     * the calling object.
	 * @param depth an int type object storing the depth of the calling
     * construct within a parse tree.
	 * @see #insertCountDepthLength(String, int, String, int)
	 * @see #opCountCheck(String, int)
	 * @see #insertCodeBlockInfo(String, int, int)
	 */
    public void storeClassInfo(String text)
	{
        System.out.println("In Class:"+text);
       // System.out.println(text);
		this.pId = 1; // root has parent Id=1 i.e. compilation unit


	}

	/**
	 * The function stores the information about the storage call
     * specifiers.
	 * 
	 * @param text a String type object storing the complete text of
     * the calling object.
	 * @param depth an int type object storing the depth of the calling
     * construct within a parse tree.
	 * @see #initParentId()
	 * @see #insertCountDepthLength(String, int, String, int)
	 */
    public void enterSCS(String text, int depth)
	{
		String constructName = this.findConstructName(text);
		this.initParentId();
		if (constructName != null)
			this.insertCountDepthLength(constructName, text.trim(), this.pId);
	}

	/**
	 * The function initializes the parent Id of the calling node.
	 */
    protected void initParentId()
	{//if the current construct is within a function
		if (this.funcId != 0)	
			this.pId = this.funcId;
		else if (this.classId != 0)	//within a class
			this.pId = this.classId; // every function is within some class
		else	//if the current construct is not within a class or function 
			this.pId = 1; 	// construct Id of Compilation Unit (CU)
	}

	/**
	 * Store the information related to jump statements. 
	 * For instance: viz., goto, break, return and continue.
	 * 
	 * @param text a String type object storing the complete text
     * of the calling object.
	 * @param depth an int type object storing the depth of the calling
     * construct within a parse tree.
	 *  @see #insertCountDepthLength(String, int, String, int)
	 * @see #opCountCheck(String, int)
	 * @see #insertCodeBlockInfo(String, int, int)
	 */
    public void enterJumpStmt(String text, int depth)
	{
		this.initParentId();
		//storing count, depth, length info
		this.insertCountDepthLength("jmp_stat", text, this.pId);
		
		String constructName = this.findConstructName(text);
		if (constructName != null)
		{
			int cId = this.insertCountDepthLength(constructName,text, this.pId);
			//insert operator counts
			this.opCountCheck(text, cId);
	        //finding code blocks
	        this.insertCodeBlockInfo(text, cId, depth);
		}
	}
    
	/**
	 * Store the information related to iterative statements.
	 * For instance: for, while, and do while.
	 * @param text a String type object storing the complete text of
     * the calling object.
	 * @param depth an int type object storing the depth of the calling
     * construct within a parse tree.
	 * @see #initParentId()
	 * @see #insertIter_SelectInfo(String, int, String)
	 */
	public void enterIterationStatement(String text, int depth)
	{
		this.initParentId();
		// increment the count of occurrence of this construct
		this.insertIter_SelectInfo("iterStat", depth, text);
	}

	/**
	  * Store the information related to selection statements.
	  * For instance: if and switch.
	  * @param text a String type object storing the complete text of
      * the calling object.
	  * @param depth an int type object storing the depth of the calling
      * construct within a parse tree.
	  * @see #initParentId()
	  * @see #insertIter_SelectInfo(String, int, String)
	  */
	public void enterSelectionStatement(String text, int depth)
	{
		this.initParentId();
		 // increment the count of occurrence of this construct
		this.insertIter_SelectInfo("selectStat", depth, text);
	}

	/**
	 * Store the information related to structure and union type
     * constructs.
	 * @param text a String type object storing the complete text
     * of the calling object.
	 * @param depth an int type object storing the depth of the calling
     * construct within a parse tree.
	 * @see #initParentId()
	 * @see #insertCountDepthLength(String, int, String, int)
	 */
	public void enterStructOrUnionSpecifier(String text, int depth)
	{
		String constructName = this.findConstructName(text);
		if (constructName != null)
		{
            this.initParentId();
			this.insertCountDepthLength(constructName, text, this.pId);
            //storing sub data variable count
			CodeInfo obj = new CodeInfo(this.structId, this.incrTypId(constructName), constructName, this.pId, "subDataVariablesCount");
			this.getHashmap().put(obj, text.split(";").length - 1);
		}
	}

	/**
	 * Function used to store the length of the calling function.
	 *  The function calling point differs in object-oriented 
     * and non-object oriented languages.
	 *  Therefore, this portion is written as a separate function
     * from enterFunctionDefinition().
	 * @param text	a String type object storing the complete text
     * of the calling object.
	 */
	public void storeFuncLength(String text)
	{
		CodeInfo obj = new CodeInfo(this.funcId, 1000, "function", this.pId, "length");
		this.getHashmap().put(obj, text.length());
	}

	/**
	 * Function used to store the information related to function
     * definitions.
	 * 
	 * @param text	a String type object storing the complete text
     * of the calling object.
	 * @param depth	an int type object storing the depth of the current
     * code block construct within a parse tree
	 * @param firstChild	a String type object storing the text of the
     * first child of this function node within the parse tree.
	 * @param secondChild	a String type object storing the text of the
     * second child of this function node within the parse tree.
	 * @see #incrOccCount(int, int, String, int)
	 * @see #opCountCheck(String, int)
	 * @see #insertCodeBlockInfo(String, int, int)
	 */
	public void enterFunctionDefinition(String text, int depth, String firstChild, String secondChild)
	{
		this.pId = 1; // CU
		CodeInfo obj;
		//we set the function flag
		this.funcId = Constants.funcStaticId;
		// incrementing the occurrence count
		this.incrOccCount(this.incrCId(), this.incrTypId("function"), "function", this.pId); 
		obj = new CodeInfo(this.incrCId(), this.incrTypId("function"), "function", this.pId, "depth");
		this.getHashmap().put(obj, depth);

		// extracting function names and thus their lengths
		int end = firstChild.indexOf('(');
		String str = null;
		if (end > 0)
			str = firstChild.substring(0, end);

		if (str != null)
		{
			obj = new CodeInfo(this.funcId, this.incrTypId("function"), "function", this.pId, "functionNameLength");
			this.getHashmap().put(obj, str.length());
			// Finding no. of Arguments by index of....

			obj = new CodeInfo(this.funcId, this.incrTypId("function"), "function", this.pId, "functionArgumentsCount");
			this.getHashmap().put(obj, secondChild.split(",").length - 1);
			// insert operand counts in function

			this.opCountCheck(text, this.funcId);
			// finding code blocks
			this.insertCodeBlockInfo(text, this.funcId, depth);
		}
	}

	
	/**
	 * Stores a partial part of information related to structure and 
     * union.
	 * 
	 * @param text	a String type object storing the complete text 
     * of the calling object (structure or union).
	 * @see #initParentId()
	 * @see #incrOccCount(int, int, String, int)
	 */
	public void enterStructOrUnion(String text)
	{
		int constructId = this.incrCId(); // Incrementing the construct Id
		// fetching the first character to recognize if it is a struct or a union
        char ch = text.charAt(0); 
		String constructName = null;
		int flag = 0;
		if (ch == 's')
			constructName = "struct";
		else if (ch == 'u')
			constructName = "union";
		else
			flag = 1;
		if (flag == 0) // if it was a struct or a union node
		{
			int constructTypId = this.incrTypId(constructName); // fetch the type Id if it exits or incement it
			this.structId = constructId; // set the struct Id which will be lated used in storing other parameters of
											// the same structure
			this.initParentId();
			//store the count information
			this.incrOccCount(constructId, constructTypId, constructName, this.pId);
		}
	}

	/**
	 * This function stores the information related to compilation
     * unit.
	 * @param text	a String type object storing the complete text
     * of the calling object.
	 * @see #incrOccCount(int, int, String, int)
	 * @see #opCountCheck(String, int)
	 * @see #insertCodeBlockInfo(String, int, int)
	 */
	public void enterCompilationUnit(String text)
	{
        System.out.println("In compilation unit:"+text);
        /*this.funcId = 0; // intialization for this particular file
		this.classId = 0; // intialization for this particular file

		int constructTypId = this.incrTypId("cu"); // fetching the type Id, or incrementing in case it
																// doesn't exist
		int constructId = 1;
		this.pId = 0; // root has parent Id=0
		// incrementing the occurrence count
		this.incrOccCount(constructId, constructTypId, "cu", this.pId); 

		// creating another info object to store the length
		CodeInfo obj = new CodeInfo(constructId, constructTypId, "cu", this.pId, "length");
		this.getHashmap().put(obj, text.length()); // inserting the length information

		// finding the operand counts and storing the corresponding information
		this.opCountCheck(text, constructId);
		// inserting the code block information (count and length)
		this.insertCodeBlockInfo(text, constructId, 0);*/
	}

	/**
	 * Function to store the information related to iterative
     * statement or selection statement.
	 * @param text	a String type object storing the complete text
     * of the calling object.
	 * @param depth	an int type object storing the depth of the current
     * code block construct within a parse tree.
	 * @see #initParentId()
	 * @see #insertIter_SelectInfo(String, int, String)
	 */
	public void enterStatement(String text, int depth)
	{
		/**
		 * Function to store the information related to iterative statement
         * or selection statement.
		 * This function is used in case of Java feature extraction process.
		 */
		this.initParentId();
		 String constructName=this.findConstructName(text);
		 if(constructName!=null)
			 this.insertIter_SelectInfo(constructName, depth, text);
	}

	public void storeOneLinerConstructInfo(String string, int depth, String text) {
        System.err.println("WARN:: !!!!!! Invocation of empty storeOneLinerConstructInfo() !!!!!");
	}
}//class ends
