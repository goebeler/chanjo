import java.util.Iterator;


public class Recommender {
	
	private float m_WeightTable[];
	
	/**
	 * Creates a new trained recommender.
	 * @param _userData The training data.
	 * @param _itemList A list of all available items.
	 * 
	 * TODO: use a mask to disable the usage of all userData (cross validation)
	 */
	public Recommender( InstanceBase _userData, String[] _itemList, ParameterSet _Param ) {
		int numItems = _userData.getNumUniqueEntries(2);	// TODO + _itemList.length
		int yyy = _userData.getNumUniqueEntries(0);
		int ix = _userData.getNumUniqueEntries(0)*numItems;
		m_WeightTable = new float[_userData.getNumUniqueEntries(0)*numItems];
		
		// Iterate over the training data and increase the entries for the users
		// actions.
		for(Iterator<int[]> it = _userData.getMappedIterator(); it.hasNext(); ) {
			int[] line = it.next();
			m_WeightTable[line[0] * numItems + line[2]] += _Param.ActionWeight[line[1]];
		}
	}
	
	
	public String[] getItemListForUser(int index) {
		return null;
	}
	
	private float userSimilarity(int index1, int index2) {
		return 0.0f;
	}
	
	private void fillGapsWithUserSimilarity(int otherUserIndex) {
		
	}
	
	private void fillGapsWithItemSimilarity(InstanceBase itemSimilarities) {
		
	}
}
