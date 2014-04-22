/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jintangible;

import java.text.*;
import java.io.*;

/**
 *
 * @author grouptheory
 */
public class Equations {

    private NumberFormat FORMAT;

    private double [] _Z;
    private double [] _alpha;
    private double [] _beta;
    private int _n;
    private int _m;
    private int _l;

    private ActualValues actual;

    public int NumProps() {
        return _l;
    }

    public int NumObjs() {
        return _n;
    }

    public int NumTimes() {
        return _m;
    }
    
    public Equations(ActualValues actual) {
        _l = actual.NumProps();
        _n = actual.NumObjs();
        _m = actual.NumTimes();
        initialize_Z(actual);
        initialize_alpha(actual);
        initialize_beta(actual);
    }

    public Equations(int n, int m, int l) {
        _n = n;
        _m = m;
        _l = l;
        initialize_Z((ActualValues)null);
        initialize_alpha((ActualValues)null);
        initialize_beta((ActualValues)null);
    }

    private Equations(Equations orig) {
        _n = orig._n;
        _m = orig._m;
        _l = orig._l;
        initialize_Z(orig);
        initialize_alpha(orig);
        initialize_beta(orig);
    }

    public Equations clone(double perturbPercent, boolean allRatios) {
        Equations dupe = new Equations(this);
        //dupe.perturb_Z(perturbPercent);
        if (allRatios) {
            dupe.perturb_alpha(perturbPercent);
            dupe.perturb_beta(perturbPercent);
        }
        else {
            boolean a = alphaORbeta();
            if (a) {
                dupe.perturb1random_alpha(perturbPercent);
            }
            else {
                dupe.perturb1random_beta(perturbPercent);
            }
        }
        return dupe;
    }

    private int getRandomObject(int low) {
        return (int)RandomSource.GetUniformRandom((double)low, (double)this.NumObjs());
    }

    private int getRandomTime(int low) {
        return (int)RandomSource.GetUniformRandom((double)low, (double)this.NumTimes());
    }

    private int getRandomProperty(int low) {
        return (int)RandomSource.GetUniformRandom((double)low, (double)this.NumProps());
    }

    public double getZ(int obj, int time) {
        return _Z[obj*NumTimes() + time];
    }

    public void setZ(int obj, int time, double val){
        _Z[obj*NumTimes() + time] = val;
    }

    private void initialize_Z(ActualValues actual) {
        _Z = new double [NumObjs() * NumTimes()];

        for (int i=0; i<NumObjs(); i++) {
            for (int j=0; j<NumTimes(); j++) {
                double z = 0;
                for (int p=0; p<NumProps(); p++){
                    double v = 0.0;
                    if (actual != null) {
                        v = actual.get(p,i,j);
                    }
                    z += v;
                }
                setZ(i,j,z);
            }
        }
    }

    private void initialize_Z(Equations orig) {
        _Z = new double [NumObjs() * NumTimes()];

        for (int i=0; i<NumObjs(); i++) {
            for (int j=0; j<NumTimes(); j++) {
                setZ(i,j,orig.getZ(i,j));
            }
        }
    }

    void perturb_Z(double percent){
        for (int i=0; i<NumObjs(); i++) {
            for (int j=0; j<NumTimes(); j++) {
                double actual = getZ(i,j);
                double noisy = actual;
                if (percent>0) {
                    double delta = percent/100.0 * actual;
                    noisy = RandomSource.GetGaussianPositiveRandom(actual,delta);
                }
                setZ(i,j,noisy);
            }
        }
    }

    private int index_alpha(int prop, int obj) {
        return prop*(NumObjs()-1) + obj-1;
    }

    public double getalpha(int prop, int obj) {
        return _alpha[index_alpha(prop,obj)];
    }

    public void setalpha(int prop, int obj, double val) {
        _alpha[index_alpha(prop,obj)] = val;
    }

    private void initialize_alpha(ActualValues actual) {
        _alpha = new double [NumProps() * (NumObjs()-1)];

        for (int p=0; p<NumProps(); p++){
            for (int i=1; i<NumObjs(); i++) {
                double v = 0.0;
                if (actual != null) {
                    double num = actual.get(p,0,0);
                    double den = actual.get(p,i,0);
                    v = num/den;
                }
                setalpha(p,i, v);
            }
        }
    }

    private void initialize_alpha(Equations orig) {
        _alpha = new double [NumProps() * (NumObjs()-1)];

        for (int p=0; p<NumProps(); p++){
            for (int i=1; i<NumObjs(); i++) {
                setalpha(p,i, orig.getalpha(p, i));
            }
        }
    }

    void perturb1_alpha(double percent, int p, int i){
        double actual = getalpha(p,i);
        double noisy = actual;
        if (percent>0) {
            double delta = percent/100.0 * actual;
            noisy = RandomSource.GetGaussianPositiveRandom(actual,delta);
        }
        setalpha(p,i,noisy);
    }

