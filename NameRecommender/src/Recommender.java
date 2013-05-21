import java.util.Iterator;


public class Recommender {
	
	private SparseFloatMatrix m_WeightTable;
	
	/**
	 * Creates a new trained recommender.
	 * @param _userData The training data.
	 * @param _itemList A list of all available items.
	 * 
	 * TODO: use a mask to disable the usage of all userData (cross validation)
	 */
	public Recommender( InstanceBase _userData, String[] _itemList, ParameterSet _Param ) {
		int numItems = _userData.getNumUniqueEntries(2);	// TODO + _itemList.length
		int numUsers = _userData.getNumUniqueEntries(0);
		m_WeightTable = new SparseFloatMatrix( numUsers, numItems );
		
		// Iterate over the training data and increase the entries for the users
		// actions.
		for(Iterator<int[]> it = _userData.getMappedIterator(); it.hasNext(); ) {
			int[] line = it.next();
			float newValue = m_WeightTable.get(line[0], line[2]) + _Param.ActionWeight[line[1]];
			m_WeightTable.set(line[0], line[2], newValue);
		}
	}
	
	
	public String[] getItemListForUser(int index) {
		for( int i=0; i<m_WeightTable.getNumRows(); ++i) {
			int iLastCol = -1;
			for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(i); it.hasNext(); ) {
				SparseFloatMatrix.IndexValuePair e0 = it.next();
				if( iLastCol > e0.index )
					System.out.println("Error in the matrix layout.");
				iLastCol = e0.index;
			}
		}
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
