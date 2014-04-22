/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jintangible;

import no.uib.cipr.matrix.*;
import java.text.*;
import java.io.*;


/**
 *
 * @author grouptheory
 */
public class Main {

    public static String DIR = "data/";

    private static void printparams(FileWriter OUT, int numvars, int numobjs, int numtimes, int numtrials,
            double uniformMean, double uniformStd) throws IOException {
        OUT.write("#---------------------"+"\n");
        OUT.write("# gnuplot data"+"\n");
        OUT.write("# vars="+(numvars)+""+"\n");
        OUT.write("# objs="+(numobjs)+""+"\n");
        OUT.write("# times="+(numtimes)+""+"\n");
        OUT.write("# trials="+(numtrials)+""+"\n");
        OUT.write("# mean="+(uniformMean)+""+"\n");
        OUT.write("# std="+(uniformStd)+""+"\n");
        OUT.write("#---------------------"+"\n");
    }

    static public FileWriter openFile(String FNAME) {
        FileWriter F = null;
        try
	{
	    F = new FileWriter(FNAME);
	}
	catch(IOException e)
	{
	     e.printStackTrace();
	}
        return F;
    }

    static public void closeFile(FileWriter F) {
        try {
	    F.flush();
	    F.close();
	}
	catch(IOException e)
	{
	     e.printStackTrace();
	}
    }

    private static String OUTNAME(boolean allRatios, int numvars, int numobjs, int numtimes, int numtrials, double std) {
        NumberFormat FORMAT = new DecimalFormat("0.00");
        String s= Main.DIR+"X";
        if (allRatios) s+="all-";
        else s+="one-";
        s+="n="+numobjs+"-m="+numtimes+"-l="+numvars+"-x="+numtrials+"-s="+FORMAT.format(std)+".dat";
        return s;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // open the log
        FileWriter LOG = Main.openFile("Main.log");
        
        int numobjs = 5;
        int numtimes = 5;
        int numvars = 20;
        
        boolean verbose = false;
        int numtrials = 2000;
        double uniformMean = 10000.0;
        double stdRatio = 0.5;
        double uniformStd = uniformMean * stdRatio;
        double mindev = 0.0;
        double maxdev = 20.0;
        double devinc = 1.0;
        boolean allRatios = true;
        
        FileWriter OUT = Main.openFile( OUTNAME(allRatios, numvars, numobjs, numtimes, numtrials, stdRatio));

        double [] mean = new double[numvars];
        double [] std = new double[numvars];

        for (int i=0; i<numvars; i++) {
            mean[i]=uniformMean;
            std[i]=uniformStd;
        }

        ActualValues actual = new ActualValues(numvars,numobjs,numtimes, mean, std);

        try {
            LOG.write("\n\nTrue solution\n");
            actual.printsystem(LOG);
        }
        catch (IOException e) { e.printStackTrace(); }

        DenseVector truex = actual.GetSolution(verbose, LOG);

        try {
            printparams(OUT, numvars, numobjs, numtimes, numtrials, uniformMean, uniformStd);

            String header = Experimenter.getheader()+"\n";
            OUT.write(header);
        }
        catch (IOException e) { e.printStackTrace(); }


        double pert = mindev; int pertIdx = 0;
        for (; pert<=maxdev;) {

            Experimenter exp = new Experimenter(numvars*numobjs*numtimes);

            System.out.println("performing "+numtrials+" trials at perterbation "+pert);

            for (int trial=0; trial<numtrials;trial++) {

                Equations orig = new Equations(actual);
                if (verbose) {
                    try {
                        LOG.write("\n\nNoise-free system\n");
                        orig.printsystem(LOG);
                    }
                    catch (IOException e) { e.printStackTrace(); }
                }

                Equations perturbed = orig.clone(pert, allRatios);
                MatrixSolver mat = new MatrixSolver(actual, perturbed, 0, LOG);
                DenseVector x = mat.solve(actual, verbose, LOG);

                DenseVector diff = MatrixSolver.diffPercentile(truex, x);

                exp.record(pertIdx, pert, trial, diff);
            }

            try { 
                String s = exp.getstats(pertIdx)+"\n";
                OUT.write(s);
            }
            catch (IOException e) { e.printStackTrace(); }

            pert+=devinc; pertIdx++;
        }

        // close the log, output
        Main.closeFile(OUT);
        Main.closeFile(LOG);
    }

}
