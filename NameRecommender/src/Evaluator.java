import java.util.Iterator;
import java.util.TreeSet;


public class Evaluator {

	public Recommender train(InstanceBase _userData, InstanceBase[] _itemSimilarity) {
		ParameterSet parameters = new ParameterSet();
		boolean[] filter = new boolean[_userData.getNumInstances()];
		SparseFloatMatrix ratrings = createRatingMatrix(_userData, filter, parameters);
		return new Recommender(ratrings);
	}
	
	/**
	 * Fill a sparse matrix with all allowed actions from the database.
	 * @param _userData The database which contains all actions
	 * @param _filterActions An array with the same number of entries as in the
	 * 	database. For each entry set to true the matrix will be updated. If
	 * 	filter action is false the entry will be skipped.
	 * @return A matrix with a size of #users x #items.
	 */
	SparseFloatMatrix createRatingMatrix(InstanceBase _userData, boolean[] _filterActions, ParameterSet _param) {
		int numItems = _userData.getNumUniqueEntries(2);
		int numUsers = _userData.getNumUniqueEntries(0);
		SparseFloatMatrix ratrings = new SparseFloatMatrix( numUsers, numItems );
				
		// Iterate over the training data and increase the entries for the users
		// actions.
		int i=0;
		for(Iterator<int[]> it = _userData.getMappedIterator(); it.hasNext(); ) {
			int[] line = it.next();
			if( _filterActions[i++] ) {
				float newValue = _param.ActionWeight[line[1]];
				newValue += ratrings.get(line[0], line[2]);
				ratrings.set(line[0], line[2], newValue);
			}
		}
		
		return ratrings;
	}
	
	
	/**
	 * Derived Data
	 * 
	 * The userList contains all unique user ids which can be accessed by there
	 * indices afterwards.
	 */
	//private String[] m_UserList;

	/**
	 * Derived Data
	 * 
	 * A list of unique names.
	 */
	//private String[] m_NameList;
}
