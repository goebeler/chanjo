The program has two different parts. They are:

	- creation of recommendation model and recommendation using created model. 
	- evaluation of the model with parameters given in ParameterSet class.

command to run program :
	-in order to get recommendations for the test users
	java -jar <chanjo.jar location> 1 <train_data location> <namelist file location> <test_data location> 

	-in order to evaluate model with given parameters and get RSME.
	java -jar <chanjo.jar location> 2 <train_data location> 

for example: 
	java -jar C:\chanjo.jar 1 C:\train_data.trainData C:\namelist.txt C:\nameling.testUsers

	
file locations:
	chanjo.jar inside folder /runnable_jar

	