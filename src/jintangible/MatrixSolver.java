/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jintangible;

import no.uib.cipr.matrix.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author grouptheory
 */
public class MatrixSolver {

    private Equations _eqn;
    
    private int _num_nonhom2;
    private int _num_alpha2;
    private int _num_beta2;
    private int _rowsMax;
    private int _rowsActual;
    private int _cols;
    private int _maxAuxRatios;
    private int _numAuxRatios;
    private int _firstAuxRatioIndex;

    public int NumProps() {
        return _eqn.NumProps();
    }

    public int NumObjs() {
        return _eqn.NumObjs();
    }

    public int NumTimes() {
        return _eqn.NumTimes();
    }

    private DenseMatrix matrix_A;
    private DenseVector vector_v;
    
    public MatrixSolver(InstanceSpec spec, Equations eqn, int maxAuxRatios, FileWriter LOG) {
        _eqn = eqn;

        int num_nonhom = (NumObjs() * NumTimes());
        int num_alpha = NumProps() * (NumObjs()-1);              // alpha: intercity, time 0
        int num_beta = NumProps() * NumObjs() * (NumTimes()-1);  // beta: intertime, at each city
        
        _rowsMax = num_nonhom + num_alpha + num_beta;
        _rowsActual = 0;
        _cols = NumProps() * NumObjs() * NumTimes();

        try {
            LOG.write("\nMaximum number of constraints possible = "+_rowsMax+"\n");
        }
        catch (IOException ex2) { }

        DenseMatrix testA = this.makeMatrix(spec);
        DenseVector testv = this.makeVector();

        try {
            LOG.write("Actual number of constraints from proxies = "+_rowsActual+"\n");
        }
        catch (IOException ex2) { }

        _firstAuxRatioIndex = _rowsActual;
        _rowsActual += maxAuxRatios;

        _maxAuxRatios = maxAuxRatios;
        _numAuxRatios = 0;

        matrix_A = this.makeMatrixPruned(spec);
        vector_v = this.makeVectorPruned();

        try {
            LOG.write("Actual number of constraints (proxies + auxilliary) = "+_rowsActual+"\n");
        }
        catch (IOException ex2) { }
    }

    public void AppendRatioConstraints(InstanceSpec spec, int k, CSVLoader.RatioTable rt, FileWriter LOG) throws Exception {

        for (Iterator it = rt.alphaConstraintIterator(); it.hasNext();) {
            CSVLoader.RatioTable.AlphaRatioConstraint cons =
                    (CSVLoader.RatioTable.AlphaRatioConstraint)it.next();
            try {
                LOG.write("MatrixSolver> "+rt.name()+": alpha ratio constraint "+cons._c1+"/"+cons._c2+" @time="+cons._t1+" = "+cons._val+"\n");
                LOG.flush();
            }
            catch (IOException ex2) { }
            
            appendAlphaConstraint(spec, k, cons);
        }

        for (Iterator it = rt.betaConstraintIterator(); it.hasNext();) {
            CSVLoader.RatioTable.BetaRatioConstraint cons =
                    (CSVLoader.RatioTable.BetaRatioConstraint)it.next();
            try {
                LOG.write("MatrixSolver> "+rt.name()+": beta ratio constraint "+cons._t1+"/"+cons._t2+" @city="+cons._c1+" = "+cons._val+"\n");
                LOG.flush();
            }
            catch (IOException ex2) { }

            appendBetaConstraint(spec, k, cons);
        }
    }

    private void appendAlphaConstraint(InstanceSpec spec, int k, CSVLoader.RatioTable.AlphaRatioConstraint cons) throws Exception {
        if (_numAuxRatios >= _maxAuxRatios) throw new Exception("Too many AuxRatios added.");
        int row = _firstAuxRatioIndex+_numAuxRatios;

        int col1 = spec.index_val(k, cons._c1index, cons._t1index);
        double val1 = -1.0;
        matrix_A.set(row, col1, val1);

        int col2 = spec.index_val(k, cons._c2index, cons._t1index);
        double val2 = cons._val;
        matrix_A.set(row, col2, val2);

        _numAuxRatios++;
    }

