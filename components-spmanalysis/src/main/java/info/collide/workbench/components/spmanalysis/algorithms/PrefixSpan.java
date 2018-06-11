package info.collide.workbench.components.spmanalysis.algorithms;

import java.io.IOException;

import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoPrefixSpan;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;

public class PrefixSpan implements Algorithm {

	@Override
	public String run(String sequenceDBPath, double minSupport) throws IOException {

		String outputPath = ".//output" + System.currentTimeMillis() + ".txt";
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase();
		sequenceDatabase.loadFile(sequenceDBPath);

		AlgoPrefixSpan algo = new AlgoPrefixSpan();

		int minsup = (int) Math.ceil((minSupport * sequenceDatabase.getSequences().size()));

		// execute the algorithm
		algo.setShowSequenceIdentifiers(true);
		algo.runAlgorithm(sequenceDatabase, outputPath, minsup);

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
