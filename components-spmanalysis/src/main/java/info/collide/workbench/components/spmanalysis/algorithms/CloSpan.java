package info.collide.workbench.components.spmanalysis.algorithms;

import java.io.IOException;

import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.AlgoCloSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator_Qualitative;

public class CloSpan implements Algorithm {

	@Override
	public String run(String sequenceDBPath, double minSupport) throws IOException {

		String outputFilePath = ".//output" + System.currentTimeMillis() + ".txt";
		// Load a sequence database
		boolean keepPatterns = true;
		boolean verbose = false;
		boolean findClosedPatterns = true;
		boolean executePruningMethods = true;

		AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();

		SequenceDatabase sequenceDatabase = new SequenceDatabase();

		sequenceDatabase.loadFile(sequenceDBPath, minSupport);

		AlgoCloSpan algorithm = new AlgoCloSpan(minSupport, abstractionCreator, findClosedPatterns,
				executePruningMethods);

		algorithm.runAlgorithm(sequenceDatabase, keepPatterns, verbose, outputFilePath, true);

		return outputFilePath;
	}

	@Override
	public String getSupportMark() {
		return "#SUP:";
	}

	@Override
	public int getStartingIndex() {
		return 1;
	}

}