    private void appendBetaConstraint(InstanceSpec spec, int k, CSVLoader.RatioTable.BetaRatioConstraint cons) throws Exception {
        if (_numAuxRatios >= _maxAuxRatios) throw new Exception("Too many AuxRatios added.");
        int row = _firstAuxRatioIndex+_numAuxRatios;

        int col1 = spec.index_val(k, cons._c1index, cons._t1index);
        double val1 = -1.0;
        matrix_A.set(row, col1, val1);

        int col2 = spec.index_val(k, cons._c1index, cons._t2index);
        double val2 = cons._val;
        matrix_A.set(row, col2, val2);
        
        _numAuxRatios++;
    }

    private DenseMatrix makeMatrix(InstanceSpec spec) {
	DenseMatrix mat = new DenseMatrix(_rowsMax, _cols);
	fillMatrix(mat, spec, true);
	return mat;
    }

    private DenseMatrix makeMatrixPruned(InstanceSpec spec) {
	DenseMatrix prunedA = new DenseMatrix(_rowsActual, _cols);
	fillMatrix(prunedA, spec, false);
        return prunedA;
    }

    private DenseVector makeVector() {
	DenseVector vec = new DenseVector(_rowsMax);
	fillVector(vec);
	return vec;
    }

    private DenseVector makeVectorPruned() {
	DenseVector prunedv = new DenseVector(_rowsActual);
	fillVector(prunedv);
        return prunedv;
    }
    
    private void fillMatrix(DenseMatrix mat, InstanceSpec spec, boolean probe) {
        int offset = 0;
        // set the law coefficient entries (1's)
        for (int obj=0; obj<NumObjs(); obj++) {
            for (int t=0; t<NumTimes(); t++) {
                int row = (obj * NumTimes() + t);
                for (int p=0; p<NumProps(); p++){
                    int col = spec.index_val(p, obj, t);
                    mat.set(row, col, 1.0);
                }
                offset++;
                if (probe) _rowsActual++;
            }
        }
        // set the alpha coefficients
        for (int p=0; p<NumProps(); p++){
            for (int obj=1; obj<NumObjs(); obj++) {
                
                double val=_eqn.getalpha(p,obj);

                if (val > 0.0) {
                    int row = offset;
                    int col_0 = spec.index_val(p, 0, 0);
                    mat.set(row, col_0, -1.0);
                    
                    int col_obj = spec.index_val(p, obj, 0);
                    mat.set(row, col_obj, val);
                    offset++;
                    if (probe) _rowsActual++;
                }
            }
        }
        // set the beta coefficients
        for (int p=0; p<NumProps(); p++){
            for (int obj=0; obj<NumObjs(); obj++) {
		for (int t=1; t<NumTimes(); t++) {

		    double val=_eqn.getbeta(p,obj,t);

                    if (val > 0.0) {
                        int row = offset;
                        int col_0 = spec.index_val(p, obj, 0);
                        mat.set(row, col_0, -1.0);

                        int col_obj = spec.index_val(p, obj, t);
                        mat.set(row, col_obj, val);
                        offset++;
                        if (probe) _rowsActual++;
                    }
		}
	    }
	}
    }

    private void fillVector(DenseVector v) {
	int offset = 0;
        for (int obj=0; obj<NumObjs(); obj++) {
            for (int t=0; t<NumTimes(); t++) {
                int row = (obj * NumTimes() + t);
                double val=_eqn.getZ(obj, t);
		v.set(row, val);
                offset++;
	    }
	}

        // set the alpha coefficients
        for (int p=0; p<NumProps(); p++){
            for (int obj=1; obj<NumObjs(); obj++) {

                double val=_eqn.getalpha(p,obj);

                if (val > 0.0) {
                    int row = offset;
                    v.set(row, 0.0);
                    offset++;
                }
	    }
	}

        // set the beta coefficients
        for (int p=0; p<NumProps(); p++){
            for (int obj=0; obj<NumObjs(); obj++) {
		for (int t=1; t<NumTimes(); t++) {

		    double val=_eqn.getbeta(p,obj,t);

                    if (val > 0.0) {
                        int row = offset;
                        v.set(row, 0.0);
                        offset++;
                    }
		}
	    }
	}
    }

