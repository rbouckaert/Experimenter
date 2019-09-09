package test;

import org.junit.Test;

import beast.evolution.speciation.YuleModel;
import beast.util.TreeParser;
import junit.framework.TestCase;

public class YuleLikelihoodTest extends TestCase {

	@Test
	public void testYuleLikelihood() {
        TreeParser tree = new TreeParser("((A:1.0,B:1.0):1.0,(C:1.0,D:1.0):1.0);");
        
        YuleModel likelihood = new YuleModel();
        likelihood.initByName("tree", tree, "birthDiffRate", "1.0");

        assertEquals(-6.0, likelihood.calculateLogP());
    }

}