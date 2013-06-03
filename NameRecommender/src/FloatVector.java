
/**
 * @author Johannes
 *
 * A simple column vector of a fixed size.
 * 
 * The size of the vector is fixed during initialization.
 * This class supports:
 *		* vector + vector where both have to be of the same size.  
 *		* vector * scalar
 * 
 */
public class FloatVector {
	private float m_Rows[];
	
	FloatVector(int _length) {
		m_Rows = new float[_length];
	}
	
	public int length() {
		return m_Rows.length;
	}
	
	/**
	 * Fast get without a check of the index
	 * @param _row Index of the row (which is a single float value).
	 * @return The value of the _row-th vector component.
	 */
	public float get( int _row ) {
		return m_Rows[_row];
	}
	
	
	/**
	 * Adds a vector to the current one by changing its content. This method
	 * does not create a new copy for performance reasons.
	 * 
	 * @param _b The other vector
	 */
	public void add( FloatVector _b ) {
		if(_b.length() != length()) throw new IllegalArgumentException();
		
		for( int i=0; i<length(); ++i )
			m_Rows[i] += _b.get(i);
	}
	
	/**
	 * Multiplication with a scalar
	 * @param _s The scalar value.
	 */
	public void mul( float _s ) {
		for( int i=0; i<length(); ++i )
			m_Rows[i] *= _s;
	}
}
