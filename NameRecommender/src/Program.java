import java.util.Iterator;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class Program {

	/**
	 * @param args The program arguments are the filenames of the data on which
	 * 	the name recommendation should be done.
	 * 
	 * The first has to be of the user activity data.
	 * 
	 * Afterwards an arbitrary number of files with the item similarity can
	 * be injected.
	 */
	public static void main(String[] args) {
		
		if( args.length < 2 ) {
			System.out.println("At least type of the task and the user activity data has to be given.");
			return;
		}
		
		String task = args[0];
		loadData(args);
		if( task.equalsIgnoreCase("1") ) {
			createRecommender();
		}
		else{		
			System.out.println("RMSE = " + crossValidate(10));
		}
		
		if( testUsers != null )
			outputResults(1000);
	}
	
	/**
	 * Load several files with instance data. The structure of the files could
	 * be arbitrary for the loader but nut for the following processes.
	 * 
	 * @param files A list with filenames. The first one has to be the userData.
	 * Afterward any number of item similarity files can follow.
	 */
	private static void loadData(String[] files) {
		// Load without time stamp
		userData = new InstanceBase(files[1], 4, 3);
		
		if( files.length > 2 )
		{
			existingNames = new InstanceBase(files[2], 1, 1);
			filterData( userData, 2, existingNames );
			System.out.println("\nPrepared data.");
		}
		
		if( files.length > 3 )
			testUsers = new InstanceBase(files[3], 1, 1);
	}
	
	/**
	 * Remove instances from the data by removing all which have a wrong value
	 * in the specified attribute. A value is not permitted (wrong) if it is
	 * not in the given itemList (on attribute 0 form that list).
	 * @param _data
	 * @param _itemList
	 */
	private static void filterData(InstanceBase _data, int _attribute, InstanceBase _itemList) {
		int index = 0;
		for(Iterator<String[]> it = _data.getIterator(); it.hasNext(); ) {
			String[] line = it.next();
			if( _itemList.getMappedID(0, line[_attribute]) == -1 )
			{
				it = _data.remove(index);
				//it.remove();
			} else
				++index;
		}
	}

	/**
	 * Initialize a trained recommender using the loaded data. The recommender
	 * is required to generate any output data.
	 * 
	 * Calling this method twice would create the same result and is only
	 * computional overhead.
	 */
	private static void createRecommender() {
		Evaluator trainer = new Evaluator();
		recommender = trainer.train( userData, existingNames );
	}
	
	/**
	 * Cross validates recommendation model. 
	 * Initialized a trained recommender for each fold. 
	 * @param _numberOfFolds number of folds for cross validation
	 * @return rmse error of the model 
	 */
	private static float crossValidate(int _numberOfFolds) {
		Evaluator ev = new Evaluator();
		System.out.println("cross validation started");
		return ev.crossValidate( userData, _numberOfFolds);
	}
	
	
	/**
	 * This method uses the current recommender to create the recommendation
	 * lists. createRecommender must be called before!
	 */
	private static void outputResults(int numRecommendations) {
		// Create the file for the challenge
		try {
			OutputStreamWriter file = new OutputStreamWriter(new FileOutputStream("recommendations_for_test_users.txt"), "UTF-8");
		
			System.out.println( "\n\nCapturing results");
			System.out.println( "Writing recommendations_for_test_users.txt: " + file.getEncoding());
			for(Iterator<int[]> it = testUsers.getMappedIterator(); it.hasNext(); ) {
				int id = it.next()[0];
				// Write user (original) id first
				file.write(userData.getString(0, id));
				int[] items = recommender.getItemListForUser(id, numRecommendations);
				for( int i=0; i<items.length; ++i ) {
					// Write names in a tab separated list
					String name = userData.getString(2, items[i]);
					file.write("\t" + name);
				}
				file.write("\n");
			}
			file.close();
			System.out.println( "Finished successfuly");

		} catch (IOException e) {
			System.out.println("Could not write results to a local file.");
		}
	}

	private static Recommender recommender;
	private static InstanceBase userData;
	private static InstanceBase testUsers;
	private static InstanceBase existingNames;
}
