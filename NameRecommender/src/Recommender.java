import java.util.Iterator;


public class Recommender {
	
	// Parameters to tune the algorithm
	int MAX_RANK = 12;	// The rank how many weights should be used per item.
			 			// 	Using a larger number than 20 has nearly no effect on the quality.
	
	int ITERATRIONS = 15;	// Number of iterations - above 20 the quality is not increased that much.
	float GAMMA = 0.002f;
	float LAMBDA = 0.04f;
	
	private SparseFloatMatrix m_WeightTable;
	private FloatVector m_X[];	// characterize items (2D array: #items * FloatVector(MAX_RANK))
	private FloatVector m_Y[];	// characterize users based on the items they rated (2D array: #items * FloatVector(MAX_RANK))
	private float m_Q[];		// Is the item's influence positive or negative (array: #items)
	
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
	
	
	private void initializeItemAttributes() {
		// Initialization of the two matrices x,y means to fill them with zero
		m_X = new FloatVector[m_WeightTable.getNumColumns()];
		m_Y = new FloatVector[m_WeightTable.getNumColumns()];
		for( int i=0; i<m_WeightTable.getNumColumns(); ++i ) {
			m_X[i] = new FloatVector(MAX_RANK);
			m_Y[i] = new FloatVector(MAX_RANK);
		}
		m_Q = new float[m_WeightTable.getNumColumns()];
	}

	private void learnFactorizedNeighborhoodModel() {
		initializeItemAttributes();
		for( int i=0; i<ITERATRIONS; ++i ) {
			for( int u=0; u<m_WeightTable.getNumRows(); ++u ) {
				float sum = 0.0f;
				float ratedItemsWeight = (float)(1.0/Math.sqrt(m_WeightTable.getNumEntriesInRow(u)));	// This is |R(u)|^-0.5 in the document 
				FloatVector p = new FloatVector(MAX_RANK);
			}
		}
	}
}
