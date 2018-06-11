package info.collide.workbench.components.spmanalysis.algorithms;

import java.io.IOException;

import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoCMSPAM;

public class CMSPAM implements Algorithm {

	@Override
	public String run(String sequenceDBPath, double minSupport) throws IOException {

		String output = ".//output" + System.currentTimeMillis() + ".txt";
		// Create an instance of the algorithm
		AlgoCMSPAM algo = new AlgoCMSPAM();

		algo.runAlgorithm(sequenceDBPath, output, minSupport, true);

		return output;
	}

	@Override
	public String getSupportMark() {
		return "SUP:";
	}

	@Override
	public int getStartingIndex() {
		return 0;
	}

}
