import java.util.Iterator;


public class Recommender {
	
	// Parameters to tune the algorithm
	int MAX_RANK = 12;	// The rank how many weights should be used per item.
			 			// 	Using a larger number should always increse the quality of the outcome.
	
	int ITERATRIONS = 15;	// Number of iterations - above 20 the quality is not increased that much.
	float GAMMA = 0.002f;
	float LAMBDA = 0.04f;
	
	private SparseFloatMatrix m_WeightTable;
	private FloatVector m_X[];	// characterize items (2D array: #items * FloatVector(MAX_RANK))
	private FloatVector m_Y[];	// characterize users based on the items they rated (2D array: #items * FloatVector(MAX_RANK))
	private FloatVector m_Q[];	// Is the item's influence positive or negative (array: #items). Using m_Q = m_X enforces symmetric weights (see page 177) -> left out 
	float m_AverageRating;		// The average rating over the whole table
	private float m_Bu[];		// Observed deviations of user u from the average
	private float m_Bi[];		// Observed deviations of item i from the average
	
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
		
		m_AverageRating = 0.0f;
		
		// Iterate over the training data and increase the entries for the users
		// actions.
		for(Iterator<int[]> it = _userData.getMappedIterator(); it.hasNext(); ) {
			int[] line = it.next();
			float newValue = _Param.ActionWeight[line[1]];
			m_AverageRating += newValue;
			newValue += m_WeightTable.get(line[0], line[2]);
			m_WeightTable.set(line[0], line[2], newValue);
		}
		
		m_AverageRating /= numItems*numUsers;
		learnFactorizedNeighborhoodModel();
	}
	
	
	public String[] getItemListForUser(int index) {
		for( int i=0; i<m_WeightTable.getNumRows(); ++i) {
			int iLastCol = -1;
			// Temporary test code
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
			// TODO: fill in non zeros
			m_X[i] = new FloatVector(MAX_RANK);
			m_Y[i] = new FloatVector(MAX_RANK);
			m_Q[i] = new FloatVector(MAX_RANK);
		}
		
		m_Bu = new float[m_WeightTable.getNumRows()];
		m_Bi = new float[m_WeightTable.getNumColumns()];
	}
	
	/**
	 * Compute baseline predictors b_ui.
	 * @param _user The user which's items are of interest.
	 * @param _item The item of interest.
	 * @return The baseline predictor.
	 */
	private float computeBaselinePredictor( int _user, int _item ) {
		return m_Bu[_user] + m_Bi[_item] + m_AverageRating;
	}

	private void learnFactorizedNeighborhoodModel() {
		initializeItemAttributes();
		for( int i=0; i<ITERATRIONS; ++i ) {
			for( int u=0; u<m_WeightTable.getNumRows(); ++u ) {
				FloatVector sum = new FloatVector(MAX_RANK);				
				float norm = (float)(1.0/Math.sqrt(m_WeightTable.getNumEntriesInRow(u)));	// This is |R(u)|^-0.5 in the document
				// Compute |R(u)|^-0.5 SUM j€R(u) [(r_uj-b_uj)*x_j+y_j]
				FloatVector p = new FloatVector(MAX_RANK);
				for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); ) {
					SparseFloatMatrix.IndexValuePair entry = it.next();	// entry.index==j, entry.value==r_uj
					float b_uj = computeBaselinePredictor(u, entry.index);
					p.add( FloatVector.mad(entry.value-b_uj, m_X[entry.index], m_Y[entry.index] ) );
				}
				p.mul(norm);
				
				for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); ) {
					SparseFloatMatrix.IndexValuePair entry = it.next();
					int j = entry.index;
					float rh_ui = computeBaselinePredictor(u, j) + m_Q[j].dot(p);
					float e_ui = entry.value - rh_ui;
					// Accumulate information for gradient descent steps on m_X, m_Y
					sum.add( FloatVector.mul(e_ui, m_Q[j]) );
					// Perform gradient steps on m_Q, b_u and b_i
					m_Q[j].add( FloatVector.mad(GAMMA*e_ui, p, FloatVector.mul(-GAMMA*LAMBDA, m_Q[j])) );
					m_Bu[u] += GAMMA * (e_ui - LAMBDA * m_Bu[u]);
					m_Bi[j] += GAMMA * (e_ui - LAMBDA * m_Bi[j]);
				}
				
				for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); ) {
					SparseFloatMatrix.IndexValuePair entry = it.next();
					int j = entry.index;
					m_X[j].add( FloatVector.mad(GAMMA*norm*(entry.value-computeBaselinePredictor(u, j)), sum, FloatVector.mul(-GAMMA*LAMBDA, m_X[j])) );
					m_Y[j].add( FloatVector.mad(GAMMA*norm, sum, FloatVector.mul(-GAMMA*LAMBDA, m_Y[j])) );
				}				
			}
		}
	}
}
