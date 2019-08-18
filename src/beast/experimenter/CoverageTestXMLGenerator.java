package beast.experimenter;



import beast.evolution.substitutionmodel.*;
import beast.evolution.tree.Tree;
import beast.evolution.sitemodel.*;
import beast.evolution.alignment.*;
import beast.evolution.branchratemodel.StrictClockModel;
import beast.util.*;
import beast.app.seqgen.MergeDataWith;
import beast.app.seqgen.SequenceSimulator;
import beast.app.util.Application;
import beast.app.util.*;
import beast.core.*;
import beast.core.parameter.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import beagle.BeagleFlag;

@Description("Generate XML for performing coverage test (using CoverageCalculator)")
public class CoverageTestXMLGenerator extends beast.core.Runnable {
	final public Input<File> workingDirInput = new Input<>("workingDir", "working directory where input files live and output directory is created");
	final public Input<String> outDir = new Input<>("outDir", "output directory where generated XML goes (as sub dir of working dir)", "mcmc");
	final public Input<LogFile> logFileInput = new Input<>("logFile", "trace log file containing model paramter values to use for generating sequence data");
	final public Input<TreeFile> treeFileInput = new Input<>("treeFile", "tree log file containing trees to generate sequence data on");
	final public Input<XMLFile> xmlFileInput = new Input<>("xmlFile", "XML template file containing analysis to be merged with generated sequence data");
	final public Input<Integer> skipLogLinesInput = new Input<>("skip", "numer of log file lines to skip", 1);

	@Override
	public void initAndValidate() {
	}


	// set to true to use equal frequencies, otherwise sample frequencies from Dirichlet(1,1,1,1)
	static boolean equalFreqs = false;
	static boolean useGamma = false;
	static boolean usePropInvariant = false;


	static int N = 100;

	static void process(String dir, String analysisXML, String wdir) throws IllegalArgumentException, IllegalAccessException, IOException, XMLParserException {

		if (!(new File(dir).exists())) {
			new File(dir).mkdirs();
		}
		System.err.print("Processing " + dir);
		
	for (int i = 0; i < N; i++) {

		// set up model to draw samples from
		Sequence A=new Sequence(); A.initByName("taxon","sequence01","value","?");
		Sequence B=new Sequence(); B.initByName("taxon","sequence02","value","?");
		Sequence C=new Sequence(); C.initByName("taxon","sequence03","value","?");
		Sequence D=new Sequence(); D.initByName("taxon","sequence04","value","?");
		Sequence E=new Sequence(); E.initByName("taxon","sequence05","value","?");
		Sequence F=new Sequence(); F.initByName("taxon","sequence06","value","?");
		Sequence G=new Sequence(); G.initByName("taxon","sequence07","value","?");
		Sequence I=new Sequence(); I.initByName("taxon","sequence08","value","?");
		Sequence H=new Sequence(); H.initByName("taxon","sequence09","value","?");
		Sequence J=new Sequence(); J.initByName("taxon","sequence10","value","?");
		
//		Sequence K=new Sequence(); K.initByName("taxon","sequence11","value","?");
//		Sequence L=new Sequence(); L.initByName("taxon","sequence12","value","?");
//		Sequence M=new Sequence(); M.initByName("taxon","sequence13","value","?");
//		Sequence N=new Sequence(); N.initByName("taxon","sequence14","value","?");
//		Sequence O=new Sequence(); O.initByName("taxon","sequence15","value","?");
//		Sequence P=new Sequence(); P.initByName("taxon","sequence16","value","?");
//		Sequence Q=new Sequence(); Q.initByName("taxon","sequence17","value","?");
//		Sequence R=new Sequence(); R.initByName("taxon","sequence18","value","?");
//		Sequence S=new Sequence(); S.initByName("taxon","sequence19","value","?");
//		Sequence T=new Sequence(); T.initByName("taxon","sequence20","value","?");
//	
//		Sequence U=new Sequence(); U.initByName("taxon","sequence21","value","?");
//		Sequence V=new Sequence(); V.initByName("taxon","sequence22","value","?");
//		Sequence W=new Sequence(); W.initByName("taxon","sequence23","value","?");
//		Sequence X=new Sequence(); X.initByName("taxon","sequence24","value","?");
//		Sequence Y=new Sequence(); Y.initByName("taxon","sequence25","value","?");
//		Sequence Z=new Sequence(); Z.initByName("taxon","sequence26","value","?");
//		Sequence AA=new Sequence(); AA.initByName("taxon","sequence27","value","?");
//		Sequence AB=new Sequence(); AB.initByName("taxon","sequence28","value","?");
//		Sequence AC=new Sequence(); AC.initByName("taxon","sequence29","value","?");
//		Sequence AD=new Sequence(); AD.initByName("taxon","sequence30","value","?");
//
//		Sequence AE=new Sequence(); AE.initByName("taxon","sequence31","value","?");
//		Sequence AF=new Sequence(); AF.initByName("taxon","sequence32","value","?");
//		Sequence AG=new Sequence(); AG.initByName("taxon","sequence33","value","?");
//		Sequence AH=new Sequence(); AH.initByName("taxon","sequence34","value","?");
//		Sequence AI=new Sequence(); AI.initByName("taxon","sequence35","value","?");
//		Sequence AJ=new Sequence(); AJ.initByName("taxon","sequence36","value","?");
//		Sequence AK=new Sequence(); AK.initByName("taxon","sequence37","value","?");
//		Sequence AL=new Sequence(); AL.initByName("taxon","sequence38","value","?");
//		Sequence AM=new Sequence(); AM.initByName("taxon","sequence39","value","?");
//		Sequence AN=new Sequence(); AN.initByName("taxon","sequence40","value","?");

		Alignment data = new Alignment();
		data.initByName("sequence", A, "sequence", B, "sequence", C, "sequence", D, "sequence", E,
				"sequence", F, "sequence", G, "sequence", H, "sequence", I, "sequence", J				
//				,"sequence", K, "sequence", L, "sequence", M, "sequence", N, "sequence", O,
//				"sequence", P, "sequence", Q, "sequence", R, "sequence", S, "sequence", T,
//				"sequence", U, "sequence", V, "sequence", W, "sequence", X, "sequence", Y,
//				"sequence", Z, "sequence", AA, "sequence", AB, "sequence", AC, "sequence", AD,
//				"sequence", AE, "sequence", AF, "sequence", AG, "sequence", AH, "sequence", AI,
//				"sequence", AJ, "sequence", AK, "sequence", AL, "sequence", AM, "sequence", AN
				);
//		tree = new beast.util.TreeParser(newick="(((A:0.1,B:0.1),C:0.15):0.05,(D:0.1,E:0.1):0.1)", taxa=data, IsLabelledNewick=true);
		Tree tree = trees.get(i);

		RealParameter freqs=(equalFreqs ? new RealParameter("0.25 0.25 0.25 0.25") : new RealParameter(f[i][0] + " " + f[i][1] + " " + f[i][2] + " " + f[i][3]));
		Frequencies f = new Frequencies();
		f.initByName("frequencies",freqs);
		
		HKY hky = new beast.evolution.substitutionmodel.HKY();
		hky.initByName("frequencies", f, 
			"kappa", kappa[i] + ""
		);
//		LogNormalDistributionModel distr = new beast.math.distributions.LogNormalDistributionModel();
//		distr.initByName("S",s[i] + "", "meanInRealSpace", true, "M", "1.0");
//		IntegerParameter  rateCategories = new beast.core.parameter.IntegerParameter(c[i][0] + " " + c[i][1] + " "  + c[i][2] + " "  + c[i][3] + " "  + c[i][4] + " "  + c[i][5] + " "  + c[i][6] + " "  + c[i][7]);
//		UCRelaxedClockModel clockmodel = new beast.evolution.branchratemodel.UCRelaxedClockModel();
//		clockmodel.initByName("distr", distr, "rateCategories", rateCategories, "tree", tree);

		StrictClockModel clockmodel = new StrictClockModel();

		// change gammaCategoryCount=1 for generating without gamma rate categories
		int gcc = (useGamma ? 4 : 1);
	    RealParameter p = new RealParameter("0.0");
		SiteModel sitemodel = new SiteModel();
		sitemodel.initByName("gammaCategoryCount", gcc, "substModel", hky, "shape", ""+shapes[i], "proportionInvariant", p);
		MergeDataWith mergewith = new beast.app.seqgen.MergeDataWith();
		mergewith.initByName("template", wdir + analysisXML, "output", dir + "/analysis-out" + i + ".xml");
		SequenceSimulator sim = new beast.app.seqgen.SequenceSimulator();
		sim.initByName("data", data, "tree", tree, "sequencelength", 2500, "outputFileName", 
				"gammaShapeSequence.xml", "siteModel", sitemodel, "branchRateModel", clockmodel, 
				"merge", mergewith);
		// produce gammaShapeSequence.xml and merge with analysis.xml to get analysis-out.xml
		sim.run();
		System.err.print('.');
	  }
	System.err.println();
	}


