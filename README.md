The shared link of **DESCo system** provides access to two .zip folders (**backEndFolder** and
**Sep_Project-0.0.1-SNAPSHOT-bin**) and this README file. The steps listed below enlist the step-by-step
procedure to run the application, using these zipped folders. The instructions mentioned below are written
with reference to the Ubuntu operating system (OS); you can perform the steps in a similar fashion for
other OS as well.
##Software Requirements:

- **OS Requirements:**​ Ubuntu or any other OS.
- **Java(TM) SE Runtime Environment:**​ You can install it from here. The Java RE used by us to run this application is **1.8.0_181**. Please ensure you use a java version >=1.8.
- **Unzip Software:**​ You can install using the command: *sudo apt-get install unzip*

##Steps of execution:
1. Download both the .zip folders (viz., **backEndFolder.zip** and **Sep_Project-0.0.1-SNAPSHOT-bin.zip**), present in the shared directory, in a local directory. 
	Let this local directory be */home/user_abc/downloads/* for our reference.
2. Unzip both the downloaded folders, in **Step 1**, using the command: *unzip folderName.zip*.
	- For instance, *unzip /home/user_abc/downloads/Sep_Project-0.0.1-SNAPSHOT-bin.zip*
3. Create a new directory, named as *testFiles*, within this local directory.
	1. Use the command: *mkdir testFiles*
	2. We assume you are within the local directory while executing the command in **Step 3.1**). You can use the following command to move into this directory from your home location:
		- *cd user_abc/downloads/*
4. Insert the files to be tested in the *testFiles*, folder created in the previous step i.e. **Step 3.2**.
5. In your local directory ( */home/user_abc/downloads/*), open a terminal to execute the application from the command line.
6. Move into the lib directory present in the unzipped application folder
	- Use command: *cd Sep_Project-0.0.1-SNAPSHOT/lib/*
	
7. Unzip the jar files using the command: *find -name "*.jar" | xargs -n 1 jar xf*
8. Run the application (with the **backEndFolderPath** and **testFiles** folder path as arguments)
	using the command:
	- *java in.desco.ui.UserInterface /home/user_abc/downloads/backEndFolder/ /home/user_abc/downloads/testFiles/*