   public void writeLP1(InstanceSpec spec, FileWriter LOG) {
       try {
           LOG.write("\n\n/* lp_solve program 1 begins */\n\n");

           int homrows = NumObjs() * NumTimes();

           LOG.write("min: pL1_div_from_law;\n");
           
           LOG.write("pL1_div_from_law = ");
           for (int row=0; row < homrows; row++) {
               LOG.write("comp_pos"+row+" + comp_neg"+row+" + ");
           }
           LOG.write("0.0; \n");

           for (int row=0; row < this.matrix_A.numRows(); row++) {

               LOG.write("row"+row+": ");

               for (int col=0; col < this.matrix_A.numColumns(); col++) {
                   double val = this.matrix_A.get(row, col);
                   if (val != 0.0) {
                       int p = spec.inv_index_prop(col);
                       int obj = spec.inv_index_obj(col);
                       int time = spec.inv_index_time(col);

                       LOG.write(" ");
                       if (val > 0.0) LOG.write("+");
                       LOG.write(""+val+" ");
                       LOG.write("p"+p+"_c"+obj+"_t"+time+" ");
                   }
               }

               double zval = this.vector_v.get(row);

               if (row < homrows) {
                   LOG.write(" - "+zval+" = diff"+row+" ;\n");
               }
               else {
                   LOG.write(" = "+zval+" ;\n");
               }
           }


           for (int row=0; row < homrows; row++) {

               LOG.write("split"+row+": ");

               LOG.write("diff"+row+" = comp_pos"+row+" - comp_neg"+row+" ;\n");
               LOG.write("comp_pos"+row+" >= 0.0 ;\n");
               LOG.write("comp_neg"+row+" >= 0.0 ;\n");
           }


           for (int col=0; col < this.matrix_A.numColumns(); col++) {
               int p = spec.inv_index_prop(col);
               int obj = spec.inv_index_obj(col);
               int time = spec.inv_index_time(col);

               LOG.write("pos_p"+p+"_c"+obj+"_t"+time+": p"+p+"_c"+obj+"_t"+time+" > 0.0 ;\n");
           }

           LOG.write("\n\n/* lp_solve program 1 ends */\n\n");
       }
       catch (IOException ex) {
           ex.printStackTrace();
       }
   }

   public void writeLP2(InstanceSpec spec, FileWriter LOG) {
       try {
           LOG.write("\n\n/* lp_solve program 2 begins */\n\n");

           int homrows = NumObjs() * NumTimes();

           LOG.write("min: pL1_div_from_ratios;\n");

           LOG.write("pL1_div_from_ratios = ");
           for (int row=homrows; row < this.matrix_A.numRows(); row++) {
               LOG.write("comp_pos"+row+" + comp_neg"+row+" + ");
           }
           LOG.write("0.0; \n");

           for (int row=0; row < this.matrix_A.numRows(); row++) {

               LOG.write("row"+row+": ");

               for (int col=0; col < this.matrix_A.numColumns(); col++) {
                   double val = this.matrix_A.get(row, col);
                   if (val != 0.0) {
                       int p = spec.inv_index_prop(col);
                       int obj = spec.inv_index_obj(col);
                       int time = spec.inv_index_time(col);

                       LOG.write(" ");
                       if (val > 0.0) LOG.write("+");
                       LOG.write(""+val+" ");
                       LOG.write("p"+p+"_c"+obj+"_t"+time+" ");
                   }
               }

               double zval = this.vector_v.get(row);

               if (row >= homrows) {
                   LOG.write(" - "+zval+" = diff"+row+" ;\n");
               }
               else {
                   LOG.write(" = "+zval+" ;\n");
               }
           }


           for (int row=homrows; row < this.matrix_A.numRows(); row++) {

               LOG.write("split"+row+": ");

               LOG.write("diff"+row+" = comp_pos"+row+" - comp_neg"+row+" ;\n");
               LOG.write("comp_pos"+row+" >= 0.0 ;\n");
               LOG.write("comp_neg"+row+" >= 0.0 ;\n");
           }


           for (int col=0; col < this.matrix_A.numColumns(); col++) {
               int p = spec.inv_index_prop(col);
               int obj = spec.inv_index_obj(col);
               int time = spec.inv_index_time(col);

               LOG.write("pos_p"+p+"_c"+obj+"_t"+time+": p"+p+"_c"+obj+"_t"+time+" > 0.0 ;\n");
           }

           LOG.write("\n\n/* lp_solve program 2 ends */\n\n");
       }
       catch (IOException ex) {
           ex.printStackTrace();
       }
   }

