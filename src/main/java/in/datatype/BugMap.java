package in.datatype;

public class BugMap
{
	//private data members of the class 
	private String fileName;	//	private data member to refer to measure Type
	private String source;	
	private String bugId;
	private String bugEngine;
	private String attachId;
	
	//Constructor for initialization of an Info type object
	public BugMap(String bugEngine, String bgId, String fileNm, String src) 
	{
		this.bugId = bgId; 
		this.fileName = fileNm;
		this.source = src;
		this.bugEngine= bugEngine;
	}

	//public member functions to fetch the values of private data-members
	public String getBugEngineId() {	return this.bugEngine;	}
	public String getBugId() 	{	return this.bugId; }	//fetching the bug Ids
	public String getFileNm() 	{ 	return this.fileName;	}	//fetching the File Name of the calling BugReport object
	public String getSource() 	{	return this.source;	}	//fetching the Source Type (Summary, Patch, Comment) of the calling BugReport object
}
