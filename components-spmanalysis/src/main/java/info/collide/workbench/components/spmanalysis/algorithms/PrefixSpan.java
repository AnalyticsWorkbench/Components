package info.collide.workbench.components.spmanalysis.algorithms;

import java.io.IOException;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoPrefixSpan;

public class PrefixSpan implements Algorithm {

	@Override
	public String run(String sequenceDBPath, double minSupport) throws IOException {

		String outputPath = ".//output" + System.currentTimeMillis() + ".txt";

		AlgoPrefixSpan algo = new AlgoPrefixSpan();

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
