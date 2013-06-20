import java.util.Iterator;
import java.io.FileWriter;
import java.io.IOException;


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
		
		if( args.length < 1 ) {
			System.out.println("At least the user activity data has to be given.");
			return;
		}
		
		loadData(args);
		createRecommender();
		if( testUsers != null )
			outputResults();
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
		userData = new InstanceBase(files[0], 4, 3);
		
		if( files.length > 1 )
			existingNames = new InstanceBase(files[1], 1, 1);
		
		if( files.length > 2 )
			testUsers = new InstanceBase(files[2], 1, 1);
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
		return ev.crossValidate( userData, _numberOfFolds);
	}
	
	
	/**
	 * This method uses the current recommender to create the recommendation
	 * lists. createRecommender must be called before!
	 */
	private static void outputResults() {
		// Create the file for the challenge
		try {
			FileWriter file;
			file = new FileWriter("recommendations_for_test_users.txt");

		
			System.out.println( "\n\nCapturing results\n");
			for(Iterator<int[]> it = testUsers.getMappedIterator(); it.hasNext(); ) {
				int id = it.next()[0];
				// Write user (original) id first
				file.write(userData.getString(0, id));
				int[] items = recommender.getItemListForUser(id, 20);
				for( int i=0; i<20; ++i ) {
					// Write names in a tab separated list
					String name = userData.getString(2, items[i]);
					file.write("\t" + name);
		//			System.out.print(name + ", ");
				}
				file.write("\n");
		//		System.out.println();
			}
			file.close();

		} catch (IOException e) {
			System.out.println("Could not write results to a local file.");
		}
	}

	private static Recommender recommender;
	private static InstanceBase userData;
	private static InstanceBase testUsers;
	private static InstanceBase existingNames;
}
