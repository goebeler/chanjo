
/**
 * The set of parameters which controls the output of the recommender. This
 * is the only thing which can be learned to change the recommended item list. 
 */
public class ParameterSet {
	/** Each action of a user is increasing its interest value of the
	 * specific name.
	 * Each action can have an other weight where the order is not really known.
	 */
	float[] ACTION_WEIGHT = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
	
	/** The rank how many weights should be used per item/user vector.
	 * Using a larger number should always increase the quality of the outcome.
	 */
	int MAX_RANK = 20;
	
	/** The shrinkage controls how fast the MAX_RANK factors are decreased (damped)
	 */
	float SHRINKAGE = 25;
	
	/** A threshold how long to compute until convergence. If the change since
	 * the last step is less than EPSILON stop.
	 */
	float EPSILON = 0.001f;
}
