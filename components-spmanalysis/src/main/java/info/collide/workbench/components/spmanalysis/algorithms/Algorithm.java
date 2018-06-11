package info.collide.workbench.components.spmanalysis.algorithms;

import java.io.IOException;

public interface Algorithm {

	/**
	 * Executes the algorithm
	 * 
	 * @param sequenceDBPath
	 *            path to the sequence database
	 * @param minSupport
	 *            the relative minimum support of the pattern
	 * @return the path to the result file
	 * @throws IOException
	 *             thrown, when there are problems reading the sequence database
	 *             or writing the result file
	 */
	public abstract String run(String sequenceDBPath, double minSupport) throws IOException;

	/**
	 * A algorithm specific mark for correct parsing of the result file
	 * 
	 * @return the mark, where the support starts in the result file
	 */
	public abstract String getSupportMark();
	
	/**
	 * A algorithm specific starting index of the sequences
	 * @return 0 or 1
	 */
	public int getStartingIndex();

}
