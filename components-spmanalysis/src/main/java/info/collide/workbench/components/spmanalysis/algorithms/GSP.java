package info.collide.workbench.components.spmanalysis.algorithms;

import java.io.IOException;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.AlgoGSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator_Qualitative;

public class GSP implements Algorithm {

	@Override
	public String run(String sequenceDBPath, double minSupport) throws IOException {

		String output = ".//output" + System.currentTimeMillis() + ".txt";
		// Load a sequence database
		double mingap = 0, maxgap = Integer.MAX_VALUE, windowSize = 0;

		boolean keepPatterns = true;
		boolean verbose = false;

		AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
		SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator);

		sequenceDatabase.loadFile(sequenceDBPath, minSupport);

		AlgoGSP algorithm = new AlgoGSP(minSupport, mingap, maxgap, windowSize, abstractionCreator);

		algorithm.runAlgorithm(sequenceDatabase, keepPatterns, verbose, output, true);

		return output;
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
