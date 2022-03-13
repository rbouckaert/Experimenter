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
import java.util.ArrayList;
import java.util.List;

import beagle.BeagleFlag;

@Description("Generate XML for performing coverage test (using CoverageCalculator)")
public class CoverageTestXMLGenerator extends beast.core.Runnable {
	final public Input<File> workingDirInput = new Input<>("workingDir",
			"working directory where input files live and output directory is created");
	final public Input<String> outDirInput = new Input<>("outDir",
			"output directory where generated XML goes (as sub dir of working dir)", "mcmc");
	final public Input<LogFile> logFileInput = new Input<>("logFile",
			"trace log file containing model paramter values to use for generating sequence data");
	final public Input<TreeFile> treeFileInput = new Input<>("treeFile",
			"tree log file containing trees to generate sequence data on");
	final public Input<XMLFile> xmlFileInput = new Input<>("xmlFile",
			"XML template file containing analysis to be merged with generated sequence data");
	final public Input<Integer> skipLogLinesInput = new Input<>("skip", "numer of log file lines to skip", 1);
	final public Input<Integer> burnInPercentageInput = new Input<>("burnin",
			"percentage of trees to used as burn-in (and will be ignored)", 1);
	final public Input<Boolean> useGammaInput = new Input<>("useGamma", "use gamma rate heterogeneity", true);

	int N = 100;
	List<Tree> trees;
	Double[][] f;
	Double[] kappa;
	Double[] shapes;

	@Override
	public void initAndValidate() {
	}

	void process() throws IllegalArgumentException, IllegalAccessException, IOException, XMLParserException {
		String dir = outDirInput.get();
		String analysisXML = xmlFileInput.get().getAbsolutePath();
		String wdir = workingDirInput.get().getAbsolutePath();

		if (!(new File(dir).exists())) {
			new File(dir).mkdirs();
		}
		System.err.print("Processing " + dir);

		for (int i = 0; i < N; i++) {

			// set up model to draw samples from
			String[] taxa = trees.get(0).getTaxaNames();
			List<Sequence> seqs = new ArrayList<>();
			for (int j = 0; j < taxa.length; j++) {
				Sequence A = new Sequence();
				A.initByName("taxon", taxa[j], "value", "?");
				seqs.add(A);
			}

			Alignment data = new Alignment();
			data.initByName("sequence", seqs);

			Tree tree = trees.get(i);

			RealParameter freqs = new RealParameter(f[0][i] + " " + f[1][i] + " " + f[2][i] + " " + f[3][i]);
			Frequencies f = new Frequencies();
			f.initByName("frequencies", freqs);

			HKY hky = new beast.evolution.substitutionmodel.HKY();
			hky.initByName("frequencies", f, "kappa", kappa[i] + "");

			StrictClockModel clockmodel = new StrictClockModel();

			// change gammaCategoryCount=1 for generating without gamma rate
			// categories
			int gcc = (useGammaInput.get() ? 4 : 1);
			RealParameter p = new RealParameter("0.0");
			SiteModel sitemodel = new SiteModel();
			sitemodel.initByName("gammaCategoryCount", gcc, "substModel", hky, "shape", "" + shapes[i],
					"proportionInvariant", p);
			MergeDataWith mergewith = new beast.app.seqgen.MergeDataWith();
			mergewith.initByName("template", analysisXML, "output", dir + "/analysis-out" + i + ".xml");
			SequenceSimulator sim = new beast.app.seqgen.SequenceSimulator();
			sim.initByName("data", data, "tree", tree, "sequencelength", 2500, "outputFileName",
					"gammaShapeSequence.xml", "siteModel", sitemodel, "branchRateModel", clockmodel, "merge",
					mergewith);
			// produce gammaShapeSequence.xml and merge with analysis.xml to get
			// analysis-out.xml
			sim.run();
			System.err.print('.');
		}
		System.err.println();
	}

	public void run() throws Exception {
		LogAnalyser trace = new LogAnalyser(logFileInput.get().getPath(), burnInPercentageInput.get(), true, false);

		N = trace.getTrace(0).length;

		List<String> labels = trace.getLabels();
		f = new Double[N][];
		int index = getIndex(labels, "freq");
		if (index > 0) {
			f[0] = trace.getTrace(index);
			f[1] = trace.getTrace(index + 1);
			f[2] = trace.getTrace(index + 2);
			f[3] = trace.getTrace(index + 3);
		} else {
			// no frequencies specified
			f[0] = defaultValue(0.25);
			f[1] = defaultValue(0.25);
			f[2] = defaultValue(0.25);
			f[3] = defaultValue(0.25);
		}

		index = getIndex(labels, "kappa");
		if (index > 0) {
			kappa = trace.getTrace(index);
		} else {
			kappa = defaultValue(1.0);
		}
		
		shapes = null;
		if (useGammaInput.get()) {
			shapes = trace.getTrace(getIndex(labels, "shape"));
		} else {
			shapes = defaultValue(1.0);
		}

		String wdir = workingDirInput.get().getAbsolutePath();

		Logger.FILE_MODE = beast.core.Logger.LogFileMode.overwrite;

		// set up flags for BEAGLE -- YMMV
		long beagleFlags = BeagleFlag.VECTOR_SSE.getMask() | BeagleFlag.PROCESSOR_CPU.getMask();
		System.setProperty("beagle.preferred.flags", Long.toString(beagleFlags));

		NexusParser parser = new beast.util.NexusParser();
		File fin = treeFileInput.get();
		parser.parseFile(fin);
		trees = parser.trees;
		int burnin = 0;
		while (trees.size() > N) {
			trees.remove(0);
			burnin++;
		}

		if (trees.size() + burnin != N * (100 + burnInPercentageInput.get()) / 100) {
			throw new RuntimeException("treeFile length != logFile length");
		}

		process();

		System.err.println("Done");

	}

	private Double[] defaultValue(double d) {
		Double [] v = new Double[N];
		for (int i = 0; i < N; i++) {
			v[i] = d;
		}
		return v;
	}

	private int getIndex(List<String> labels, String prefix) {
		for (int i = 0; i < labels.size(); i++) {
			if (labels.get(i).startsWith(prefix)) {
				return i + 1;
			}
		}
		return 0;
	}

	public static void main(String[] args) throws Exception {
		new Application(new CoverageTestXMLGenerator(), "CoverageTestXMLGenerator", args);
	}

}
