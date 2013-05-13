import java.util.TreeSet;


public class Evaluator {

	public Recommender train(InstanceBase userData, InstanceBase[] itemSimilarity) {
		extractUniqueLists(userData, itemSimilarity);
		
		return new Recommender();
	}
	
	/**
	 * Create lists with all available users and items -> required to setup the
	 * recommendation array.
	 * 
	 * @param userData The user activity data scanned for users and items.  
	 * @param itemSimilarity Further item list scanned for more items.
	 */
	private void extractUniqueLists(InstanceBase userData, InstanceBase[] itemSimilarity) {
		TreeSet<String> userSet = new TreeSet<String>();
		TreeSet<String> itemSet = new TreeSet<String>();
		// Scan training set for names and users.
		for(int i=0; i<userData.getNumInstances(); ++i) {
			userSet.add( userData.getDatum(i, 0) );
			itemSet.add( userData.getDatum(i, 2).toLowerCase() );
		}
	
		// TODO: Test if these additional names can improve the results
/*		for(int j=0; j<itemSimilarity.length; ++j) {
			// Attention! The 101 is context knowledge. There are 101 repeated
			// entries per name. 
			for(int i=0; i<itemSimilarity[j].getNumInstances(); i+=101) {
				itemSet.add( itemSimilarity[j].getDatum(i, 2).toLowerCase() );
			}
		}*/
		
		userList = (String[])userSet.toArray(new String[userSet.size()]);
		nameList = (String[])itemSet.toArray(new String[itemSet.size()]);
		System.out.println("\nIdentified " + userList.length + " unique users.");
		System.out.println("Identified " + nameList.length + " unique items.");
	}
	
	
	/**
	 * Derived Data
	 * 
	 * The userList contains all unique user ids which can be accessed by there
	 * indices afterwards.
	 */
	private String[] userList;

	/**
	 * Derived Data
	 * 
	 * A list of unique names.
	 */
	private String[] nameList;
}
