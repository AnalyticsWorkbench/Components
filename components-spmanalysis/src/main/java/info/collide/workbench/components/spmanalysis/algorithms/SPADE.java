package info.collide.workbench.components.spmanalysis.algorithms;

import java.io.IOException;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.AlgoSPADE;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration.CandidateGenerator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration.CandidateGenerator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator_FatBitmap;

public class SPADE implements Algorithm {

	@Override
	public String run(String sequenceDBPath, double minSupport) throws IOException {

		String outputPath = ".//output" + System.currentTimeMillis() + ".txt";
		// Load a sequence database

		boolean keepPatterns = true;
		boolean verbose = false;

		AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
		boolean dfs = true;

		IdListCreator idListCreator = IdListCreator_FatBitmap.getInstance();

		CandidateGenerator candidateGenerator = CandidateGenerator_Qualitative.getInstance();

		SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator);

		sequenceDatabase.loadFile(sequenceDBPath, minSupport);

		AlgoSPADE algorithm = new AlgoSPADE(minSupport, dfs, abstractionCreator);

		algorithm.runAlgorithm(sequenceDatabase, candidateGenerator, keepPatterns, verbose,
				outputPath, true);
		return outputPath;
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
