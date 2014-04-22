/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jintangible;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import no.uib.cipr.matrix.*;
import java.text.*;

/**
 *
 * @author grouptheory
 */
public class Experimenter {

    private HashMap _pert2dim2trial2dev = new HashMap();
    private HashMap _pert2trial2dist = new HashMap();
    private HashMap _pertindex2pertdouble = new HashMap();
    private NumberFormat FORMAT;

    private int _numvars;
    
    public Experimenter(int numvars) {
        _numvars = numvars;
        FORMAT = new DecimalFormat("0.000");
    }
    
    public double record(int pertIdx, double pertValue, int trial, DenseVector diff) {

        if ( ! _pertindex2pertdouble.containsKey(pertIdx)) {
            _pertindex2pertdouble.put(pertIdx, pertValue);
        }
        
        for (int i=0; i<_numvars; i++) {
            double v = diff.get(i);
            HashMap m = getDictionary_pert2dim2trial2dev(pertIdx, i);
            m.put(trial, v);
        }

        HashMap map = getDictionary_pert2trial2dist(pertIdx);
        double dev = diff.norm(Vector.Norm.TwoRobust);
        map.put(new Integer(trial), new Double(dev));
        
        return dev;
    }

    private double getPertValue(int pertIdx) {
        if (_pertindex2pertdouble.containsKey(pertIdx)) {
            return (Double)_pertindex2pertdouble.get(pertIdx);
        }
        return -1.0;
    }

    private double getMeanAbs_dev(int pertIdx, int dim) {
        HashMap map = getDictionary_pert2dim2trial2dev(pertIdx, dim);
        double total, count;
        total = count = 0;
        for (Iterator it=map.values().iterator();it.hasNext();) {
            Double dev = (Double)it.next();
            total+=Math.abs(dev);
            count+=1.0;
        }
        return total/count;
    }

    public double getMeanAbs_devAve(int pertIdx) {
        double total, count;
        total = count = 0;
        for (int i=0; i<_numvars; i++) {
            total += getMeanAbs_dev(pertIdx, i);
            count+=1.0;
        }
        return total/count;
    }

    private double getStdAbs_dev(int pertIdx, int dim) {
        double mean = getMeanAbs_dev(pertIdx, dim);
        HashMap map = getDictionary_pert2dim2trial2dev(pertIdx, dim);
        double total, count;
        total = count = 0;
        for (Iterator it=map.values().iterator();it.hasNext();) {
            Double dev = Math.abs((Double)it.next());
            total+=((dev-mean)*(dev-mean));
            count+=1.0;
        }
        return Math.sqrt(total/count);
    }

    public double getStdAbs_devAve(int pertIdx) {
        double total, count;
        total = count = 0;
        for (int i=0; i<_numvars; i++) {
            total += getStdAbs_dev(pertIdx, i);
            count+=1.0;
        }
        return total/count;
    }

    private double getMean_dev(int pertIdx, int dim) {
        HashMap map = getDictionary_pert2dim2trial2dev(pertIdx, dim);
        double total, count;
        total = count = 0;
        for (Iterator it=map.values().iterator();it.hasNext();) {
            Double dev = (Double)it.next();
            total+=dev;
            count+=1.0;
        }
        return total/count;
    }

    public double getMean_devAve(int pertIdx) {
        double total, count;
        total = count = 0;
        for (int i=0; i<_numvars; i++) {
            total += getMean_dev(pertIdx, i);
            count+=1.0;
        }
        return total/count;
    }

    private double getStd_dev(int pertIdx, int dim) {
        double mean = getMean_dev(pertIdx, dim);
        HashMap map = getDictionary_pert2dim2trial2dev(pertIdx, dim);
        double total, count;
        total = count = 0;
        for (Iterator it=map.values().iterator();it.hasNext();) {
            Double dev = (Double)it.next();
            total+=((dev-mean)*(dev-mean));
            count+=1.0;
        }
        return Math.sqrt(total/count);
    }

    public double getStd_devAve(int pertIdx) {
        double total, count;
        total = count = 0;
        for (int i=0; i<_numvars; i++) {
            total += getStd_dev(pertIdx, i);
            count+=1.0;
        }
        return total/count;
    }

    public double getMean_dist(int pertIdx) {
        HashMap map = getDictionary_pert2trial2dist(pertIdx);
        double total, count;
        total = count = 0;
        for (Iterator it=map.values().iterator();it.hasNext();) {
            Double dev = (Double)it.next();
            total+=dev;
            count+=1.0;
        }
        return total/count;
    }

    public double getStd_dist(int pertIdx) {
        double mean = getMean_dist(pertIdx);
        HashMap map = getDictionary_pert2trial2dist(pertIdx);
        double total, count;
        total = count = 0;
        for (Iterator it=map.values().iterator();it.hasNext();) {
            Double dev = (Double)it.next();
            total+=((dev-mean)*(dev-mean));
            count+=1.0;
        }
        return Math.sqrt(total/count);
    }

    public static String getheader() {
        String s = "";
        s += "# pertValue";
        s += ",meanDist";
        s += ",stdDist";
        s += ",mdevAve";
        s += ",sdevAve";
        s += ",mAbsdevAve";
        s += ",sAbsdevAve,";
        return s;    
    }
    
    public String getstats(int pertIdx) {
        String s = "";
        double pertValue = getPertValue(pertIdx);
        double meanDist = getMean_dist(pertIdx);
        double stdDist = getStd_dist(pertIdx);
        double mdevAve = getMean_devAve(pertIdx);
        double sdevAve = getStd_devAve(pertIdx);
        double mAbsdevAve = getMeanAbs_devAve(pertIdx);
        double sAbsdevAve = getStdAbs_devAve(pertIdx);
        s+= FORMAT.format(pertValue)+","+
                FORMAT.format(meanDist)+","+FORMAT.format(stdDist)+","+
                FORMAT.format(mdevAve)+","+FORMAT.format(sdevAve)+","+
                FORMAT.format(mAbsdevAve)+","+FORMAT.format(sAbsdevAve);
        return s;
    }
    
    private HashMap getDictionary_pert2trial2dist(int pertIdx) {
        HashMap map = (HashMap)_pert2trial2dist.get(new Integer(pertIdx));
        if (map == null) {
            map = new HashMap();
            _pert2trial2dist.put(new Integer(pertIdx), map);
        }
        return map;
    }



    private HashMap getDictionary_pert2dim2trial2dev(int pertIdx, int dim) {
        HashMap dim2trial2dev = (HashMap)_pert2dim2trial2dev.get(new Integer(pertIdx));
        if (dim2trial2dev == null) {
            dim2trial2dev = new HashMap();
            _pert2dim2trial2dev.put(new Integer(pertIdx), dim2trial2dev);
        }
        HashMap map = (HashMap)dim2trial2dev.get(dim);
        if (map == null) {
            map = new HashMap();
            dim2trial2dev.put(new Integer(dim), map);
        }
        return map;
    }
}
