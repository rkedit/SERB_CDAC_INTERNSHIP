import constant
import sys
import os
from sklearn.preprocessing import MinMaxScaler
#from sklearn.externals import pickle
import pickle
#import time
from collections import Counter

def readFeatures(filePath):
	'''This function is used to read the features and return them in the form of a list'''
	file = open(filePath, 'r')
	features = file.read()
	featureSet = features.strip().split(" ");
	return featureSet
	
def performClassification(modelPath, Xtest):
	'''This function is used to perform a defect estimation task on the input featureSet of the source files.
modelPath specifies the location of the best classifiying ML model to perform the given task.'''	
	print(modelPath)
	#scale the features
	scalar = MinMaxScaler()
	#print(Xtest)
	Xtest = scalar.fit_transform(Xtest)
	# load the model from disk
	model = pickle.load(open(modelPath, 'r'))
	# load the model from disk
	#model = joblib.load(modelPath)
	#perform the prediction task
	testLabel = model.predict(Xtest)
	#print testLabel
	return testLabel

def getKmodelPaths(folderPath):
	'''This function is used to fetch all the k models present in the input folder and return them as a list'''
	modelPaths = []
	#iterate to get all the k model paths
	for subPath in os.listdir(folderPath):
		cmplPath = folderPath +"/" +subPath #build the complete path
		modelPaths.append(cmplPath) #store the complete path of the model in the list
	return modelPaths

def computeFinalLabel(labels):
	'''This function is used to find one single label corresponding to a task evaluated by different ML models. We take the threshold as 1/3. This means that we mark the final label as 1 only if 1/3 of the total models give the label response as 1. Otherwise, we return the final label as 0.'''
	totlCount = sum(labels)	#we count the total models that mark the label as '1'
	threshCount = len(labels)/3	#find the 1/3 number of models
	if(totlCount>=threshCount):	#threshold condition to fix the final label as '1'
		return str(1)
	else:
		return str(0) 

def preprocessFeatures(testFeatures):
	'''This model fetches all the features one by one and converts them to float data-type. This is done because only the features with the float data-type can be input to various ML models. testFeatures contain the features extracted from the source file.'''
	normFeatures = []	#new list of features in float data type
	#iterate through the entire testFeatures list and convert all the features
	for elem in testFeatures:
		normElem = float(elem)
		normFeatures.append(normElem)
	return normFeatures

def findExtension(filePath):
	'''This function is used to find the extension of a filePath provided as input'''
	ind = filePath.find('.')	#find the occurrence of '.'
	ext = filePath[ind+1:]	#find the extension
	return ext


def main():
    #print(">>>> python main function. args count " + str(len(sys.argv)))
    #modelPath = sys.argv[1]	#path of a folder containing various models found to be best for a particular defect estimation task
    modelPath = "/home/ritu/repos/projectOrgg/clusterWrite/finalWorkVersions/papercomparisonWork/PredictionResultsNew/Phase1Check/Models"
    backEndPath = "/home/ritu/backEndFolder"
    #backEndPath = sys.argv[2]	#path of the back end folder containing other necessary source failes or data files
    #backEndPath = "/home/ritu/repos/projectOrgg/clusterWrite/finalWorkVersions"
    #print("In python")
    #path of the file containing features extracted from the test file provided as input by the user
    testFeaturesPath = backEndPath+"/"+constant.TEST_FILE_FEATURES_FILE_NAME
    #print(">>> Model path: "+ modelPath)
    #fetch the test featureSet by reading the test features written as a text file at testFeaturesPath location 
    testFeatures = readFeatures(testFeaturesPath)
    #print(testFeatures)
    #pre-process the features by converting them into a suitable input format
   
     
    normFeatures = preprocessFeatures(testFeatures)
    Xtest = []
    Xtest.append(normFeatures)
   
    #print(">>> Normalized Features")
    #fetch all the k model paths containing the defect estimation model paths
    kModelPaths = getKmodelPaths(modelPath)
    #print(kModelPaths)
    labels = []	#list containing the labels obtained by performing defect estimation using various ML models
    #perform defect estimation by using each of the ML model whose location is fetched in kModelPaths
    for modelPath in kModelPaths:	#iterating the list
    	ext = findExtension(modelPath)	#find the extension
    	if(ext=='pkl'):	#filtering only the model files with .sav extension
    		label = performClassification(modelPath, Xtest)	#perform the defect estimation
    		#print label
    		labels.append(label[0])	#append the label obtained in the labels list
    	else:
    		continue	#ignore the rest of the files
    finalLabel = computeFinalLabel(labels)	#find the final label for thie test file by taking a threshold
    print(str(int(finalLabel[0][0])))	#return the final label and print the same
    return str(int(finalLabel[0][0]))
if __name__ == '__main__':
    main()

