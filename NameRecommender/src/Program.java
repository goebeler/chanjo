
public class Program {

	/**
	 * @param args The program arguments are the filenames of the data on which
	 * 	the name recommendation should be done.
	 * 
	 * The first has to be of the used activity data.
	 * 
	 * Afterwards an arbitrary number of files with the item similarity can
	 * be injected.
	 */
	public static void main(String[] args) {
		
		if( args.length < 1 ) {
			System.out.println("At least the used activity data has to be given.");
			return;
		}
		
		loadData(args);
		createRecommender();
		outputResults();
	}
	
	private static void loadData(String[] files) {
		userData = new InstanceBase(files[0]);
		
		if( files.length > 1 ) {
			itemData = new InstanceBase[files.length-1];
			for( int i=1; i<files.length; ++i )
				itemData[i-1] = new InstanceBase(files[i]);
		}
	}

	private static void createRecommender() {
		Evaluator trainer = new Evaluator();
		recommender = trainer.train( userData, itemData );
	}
	
	
	private static void outputResults() {
		System.out.println(recommender.getItemListForUser(0));
	}

	private static Recommender recommender;
	private static InstanceBase userData;
	private static InstanceBase[] itemData;
}