	static List<Tree> trees;


	
	public void run() throws Exception {
		
		String wdir = workingDirInput.get().getAbsolutePath(); // "/Users/remco/workspace/starbeast3/mcmctest/coalescent-constant";

		Logger.FILE_MODE = beast.core.Logger.LogFileMode.overwrite;

		// set up flags for BEAGLE -- YMMV
		long beagleFlags = BeagleFlag.VECTOR_SSE.getMask() | BeagleFlag.PROCESSOR_CPU.getMask();
		System.setProperty("beagle.preferred.flags", Long.toString(beagleFlags));
//		IntegerParameter modelIndicator = new IntegerParameter("0");
//		RealParameter rates = new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0");
//		Frequencies freqs = new Frequencies();
//		freqs.initByName("frequencies","0.25 0.25 0.25 0.25");
//
//		NucleotideRevJumpSubstModel substModel0 = new NucleotideRevJumpSubstModel();
//		substModel0.initByName("modelSet","transitionTransversionSplit", "modelIndicator",modelIndicator, "rates", rates, "frequencies", freqs);

		
		// sanity check
		//if (s.length != N) {throw new RuntimeException("s.length != N");}
		if (f.length != N) {throw new RuntimeException("f.length != N");}
		if (kappa.length != N) {throw new RuntimeException("r.length != N");}
		if (shapes.length != N) {throw new RuntimeException("shapes.length != N");}
		//if (c.length != N) {throw new RuntimeException("c.length != N");}
		
		
		NexusParser parser = new beast.util.NexusParser();
		File fin = new File(wdir + "/dna.trees");
		parser.parseFile(fin);
		trees = parser.trees;
		if (trees.size() != N) {throw new RuntimeException("trees.length != N: " + trees.size() + " != " + N);}

		equalFreqs = false;useGamma = true;usePropInvariant = false;
		process(wdir + "/mcmc/", "/mcmcanalysis.xml", wdir);

		System.err.println("Done");

	}
			
	public static void main(String[] args) throws Exception {
		new Application(new CoverageTestXMLGenerator(), "CoverageTestXMLGenerator", args);
	}


}
