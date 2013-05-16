
/**
 * The set of parameters which controls the output of the recommender. This
 * is the only thing which can be learned to change the recommended item list. 
 */
public class ParameterSet {
	// Each action of a user is increasing its interest value of the
	// specific name.
	// Each action can have an other weight where the order is not really
	// known.
	float[] ActionWeight = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
	
	// How strong should be the influence of a similar user or item to the
	// the interest values.
	float UserSimilarityPropagation;
	float ItemSimilarityPropagation;
}
