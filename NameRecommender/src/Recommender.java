import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;


public class Recommender {
	
	// Parameters to tune the algorithm
	ParameterSet m_Params;
//	int MAX_RANK = 40;			// The rank how many weights should be used per item.
			 					// 	Using a larger number should always increase the quality of the outcome.
	
	int ITERATRIONS = 15;		// Number of iterations - above 20 the quality is not increased that much.
	float GAMMA = 0.002f;
	float LAMBDA = 0.04f;
	
	//float ALPHA = 25;			// Initialization shrinkage
	//float EPSILON = 0.001f;		// Error on initialization 
	
	private SparseFloatMatrix m_WeightTable;
	private FloatVector m_X[];	// characterize items (2D array: #items * FloatVector(MAX_RANK))
	private FloatVector m_Y[];	// characterize users based on the items they rated (2D array: #items * FloatVector(MAX_RANK))
	private FloatVector m_Q[];	// Is the item's influence positive or negative (array: #items). Using m_Q = m_X enforces symmetric weights (see page 177) -> left out
	private FloatVector m_P[];	// User factors of the fall back method (array: #users)
	float m_AverageRating;		// The average rating over the whole table
	private float m_Bu[];		// Observed deviations of user u from the average
	private float m_Bi[];		// Observed deviations of item i from the average
	
	private int m_NumUsers;
	private int m_NumItems;
	
	/**
	 * Creates a new trained recommender.
	 * @param _userData The training data.
	 * @param _itemList A list of all available items.
	 * 
	 */
	public Recommender( SparseFloatMatrix _ratings, ParameterSet _Params ) {
		m_Params = _Params;
		m_WeightTable = _ratings;
		m_NumUsers = _ratings.getNumRows();
		m_NumItems = _ratings.getNumColumns();
		
		m_AverageRating = 0.0f;
		
		// Iterate over the training data and increase the entries for the users
		// actions.
		for( int i=0; i<m_NumUsers; ++i ) {
			for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(i); it.hasNext(); ) {
				SparseFloatMatrix.IndexValuePair e0 = it.next();
				m_AverageRating += e0.value;
			}
		}
		
