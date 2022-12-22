package in.dao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import in.datatype.BugMap;
import in.datatype.CodeInfo;

public class DataAccessObject
{
	private String fileName;
	private HashMap <CodeInfo, Integer>hm;
	
	public DataAccessObject(String fileNm, HashMap <CodeInfo, Integer>hmap)
	{
		this.fileName=fileNm;
		this.hm=hmap;
	}
	public DataAccessObject()
	{
		
	}
	public static void insertBugInfo(String fileName)
	{
		Connection connection=(Connection) DBConnectionProvider.getConnection();
		try
		{
			PreparedStatement stmt=connection.prepareStatement("load data local infile ? into table bug_info fields terminated by ',' enclosed by '\"' lines terminated by '\\n';"); 
			stmt.setString(1,fileName);
			stmt.executeUpdate();
			connection.close();
		}catch (SQLException e)	{	e.printStackTrace();	}
	}
	public void insertData()
	  {
		// Converting them into a set of entries
		  Set<CodeInfo> keys = this.hm.keySet();
		  Connection connection=(Connection) DBConnectionProvider.getConnection();
		  try
		  {
			  PreparedStatement stmt  = null;
			  stmt = connection.prepareStatement("INSERT INTO SourceCodeFeatures VALUES (?,?,?,?,?,?,?);");
			  //PreparedStatement stmt = connection.prepareStatement("INSERT INTO PythonWork VALUES (?,?,?,?,?,?,?);");
			  stmt.setString(1, this.fileName);
			  for (CodeInfo p : keys) 
			  {
				  int cid, pid, typeId, measureVal;
				  String constructNm, measureType;
				  cid=p.getCId();
				  constructNm=p.getConstructNm();
				  pid=p.getPId();
				  typeId=p.getTypId();
				  measureType=p.getMeasureType();
				  measureVal=this.hm.get(p);
				  stmt.setInt(2, cid);
				  stmt.setInt(3, typeId);
				  stmt.setString(4, constructNm);
				  stmt.setInt(5, pid);
				  stmt.setString(6, measureType);
				  stmt.setInt(7, measureVal);
				  stmt.executeUpdate();
			  }
			connection.close();
		  } catch (SQLException e) { e.printStackTrace(); }
	  }

