/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jintangible;

import no.uib.cipr.matrix.*;
import java.text.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author grouptheory
 */
public class AllProxies {

    public static String DIR = "analysis/";

    protected static String OUTNAME(int numvars, int numobjs, int numtimes) {
        NumberFormat FORMAT = new DecimalFormat("0.00");
        String s= AllProxies.DIR+"out-analysis-n="+numobjs+"-m="+numtimes+"-l="+numvars+".dat";
        return s;
    }

    private static CSVLoader.ProxyTable loadVariableFile(String infile, boolean verbose, FileWriter LOG) {
        CSVLoader.ProxyTable pt = null;
        try {
            pt = CSVLoader.ReadProxyCSV(infile, verbose, LOG);

            try {
                LOG.write("Processed valid proxy file "+infile+".\n");
                LOG.flush();
            }
            catch (IOException ex2) { }
        }
        catch (IOException ex) {
            try {
                LOG.write("Did not find valid proxy file "+infile+".\n");
                LOG.flush();
            }
            catch (IOException ex2) { }
        }
        return pt;
    }

    private static CSVLoader.ProxyTable loadTangibleLHSFile(FileWriter LOG) {
        String infile = AllProxies.DIR+"in-lhs-tangible.csv";
        return loadVariableFile(infile, true, LOG);
    }

    private static CSVLoader.ProxyTable loadOneProxyVariableFile(int k, FileWriter LOG) {
        String infile = AllProxies.DIR+"in-rhs-intangible-proxy-"+k+".csv";
        return loadVariableFile(infile, false, LOG);
    }

    protected static final HashMap loadAllVariableProxyFiles(int numvars, FileWriter LOG) {
        HashMap var2pt = new HashMap();
        for (int k=0; k<numvars; k++) {
            CSVLoader.ProxyTable pt = loadOneProxyVariableFile(k, LOG);
            if (pt != null) var2pt.put(k, pt);
        }
        return var2pt;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        
        int numobjs = 7;
        int numtimes = 3;
        int numprops = 3;

        InstanceSpec spec = new InstanceSpec(numprops, numobjs, numtimes);

        // open the log
        FileWriter LOG = Main.openFile("log.txt");
        FileWriter OUT = Main.openFile( OUTNAME(numprops, numobjs, numtimes));

        Equations eqn = new Equations(spec.NumObjs(), spec.NumTimes(), spec.NumProps());
        HashMap var2pt = loadAllVariableProxyFiles(spec.NumProps(), LOG);

        //-------------------------------------
        CSVLoader.ProxyTable zproxy = setupViaProxies(spec, eqn, var2pt, LOG);

        prepareLinearAlgebraSystem(spec, eqn, var2pt, 0, LOG);

        solve(spec, eqn, var2pt, LOG);
        //-------------------------------------
        
        // close the log, output
        Main.closeFile(OUT);
        Main.closeFile(LOG);
    }

    static protected final CSVLoader.ProxyTable setupViaProxies(InstanceSpec spec, Equations eqn, HashMap var2pt, FileWriter LOG) {

        CSVLoader.ProxyTable zproxy = loadTangibleLHSFile(LOG);

        if (zproxy == null) {
            try {
                LOG.write("Unable to set LHS tangible values!\n");
                LOG.flush();
            }
            catch (IOException ex2) { }
        }
        else {

            try {
                LOG.write("Setting LHS tangible values.\n");
                LOG.flush();
            }
            catch (IOException ex2) { }

            for (int i=0;i<spec.NumObjs();i++) {
                String ci = zproxy.get_ci(i);
                for (int j=0;j<spec.NumTimes();j++) {
                    String tj = zproxy.get_tj(j);
                    double val = zproxy.get(ci, tj);

                    eqn.setZ(i, j, val);
                }
            }
        }

        for (int k=0;k<spec.NumProps();k++) {

            if (!var2pt.containsKey(k)) {
                try {
                    LOG.write("Did not use proxy variable to set alphas/betas for intangible "+k+".\n");
                    LOG.flush();
                    continue;
                }
                catch (IOException ex2) { }
            }

            CSVLoader.ProxyTable xproxy = (CSVLoader.ProxyTable)var2pt.get(k);

            try {
                LOG.write("Used proxy variable to set alphas/betas for intangible "+k+": "+xproxy.name()+".\n");
                LOG.flush();
            }
            catch (IOException ex2) { }

            // init alpha
            for (int i=1; i<spec.NumObjs(); i++) {
                double v = 0.0;
                double num = xproxy.get(xproxy.get_ci(0),xproxy.get_tj(0));
                double den = xproxy.get(xproxy.get_ci(i),xproxy.get_tj(0));
                v = num/den;

                String ci = xproxy.get_ci(i);
                
                int imapped = -1;
                try {
                    imapped = zproxy.get_objectInd(ci);
                }
                catch (Exception ex) {
                    System.out.println("prop="+k);
                    System.out.println("obj="+i);
                }
                
                eqn.setalpha(k, imapped, v);
            }

            // init beta
            for (int i=0; i<spec.NumObjs(); i++) {
                for (int j=1; j<spec.NumTimes(); j++) {
                    double v = 0.0;
                    double num = xproxy.get(xproxy.get_ci(i),xproxy.get_tj(0));
                    double den = xproxy.get(xproxy.get_ci(i),xproxy.get_tj(j));
                    v = num/den;

                    String ci = xproxy.get_ci(i);
                    int imapped = zproxy.get_objectInd(ci);

                    String tj = xproxy.get_tj(j);
                    int jmapped = zproxy.get_timeInd(tj);

                    eqn.setbeta(k, imapped, jmapped, v);
                }
            }
        }

        return zproxy;
    }

    static protected MatrixSolver TheMatrixSolver;
    static protected DenseVector TheVector;
    
    static protected final void prepareLinearAlgebraSystem(InstanceSpec spec, Equations eqn, HashMap var2pt, int maxAuxRatios, FileWriter LOG) {
        try {
            LOG.write("\n\nConstraint system derived from proxies:\n");
            LOG.flush();
            eqn.printsystem(LOG);

            TheMatrixSolver = new MatrixSolver(spec, eqn, maxAuxRatios, LOG);
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    static protected final void solve(InstanceSpec spec, Equations eqn, HashMap var2pt, FileWriter LOG) {
        try {
            TheVector = TheMatrixSolver.solve(spec, true, LOG);
            
            LOG.write("\n\nSolution via pseudo-inverses:\n");

            for (int k=0;k<spec.NumProps();k++) {
                if (var2pt.containsKey(k)) {

                CSVLoader.ProxyTable xproxy = (CSVLoader.ProxyTable)var2pt.get(k);

                    for (int i=0; i<spec.NumObjs(); i++) {
                        for (int j=0; j<spec.NumTimes(); j++) {
                            int index = spec.index_val(k, i, j);
                            double val=TheVector.get(index);

                            String ci = xproxy.get_ci(i);

                            String tj = xproxy.get_tj(j);

                            LOG.write(""+xproxy.name()+"("+ci+","+tj+") = "+val+"\n");
                        }
                    }
                }
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }

}
