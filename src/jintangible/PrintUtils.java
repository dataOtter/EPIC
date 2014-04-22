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
public class PrintUtils {

    public static void printVector(Vector vec, String str, FileWriter LOG) throws IOException {
        LOG.write("\nVector ["+str+"] is rows:"+vec.size()+"\n");

        for (int i=0;i<vec.size();i++) {
            NumberFormat formatter;
            if (vec.get(i) >= 0) formatter = new DecimalFormat("+0.00");
            else formatter = new DecimalFormat("0.00");
            String s=formatter.format(vec.get(i));
            LOG.write(""+s+", ");
        }
        LOG.write("\n");
    }

    public static void printMatrix(Matrix mat, String str, FileWriter LOG) throws IOException {
        LOG.write("\nMatrix ["+str+"] is rows:"+mat.numRows()+" by cols:"+mat.numColumns()+"\n");

        for (int i=0;i<mat.numRows();i++) {
            for (int j=0;j<mat.numColumns();j++) {
                NumberFormat formatter;
                if (mat.get(i,j) >= 0) formatter = new DecimalFormat("+0.00");
                else formatter = new DecimalFormat("0.00");
                String s = formatter.format(mat.get(i,j));
                LOG.write(""+s+", ");
            }
            LOG.write("\n");
        }
    }

}
