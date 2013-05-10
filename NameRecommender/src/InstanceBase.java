

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


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
    
    public final int getNumAttributes()         {return m_NumAttributes;}
    public final int getNumInstances()          {return m_Data.size();}

    /**
     * @param _DataFile 
     * Filename for a text file containing data (one instance per line).
     * Each line should be: "Attr1, Attr2, ... , AttrN" where the
	 * separation can also be done by white spaces or ';'.
     */
    public InstanceBase(String _DataFile) {
        // load data from file
        try {
            BufferedReader file = new BufferedReader(new FileReader(_DataFile));
            String line;
			// Unknown number of attributes
			m_NumAttributes = -1;
            do {
                line = file.readLine();
                if(line != null && !line.isEmpty()) {
                    // The current line contains some data.
					// Parse it as somehow separated token list
					String[] tokens = line.split( "[;|,]\\s*" );
					
					// This is the first data found
					if( m_NumAttributes == -1 )
						m_NumAttributes = tokens.length;
					if( m_NumAttributes != tokens.length )
					{
						// This token has an other length than the first one
						// which is not allowed.
						System.out.println(
								"Not every line contains the same number of tokens (expected " + m_NumAttributes
								+ " found " + tokens.length + ")"
						);
					} else
						m_Data.add(tokens);
                }
            } while(line != null);
            file.close();
        } catch (IOException ex) {
        	System.out.println(ex.getMessage());
        }
        
        // Debug output
        System.out.println("Loaded data base.\n\n#INSTANCES: " + getNumInstances());
        System.out.println("Loaded data base.\n\n#ATTRIBUTES: " + getNumAttributes());
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

}
