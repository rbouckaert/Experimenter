package beast.experimenter;


import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import beast.app.util.Application;
import beast.app.util.LogFile;
import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.util.LogAnalyser;
import beast.core.Runnable;
import beast.core.util.Log;

@Description("Calculate Kolmogorov-Smirnof statistic for comparing trace logs")
public class TraceKSStats extends Runnable {
	final public Input<LogFile> trace1Input = new Input<>("trace1", "first trace file to compare", Validate.REQUIRED);
	final public Input<LogFile> trace2Input = new Input<>("trace2", "second trace file to compare", Validate.REQUIRED);

	@Override
	public void initAndValidate() {
	}

	@Override
	public void run() throws Exception {
		int burnInPercentage = 10;
		LogAnalyser trace1 = new LogAnalyser(trace1Input.get().getAbsolutePath(), burnInPercentage, true, false);
		LogAnalyser trace2 = new LogAnalyser(trace2Input.get().getAbsolutePath(), burnInPercentage, true, false);
		
		// ensure traces are over the same entries
		if (trace1.getLabels().size() != trace2.getLabels().size()) {
			Log.warning("Looks like different log files -- expect things to crash");
		}
		String label = "Trace entry";				
		Log.info(label + (label.length() < CoverageCalculator.space.length() ? CoverageCalculator.space.substring(label.length()) : " ") + " p-value");
		for (int i = 0; i < trace1.getLabels().size(); i++) {
			if (!trace1.getLabels().get(i).equals(trace2.getLabels().get(i))) {
				Log.warning("Columns do not match: " + trace1.getLabels().get(i) + " != " + trace2.getLabels().get(i));
			}
			Double [] x = trace1.getTrace(i);
			Double [] y = trace2.getTrace(i);
			double [] x0 = toDouble(x);
			double [] y0 = toDouble(y);
			
			KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
			double p = test.kolmogorovSmirnovTest(x0, y0);
			label = trace1.getLabels().get(i);
			Log.info(label + (label.length() < CoverageCalculator.space.length() ? CoverageCalculator.space.substring(label.length()) : " ") + " " + p);
		}

	}

	private double[] toDouble(Double[] x) {
		double [] x0 = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			x0[i] = x[i];
		}
		return x0;
	}

	public static void main(String[] args) throws Exception {
		new Application(new TraceKSStats(), "Trace K-S statistics", args);
	}

}