	public void insertBugMap(HashMap <Integer, BugMap>hmap)
	{
		//Mapping table stores the mapping between BugIds and the corresponding File Names and Source from which the fileName has been obtained
		//Given an input mapping hashmap after fetching from all the sources, it stores the mapping in the database 
		// insert into mapping changed to insert into BugFileMap
		Set<Integer> keys = hmap.keySet();
		  Connection connection=(Connection) DBConnectionProvider.getConnection();
		  try
		  {
			  PreparedStatement stmt = connection.prepareStatement("INSERT INTO NewBugFileMap VALUES (?,?,?,?,?);");
			  PreparedStatement stmt2 = connection.prepareStatement("select distinct product from bug_info where bug_id=? and not(product=? or product=?);");
			  stmt2.setString(2, "gcc");
			  stmt2.setString(3, "Paho");
			  for (Integer p : keys) 
			  {
				  String bugId, fileNm, source, bugEngineId;
				  bugEngineId=null;
				  bugId = hmap.get(p).getBugId();
				  bugEngineId = hmap.get(p).getBugEngineId();
				  fileNm = hmap.get(p).getFileNm();
				  source = hmap.get(p).getSource();
				  String productName = null;
				  Integer bugEngId = Integer.parseInt(bugEngineId);
				  if(bugEngId == 1)
					  productName="gcc";
				  else if(bugEngId == 3)
					  productName="Paho";
				  else if(bugEngId == 4)
					  productName="Python";
				  else
				  {
					  stmt2.setString(1, bugId);
					  ResultSet rs = stmt2.executeQuery();
					  while(rs.next())
					  {
						  productName=rs.getString(1);
						  break;
					  }
				  }
				  stmt.setString(1, bugEngineId);
				  stmt.setString(2, bugId);
				  stmt.setString(3, fileNm);
				  stmt.setString(4, productName);
				  stmt.setString(5, source);
				  stmt.executeUpdate();
			  }
			  connection.close();
		  } catch (SQLException e) { e.printStackTrace(); }
	}
	/*
	public void performTempTask()
	{
		Connection connection=(Connection) DBConnectionProvider.getConnection();
		try
		  { 
			PreparedStatement stmt= connection.prepareStatement("insert into CombinedFileIdentifierMap select tableId, bugEngineId, bugID, fileId, project, source, fileName, 0 from LinkedFileIdentifierMap;");				
			stmt.executeUpdate();
		  }
		catch (SQLException e) { e.printStackTrace(); }
		
	}*/
	public void fetchPatchBugIds()
	{
		//fetch bugIds corresponding to the bug reports having patches attached and writing them into a file
		String outFile1="bugIds/gcc_bugs.txt";
		String outFile2="bugIds/eclipse_paho_bugs.txt";
		String outFile3="bugIds/apache_bugs.txt";
		String bugId, product;
		bugId=product=null;
		File file1 = new File(outFile1);
		File file2 = new File(outFile2);
		File file3 = new File(outFile3);	
		FileWriter fstream1;
		FileWriter fstream2;
		FileWriter fstream3;
			
		try
		{
			fstream1 = new FileWriter(file1);
			fstream2 = new FileWriter(file2);
			fstream3 = new FileWriter(file3);
			
			BufferedWriter out1 = new BufferedWriter(fstream1);
			BufferedWriter out2 = new BufferedWriter(fstream2);
			BufferedWriter out3 = new BufferedWriter(fstream3);
			
			Connection connection=(Connection) DBConnectionProvider.getConnection();
			try
			  { 
				PreparedStatement stmt1= connection.prepareStatement("select distinct bug_id, product from bug_info where not(product='Paho') and (keywords like '%Patch%' or keywords like '%code%' or keywords like '%build%' or keywords like '%diagnostic%' or summary like '%patch%' or summary like '%code%' or summary like '%build%' or summary like '%diagnostic%') order by cast(bug_id as unsigned);");				
				PreparedStatement stmt2= connection.prepareStatement("select distinct bug_id from bug_info where product='Paho';");
				// execute select SQL statement
				ResultSet rs1 = stmt1.executeQuery();
				ResultSet rs2 = stmt2.executeQuery();
				while(rs1.next())
				{
					bugId=rs1.getString(1);
					product =rs1.getString(2);
					if(product.equals("gcc"))
					{
						out1.write(bugId+"\n");	//\n is used as a separating character
					}
					else if(!product.equals("Paho"))
					{
						out3.write(bugId+"\n");	//\n is used as a separating character
					}
				}
				while(rs2.next())
				{
					bugId=rs2.getString(1);
					out2.write(bugId+"\n");
				}
				//after getting the complete hashmap lets insert them
				connection.close();
				out1.close();
				out2.close();
				out3.close();
			  } catch (SQLException e) { e.printStackTrace(); }
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public HashMap <String, Integer> insertFileIdMap(String tableNm)
	{
		//returns a fileMap which maps each fileName (whether a linked file or not) to a unique Id
		HashMap <String, Integer> fileMap = new HashMap <String, Integer>();
		Integer key=0;
		String fileNm= null;
		Connection connection=(Connection) DBConnectionProvider.getConnection();
			try
			{
				String query1 = "select distinct fileName from $tableName;";
				String query2 = "UPDATE $tableName SET fileId=? WHERE fileName=?;";
				
				String query11 = query1.replace("$tableName",tableNm);
				String query21 = query2.replace("$tableName",tableNm);
				PreparedStatement stmt = connection.prepareStatement(query11);
				PreparedStatement stmt2 =  connection.prepareStatement(query21);
				// execute select SQL statement
				
				ResultSet rs = stmt.executeQuery();
				while(rs.next())
				{
					fileNm = rs.getString(1);
					key=key+1;
					
						fileMap.put(fileNm, key);
						stmt2.setInt(1, key);
						stmt2.setString(2, fileNm);
						stmt2.executeUpdate();
				
				}
				connection.close();
			}	catch (SQLException e) { e.printStackTrace(); }	
		return fileMap;
	}
	public HashMap <String, Integer> getFileIdMap()
	{
		//returns a fileMap which maps each fileName (whether a linked file or not) to a unique Id
		HashMap <String, Integer> fileMap = new HashMap <String, Integer>();
		Integer fileId=null;
		String fileNm= null;
			Connection connection=(Connection) DBConnectionProvider.getConnection();
			try
			{
				PreparedStatement stmt = connection.prepareStatement("select * from FileIdMap;");
				//PreparedStatement stmt2 = connection.prepareStatement("select * from test.FileIdMap;");
				// execute select SQL statement
				ResultSet rs = stmt.executeQuery();
				while(rs.next())
				{
					fileId = rs.getInt(1);
					fileNm=rs.getString(2);
					fileMap.put(fileNm, fileId);
				}
				connection.close();
			}catch (SQLException e) { e.printStackTrace(); }	
		return fileMap;
	}
	public HashMap <String, Integer> getProductIdMap()
	{
		//returns a fileMap which maps each fileName (whether a linked file or not) to a unique Id
		HashMap <String, Integer> fileMap = new HashMap <String, Integer>();
		Integer key=0;
		Integer productId=null;
		String productNm= null;
			Connection connection=(Connection) DBConnectionProvider.getConnection();
			try
			{
				PreparedStatement stmt = connection.prepareStatement("select * from ProductIdMap;");
				// execute select SQL statement
				ResultSet rs = stmt.executeQuery();
				while(rs.next())
				{
					productId = rs.getInt(1);
					productNm=rs.getString(2);
					key=key+1;
					fileMap.put(productNm, productId);
				}
				connection.close();
			}catch (SQLException e) { e.printStackTrace(); }	
		return fileMap;
	}
	public HashMap <Integer, BugMap> getBugFileMapping(String outFile,HashMap <String, Integer> productIdMap)
	{
		HashMap <Integer, BugMap> mapping = new HashMap <Integer, BugMap>();
		Integer key=0;
			Connection connection=(Connection) DBConnectionProvider.getConnection();
			try
			{
				//GETTING THE LINKED FILE NAMES COMPLETE PATH
				//PreparedStatement stmt = connection.prepareStatement("select distinct j.FileName, m.bugEngineId, m.bugId, m.source, m.product from test1.Records as j INNER JOIN BugFileMap as m on j.fileName like CONCAT('%/', m.fileName, '%');");
				PreparedStatement stmt = connection.prepareStatement("select distinct j.FileName, m.bugEngineId, m.bugId, m.source, m.product from RecordFileNames as j  INNER JOIN BugFileMap as m on j.FileName like CONCAT('%/', m.fileName, '%');");
				//PreparedStatement stmt2 = connection.prepareStatement("Insert into test1.LinkedFileIdentifierMap values (?,?,?,?,?,?,?);");
				PreparedStatement stmt2 = connection.prepareStatement("Insert into FinalRecordIdentifierMap values (?,?,?,?,?,?,?);");
				//PreparedStatement stmt3 = connection.prepareStatement("Select distinct fileName from MergedLinkedFileIdentifierMap;");
				//ResultSet rs2=stmt3.executeQuery();
				//BugEngID, bugId, fileId, project, source, fileName
				// execute select SQL statement
				ResultSet rs = stmt.executeQuery();
				Integer i=1;
				while(rs.next())
				{
					String fileName=rs.getString(1);
					/*
					while(rs2.next())
					{
						String fileName2=rs2.getString(1);
						if(!fileName.equals(fileName2))
						{*/
							String bugEngine= rs.getString(2);
							String bugId = rs.getString(3);
							String source =rs.getString(4);
							String product =rs.getString(5);
							//System.out.println("Product:"+product);
							String productId = productIdMap.get(product).toString(); 
							//System.out.print("ProductID:"+productId);
							String fileId=i.toString();
							BugMap obj=new BugMap(bugEngine, bugId, i.toString() , source);
							key=key+1;
							//System.out.print(key);
							if(bugId!=null)
							{
								stmt2.setInt(1, key);
								stmt2.setString(2, bugEngine);
								stmt2.setString(3, bugId);
								stmt2.setString(4, fileId);
								stmt2.setString(5, productId);
								stmt2.setString(6, source);
								stmt2.setString(7, fileName);
								stmt2.executeUpdate();
								mapping.put(key, obj);
							}
							i=i+1;
						}
				//	}
			//	}
				//after getting the complete hashmap lets insert them
				connection.close();
			}catch (SQLException e) { e.printStackTrace(); }
		System.out.println("Mappings Found"+mapping.size());
		return mapping;
	}
	
	public HashMap<String, Integer> insertProductIdMap()
	{
		HashMap<String, Integer> productIdMap= new HashMap<String, Integer>();
		String outFile="ProductIdMap.txt";
		File productFile = new File(outFile);
		String productName=null;
		Integer productId=0;
		try
		{
			FileWriter fstream = new FileWriter(productFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Product Name\t Product Id\n");
			Connection connection= (Connection) DBConnectionProvider.getConnection();
			try
			{
				PreparedStatement stmt = connection.prepareStatement("select distinct product from bug_info;");	
				PreparedStatement stmt2 =  connection.prepareStatement("Insert into ProductIdMap values (?,?);");
				ResultSet rs = stmt.executeQuery();
				while(rs.next())
				{
					productId=productId+1;
					productName = rs.getString(1);
					stmt2.setInt(1, productId);
					stmt2.setString(2, productName);
					stmt2.executeUpdate();
					productIdMap.put(productName, productId);
					out.write(productName+"\t"+productId+"\n");
				}
				connection.close();
			} catch (SQLException e) { e.printStackTrace(); }
			out.close();
		}	catch (IOException e1)	{	e1.printStackTrace();	}
		return productIdMap;
	}

	public HashMap<String, Integer> insertConstructIdMap()
	{
		HashMap<String, Integer> constructIdMap= new HashMap<String, Integer>();
		String constructName=null;
		String measureType=null;
		Integer constructId=0;
		ArrayList <String> ConstructNm = new ArrayList <String>();
			Connection connection= (Connection) DBConnectionProvider.getConnection();
			try
			{
				PreparedStatement stmt = connection.prepareStatement("select distinct ConstructName, MeasureType from CandidateCombinedFileLevelTrends order by ConstructName;");
				PreparedStatement stmt2 =  connection.prepareStatement("Insert into ConstructIdMap values (?,?);");
				ResultSet rs = stmt.executeQuery();
				while(rs.next() ) //&& constructId<=109
				{
					constructId=constructId+1;
					constructName = rs.getString(1);
					measureType = rs.getString(2);
					constructName=constructName.concat("_").concat(measureType);
					ConstructNm.add(constructName);
					stmt2.setInt(1, constructId);
					stmt2.setString(2, constructName);
					stmt2.executeUpdate();
					constructIdMap.put(constructName, constructId);
				}
				connection.close();
			} catch (SQLException e) { e.printStackTrace(); }
		
		
		// delete from ConstructIdMap where cast(ConstructId as unsigned)>90;
		return constructIdMap;
	}
	public HashMap <String, Integer> getConstructIdMap()
	{
		//returns a fileMap which maps each fileName (whether a linked file or not) to a unique Id
		HashMap<String, Integer> constructIdMap= new HashMap<String, Integer>();
		Integer constructId=null;
		String constructNm= null;
		Connection connection=(Connection) DBConnectionProvider.getConnection();
		try
		{
			PreparedStatement stmt = connection.prepareStatement("select distinct ConstructId, ConstructName from ConstructIdMapOrg;");
			// execute select SQL statement
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				constructId = rs.getInt(1);
				constructNm=rs.getString(2);
				constructIdMap.put(constructNm, constructId);
			}
			connection.close();
		}catch (SQLException e) { e.printStackTrace(); }	
		return constructIdMap;
	}
	
	public HashMap <String, Integer> insertLinkedFileIdMap()
	{
		HashMap<String, Integer> linkedFileIdMap= new HashMap<String, Integer>();
		Integer fileId=0;
		String fileNm= null;
		Connection connection=(Connection) DBConnectionProvider.getConnection();
		try
		{
			PreparedStatement stmt = connection.prepareStatement("select distinct FileName from SubRecords;");
			PreparedStatement stmt2 = connection.prepareStatement("update SubRecords set FileId=? where FileName=?;");
			// execute select SQL statement
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				fileId=fileId+1;
				
				fileNm=rs.getString(1);
				linkedFileIdMap.put(fileNm, fileId);
				stmt2.setInt(1, fileId);
				stmt2.setString(2, fileNm);
				stmt2.executeUpdate();
			
			}
			connection.close();
		}catch (SQLException e) { e.printStackTrace(); }	
		return linkedFileIdMap;
	}
	public HashMap <String, Integer> getLinkedFileIdMap()
	{
		HashMap<String, Integer> linkedFileIdMap= new HashMap<String, Integer>();
		Integer fileId= null;
		String fileNm= null;
		Connection connection=(Connection) DBConnectionProvider.getConnection();
		try
		{
			PreparedStatement stmt = connection.prepareStatement("select distinct FileId, FileName from SubRecords;");
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				fileId=rs.getInt(1);
				fileNm=rs.getString(2);
				linkedFileIdMap.put(fileNm, fileId);
			}
			connection.close();
		}catch (SQLException e) { e.printStackTrace(); }	
		return linkedFileIdMap;
	}
	public void insertLangSpecFileIds()
	{
		Connection connection=(Connection) DBConnectionProvider.getConnection();
		ArrayList <String> tableList= new ArrayList<String>();
		tableList.add("CandidateCombinedFileLevelTrends");
		HashMap <String, Integer> fileMap=new HashMap <String, Integer>(); 
		tableList.add("CandidateJavaFileLevelTrends");
		tableList.add("CandidateCFileLevelTrends");
		tableList.add("CandidateCppFileLevelTrends");
		tableList.add("CandidatePyFileLevelTrends");
		//System.out.println(tableList.get(1));
		int count=0;
		String tableName=null;
		String fileNm=null;
		String query1 = null;
		String query2 = null;
		
		String query3=null;
		String query4=null;
		
		
		Integer langFId=0;
		query1="select distinct fileName from $tableName;";
		query2="Update $tableName set fileId=? where fileName=?;";
		
		try
		{
			
			for(int j=0;j<tableList.size();j++)
			{
				tableName=tableList.get(j);
				//System.out.println(tableName);
				langFId=0;
				System.out.println("Performing updation on table:".concat(tableName));		
				query3 =query1.replace("$tableName",tableName);
				PreparedStatement stmt = connection.prepareStatement(query3);	
				ResultSet rs = stmt.executeQuery();
				while(rs.next())
				{
					langFId=langFId+1;
					fileNm=rs.getString(1);
					fileMap.put(fileNm, langFId);
				}
				query4 =query2.replace("$tableName",tableName);
				PreparedStatement stmt2 =  connection.prepareStatement(query4);
				
				Set<String> keys = fileMap.keySet();
				for (String fileName : keys) 
				{
					Integer fileId=fileMap.get(fileName);
					stmt2.setInt(1, fileId);
					stmt2.setString(2, fileName);
					stmt2.executeUpdate();
				}
			}
			connection.close();
		}catch (SQLException e) { e.printStackTrace(); }
	}
	public HashMap <String, Float> getFeatures(HashMap <String, Integer> constructIdMap, Integer queryFlag, String language, String type)
	{
		//other inputs could be queryFlag and case input
		//insert query flag and file type e.g. java
		System.out.println("queryFlag");
		System.out.println(queryFlag);
		System.out.println(language);
		HashMap <String, Float> featureValMap = new  HashMap <String, Float>();
		//bugId_FeatureId to FeatureName mapping is retained in a text file, which will be required during the analysis of the results after clustering 
		//String fileIds="FileMaps/".concat(language).concat("Q").concat(queryFlag.toString()).concat("IdMap.txt");
		File fileIdMap = null;
		String fileName=null;
		//CandidateCombinedFileLevelTrends
		Integer fileId=null;
		String constructName = null;
		Integer constructId = null;
		String measureType= null;
		
		String featureId;
		Float featureVal=null;
		String cmplFeatureId = null;
		String query1=null;
		String query2=null;
		String query3=null;
		String outFolder=null;
		String outFolder2=null;
		String outFolder3=null;
		String tableNm=null;
		if(type.equals("ClusterFeatures"))
		{
			outFolder="ClusterFeatures/";
			tableNm="all".concat(language).concat("FileLevelTrends"); //later can also be changed to Global level trends
		}
		else if(type.equals("FileLevel"))
		{
			outFolder="FileFeatures/";
			//outFolder2="UnNormIndividualFileFeatures/";
			//outFolder3="IndividualFileFeatures/";
			tableNm="Candidate".concat(language).concat(type).concat("Trends"); //later can also be changed to Global level trends
		}
		else
		{
			outFolder="GlobalFeatures/";
			//outFolder2="UnNormGlobalFeatures/";
			//tableNm=language.concat("Linked").concat(type).concat("Trends"); //later can also be changed to Global level trends
		}
		System.out.print("Getting features for "+tableNm);
		String outFile=language.concat(type).concat("Q").concat(queryFlag.toString()).concat("Features.txt");
		//String outFile2 = language.concat(type).concat("Q").concat(queryFlag.toString()).concat("Features.txt");;
		//String outFile3 = language.concat(type).concat("Q").concat(queryFlag.toString()).concat("Features.txt");;
		outFile=outFolder.concat(outFile);
		File file = new File(outFile);
		
		
		//outFile2= outFolder2.concat(outFile2);
		//File file2 = new File(outFile2);
		//outFile3=outFolder3.concat(outFile3);
		//File file3 = new File(outFile3);
		
		
		int count=0;
		ResultSet rs=null;
		int combined=0;
		String query5=null;
		String query6 = null;
		Integer fileCount=0;
		try
		{
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream); 
			//FileWriter fstream12 = new FileWriter(file2);
			//BufferedWriter out12 = new BufferedWriter(fstream12);
			
			//FileWriter fstream3 = new FileWriter(file3);
			//BufferedWriter out3 = new BufferedWriter(fstream3);
			
			Connection connection=(Connection) DBConnectionProvider.getConnection();
			try
			{
				
				//insert Language FileIDs 
				if(type.equals("ClusterFeatures"))
				{
					String query31=null;
					String query11=null;
					
					query11="select distinct fileId, maxMeasureVal from $tableName where constructName='file' and measureType='length' and queryFlag=1;";
					HashMap<Integer, Integer> fileLength= new HashMap<Integer, Integer>();
					query31 = query11.replace("$tableName",tableNm);
					PreparedStatement stmt1 = connection.prepareStatement(query31);	
					ResultSet rs11 = stmt1.executeQuery();
					while(rs11.next())
					{
						Integer fileNum=rs11.getInt(1);
						Integer length=rs11.getInt(2);
						fileLength.put(fileNum,length);
					}
					query3 = "select distinct fileId, fileName from $tableName;";
					query5 = "select count(distinct fileId) from $tableName;";
					query6 = query5.replace("$tableName",tableNm);
					String query4 = query3.replace("$tableName",tableNm);
					PreparedStatement stmt4 = connection.prepareStatement(query6);
					
					ResultSet rs3 = stmt4.executeQuery();
					
					PreparedStatement stmt2 = connection.prepareStatement(query4);
					ResultSet rs2 = stmt2.executeQuery();
					while(rs3.next())
						fileCount=rs3.getInt(1);
					//declare an array to store features corresponding to these many files
					float[][] features = new float[fileCount][436];	//436 features in each row
					PreparedStatement stmt=null;
					PreparedStatement stmt3=null;
					if(queryFlag<=3)
					{
						query1="select distinct fileId, fileName, constructName, measureType, maxMeasureVal, minMeasureVal, avgMeasureVal, stdDevMeasureVal from $tableName where queryFlag=? and FileName = ?;";
						String query =query1.replace("$tableName",tableNm);
						stmt = connection.prepareStatement(query);
						stmt.setInt(1, queryFlag);
					}
					else
					{
						query2="select distinct fileId, fileName, constructName, measureType, maxMeasureVal, minMeasureVal, avgMeasureVal, stdDevMeasureVal from $tableName where (queryFlag=1 or queryFlag=2 or queryFlag=3) and FileName = ?;";
						String query = query2.replace("$tableName",tableNm);
						stmt3 = connection.prepareStatement( query);	
					}
					
					
					while(rs2.next())
					{
						count=count+1;
						fileName=rs2.getString(2);
						fileId = rs2.getInt(1);
						if(queryFlag<=3)
						{
							stmt.setString(2, fileName);
							rs = stmt.executeQuery();
						}
						else
						{
							stmt3.setString(1, fileName);
							rs = stmt3.executeQuery();
						}
						
						while(rs.next())
							{
							//fileId gives the row and constructId gives the columnId  
								constructName = rs.getString(3);
								measureType = rs.getString(4);
								constructName=constructName.concat("_").concat(measureType);
								constructId = constructIdMap.get(constructName);
								Integer col=(constructId-1)*4;
								features[fileId-1][col]=(float)rs.getInt(5);
								col=col+1;
								features[fileId-1][col]=(float)rs.getInt(6);
								col=col+1;
								features[fileId-1][col]=rs.getInt(7);
								col=col+1;
								features[fileId-1][col]=(float)rs.getInt(8);
							}	
					}
					for(int i=0; i<features.length;i++)
					{
						//get fileLength
						float fileLen=fileLength.get(i+1);
						for(int j=0;j<436;j++)
						{
							if(j>=124 && j<=127)
								continue;
							else
							{
								out.write(features[i][j]/fileLen+" ");
								
							}
						}
						out.newLine();
					}
					
				}
				else if(type.equals("FileLevel") )
				{
					String query31=null;
					String query11=null;
					
					query11="select distinct fileId, maxMeasureVal from $tableName where constructName='file' and measureType='length' and queryFlag=1;";
					HashMap<Integer, Integer> fileLength= new HashMap<Integer, Integer>();
					query31 = query11.replace("$tableName",tableNm);
					PreparedStatement stmt1 = connection.prepareStatement(query31);	
					ResultSet rs11 = stmt1.executeQuery();
					while(rs11.next())
					{
						Integer fileNum=rs11.getInt(1);
						Integer length=rs11.getInt(2);
						fileLength.put(fileNum,length);
					}
					query3="select distinct fileId, fileName from $tableName;";
					query5="select count(distinct fileId) from $tableName;";
					query6 =query5.replace("$tableName",tableNm);
					String query4 =query3.replace("$tableName",tableNm);
					PreparedStatement stmt4 = connection.prepareStatement(query6);
					
					ResultSet rs3 = stmt4.executeQuery();
					
					PreparedStatement stmt2 = connection.prepareStatement(query4);
					ResultSet rs2 = stmt2.executeQuery();
					while(rs3.next())
						fileCount=rs3.getInt(1);
					//declare an array to store features corresponding to these many files
					float[][] features = new float[fileCount][436];	//436 features in each row
					PreparedStatement stmt=null;
					PreparedStatement stmt3=null;
					if(queryFlag<=3)
					{
						query1="select distinct fileId, fileName, constructName, measureType, maxMeasureVal, minMeasureVal, avgMeasureVal, stdDevMeasureVal from $tableName where queryFlag=? and FileName = ?;";
						String query =query1.replace("$tableName",tableNm);
						stmt = connection.prepareStatement(query);
						stmt.setInt(1, queryFlag);
					}
					else
					{
						query2="select distinct fileId, fileName, constructName, measureType, maxMeasureVal, minMeasureVal, avgMeasureVal, stdDevMeasureVal from $tableName where (queryFlag=1 or queryFlag=2 or queryFlag=3) and FileName = ?;";
						String query = query2.replace("$tableName",tableNm);
						stmt3 = connection.prepareStatement( query);	
					}
					
					
					while(rs2.next())
					{
						count=count+1;
						fileName=rs2.getString(2);
						fileId = rs2.getInt(1);
						if(queryFlag<=3)
						{
							stmt.setString(2, fileName);
							rs = stmt.executeQuery();
						}
						else
						{
							stmt3.setString(1, fileName);
							rs = stmt3.executeQuery();
						}
						
						while(rs.next())
							{
							//fileId gives the row and constructId gives the columnId  
								constructName = rs.getString(3);
								measureType = rs.getString(4);
								constructName=constructName.concat("_").concat(measureType);
								constructId = constructIdMap.get(constructName);
								Integer col=(constructId-1)*4;
								features[fileId-1][col]=(float)rs.getInt(5);
								col=col+1;
								features[fileId-1][col]=(float)rs.getInt(6);
								col=col+1;
								features[fileId-1][col]=rs.getInt(7);
								col=col+1;
								features[fileId-1][col]=(float)rs.getInt(8);
							}	
					}
					//writing the complete set of combined features Q4 for reverse analysis
					
					if(language.equals("Combined") && queryFlag==4)
					{//required for reverse analysis
						/*
						String fileNm="CompleteFileFeatures/".concat(language).concat("Q").concat(queryFlag.toString()).concat("FileLevelTrends.txt");
						
						File cmplFileFeatures = new File(fileNm);
						FileWriter fstream13 = new FileWriter(cmplFileFeatures);
						BufferedWriter out13 = new BufferedWriter(fstream13);
						
						for(int i=0; i<features.length;i++)
						{
							//get fileLength
							float fileLen=fileLength.get(i+1);
							for(int j=0;j<436;j++)
							{
								if(j>=124 && j<=127)
									continue;
								else
								{
									out13.write(features[i][j]/fileLen+" ");
								}
							}
							out13.newLine();
						}
						out13.close();*/
					}
					//let's write them into a file
					float[][] subFeatures=null;
	
					//FileWriter fstream11 = null;
					//BufferedWriter out11 = null;
					
					String fileIdStr=null;
					if(!(language.equals("Cpp")))
					{
						if(!(language.equals("Combined")))
						{//writing the mapping
							//fileIdMap = new File(fileIds);
							//fstream11 = new FileWriter(fileIdMap);
							//out11 = new BufferedWriter(fstream11);
							subFeatures = new float[173][436];	//436 features in each row with 173*4 rows
							//we need to get a subset of features
							int max=features.length-1;
							Random randNum=new Random();
							ArrayList<Integer> indexes = new ArrayList<Integer>(173);
							int fileNum;
							for(int i=0;i<173;i++)
							{//getting 173 random features for all languages
								fileNum=randNum.nextInt(max);
								if(i==0)
									indexes.add(fileNum);
								else
								{
									while(indexes.contains(fileNum))
										fileNum=randNum.nextInt(max);
									indexes.add(fileNum);
								}
								for(int j=0;j<436;j++)
									subFeatures[i][j]=features[fileNum][j];
								if(i!=172)
									fileIdStr=Integer.toString(i).concat(":").concat(Integer.toString(fileNum)).concat("\n");
								else
									fileIdStr=Integer.toString(i).concat(":").concat(Integer.toString(fileNum));
								//out11.write(fileIdStr);
							}
							//out11.close();
						}
						else
						{
							//fileIdMap = new File(fileIds);
							//fstream11 = new FileWriter(fileIdMap);
							//out11 = new BufferedWriter(fstream11);
							subFeatures = new float[692][436];	//436 features in each row with 173*4 rows
							ArrayList<Integer> indexes = null;
							//get 173 random features from each category
							//int count=0;
							//java
							int max, min;
							max=283;	//count-1 of java files
							min=642; //min fileId-1
							Random randNum=new Random();
							indexes = new ArrayList<Integer>(173);
							int fileNum;
							for(int i=0;i<173;i++)
							{//getting 173 random features for all languages
								fileNum=min+randNum.nextInt(max);
								if(i==0)
									indexes.add(fileNum);
								else
								{
									while(indexes.contains(fileNum))
										fileNum=min + randNum.nextInt(max);
									indexes.add(fileNum);
								}
								for(int j=0;j<436;j++)
									subFeatures[i][j]=features[fileNum][j];
								fileIdStr=Integer.toString(i).concat(":").concat(Integer.toString(fileNum)).concat("\n");
								//System.out.println(fileIdStr);
								//out11.write(fileIdStr);
							}
							//python
							max=1015;
							min=926;	//min fileId-1
							indexes = new ArrayList<Integer>(173);
							for(int i=173;i<346;i++)
							{//getting 173 random features for all languages
								fileNum=min+randNum.nextInt(max);
								if(i==173)
									indexes.add(fileNum);
								else
								{
									while(indexes.contains(fileNum))
										fileNum=min + randNum.nextInt(max);
									indexes.add(fileNum);
								}
								for(int j=0;j<436;j++)
									subFeatures[i][j]=features[fileNum][j];
								fileIdStr=Integer.toString(i).concat(":").concat(Integer.toString(fileNum)).concat("\n");
								//System.out.println(fileIdStr);
								
								//out11.write(fileIdStr);
							}
							//C
							max=468;
							min=173;
							indexes = new ArrayList<Integer>(173);
							for(int i=346;i<519;i++)
							{//getting 173 random features for all languages
								fileNum=min+randNum.nextInt(max);
								if(i==346)
									indexes.add(fileNum);
								else
								{
									while(indexes.contains(fileNum))
										fileNum=min + randNum.nextInt(max);
									indexes.add(fileNum);
								}
								for(int j=0;j<436;j++)
									subFeatures[i][j]=features[fileNum][j];
								fileIdStr=Integer.toString(i).concat(":").concat(Integer.toString(fileNum)).concat("\n");
								//System.out.println(fileIdStr);
								
								//out11.write(fileIdStr);
							}
							//CPP or CC
							max=172;
							min=0;
							fileNum=min;
							
							indexes = new ArrayList<Integer>(173);
							for(int i=519;i<692;i++)
							{//getting 173 random features for all languages
								for(int j=0;j<436;j++)
									subFeatures[i][j]=features[fileNum][j];
								if(i!=691)
									fileIdStr=Integer.toString(i).concat(":").concat(Integer.toString(fileNum)).concat("\n");
								else
									fileIdStr=Integer.toString(i).concat(":").concat(Integer.toString(fileNum));
								//out11.write(fileIdStr);
								//System.out.println(fileIdStr);
								
								fileNum=fileNum+1;
							}
							//out11.close();
						}
					}
				
					int maxFiles;
					if(language.equals("Combined"))
						maxFiles=692;
					else
					{
						//maxFiles=173;
						maxFiles=features.length;
					}
		
					if(language.equals("Cpp"))
					{
						//fileIdMap = new File(fileIds);
						//fstream11 = new FileWriter(fileIdMap);
						//out11 = new BufferedWriter(fstream11);
					}
					for(int i=0; i<maxFiles;i++)
					{
						//get fileLength
						float fileLen=fileLength.get(i+1);
						for(int j=0;j<436;j++)
						{
							if(j>=124 && j<=127)
								continue;
							else
							{
								if(language.equals("Cpp"))
								{
									//out3.write(features[i][j]/fileLen+" ");
									//out.write(features[i][j]/fileLen+" ");
									//out12.write(features[i][j]+" ");
									fileIdStr=Integer.toString(i).concat(":").concat(Integer.toString(i)).concat("\n");
									//System.out.println(fileIdStr);
									//out11.write(fileIdStr);
								}
								else
								{
									//out3.write(features[i][j]/fileLen+" ");
									if(!language.equals("CombinedUnLinked"))
										out.write(subFeatures[i][j]/fileLen+" ");
									else
										out.write(features[i][j]/fileLen+" ");
									//out12.write(subFeatures[i][j]+" ");
									//out12.write(features[i][j]+" ");//taking all the normalized features in files from individual languages
								}
							}
						}
						out.newLine();
						//out12.newLine();
						//out3.newLine();
					}
					if(language.equals("Cpp"))
					{
						//out11.close();
						//out3.close();
					}
					
				}
				else
				{
					
					String query=null;
					if(queryFlag<=3)
					{
						//query1="select distinct ConstructName, MeasureType, avgFileAvgMeasureVal, maxFileMaxMeasureVal, minFileMinMeasureVal, stdDevFileStdDevMeasureVal from $tableName where queryFlag=? ;";
						query1="select distinct ConstructName, MeasureType, avgFileMaxMeasureVal, avgFileMinMeasureVal, avgFileAvgMeasureVal, avgFileStdDevMeasureVal, maxFileMaxMeasureVal, maxFileMinMeasureVal, maxFileAvgMeasureVal, maxFileStdDevMeasureVal, minFileMaxMeasureVal, minFileMinMeasureVal, minFileAvgMeasureVal, minFileStdDevMeasureVal, stdDevFileMaxMeasureVal, stdDevFileMinMeasureVal, stdDevFileAvgMeasureVal, stdDevFileStdDevMeasureVal from $tableName where queryFlag=? ;";
						query =query1.replace("$tableName",tableNm);
					}
					else
					{
						query2="select distinct ConstructName, MeasureType, avgFileMaxMeasureVal, avgFileMinMeasureVal, avgFileAvgMeasureVal, avgFileStdDevMeasureVal, maxFileMaxMeasureVal, maxFileMinMeasureVal, maxFileAvgMeasureVal, maxFileStdDevMeasureVal, minFileMaxMeasureVal, minFileMinMeasureVal, minFileAvgMeasureVal, minFileStdDevMeasureVal, stdDevFileMaxMeasureVal, stdDevFileMinMeasureVal, stdDevFileAvgMeasureVal, stdDevFileStdDevMeasureVal from $tableName where (queryFlag=1 or queryFlag=2 or queryFlag=3);";
						//query2="select distinct ConstructName, MeasureType, avgFileAvgMeasureVal, maxFileMaxMeasureVal, minFileMinMeasureVal, stdDevFileStdDevMeasureVal from $tableName where (queryFlag=1 or queryFlag=2 or queryFlag=3);";
						query =query2.replace("$tableName",tableNm);
					}
					if(queryFlag<=3)
					{
						PreparedStatement stmt = connection.prepareStatement(query);
						stmt.setInt(1, queryFlag);	
						rs = stmt.executeQuery();
					}
					else
					{
						PreparedStatement stmt3 = connection.prepareStatement( query);
						rs = stmt3.executeQuery();
					}
					
					float[][] features = new float[109][16];	//404 features in each row
					while(rs.next())
					{
						constructName = rs.getString(1);
						measureType = rs.getString(2);
						constructName=constructName.concat("_").concat(measureType);
						constructId = constructIdMap.get(constructName);
						featureId=constructId.toString();
						for(int i=3;i<=18;i++)
						{
							if((i>=3 && i<=6) || i==9 || i==10 || (i>=13 && i<=18))
								featureVal = rs.getFloat(i);
							else
								featureVal = (float)rs.getInt(i);
							features[constructId-1][i-3]=featureVal;
						}
					}
				
					for(int j=0;j<109;j++)
					{
						if(j==31)
							continue;
						else
						{
							for(int k=0;k<16;k++)
							{
								out.write(features[j][k]+" ");
							}
							out.newLine();
						}
					}
				}
				connection.close();
				out.close();
				//out12.close();
				//out3.close();
			}
			catch (SQLException e) { e.printStackTrace(); }
		}
		catch (IOException e1)
		{
				// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return featureValMap;
	}
}
	/*
	public HashMap<Integer, BugReport> fetchSummary()
	{
		//creating the mapping storing the bugIds and the FileNms
		HashMap <Integer, BugReport>hmap = new HashMap<Integer, BugReport>();
		Connection connection=(Connection) DBConnectionProvider.getConnection();
		//System.out.println("Connection: "+connection.);
		ArrayList <String> fileNames = new ArrayList <String>();
		try
		  { 
			  PreparedStatement stmt = connection.prepareStatement("select bug_id, summary from bug_info order by bug_id;");
			// execute select SQL statement
			ResultSet rs = stmt.executeQuery();
			System.out.println("Records fetched: "+rs.getFetchSize());
			while(rs.next())
			{
				String bugId=rs.getString("bug_id");
				String summary=rs.getString("summary");
				fileNames=extractFileNm(summary);
				for(int i=0;i<fileNames.size();i++)
				{
					String fileNm = fileNames.get(i);
					BugReport obj=new BugReport(fileNm,"summary");
					if(fileNm!=null)
						hmap.put(Integer.parseInt(bugId), obj);
				}
			}
			//after getting the complete hashmap lets insert them
			connection.close();
		  } catch (SQLException e) { e.printStackTrace(); }
		return hmap;
	}
	*/

