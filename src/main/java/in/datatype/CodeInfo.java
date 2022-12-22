/**
 * <b> in.datatype package </b>:  contains the collection of files created to be used as data type entities
 */
package in.datatype;

/**
 * <b>Import statements</b>: Certain java.util collections are used as imports.
 * 
 * @see  java.util.HashMap
 * @see  java.util.Set
 */
import java.util.HashMap;
import java.util.Set;

/**
 * The <b>CodeInfo</b> class is used as a data type created to store the lexical measure values associated with various constructs. 
 * Each construct occurrence is assigned a unique construct identifier, a construct type id, a parent Id (the construct id of the parent node).
 * Each of the lexical property associated with the construct is represented by the measure type field. For instance, count, depth ,length, etc.
 * All these fields (viz., constructId, constructTypeId, parentId, etc.) are represented as the private data members of the class.
 *<p>
 *The class contains methods responsible for maintaining these identifier values and fetching these private data-member fields.
 */


public class CodeInfo
{
	//private data members of the class 
		private Integer constructId;	// private data member to assign a unique Id to every programming construct
		private Integer constructTypeId;	// private data member to assign a unique Type Id to the type of every programming construct
		private String constructName; // private data member to refer to construct names
		private Integer parentId;	//	private data member to refer to Parent Ids
		private String measureType;	//	private data member to refer to measure Type
			
		
		/**
		 *Constructor for initialization of the private data members of CodeInfo type object.
		 * @param cnstrctId a unique identifier field for representing the occurrence of a programming construct
		 * @param cnstrctTypId	a unique identifier field for representing the type of a programming construct
		 * @param cnstrctNm	the name of the programming construct
		 * @param prtId a unique identifier for representing the parent node of the current programming construct
		 * @param msrTyp	a unique measure type used to represent a lexical property (for instance, count, depth ,length, etc.)
		 */
		public CodeInfo(Integer cnstrctId, Integer cnstrctTypId, String cnstrctNm, Integer prtId, String msrTyp) 
		{
			//Constructor for initialization of an CodeInfo type object
			this.constructId = cnstrctId;
			this.constructTypeId=cnstrctTypId;
			this.constructName = cnstrctNm;
			this.parentId = prtId;
			this.measureType = msrTyp;
		}

		//public member functions to fetch the values of private data-members
		
		/**
		 * A public data member function used to fetch the value of construct name (private data member) associated with the calling object.
		 * @return the construct name of the calling object returned as  a String object
		 */
		public String getConstructNm() 	{ 	return this.constructName;	}	//fetching the Construct Name of the calling 'CodeInfo' object
		
		/**
		 * A public data member function used to fetch the value of measure type (viz., depth, count, etc.) associated with the calling object.
		 * @return the value of measure type returned as a String object
		 */
		public String getMeasureType() 	{	return this.measureType;	}	//fetching the Measure Type of the calling 'CodeInfo' object
		
		/**
		 * A public data member function used to fetch the value of construct Id associated with the calling object.
		 * @return the value of construct Id returned as an Integer object.
		 */
		public Integer getCId()	        {	return this.constructId;	}	//fetching the Construct Id of the calling 'CodeInfo' object
		
		/**
		 * A public data member function used to fetch the value of construct type associated with the calling object. 
		 * @return	the value of constructTypeId returned as an Integer object.
		 */
		public Integer getTypId()	    {	return this.constructTypeId;	}	//fetching the Construct Type Id of the calling 'CodeInfo' object
		
		/**
		 * A public data member function used to fetch the Id value of the parent node of the calling object.
		 * @return the value of parentId returned as an Integer object.
		 */
		public Integer getPId()	        {	return this.parentId;	}	//fetching the Parent Id of the calling 'CodeInfo' object

		
		
		//public member function to check whether two objects are equal or not
		//In case if a matching object is found to exist in the hashmap key entries, the reference of the matching object is returned
		//Else the same Calling object reference is returned
		
		/**
		 * A public member function to check if two objects are equal or not. The function iterates over the pre-existing collection of 
		 * information about constructs and compares the field values with that of the calling object. 
		 * If an exiting match is found, the reference of the existing object is returned, else the calling object is returned.   
		 * @param hm a collection of pre-existing information about constructs 
		 * @return a CodeInfo object storing the reference of a pre-existing object (if a match is found) or of the calling object (if no match is found) 
		 */
		public CodeInfo isEqual(HashMap<CodeInfo, Integer> hm)
		{
			//fetch the key set corresponding to the Hashmap
			Set<CodeInfo> keys = hm.keySet();	
			//iterate over the existing keys to find a matching object, if any
			for (CodeInfo p : keys) 
			{
				if(p.getTypId().equals(this.getTypId()) && p.getCId().equals(this.getCId())  && p.getPId().equals(this.getPId()))
					return p;	//return the matching object, if found 
			}
				return this;	//return the same object if no matching object found
		}// function ends
}
