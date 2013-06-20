

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;


/**
 * Provides queries on arbitrary data sets loaded from raw files.
 *
 * The file has to contain one datum per line separated by white spaces or ','
 * or ';'.
 * Each datum has to have the same number of tokens, the entry is
 * skipped if not.
 *
 * TODO: Use hashing for performanter search.
 */
public class InstanceBase {
	private int m_NumAttributes;
    private ArrayList<String[]> m_Data;
    private int[] m_NumEntriesPerAttribute;
    
    /**
     * A copy of the data where each attribute is mapped to some integers.
     *   - the same integer means the same original string.
     *   - the integers are packed [0,n] where n is the number of different
     *     entries of the respective attribute.
     *     
     * The advantages of this dual representation:
     *   - comparisons with integers are much faster
     *   - the values can be used as array indices.
     */
    private ArrayList<int[]> m_MappedData;
    
    /**
	 * A map from string to unique id for each attribute.
     */
	HashMap<String,Integer>[] m_Maps;

	/**
     * One map for each attribute containing the original strings (inverse of m_Maps).
     * 
     * The elements are sorted by there unique index. So each item
     * appears only once.
     * 
     * The map is created from createDualRepresentation()
     */
    private ArrayList<String>[] m_InverseMaps;
    
    public final int getNumAttributes()         {return m_NumAttributes;}
    public final int getNumInstances()          {return m_Data.size();}

    /**
     * @param _DataFile 
     * Filename for a text file containing data (one instance per line).
     * Each line should be: "Attr1, Attr2, ... , AttrN" where the
	 * separation can also be done by white spaces or ';'.
	 * @param _ExpectedNumAttributes The expected number of attributes per line.
	 * Lines with an other number of entries are just ignored.
	 * @param _NumAttributes The number of attributes loaded from file. This
	 * can be different from the number of attributes per line. If
	 * _NumAttributes is smaller the other attributes are ignored. Otherwise
	 * the full line is loaded.
     */
    public InstanceBase(String _DataFile, int _ExpectedNumAttributes, int _NumAttributes) {
    	m_Data = new ArrayList<String[]>();

        // load data from file
        try {
            BufferedReader file = new BufferedReader(new FileReader(_DataFile));
            String line;
			// Unknown number of attributes per line
            int NumAttributesPerLine = -1;
			int numLines = 0;
            do {
                line = file.readLine();
                ++numLines;
                if(line != null && !line.isEmpty()) {
                    // The current line contains some data.
					// Parse it as somehow separated token list
					String[] tokens = line.split( "[;|,\\t*]\\s*" );
					
					// This is the first data found
					if( NumAttributesPerLine == -1 ) {
						NumAttributesPerLine = tokens.length;
						m_NumAttributes = Math.min(_NumAttributes, NumAttributesPerLine);
					}
					if( NumAttributesPerLine != tokens.length )
					{
						// This token has an other length than the first one
						// which is not allowed.
						System.out.println(
								"Not every line contains the same number of tokens (expected " + NumAttributesPerLine
								+ " found " + tokens.length + " in line " + numLines + ")"
						);
					} else {
						String[] newInstance = new String[m_NumAttributes];
						for( int i=0; i<m_NumAttributes; ++i )
							newInstance[i] = tokens[i].toLowerCase();
						m_Data.add(newInstance);
					}
                }
            } while(line != null);
            file.close();
            
            CreateDualRepresentaion();
            
            // Debug output
            System.out.println("\nLoaded data base:\n#INSTANCES: " + getNumInstances());
            System.out.println("#ATTRIBUTES: " + getNumAttributes());
        } catch (IOException ex) {
        	System.out.println(ex.getMessage());
        	System.out.println("Absolute path is: " + new File(".").getAbsolutePath());
        }
    }
    