   public void writeLP3(InstanceSpec spec, FileWriter LOG) {
       try {
           LOG.write("\n\n/* lp_solve program 3 begins */\n\n");

           int homrows = NumObjs() * NumTimes();

           LOG.write("min: pL1_div_total;\n");

           LOG.write("pL1_div_total = ");
           for (int row=0; row < this.matrix_A.numRows(); row++) {
               LOG.write("comp_pos"+row+" + comp_neg"+row+" + ");
           }
           LOG.write("0.0; \n");

           for (int row=0; row < this.matrix_A.numRows(); row++) {

               LOG.write("row"+row+": ");

               for (int col=0; col < this.matrix_A.numColumns(); col++) {
                   double val = this.matrix_A.get(row, col);
                   if (val != 0.0) {
                       int p = spec.inv_index_prop(col);
                       int obj = spec.inv_index_obj(col);
                       int time = spec.inv_index_time(col);

                       LOG.write(" ");
                       if (val > 0.0) LOG.write("+");
                       LOG.write(""+val+" ");
                       LOG.write("p"+p+"_c"+obj+"_t"+time+" ");
                   }
               }

               double zval = this.vector_v.get(row);

               LOG.write(" - "+zval+" = diff"+row+" ;\n");
           }


           for (int row=0; row < this.matrix_A.numRows(); row++) {

               LOG.write("split"+row+": ");

               LOG.write("diff"+row+" = comp_pos"+row+" - comp_neg"+row+" ;\n");
               LOG.write("comp_pos"+row+" >= 0.0 ;\n");
               LOG.write("comp_neg"+row+" >= 0.0 ;\n");
           }


           for (int col=0; col < this.matrix_A.numColumns(); col++) {
               int p = spec.inv_index_prop(col);
               int obj = spec.inv_index_obj(col);
               int time = spec.inv_index_time(col);

               LOG.write("pos_p"+p+"_c"+obj+"_t"+time+": p"+p+"_c"+obj+"_t"+time+" > 0.0 ;\n");
           }

           LOG.write("\n\n/* lp_solve program 3 ends */\n\n");
       }
       catch (IOException ex) {
           ex.printStackTrace();
       }
   }