    boolean alphaORbeta() {
        double numA = NumProps() * (NumObjs() - 1);
        double numB = NumProps() * NumObjs() * (NumTimes() - 1);
        if (RandomSource.GetUniformRandom(0.0, numA+numB) <= numA) {
            return true;
        }
        else {
            return false;
        }
    }
    
    void perturb1random_alpha(double percent){
        perturb1_alpha(percent, this.getRandomProperty(0), this.getRandomObject(1));
    }

    void perturb_alpha(double percent){
        for (int p=0; p<NumProps(); p++){
            for (int i=1; i<NumObjs(); i++) {
                perturb1_alpha(percent, p, i);
            }
        }
    }

    private int index_beta(int prop, int obj, int time) {
        return prop*NumObjs()*(NumTimes()-1) + obj*(NumTimes()-1) + time-1;
    }

    public double getbeta(int prop, int obj, int time) {
        return _beta[index_beta(prop,obj,time)];
    }

    public void setbeta(int prop, int obj, int time, double val) {
        _beta[index_beta(prop,obj,time)] = val;
    }

    private void initialize_beta(ActualValues actual) {
        _beta = new double [NumProps() * NumObjs() * (NumTimes()-1)];

        for (int p=0; p<NumProps(); p++){
            for (int i=0; i<NumObjs(); i++) {
                for (int j=1; j<NumTimes(); j++) {
                    double v = 0.0;
                    if (actual != null) {
                        double num = actual.get(p,i,0);
                        double den = actual.get(p,i,j);
                        v = num/den;
                    }
                    setbeta(p,i,j, v);
                }
            }
        }
    }

    private void initialize_beta(Equations orig) {
        _beta = new double [NumProps() * NumObjs() * (NumTimes()-1)];

        for (int p=0; p<NumProps(); p++){
            for (int i=0; i<NumObjs(); i++) {
                for (int j=1; j<NumTimes(); j++) {
                    setbeta(p,i,j, orig.getbeta(p,i,j));
                }
            }
        }
    }

    void perturb1_beta(double percent, int p, int i, int j){
        double actual = getbeta(p,i,j);
        double noisy = actual;
        if (percent>0) {
            double delta = percent/100.0 * actual;
            noisy = RandomSource.GetGaussianPositiveRandom(actual,delta);
        }
        setbeta(p,i,j,noisy);
    }

    void perturb1random_beta(double percent){
        perturb1_beta(percent, this.getRandomProperty(0), this.getRandomObject(0),
                this.getRandomTime(1));
    }

    void perturb_beta(double percent){
        for (int p=0; p<NumProps(); p++){
            for (int i=0; i<NumObjs(); i++) {
                for (int j=1; j<NumTimes(); j++) {
                    perturb1_beta(percent, p, i, j);
                }
            }
        }
    }

    void printsystem(FileWriter LOG) throws IOException {
        LOG.write("\nLaw constraints: \n\n");
        for (int i=0; i<NumObjs(); i++) {
            for (int j=0; j<NumTimes(); j++) {

                if (getZ(i,j) >= 0) FORMAT = new DecimalFormat("+0.00");
                else FORMAT = new DecimalFormat("0.00");
                String s = FORMAT.format(getZ(i,j));

                LOG.write(""+ s +" = ");
                for (int p=0; p<NumProps(); p++){
                    LOG.write(""+ "p"+p+"(c"+i+", t"+j+")");
                    if (p<NumProps()-1) LOG.write(""+ " + ");
                }
		LOG.write("\n");
            }
        }
        
        LOG.write("\nAlpha constraints: \n\n");
        for (int p=0; p<NumProps(); p++){
            for (int i=1; i<NumObjs(); i++) {
                LOG.write(""+ "p"+p+"(c"+0+", t"+0+") / ");
                LOG.write(""+ "p"+p+"(c"+i+", t"+0+") = ");

                    if (getalpha(p,i) >= 0) FORMAT = new DecimalFormat("+0.00");
                    else FORMAT = new DecimalFormat("0.00");
                    String s = FORMAT.format(getalpha(p,i));

                LOG.write(""+ s + "\n");
            }
        }

        LOG.write("\nBeta constraints: \n\n");
	for (int p=0; p<NumProps(); p++){
	    for (int i=0; i<NumObjs(); i++) {
		for (int j=1; j<NumTimes(); j++) {
		    LOG.write(""+ "p"+p+"(c"+i+", t"+0+") / ");
		    LOG.write(""+ "p"+p+"(c"+i+", t"+j+") = ");

                    if (getbeta(p,i,j) >= 0) FORMAT = new DecimalFormat("+0.00");
                    else FORMAT = new DecimalFormat("0.00");
                    String s = FORMAT.format(getbeta(p,i,j));

		    LOG.write(""+ s + "\n");
		}
	    }
	}
    }
}
