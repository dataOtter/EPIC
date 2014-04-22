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
public class ProxiesAndRatios extends AllProxies {

    private static CSVLoader.RatioTable loadRatioFile(String infile, boolean verbose, FileWriter LOG) {
        CSVLoader.RatioTable rt = null;
        try {
            rt = CSVLoader.ReadRatioCSV(infile, verbose, LOG);

            try {
                LOG.write("Processed ratio file "+infile+".\n");
                LOG.flush();
            }
            catch (IOException ex2) { }
        }
        catch (IOException ex) {
            try {
                LOG.write("Unable to process ratio file "+infile+"!\n");
                LOG.flush();
            }
            catch (IOException ex2) { }
        }
        return rt;
    }

    private static CSVLoader.RatioTable loadOneRatioFile(int k, FileWriter LOG) {
        String infile = AllProxies.DIR+"in-rhs-ratios-"+k+".csv";
        return loadRatioFile(infile, false, LOG);
    }
    
    protected static final HashMap loadAllVariableRatioFiles(int numvars, HashMap var2pt, FileWriter LOG) {
        HashMap v2r = new HashMap();
        for (int k=0; k<numvars; k++) {
            if ( ! var2pt.containsKey(k)) {
                CSVLoader.RatioTable rt = loadOneRatioFile(k, LOG);
                if (rt != null) {
                    v2r.put(k, rt);
                    rt.print(LOG);
                }
            }
        }
        return v2r;
    }


    static protected final int numAuxRatioConstraints(InstanceSpec spec, HashMap var2rt) {
        int count = 0;
        for (int k=0; k<spec.NumProps(); k++) {
            if (var2rt.containsKey(k)) {
                CSVLoader.RatioTable rt = (CSVLoader.RatioTable)var2rt.get(k);
                count += rt.numAuxRatioConstraints();
            }
        }
        return count;
    }


    static private final void computeAuxRatioConstraintsIndexes(InstanceSpec spec, HashMap var2rt, CSVLoader.ProxyTable zproxy) {
        for (int k=0; k<spec.NumProps(); k++) {
            if (var2rt.containsKey(k)) {
                CSVLoader.RatioTable rt = (CSVLoader.RatioTable)var2rt.get(k);
                rt.computeIndexes(zproxy);
            }
        }
    }
    
    static protected final void adjoinAuxRatios(InstanceSpec spec, Equations eqn, HashMap var2rt, CSVLoader.ProxyTable zproxy, FileWriter LOG) throws Exception {
        try {
            LOG.write("\nAdjoining auxilliary ratio constraints:\n");
            LOG.flush();
        }
        catch (IOException ex2) { }

        computeAuxRatioConstraintsIndexes(spec, var2rt, zproxy);

        for (int k=0; k<spec.NumProps(); k++) {
            if (var2rt.containsKey(k)) {
                CSVLoader.RatioTable rt = (CSVLoader.RatioTable)var2rt.get(k);
                AllProxies.TheMatrixSolver.AppendRatioConstraints(spec, k, rt, LOG);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            int numobjs = 7;
            int numtimes = 2;
            int numprops = 4;

            InstanceSpec spec = new InstanceSpec(numprops, numobjs, numtimes);

            // open the log
            FileWriter LOG = Main.openFile("ProxiesAndRatios.log");
            FileWriter OUT = Main.openFile( OUTNAME(numprops, numobjs, numtimes));

            Equations eqn = new Equations(spec.NumObjs(), spec.NumTimes(), spec.NumProps());
            HashMap var2pt = loadAllVariableProxyFiles(spec.NumProps(), LOG);

            int numAux = 0;

            /*
            HashMap var2rt = loadAllVariableRatioFiles(spec.NumProps(), var2pt, LOG);
            numAux = numAuxRatioConstraints(spec, var2rt);
            */
            
            //-------------------------------------
            CSVLoader.ProxyTable zproxy = setupViaProxies(spec, eqn, var2pt, LOG);

            prepareLinearAlgebraSystem(spec, eqn, var2pt, numAux, LOG);

            // adjoinAuxRatios(spec, eqn, var2rt, zproxy, LOG);

            /*
            FileWriter LP = Main.openFile("LP/lp1.dat");
            AllProxies.TheMatrixSolver.writeLP1(spec, LP);
            AllProxies.TheMatrixSolver.writeLP1(spec, LOG);
            Main.closeFile(LP);
            LP = Main.openFile("LP/lp2.dat");
            AllProxies.TheMatrixSolver.writeLP2(spec, LP);
            AllProxies.TheMatrixSolver.writeLP2(spec, LOG);
            Main.closeFile(LP);
            LP = Main.openFile("LP/lp3.dat");
            AllProxies.TheMatrixSolver.writeLP3(spec, LP);
            AllProxies.TheMatrixSolver.writeLP3(spec, LOG);
            Main.closeFile(LP);
             */
            
            for (int deltaInt=0; deltaInt<100; deltaInt+=1) {
                String fname;
                if (deltaInt<10) fname = "LP/lp0"+deltaInt+".txt";
                else fname = "LP/lp" + deltaInt + ".txt";
                
                System.out.println("Writing LP for delta="+deltaInt+" to file "+fname);
                FileWriter LP = Main.openFile(fname);
                                
                AllProxies.TheMatrixSolver.writeLP4(spec, var2pt, (double)deltaInt/100.0, LP);
                Main.closeFile(LP);
            }

            // AllProxies.TheMatrixSolver.writeLP4(spec, 10.0/100.0, LOG);
            
            solve(spec, eqn, var2pt, LOG);
            //-------------------------------------

            // close the log, output
            Main.closeFile(OUT);
            Main.closeFile(LOG);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