   public void writeLP4(InstanceSpec spec, HashMap var2pt, double delta, FileWriter LOG) {
       try {
           LOG.write("\n\n/* lp_solve program 4 (ratio-margin="+delta+") begins */\n\n");

           
           LOG.write("/* properties */\n");
           for (int p = 0; p<spec.NumProps(); p++) {
               CSVLoader.ProxyTable pt = (CSVLoader.ProxyTable)var2pt.get(p);
               LOG.write("/* p"+p+" = "+pt.name()+" */\n");
           }
           LOG.write("\n");
           
           LOG.write("/* objects */\n");
           for (int obj1 = 0; obj1<spec.NumObjs(); obj1++) {
               CSVLoader.ProxyTable pt = (CSVLoader.ProxyTable)var2pt.get(0);
               LOG.write("/* c"+obj1+" = "+pt.get_ci(obj1)+" */\n");
           }
           LOG.write("\n");
           
           LOG.write("/* times */\n");
           for (int time = 0;time<spec._numtimes; time++) {
               CSVLoader.ProxyTable pt = (CSVLoader.ProxyTable)var2pt.get(0);
               LOG.write("/* t"+time+" = "+pt.get_tj(time)+" */\n");
           }
           LOG.write("\n");
           
           int homrows = NumObjs() * NumTimes();

           LOG.write("/* objective function is to minimize the total absolute value of normalized divergences from the law */\n");
           LOG.write("min: pL1_div_from_law;\n");
           LOG.write("pL1_div_from_law = ");
           for (int row=0; row < homrows; row++) {
               LOG.write("comp_pos"+row+" + comp_neg"+row+" + ");
           }
           LOG.write("0.0; \n");
           LOG.write("\n");

           for (int row=0; row < homrows; row++) { 

               String s = "";
               s+=("row"+row+": ");
               
               for (int col=0; col < this.matrix_A.numColumns(); col++) {
                   double val = this.matrix_A.get(row, col);
                   if (val != 0.0) {
                       int p = spec.inv_index_prop(col);
                       int obj = spec.inv_index_obj(col);
                       int time = spec.inv_index_time(col);

                       s+=(" ");
                       if (val > 0.0) {
                           s+=("+");
                       }
                       
                       s+=(""+ val +" ");
                       s+=("p"+p+"_c"+obj+"_t"+time+" ");
                   }
               }

               double zval = this.vector_v.get(row);

               int time=row%spec._numtimes;
               int obj=(row/spec._numtimes)%spec._numobjs;
                          
               LOG.write("/* divergence from the law at city c"+obj+" time t"+time+" */\n");
               LOG.write("population_c"+obj+"_t"+time+":  z_c"+obj+"_t"+time+" = "+zval+" ;\n");
               LOG.write(""+s+" - "+zval+" = diff"+row+" ;\n");
               LOG.write("normalized_divergence"+row+": "+zval+" normdiff"+row+" = diff"+row+" ;\n");
               LOG.write("\n");
           }

           // cross city ratios
           for (int p = 0; p<spec.NumProps(); p++) {
               for (int time = 0;time<spec._numtimes; time++) {
                   for (int obj1 = 0; obj1<spec.NumObjs(); obj1++) {
                       for (int obj2 = 0; obj2<spec.NumObjs(); obj2++) {
                           
                           if (obj1==obj2) continue;
                           
                           String var1 =  "p"+p+"_c"+obj1+"_t"+time;
                           String var2 =  "p"+p+"_c"+obj2+"_t"+time;
                           
                           CSVLoader.ProxyTable pt = (CSVLoader.ProxyTable)var2pt.get(p);
                           double val1 = pt.get(pt.get_ci(obj1), pt.get_tj(time));
                           double val2 = pt.get(pt.get_ci(obj2), pt.get_tj(time));
                           double trueratio = val1/val2;
                           LOG.write("/* "+var1+" / "+var2+" = "+val1+" / "+val2+" = "+trueratio+"; */\n");
                           
                           double upperbound;
                           double lowerbound;
                           
                           
                           if ((p==2)||(p==3)) { // Only Other and Sex get slack.
                               upperbound = trueratio*(1.0+delta);
                               lowerbound = trueratio*(1.0-delta);
                           }
                           else {
                               double BASELINE = 0.01;
                               upperbound = trueratio*(1.0+BASELINE);
                               lowerbound = trueratio*(1.0-BASELINE);
                           }
                           
                           LOG.write("/* cross-city ratio must be in: ("+lowerbound+","+upperbound+") */\n");
                           LOG.write(""+var1+" - "+lowerbound+" "+var2+" >= 0.0;\n");
                           LOG.write(""+var1+" - "+upperbound+" "+var2+" <= 0.0;\n");
                           LOG.write("\n");
                       }
                   }
               }
           }

           // cross time ratios
           for (int p = 0; p<spec.NumProps(); p++) {
               for (int obj1 = 0; obj1<spec.NumObjs(); obj1++) {   
                   int time1=0;
                   int time2=1;
                   String var1 =  "p"+p+"_c"+obj1+"_t"+time1;
                   String var2 =  "p"+p+"_c"+obj1+"_t"+time2;

                   CSVLoader.ProxyTable pt = (CSVLoader.ProxyTable)var2pt.get(p);
                   double val1 = pt.get(pt.get_ci(obj1), pt.get_tj(time1));
                   double val2 = pt.get(pt.get_ci(obj1), pt.get_tj(time2));
                   double trueratio = val1/val2;
                   LOG.write("/* "+var1+" / "+var2+" = "+val1+" / "+val2+" = "+trueratio+"; */\n");

                   double upperbound = trueratio*(1.0+delta);
                   double lowerbound = trueratio*(1.0-delta);
                   
                   if ((p==2)||(p==3)) { // Only Other and Sex get slack.
                       upperbound = trueratio*(1.0+delta);
                       lowerbound = trueratio*(1.0-delta);
                   }
                   else {
                       double BASELINE = 0.01;
                       upperbound = trueratio*(1.0+BASELINE);
                       lowerbound = trueratio*(1.0-BASELINE);
                   }
                           
                   LOG.write("/* cross-time ratio must be in: ("+lowerbound+","+upperbound+") */\n");
                   LOG.write(""+var1+" - "+lowerbound+" "+var2+" >= 0.0;\n");
                   LOG.write(""+var1+" - "+upperbound+" "+var2+" <= 0.0;\n");
                   LOG.write("\n");
               }
           }
           
           /*
           for (int row=homrows; row<this.matrix_A.numRows(); row++) {
               double scale = 1.0-delta;
               double scale2 = 1.0+delta;
               
               String s = ("row"+row+"a: ");
               String s2 = ("row"+row+"b: ");
               
               for (int col=0; col < this.matrix_A.numColumns(); col++) {
                   double val = this.matrix_A.get(row, col);
                   if (val != 0.0) {
                       int p = spec.inv_index_prop(col);
                       int obj = spec.inv_index_obj(col);
                       int time = spec.inv_index_time(col);

                       s+=(" ");
                       s2+=(" ");
                       if (val > 0.0) {
                           s+=("+");
                           s2+=("+");
                       }

                       double v, v2;
                       v = v2 = val;
                       if (val != -1.0) {
                           
                           if ((p==0) || (p==2))  {  // apply slack to Drugs and Sex
                               v *= scale;
                               v2 *= scale2;
                           }
                           else {
                               v *= 0.99; // Apply 1% slack to the rest
                               v2 *= 1.01;
                           }
                       }
                       s+=(""+ v +" ");
                       s2+=(""+ v2 +" ");

                       s+=("p"+p+"_c"+obj+"_t"+time+" ");
                       s2+=("p"+p+"_c"+obj+"_t"+time+" ");
                   }
               }

               double zval = this.vector_v.get(row);
               LOG.write(""+s+" <= "+zval+" ;\n");
               LOG.write(""+s2+" >= "+zval+" ;\n");
           }
           */
           
           LOG.write("/* splitting below is just a trick to (linearly) compute absolute value of each divergence from the law */ ");
           LOG.write("\n");
                   
           for (int row=0; row < homrows; row++) {
               LOG.write("split"+row+": ");
               LOG.write("normdiff"+row+" = comp_pos"+row+" - comp_neg"+row+" ;\n");
               LOG.write("comp_pos"+row+" >= 0.0 ;\n");
               LOG.write("comp_neg"+row+" >= 0.0 ;\n");
           }

           double fraction = 0.0;
           LOG.write("\n");
           LOG.write("/* Constraints below ensure all coordinates are at least "+(fraction*100)+"% of cash */ ");
           LOG.write("\n");
           
           for (int col=0; col < this.matrix_A.numColumns(); col++) {
               int p = spec.inv_index_prop(col);
               int obj = spec.inv_index_obj(col);
               int time = spec.inv_index_time(col);

               fraction = 0.005;
               // default lower bound = 2% of cash
               if (p==2) {
                   // the lower bound on the OTHER variable
//                 fraction = 0.5;
//                 fraction = 0.5;
               }
               LOG.write("pos_p"+p+"_c"+obj+"_t"+time+": p"+p+"_c"+obj+"_t"+time+" > "+fraction+" z_c"+obj+"_t"+time+" ;\n");
           }

           LOG.write("\n\n/* lp_solve program 1 ends */\n\n");
       }
       catch (IOException ex) {
           ex.printStackTrace();
       }
   }

