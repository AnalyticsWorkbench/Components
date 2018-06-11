package info.collide.workbench.components.spmanalysis.algorithms;

import java.io.IOException;

import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.AlgoClaSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreatorStandard_Map;

public class ClaSP implements Algorithm {

	@Override
	public String run(String sequenceDBPath, double minSupport) throws IOException {

		String outputFilePath = ".//output" + System.currentTimeMillis() + ".txt";
		// Load a sequence database
		boolean keepPatterns = true;
		boolean verbose = true;
		boolean findClosedPatterns = true;
		boolean executePruningMethods = true;

		AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
		IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

		SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

		double absoluteSupport;
		absoluteSupport = sequenceDatabase.loadFile(sequenceDBPath, minSupport);

		AlgoClaSP algorithm = new AlgoClaSP(absoluteSupport, abstractionCreator,
				findClosedPatterns, executePruningMethods);

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
