Experimenter is a [BEAST 2](http://beast2.org) package that assists in simulation studies to verify correctness of the implementation. The goal of this particular simulation studies is to make sure that the model or operator implementation is correct by running N analysis on simulated data (using SequenceSimulator) on a tree and site model parameters sampled from a prior.

To run a simulation study:

* set up XML for desired model and sample from prior
* generate (MCMC) analysis for each of the samples (say 100)
* run the analyses
* use loganalyser to summarise trace files
* run CoverageCalculator to summarise coverage of parameters


Make sure to have the [Experimenter](https://github.com/rbouckaert/Experimenter) package installed (details at the end).

## 1. Set up XML for desired model and sample from prior

First, you set up a BEAST analysis in an XML file in the configuration that you want to test. Set the `sampleFromPrior="true"` flag on the element with MCMC in it, and sample from the prior. Make sure that the number of samples in the trace log and tree log is the same and that they are sampled at a frequency such that there will be N useful samples (say N=100).

## 2. Generate (MCMC) analysis for each of the samples 

You can use CoverageTestXMLGenerator to generate BEAST XML files from a template XML file. The XML file used to sample from the prior can be used for this (when setting the sampleFromPrior flag to false). You can run CoverageTestXMLGenerator using the BEAST applauncher utility (or via the `File/Launch Apps` meny in BEAUti).


CoverageTestXMLGenerator generates XML for performing coverage test (using CoverageCalculator) and has the following arguments:

-workingDir <filename>	working directory where input files live and output directory is created
-outDir <string>	output directory where generated XML goes (as sub dir of working dir) (default: mcmc)
-logFile 	trace log file containing model paramter values to use for generating sequence data
-treeFile 	tree log file containing trees to generate sequence data on
-xmlFile 	XML template file containing analysis to be merged with generated sequence data
-skip <integer>	numer of log file lines to skip (default: 1)
-burnin <integer>	percentage of trees to used as burn-in (and will be ignored) (default: 1)
-useGamma [true|false]	use gamma rate heterogeneity (default: true)
-help	 show arguments


```
NB: make sure to set sampleFromPrior="false" in the XML.
```

```
NB: to ensure unique file name, add a parameter to logFileName, e.g.
logFileName="out$(N).log"
```
With this setting, when you run BEAST with `-D N=1` the log file will `be out1.log`.


## 3. Run the analyses

Use your favourite method to run the N analyses, for example with a shell script

```
for i in {0..99} do /path/to/beast/bin/beast -D N=$i beast$i.xml; done

```

where `/path/to/beast` the path to where BEAST is installed.

## 4. Use loganalyser to summarise trace files

Use the loganalyser utility that comes with BEAST in the bin directory. It is important to use the `-oneline` argument so that each log line gets summarised on a single line, which is what `CoverageCalculator` expects. Also, it is important that the log lines are in the same order as the log lines in the sample from the prior, so put the results for single digits before those of double digits, e.g. like so:

```
/path/to/beast/bin/loganalyser -oneline out?.log out??.log > results
```

where `out` the base name of your output log file.

## 5. Run `CoverageCalculator` to summarise coverage of parameters

You can run CoverageCalculator using the BEAST applauncher utility (or via the `File/Launch Apps` meny in BEAUti).

CoverageCalculator calculates how many times entries in log file are covered in an estimated 95% HPD interval and has the following arguments:

- log <filename>	log file containing actual values
- skip <integer>	numer of log file lines to skip (default: 1)
- logAnalyser <filename>	file produced by loganalyser tool using the -oneline option, containing estimated values
- out 	output file for trace log with truth and mean estimates. Not produced if not specified
- help	 show arguments

It produces a report like so:

```
                                                coverage Mean ESS Min ESS
posterior                                       0	   2188.41  1363.02
likelihood                                      0	   4333.99  3042.15
prior                                           33	   1613.20  891.92
treeLikelihood.dna                              0	   4333.99  3042.15
TreeHeight                                      95	   3076.44  2233.29
popSize                                         94	   577.20  331.78
CoalescentConstant                              91	   1620.76  787.30
logP(mrca(root))                                97	   4320.70  3328.88
mrca.age(root)                                  95	   3076.44  2233.29
clockRate                                       0	   3046.64  2174.60
freqParameter.1                                 98	   4332.76  3388.90
freqParameter.2                                 97	   4337.93  3334.29
freqParameter.3                                 96	   4378.30  3462.73
freqParameter.4                                 92	   4348.83  3316.36
```

Coverage should be around 95%. One reason coverage can be lower is if the ESSs are too small, which can be easily checked by looking at the `Mean ESS` and `Min ESS` columns. If these values are much below 200 the chain length should be increased to be sure any low coverage is not due to insufficient convergence of the MCMC chain. The occasional 90 or 91 is acceptable but coverage below 90 almost surely indicate an issue with the model or operator implementation.

The values for posterior, prior and treelikelihood can be ignored: it compares results from sampling from the prior with that of sampling from the posterior so they can be expected to be different.







# Installing Experimenter package

Currently, you need to build from source (which depends on [BEAST 2](https://github.com/CompEvol/beast2) and [BEASTlabs](https://github.com/BEAST2-Dev/BEASTLabs/) code) and install by hand (see "install by hand" section in [managing packages](http://www.beast2.org/managing-packages/).

Quick guide

* clone [BEAST 2](https://github.com/CompEvol/beast2), [BEASTlabs](https://github.com/BEAST2-Dev/BEASTLabs/) and [Experimenter](https://github.com/rbouckaert/Experimenter/) all in same directory
* build BEAST 2 (using `ant Linux`), then BEASTLabs (using `ant addon`), then 
Experimenter (again, using `ant addon`) packages.
* install BEASTlabs (using the package manager, or via BEAUti's `File/Manage pacakges` menu)
* install Experimenter package by creating `Experimenter` folder in your BEAST package folder, and unzip the file `Experimenter/build/dist/Experimenter.addon.v0.0.1.zip` (assuming version 0.0.1)