   public DenseVector solve(InstanceSpec spec, boolean verbose, FileWriter LOG) {

        if (verbose) {
            try { PrintUtils.printMatrix(matrix_A, "A", LOG); } catch (IOException e) { e.printStackTrace(); };
            try { PrintUtils.printVector(vector_v, "v", LOG); } catch (IOException e) { e.printStackTrace(); };
        }

        Matrix At = new DenseMatrix(_cols, _rowsActual);
        matrix_A.transpose(At);
        if (verbose) {
            try { PrintUtils.printMatrix(At, "At", LOG); } catch (IOException e) { e.printStackTrace(); };
        }

        Matrix AtA = new DenseMatrix(_cols, _cols);
        At.mult(matrix_A, AtA);
        if (verbose) {
            try { PrintUtils.printMatrix(At, "AtA", LOG); } catch (IOException e) { e.printStackTrace(); };
        }

        Vector Atv = new DenseVector(_cols);
        At.mult(vector_v, Atv);
        if (verbose) {
            try { PrintUtils.printVector(Atv, "Atv", LOG); } catch (IOException e) { e.printStackTrace(); };
        }

        DenseVector x = new DenseVector(_cols);
        AtA.solve(Atv, x);
        if (verbose) {
            try { PrintUtils.printVector(x, "x", LOG); } catch (IOException e) { e.printStackTrace(); };
        }

        return x;
    }

    static DenseVector diff(DenseVector truex, DenseVector estx) {
        DenseVector diff = new DenseVector(truex);
        diff.add(-1.0, estx);
        return diff;
    }

    static DenseVector diffPercentile(DenseVector truex, DenseVector estx) {
        DenseVector diff = diff(truex, estx);

        for (int i=0;i<diff.size();i++) {
            diff.set(i, diff.get(i) / truex.get(i));
        }

        return diff;
    }
    
}
