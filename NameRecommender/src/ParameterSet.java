
/**
 * The set of parameters which controls the output of the recommender. This
 * is the only thing which can be learned to change the recommended item list. 
 */
public class ParameterSet {
	/** Each action of a user is increasing its interest value of the
	 * specific name.
	 * Each action can have an other weight where the order is not really known.
	 * 
	 * The order (for the current data) is: link-search, enter_search, link_category_search, name_details, add_favorite
	 */
	float[] ACTION_WEIGHT = {0.05f, 0.2f, 0.05f, 0.1f, 0.6f};
	
	/** The rank how many weights should be used per item/user vector.
	 * Using a larger number should always increase the quality of the outcome.
	 */
	int MAX_RANK = 40;
	
	/** The shrinkage controls how fast the MAX_RANK factors are decreased (damped)
	 */
	float SHRINKAGE = 3.9f;
	
	/** A threshold how long to compute until convergence. If the change since
	 * the last step is less than EPSILON stop.
	 */
	float EPSILON = 0.0001f;
}
