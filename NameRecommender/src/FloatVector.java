
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
	 * Fast set without a check of the index
	 * @param _row Index of the row (which is a single float value).
	 * @param _value The value to be set at the specified position.
	 */
	public void set( int _row, float _value ) {
		m_Rows[_row] = _value;
	}
	
	
	/**
	 * Adds a vector to the current one by changing its content. This method
	 * does not create a new copy for performance reasons (equal to a +=).
	 * 
	 * @param _b The other vector
	 */
	public void add( FloatVector _b ) {
		if(_b.length() != length()) throw new IllegalArgumentException();
		
		for( int i=0; i<length(); ++i )
			m_Rows[i] += _b.get(i);
	}
	
	public void sub( FloatVector _b ) {
		if(_b.length() != length()) throw new IllegalArgumentException();
		
		for( int i=0; i<length(); ++i )
			m_Rows[i] -= _b.get(i);
	}

	/**
	 * Adds two vectors component wise. This method
	 * does create a new copy (equal to a +).
	 * 
	 * @param _a The first vector
	 * @param _b The second vector
	 */
	static public FloatVector add( FloatVector _a, FloatVector _b ) {
		if(_a.length() != _b.length()) throw new IllegalArgumentException();
		
		FloatVector result = new FloatVector(_a.length());
		for( int i=0; i<_a.length(); ++i )
			result.m_Rows[i] = _a.m_Rows[i] + _b.m_Rows[i];
		return result;
	}

	
	static public FloatVector sub( FloatVector _a, FloatVector _b ) {
		if(_a.length() != _b.length()) throw new IllegalArgumentException();
		
		FloatVector result = new FloatVector(_a.length());
		for( int i=0; i<_a.length(); ++i )
			result.m_Rows[i] = _a.m_Rows[i] - _b.m_Rows[i];
		return result;
	}

	
	/**
	 * Multiplication with a scalar
	 * @param _s The scalar value.
	 */
	public void mul( float _s ) {
		for( int i=0; i<length(); ++i )
			m_Rows[i] *= _s;
	}
	
	static public FloatVector mul( float _s, FloatVector _v ) {
		FloatVector result = new FloatVector(_v.length());
		for( int i=0; i<_v.length(); ++i )
			result.m_Rows[i] = _v.m_Rows[i]*_s;
		return result;
	}
	
	/**
	 * Computes a (scalar)multiply+(vector)add operation in on step.
	 * 
	 * This is much more optimal because only one allocation is required
	 * and both operations are computed inside the same loop.
	 * @param _s
	 * @param _a
	 * @param _b
	 * @return s * a + b
	 */
	static public FloatVector mad( float _s, FloatVector _a, FloatVector _b ) {
		if(_a.length() != _b.length()) throw new IllegalArgumentException();
		
		FloatVector result = new FloatVector(_a.length());
		for( int i=0; i<_a.length(); ++i )
			result.m_Rows[i] = _a.m_Rows[i]*_s + _b.m_Rows[i];
		return result;
	}
	
	
	public float dot( FloatVector _b ) {
		if(length() != _b.length()) throw new IllegalArgumentException();
		
		float result = 0.0f;
		for( int i=0; i<length(); ++i )
			result += m_Rows[i] * _b.m_Rows[i];
		return result;
	}

}
