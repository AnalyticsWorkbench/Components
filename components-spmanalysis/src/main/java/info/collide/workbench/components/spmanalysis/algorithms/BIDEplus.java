package info.collide.workbench.components.spmanalysis.algorithms;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoBIDEPlus;
import java.io.IOException;

public class BIDEplus implements Algorithm {

	@Override
	public String run(String sequenceDBPath, double minSupport) throws IOException {

		String outputPath = ".//output" + System.currentTimeMillis() + ".txt";
		// Load a sequence database // Not necessary with latest SPMF version
		//SequenceDatabase sequenceDatabase = new SequenceDatabase();
		//sequenceDatabase.loadFile(sequenceDBPath);
                
		//int minsup = (int) Math.ceil((minSupport * sequenceDatabase.getSequences().size()));
                
		AlgoBIDEPlus algo = new AlgoBIDEPlus(); //

		// execute the algorithm
                
		algo.setShowSequenceIdentifiers(true);
		algo.runAlgorithm(sequenceDBPath, minSupport, outputPath);

		return outputPath;
	}

	@Override
	public String getSupportMark() {
		return "#SUP:";
	}

	@Override
	public int getStartingIndex() {
		return 0;
	}

}
