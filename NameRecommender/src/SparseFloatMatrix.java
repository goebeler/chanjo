import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * @author Johannes
 *
 * A sparse matrix of float values which can be iterated per row.
 * 
 * TODO: setting something 0 will currently not remove the entry but should do
 * exactly that.
 */
public class SparseFloatMatrix {
	
	public static class IndexValuePair implements Comparable<IndexValuePair> {
		public IndexValuePair( int _index, float _value ) {
			index = _index;
			value = _value; 
		}

		int index;
		float value;
		
		@Override
		public int compareTo(IndexValuePair arg0) {
			return (int) Math.signum( value-arg0.value );
		}
	}
	
	// numRows Sorted linked lists
	private ArrayList<IndexValuePair>[] m_Rows;
	
	// The number of rows is accessible by m_Rows.size().
	// The number of columns is given by m_NumColumns;
	private int m_NumColumns; 
	
	SparseFloatMatrix( int _numRows, int _numColumns ) {
		m_NumColumns = _numColumns;
		m_Rows = new ArrayList[_numRows];
		for(int i=0; i<_numRows; ++i)
			m_Rows[i] = new ArrayList<IndexValuePair>();
	}
	
	public void set( int _row, int _column, float _value ) {
		int index = binsearch( _row, _column );
		if( (index < m_Rows[_row].size()) && m_Rows[_row].get(index).index == _column )
			m_Rows[_row].get(index).value = _value;
		else
			m_Rows[_row].add(index, new IndexValuePair(_column, _value));
	}
	
	public float get( int _row, int _column ) {
		int index = binsearch( _row, _column );
		if( (index < m_Rows[_row].size()) && m_Rows[_row].get(index).index == _column )
			return m_Rows[_row].get(index).value;
		else return 0.0f;	// Default value
	}
	
	public int getNumRows() {
		return m_Rows.length;
	}
	
	public int getNumColumns() {
		return m_NumColumns;
	}
	
	public int getNumEntriesInRow( int _row ) {
		return m_Rows[_row].size(); 
	}
	
	
	/**
	 * Binary search for a matrix-indexed element.
	 * 
	 * @param _column Column index of the element in the matrix. 
	 * @return The item index in the internal array.
	 */
	private int binsearch( int _row, int _column )
	{
		int l = 0;
		int r = m_Rows[_row].size()-1;
		while( l<=r ) {
			int m = (l+r)/2;
			if( m_Rows[_row].get(m).index < _column ) {
				// Go to the right side
				l = m+1;
			} else if( m_Rows[_row].get(m).index > _column ) {
				// Go to the left side
				r = m-1;
			} else
				// found
				return m;
		}
		return l;
	}
	
	/**
	 * An iterator to iterate over all elements of one row.
	 *
	 */
	public class RowIterator implements Iterator<Float> {

		private int m_Index = -1;
		private ArrayList<IndexValuePair> m_Data = null;
		
		public RowIterator( ArrayList<IndexValuePair> _data ) {
			m_Data = _data;
		}
		

		public boolean hasNext() {
			return m_Index + 1 < m_Data.size();
		}
		 
		public Float next() throws NoSuchElementException {
			if( !hasNext() ) {
				throw new NoSuchElementException("No more elements");
		    }
		    return m_Data.get(++m_Index).value;
		}
		 
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Operation is not supported");
		}
	}
	
	public Iterator<Float> getIterator( int _row ) {
		return new RowIterator( m_Rows[_row] );
	}
	
	public Iterator<IndexValuePair> getSkipIterator( int _row ) {
		return m_Rows[_row].iterator();
	}
	
	public ArrayList<IndexValuePair> getRow( int _row ) {
		return m_Rows[_row];
	}
}
