/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jintangible;

import no.uib.cipr.matrix.*;
import java.io.*;
import java.text.*;


/**
 *
 * @author grouptheory
 */
public class ActualValues extends InstanceSpec {
    
    private double [] _val;
    private NumberFormat FORMAT;

    public ActualValues(int numprops, int numobjs, int numtimes,
                        double [] means, double [] std) {
        super(numprops, numobjs, numtimes);

        _val = new double [_numprops * _numobjs * _numtimes];
        initialize(means, std);
    }

    public double get(int prop, int obj, int time) {
        return _val[index_val(prop,obj,time)];
    }

    private void set(int prop, int obj, int time, double val){
        _val[index_val(prop,obj,time)] = val;
    }
    
    private void initialize(double [] means, double [] std) {
        for (int p=0; p<_numprops; p++){
            for (int i=0; i<_numobjs; i++) {
                for (int j=0; j<_numtimes; j++) {
                    double v = RandomSource.GetGaussianPositiveRandom(means[p], std[p]);
                    set(p,i,j,v);
                }
            }
        }
    }

    public DenseVector GetSolution(boolean verbose, FileWriter LOG) {
	DenseVector vec = new DenseVector(_numprops * _numobjs * _numtimes);
	fillSolution(vec);
        if (verbose) {
            try { PrintUtils.printVector(vec, "truex", LOG); } catch (IOException e) { e.printStackTrace(); };
        }
	return vec;
    }
    
    private void fillSolution(DenseVector v) {
        for (int p=0; p<_numprops; p++){
            for (int obj=0; obj<_numobjs; obj++) {
                for (int t=0; t<_numtimes; t++) {
                    int row = index_val(p, obj, t);
                    double val=get(p, obj, t);
                    v.set(row, val);
                }
	    }
	}
    }


    void printsystem(FileWriter LOG) throws IOException {
        LOG.write("\nActual Values: \n\n");
        for (int i=0; i<NumObjs(); i++) {
            for (int j=0; j<NumTimes(); j++) {
                for (int p=0; p<NumProps(); p++){
                    LOG.write(""+ "p"+p+"(c"+i+", t"+j+")");
                    if (p<NumProps()-1) LOG.write(""+ " + ");
                }
		LOG.write(" = ");
                for (int p=0; p<NumProps(); p++){
                    if (this.get(p, i, j) >= 0) FORMAT = new DecimalFormat("+0.00");
                    else FORMAT = new DecimalFormat("0.00");
                    String s = FORMAT.format(this.get(p, i, j));
                    LOG.write(""+s);
                    if (p<NumProps()-1) LOG.write(""+ " + ");
                }
		LOG.write("\n");
            }
        }
    }
}
