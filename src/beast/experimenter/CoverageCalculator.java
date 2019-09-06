package beast.experimenter;

import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Formatter;

import beast.app.util.Application;
import beast.app.util.OutFile;
import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.Runnable;
import beast.core.util.Log;
import beast.util.LogAnalyser;

@Description("Calculate how many times entries in log file are covered in an estimated 95% HPD interval")
public class CoverageCalculator extends Runnable {
	final public Input<File> logFileInput = new Input<>("log", "log file containing actual values", Validate.REQUIRED);
	final public Input<Integer> skipLogLinesInput = new Input<>("skip", "numer of log file lines to skip", 1);
	final public Input<File> logAnalyserFileInput = new Input<>("logAnalyser", "file produced by loganalyser tool using the -oneline option, containing estimated values", Validate.REQUIRED);
	public Input<OutFile> outputInput = new Input<>("out", "output file for trace log with truth and mean estimates. Not produced if not specified");

	final static String space = "                                                ";
	
	@Override
	public void initAndValidate() {
	}

	@Override
	public void run() throws Exception {
		LogAnalyser truth = new LogAnalyser(logFileInput.get().getAbsolutePath(), 0, true, false);
		LogAnalyser estimated = new LogAnalyser(logAnalyserFileInput.get().getAbsolutePath(), 0, true, false);
		int skip = skipLogLinesInput.get();
		
		if (truth.getTrace(0).length - skip != estimated.getTrace(0).length) {
			Log.warning("WARNING: traces are of different lengths: "
					+ (truth.getTrace(0).length - skip) + "!=" + estimated.getTrace(0).length);
		}
		
		NumberFormat formatter = new DecimalFormat("#0.00");
		NumberFormat formatter2 = new DecimalFormat("#0");
		
		Log.info(space + "coverage Mean ESS Min ESS");
		
		for (int i = 0; i < truth.getLabels().size(); i++) {
			String label = truth.getLabels().get(i);
			Double [] trueValues = truth.getTrace(label);
			Double [] lows = estimated.getTrace(label+".95%HPDlo");
			Double [] upps = estimated.getTrace(label+".95%HPDup");
			Double [] ess = estimated.getTrace(label+".ESS");
			if (lows == null || upps == null) {
				Log.warning("Skipping " + label);
			} else {
				int covered = 0;
				double minESS = Double.POSITIVE_INFINITY;
				double meanESS = 0;
				for (int j = 0; j < trueValues.length - skip; j++) {
					if (lows[j] <= trueValues[j + skip] && trueValues[j + skip] <= upps[j]) {
						covered++;
					} else {
						// System.out.println(lows[j] +"<=" + trueValues[j + skip] +"&&" + trueValues[j + skip] +" <=" + upps[j]);
					}
					minESS = Math.min(minESS, ess[j]);
					meanESS += ess[j];
				}
				meanESS /= (trueValues.length - skip);
				Log.info(label + (label.length() < space.length() ? space.substring(label.length()) : " ") + 
						formatter2.format(covered) + "\t   " + 
						formatter.format(meanESS) + "  " + formatter.format(minESS));
			}
		}

		if (outputInput.get() != null) {
			Log.warning("Writing to file " + outputInput.get().getPath());
        	PrintStream out = new PrintStream(outputInput.get());
        	out.print("sample\t");
        	for (int i = 0; i < truth.getLabels().size(); i++) {
    			String label = truth.getLabels().get(i);
        		out.print(label + "\t" + label + ".mean\t");
        	}
			int n = truth.getTrace(0).length - skip;

			for (int j = 0; j < n; j++) {
				out.print(j+"\t");
	        	for (int i = 0; i < truth.getLabels().size(); i++) {
	    			String label = truth.getLabels().get(i);
	    			Double [] trueValues = truth.getTrace(label);
	    			Double [] estimates = estimated.getTrace(label+".mean");
	        		out.print(trueValues[skip + j] + "\t" + estimates[j] + "\t");
	        	}
	        	out.println();
			}
        	out.close();
        }
		
	}


	public static void main(String[] args) throws Exception {
		new Application(new CoverageCalculator(), "Coverage Calculator", args);
	}
}