		m_AverageRating /= m_NumItems*m_NumUsers;
		initializeItemAttributes();
		//learnFactorizedNeighborhoodModel();
	}
	
	/**
	 * Computes a sorted list of recommendated items for the given user.
	 * @param _user The user for which the recommendation list should be created.
	 * @param _num Number of items to recommend.
	 * @return An array with item IDs of length _num
	 */
	public int[] getItemListForUser(int _user, int _num) {
		assert(_num <= m_NumItems);
		// Compute recommendation values for each item (use index value pairs to sort indices after value)
		SparseFloatMatrix.IndexValuePair[] items = new SparseFloatMatrix.IndexValuePair[m_WeightTable.getNumColumns()]; 
		for( int i=0; i<m_NumItems; ++i ) {
			items[i] = new SparseFloatMatrix.IndexValuePair(i,0.0f);
			// Only use items the user had not interacted before.
			if(m_WeightTable.get(_user, i) == 0.0f)
				items[i].value = getPrediction(_user, i);
		}
		java.util.Arrays.sort(items);
		int[] topItems = new int[_num];
		for( int i=0; i<_num; ++i )
			topItems[i] = items[i].index;
		return topItems;
	}
	

	/**
	 * Compute the minimum between how many items did the user rate for and
	 * how often an item was counted
	 * @return
	 */
	private int support( int _user, int _item )
	{
		// count how often an item is rated by polling.
		//int ic = 0;
		//for( int i=0; i<m_NumUsers; ++i )
		//	if( m_WeightTable.get(i, _item) > 0.0f ) ++ic;
		return Math.min(m_WeightTable.getNumEntriesInRow(_user), m_WeightTable.getNumEntriesInColumn(_item));
	}
	
	/**
	 * Computes sum (r_ui-r^_ui)² with
	 * r^_ui = P dot Q
	 * This measurement is only required for initialization of the latent
	 * factor space. Later on r^_ui consists of more factors.
	 * @return Rating error of matrices Q and P.
	 */
	private float squaredError()
	{
		float res = 0;
		for( int u=0; u<m_NumUsers; ++u ) {
			for( Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); ) {
				SparseFloatMatrix.IndexValuePair entry = it.next();
				float ratingError = entry.value - m_P[u].dot(m_Q[entry.index]);
				res += ratingError * ratingError;
			}
		}
		return res;
	}
	
	/**
	 * Compute the f-th column of matrices Q,X and Y. Columns 1 to 1-f are
	 * already computed.
	 * @param f
	 */
	private void computeNextFactor( int f )
	{
		// Compute residuals-portion not explained by previous factors.
		SparseFloatMatrix ratingErrors = new SparseFloatMatrix(m_NumUsers, m_NumItems);
		// For each known rating
		for( int u=0; u<m_NumUsers; ++u ) {
			for( SparseFloatMatrix.IndexValuePair entry : m_WeightTable.getRow(u) ) {
				int i = entry.index;
				// Dot product of P_i and Q_j with respect to the computed part
				// only (which is easy, because everything else is 0.
				float ratingError = entry.value - m_P[u].dot(m_Q[i]);
				// Shrinkage
				float n_ui = support(u,i);
				ratingError = n_ui*ratingError / (n_ui+m_Params.SHRINKAGE * f);
				ratingErrors.set(u, i, ratingError);
			}
		}
		
		// Solving many least square problems
		// THE WHILE LOOP IN [NetflixKDD07] HAS AN SENSLESS CONDITION?
		float errOld = squaredError();
		float errNew = 0;
		// Set something else than 0 (otherwise endless loop)
		for( int i=0; i<m_NumItems; ++i )
			m_Q[i].set( f, 1.0f );
		while( errNew/errOld < 1-m_Params.EPSILON ) {
			// For each user
			for( int u=0; u<m_NumUsers; ++u ) {
				float newFactorNum = 0;
				float newFactorDen = 0;
				for( SparseFloatMatrix.IndexValuePair entry : ratingErrors.getRow(u) ) {
					float Q_if = m_Q[entry.index].get(f);
					newFactorNum += entry.value * Q_if;
					newFactorDen += Q_if * Q_if;
				}
				m_P[u].set( f, m_P[u].get(f) + newFactorNum/Math.max(newFactorDen, 0.00000001f) );
			}
			// for each item
			float[] newFactorDen = new float[m_NumItems];	// Save numerator and denominator here to accumulate for all items in parallel
			float[] newFactorNum = new float[m_NumItems];
			for( int u=0; u<m_NumUsers; ++u ) {
				for( SparseFloatMatrix.IndexValuePair entry : ratingErrors.getRow(u) ) {
					float P_uf = m_P[u].get(f);
					newFactorNum[entry.index] += entry.value * P_uf;
					newFactorDen[entry.index] += P_uf * P_uf;
				}
			}
			for( int i=0; i<m_NumItems; ++i )
				m_Q[i].set( f, m_Q[i].get(f) + newFactorNum[i]/Math.max(newFactorDen[i],0.00000001f) );
			
			errOld = errNew; 
			errNew = squaredError();
		}
	}
	
	/**
	 * Once m_P and m_Q are computed derive an initial state of m_X and m_Y.
	 */
	private void deriveXAndY() {
		// Use symmetric case m_Q = m_X as initial state -> copy
		for( int i=0; i<m_NumItems; ++i )
			for( int f=0; f<m_Params.MAX_RANK; ++f )
				m_X[i].set(f, m_Q[i].get(f));
		// P_u = |R(u)|^-0.5 SUM i€R(u) [(r_uj-b_uj)*x_i+y_i]
		//	   = |R(u)|^-0.5 SUM i€R(u) [(r_uj-b_uj)*x_i] + SUM i€R(u) [y_i]
		// SUM i€R(u) [y_i] = P_u - |R(u)|^-0.5 SUM i€R(u) [(r_uj-b_uj)*x_i]
		// => Equation system (left side unknown, right side compute-able
		// To get a rough estimate use "inverse Radon transformation"
		// y_i += (P_u - |R(u)|^-0.5 SUM i€R(u) [(r_uj-b_uj)*x_i])/n			(1)
		int[] n = new int[m_NumItems];
		for( int u=0; u<m_NumUsers; ++u ) {
			float norm = (float)(1.0/(m_WeightTable.getNumEntriesInRow(u)*Math.sqrt(m_WeightTable.getNumEntriesInRow(u))));
			FloatVector p = new FloatVector(m_Params.MAX_RANK);
			for( SparseFloatMatrix.IndexValuePair entry : m_WeightTable.getRow(u) ) {
				float b_uj = computeBaselinePredictor(u, entry.index);
				p.add( FloatVector.mul((entry.value-b_uj)*norm, m_X[entry.index] ) );
			}
			p = FloatVector.mad( -1.0f, m_P[u], p );	// == - right side of (1)
			for( SparseFloatMatrix.IndexValuePair entry : m_WeightTable.getRow(u) ) {
				++n[entry.index];
				m_Y[entry.index].sub(p);	// -= -right side   ==   += right side
			}
		}
		// Normalize
		for( int i=0; i<m_NumItems; ++i )
			m_Y[i].mul(1.0f/n[i]);
	}
	
	private void initializeItemAttributes() {
		m_Bu = new float[m_NumUsers];
		m_Bi = new float[m_NumItems];

		// Initialization of the two matrices x,y means to fill them with zero
		m_X = new FloatVector[m_NumItems];
		m_Y = new FloatVector[m_NumItems];
		m_Q = new FloatVector[m_NumItems];
		for( int i=0; i<m_NumItems; ++i ) {
			m_X[i] = new FloatVector(m_Params.MAX_RANK);
			m_Y[i] = new FloatVector(m_Params.MAX_RANK);
			m_Q[i] = new FloatVector(m_Params.MAX_RANK);
		}
		m_P = new FloatVector[m_NumUsers];
		for( int i=0; i<m_NumUsers; ++i )
			m_P[i] = new FloatVector(m_Params.MAX_RANK);
		
		// Fill vectors with latent factors
		for( int i=0; i<m_Params.MAX_RANK; ++i )
			computeNextFactor( i );
		//deriveXAndY();
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
		//Dump();
		for( int i=0; i<ITERATRIONS; ++i ) {
			for( int u=0; u<m_NumUsers; ++u ) {
				FloatVector sum = new FloatVector(m_Params.MAX_RANK);				
				float norm = (float)(1.0/Math.sqrt(m_WeightTable.getNumEntriesInRow(u)));	// This is |R(u)|^-0.5 in the document
				// Compute |R(u)|^-0.5 SUM j€R(u) [(r_uj-b_uj)*x_j+y_j]
				FloatVector p = new FloatVector(m_Params.MAX_RANK);
				for( SparseFloatMatrix.IndexValuePair entry : m_WeightTable.getRow(u) ) {
					float b_uj = computeBaselinePredictor(u, entry.index);
					p.add( FloatVector.mad(entry.value-b_uj, m_X[entry.index], m_Y[entry.index] ) );
				}
				p.mul(norm);
				
				for( SparseFloatMatrix.IndexValuePair entry : m_WeightTable.getRow(u) ) {
					int j = entry.index;
					float rh_ui = computeBaselinePredictor(u, j) + m_Q[j].dot(p);
					float e_ui = entry.value - rh_ui;
					assert(!Float.isNaN(e_ui));
					// Accumulate information for gradient descent steps on m_X, m_Y
					sum.add( FloatVector.mul(e_ui, m_Q[j]) );
					// Perform gradient steps on m_Q, b_u and b_i
					m_Q[j].add( FloatVector.mad(GAMMA*e_ui, p, FloatVector.mul(-GAMMA*LAMBDA, m_Q[j])) );
					m_Bu[u] += GAMMA * (e_ui - LAMBDA * m_Bu[u]);
					m_Bi[j] += GAMMA * (e_ui - LAMBDA * m_Bi[j]);
				}
				
				for( SparseFloatMatrix.IndexValuePair entry : m_WeightTable.getRow(u) ) {
					int j = entry.index;
					m_X[j].add( FloatVector.mad(GAMMA*norm*(entry.value-computeBaselinePredictor(u, j)), sum, FloatVector.mul(-GAMMA*LAMBDA, m_X[j])) );
					m_Y[j].add( FloatVector.mad(GAMMA*norm, sum, FloatVector.mul(-GAMMA*LAMBDA, m_Y[j])) );
				}
//				Dump();
			}
		}
	}
	
	
	/**
	 * Compute recommendation for a given user item pair.
	 * @param _user The user which's item are of interest.
	 * @param _item The item of interest.
	 * @return rating that presumably the user will give to the item.
	 */
	public float getPrediction(int _user, int _item) {
		
		return m_Q[_item].dot(m_P[_user]);
		
/*		float latentFactorPart = 0;
		for( SparseFloatMatrix.IndexValuePair entry : m_WeightTable.getRow(_user) ) {
			latentFactorPart += (entry.value - computeBaselinePredictor( _user, entry.index ))
					* m_Q[entry.index].dot(m_X[entry.index]) 
					+ m_Q[entry.index].dot(m_Y[entry.index]);
		}	
		
		assert(m_WeightTable.getNumEntriesInRow(_user) > 0);
		latentFactorPart /= Math.sqrt(m_WeightTable.getNumEntriesInRow(_user));
		
		return computeBaselinePredictor( _user, _item ) + latentFactorPart;
		*/
	}
	
	
	
	
	/**
	 * Method for debug purposes. Writes all vectors of the current state to
	 * the file dump.txt.
	 */
	private void Dump() {
		try {
			FileWriter file;
			file = new FileWriter("dump.txt");

			for( int i=0; i<m_NumItems; ++i ) {
				file.write("item " + i + "\n");
				file.write("X: " + m_X[i].toString() + "\n");
				file.write("Y: " + m_Y[i].toString() + "\n");
				file.write("Q: " + m_Q[i].toString() + "\n");
				file.write("Bi: " + m_Bi[i] + "\n");
			}
			
			for( int u=0; u<m_NumItems; ++u ) {
				file.write("Bu: " + m_Bu[u] + "\n");
				file.write("P: " + m_P[u].toString() + "\n");
			}
			file.close();

		} catch (IOException e) {
			System.out.println("Dump not possible.");
		}
	}
}