    /**
     * @param _aPattern
     * getNumAttributes or less Strings for each attribute of one instance. The string
     * have to match one value exactly or:
     *   "" is interpreted as don't care (this attribute is ignored = match)
     *   every thing else is interpreted as no match
     * If the number of strings (n) in _aPattern is less than the number of stored
     * elements only the first n are checked for a match and the last ones are ignored.
     * @return Number of instances matching the given "hypotheses".
     */
    public int countInstances(String[] _aPattern) {
        int iCounter = 0;
        // check line after line
        for(int i=0;i<getNumInstances();++i) {
            boolean bMatch = true;
            for(int a=0; bMatch && a<_aPattern.length; ++a) {
                // Don't care?
                if(!_aPattern[a].isEmpty())
                    // Care! Does it matches?
                    bMatch &= m_Data.get(i)[a].equals(_aPattern[a]);
            }
            // The whole instance matches the query-pattern
            if(bMatch) ++iCounter;
        }
        return iCounter;
    }

    /**
     * 
     * @param _aPattern
     * getNumAttributes Strings for each attribute of one instance. The string
     * have to match one value exactly or:
     *   "" is interpreted as don't care (this attribute is ignored = match)
     *   every thing else is interpreted as no match
     * If the number of strings (n) in _aPattern is less than the number of stored
     * elements only the first n are checked for a match and the last ones are ignored.
     * @return A list instances matching the given "hypotheses". Even if the number of
     * given patterns is less than getNumAttributes the whole data set is returned.
     */
	public ArrayList<String[]> getInstances(String[] _aPattern) {
		ArrayList<String[]> result = new ArrayList<String[]>();
        // check line for line
        for(int i=0;i<getNumInstances();++i) {
            boolean bMatch = true;
            for(int a=0;bMatch&& a<_aPattern.length; ++a) {
                // Don't care?
                if(!_aPattern[a].isEmpty())
                    // Care! Does it matches?
                    bMatch &= m_Data.get(i)[a].equals(_aPattern[a]);
            }
            // The whole instance matches the query-pattern
            if(bMatch) result.add(m_Data.get(i));
        }
        return result;
    }
	
	public int getNumUniqueEntries( int _AttributeIdx ) {
		return m_NumEntriesPerAttribute[_AttributeIdx];
	}
	
	public Iterator<String[]> getIterator() {
		return m_Data.iterator();
	}
	
	public Iterator<int[]> getMappedIterator() {
		return m_MappedData.iterator();
	}
	
	public String getString( int _AttributeIdx, int _MappedIndex ) {
		return m_InverseMaps[_AttributeIdx].get(_MappedIndex);
	}
	
	public int getMappedID( int _AttributeIdx, String _token ) {
		Integer r = m_Maps[_AttributeIdx].get(_token);
		return r==null ? -1 : r;
	}
	
	
	private void CreateDualRepresentaion() {
		m_NumEntriesPerAttribute = new int[m_NumAttributes];
		m_MappedData = new ArrayList<int[]>();
		m_InverseMaps = new ArrayList[m_NumAttributes];
		// Use hash maps to find out if the element was seen before and if yes
		// which index it has.
		m_Maps = new HashMap[m_NumAttributes];
		for(int a=0; a<m_NumAttributes; ++a) {
			m_Maps[a] = new HashMap<String,Integer>();
			m_InverseMaps[a] = new ArrayList<String>();
		}
		
		for( String[] it : m_Data )
		{
			int[] newDatum = new int[m_NumAttributes];
			for( int a=0; a<m_NumAttributes; ++a)
			{
				Integer i = m_Maps[a].get(it[a]);
				if( i==null ) {
					m_Maps[a].put(it[a], new Integer(m_NumEntriesPerAttribute[a]));
					newDatum[a] = m_NumEntriesPerAttribute[a];
					m_InverseMaps[a].add(it[a]);
					++m_NumEntriesPerAttribute[a];
				} else {
					newDatum[a] = i;
				}
			}
			m_MappedData.add(newDatum);
		}
	}
}
