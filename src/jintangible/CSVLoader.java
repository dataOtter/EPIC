/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jintangible;


import java.io.*;
import java.util.StringTokenizer;
import java.util.*;

/**
 *
 * @author grouptheory
 */
public class CSVLoader {

    public static class RatioTable {

        public static class AlphaRatioConstraint {
            public String _c1, _c2, _t1;
            public int _c1index, _c2index, _t1index;
            public double _val;
            public AlphaRatioConstraint(){}
            public void set(String c1, String c2, String t1, double val) {
                _c1 = c1; _c2 = c2; _t1 = t1; _val = val;
            }
            public void computeIndexes(ProxyTable z) {
                _c1index = z.get_objectInd(_c1);
                _c2index = z.get_objectInd(_c2);
                _t1index = z.get_timeInd(_t1);
            }
        }

        public static class BetaRatioConstraint {
            public String _t1, _t2, _c1;
            public int _t1index, _t2index, _c1index;
            public double _val;
            public BetaRatioConstraint(){}
            public void set(String t1, String t2, String c1, double val) {
                _t1 = t1; _t2 = t2; _c1 = c1; _val = val;
            }
            public void computeIndexes(ProxyTable z) {
                _t1index = z.get_timeInd(_t1);
                _t2index = z.get_timeInd(_t2);
                _c1index = z.get_objectInd(_c1);
            }
        }

        private String _name;

        private LinkedList _alphaConstraints = new LinkedList();
        private LinkedList _betaConstraints = new LinkedList();
        int _count;

        public String name() {
            return _name;
        }

        public int numAuxRatioConstraints() {
            return _count;
        }

        public RatioTable(String name) {
            _name = new String(name);
            _count=0;
        }

        public Iterator alphaConstraintIterator() {
            return _alphaConstraints.iterator();
        }

        public Iterator betaConstraintIterator() {
            return _betaConstraints.iterator();
        }

        public void addAlphaConstraint(String c1, String c2, String t1, double v, boolean verbose, FileWriter LOG) {
            if (verbose) {
                try {
                    LOG.write(""+_name+": alpha ratio constraint "+c1+"/"+c2+" @time="+t1+" = "+v+"\n");
                    LOG.flush();
                }
                catch (IOException ex2) { }
            }
            CSVLoader.RatioTable.AlphaRatioConstraint cons = new CSVLoader.RatioTable.AlphaRatioConstraint();
            cons.set(c1, c2, t1, v);
            _alphaConstraints.addLast(cons);
            _count++;
        }

        public void addBetaConstraint(String t1, String t2, String c1, double v, boolean verbose, FileWriter LOG) {
            if (verbose) {
                try {
                    LOG.write(""+_name+": beta ratio constraint "+t1+"/"+t2+" @city="+c1+" = "+v+"\n");
                    LOG.flush();
                }
                catch (IOException ex2) { }
            }
            CSVLoader.RatioTable.BetaRatioConstraint cons = new CSVLoader.RatioTable.BetaRatioConstraint();
            cons.set(t1, t2, c1, v);
            _betaConstraints.addLast(cons);
            _count++;
        }

        void computeIndexes(ProxyTable z) {
            for (Iterator it = this.alphaConstraintIterator(); it.hasNext();) {
                CSVLoader.RatioTable.AlphaRatioConstraint cons =
                        (CSVLoader.RatioTable.AlphaRatioConstraint)it.next();
                cons.computeIndexes(z);
            }

            for (Iterator it = this.betaConstraintIterator(); it.hasNext();) {
                CSVLoader.RatioTable.BetaRatioConstraint cons =
                        (CSVLoader.RatioTable.BetaRatioConstraint)it.next();
                cons.computeIndexes(z);
            }
        }

        void print(FileWriter LOG) {
            for (Iterator it = this.alphaConstraintIterator(); it.hasNext();) {
                CSVLoader.RatioTable.AlphaRatioConstraint cons =
                        (CSVLoader.RatioTable.AlphaRatioConstraint)it.next();
                try {
                    LOG.write("CSVLoader> "+_name+": alpha ratio constraint "+cons._c1+"/"+cons._c2+" @time="+cons._t1+" = "+cons._val+"\n");
                    LOG.flush();
                }
                catch (IOException ex2) { }
            }

            for (Iterator it = this.betaConstraintIterator(); it.hasNext();) {
                CSVLoader.RatioTable.BetaRatioConstraint cons =
                        (CSVLoader.RatioTable.BetaRatioConstraint)it.next();
                try {
                    LOG.write("CSVLoader> "+_name+": beta ratio constraint "+cons._t1+"/"+cons._t2+" @city="+cons._c1+" = "+cons._val+"\n");
                    LOG.flush();
                }
                catch (IOException ex2) { }
            }
        }
    }
    
    public static class ProxyTable {
        private String _name;
        private HashMap _objects = new HashMap();
        private HashMap _years = new HashMap();
        private HashMap _yearInd2objectIndex2val = new HashMap();

        private HashMap _objects2Ind = new HashMap();
        private HashMap _years2Ind = new HashMap();

        ProxyTable(String name, HashMap objects, HashMap years, HashMap yearInd2objectIndex2val,
                   boolean verbose, FileWriter LOG) {
            
            _name = new String(name);
            _objects.putAll(objects);
            _years.putAll(years);
            _yearInd2objectIndex2val.putAll(yearInd2objectIndex2val);

            for (Iterator it=_objects.entrySet().iterator(); it.hasNext();) {
                Map.Entry ent = (Map.Entry)it.next();
                int i = (Integer)ent.getKey();
                String ci = (String)ent.getValue();
                _objects2Ind.put(ci,i);

                if (verbose) {
                    try {
                        LOG.write("object "+i+" == "+ci+"\n");
                        LOG.flush();
                    }
                    catch (IOException ex2) { }
                }
            }

            for (Iterator it=_years.entrySet().iterator(); it.hasNext();) {
                Map.Entry ent = (Map.Entry)it.next();
                int j = (Integer)ent.getKey();
                String tj = (String)ent.getValue();
                _years2Ind.put(tj,j);

                if (verbose) {
                    try {
                        LOG.write("time "+j+" == "+tj+"\n");
                        LOG.flush();
                    }
                    catch (IOException ex2) { }
                }
            }
        }

        public String name() {
            return _name;
        }
        
        public int n() {
            return _objects.size();
        }

        public int m() {
            return _years.size();
        }

        public String get_ci(int i) {
            return (String)_objects.get(i);
        }

        public String get_tj(int j) {
            return (String)_years.get(j);
        }

        public int get_objectInd(String ci) {
            Object x = _objects2Ind.get(ci);
            if (x==null) {
                System.out.println("x is null for string ci="+ci);
            }
            return (Integer)x;
        }

        public int get_timeInd(String tj) {
            return (Integer)_years2Ind.get(tj);
        }

        public double get(String ci, String tj) {

            // DEBUG
            // System.out.println("get w ci="+ci+" and tj="+tj);
            
            int objecti = (Integer)_objects2Ind.get(ci);
            int yearj = (Integer)_years2Ind.get(tj);
            // DEBUG
            // System.out.println("get w i="+objecti+" and j="+yearj);

            HashMap objInd2val;
            if ( ! _yearInd2objectIndex2val.containsKey(yearj)) {
                objInd2val = new HashMap();
                _yearInd2objectIndex2val.put(yearj, objInd2val);
            }
            objInd2val = (HashMap)_yearInd2objectIndex2val.get(yearj);
            return (Double)objInd2val.get(objecti);
        }
    };
    
    public static ProxyTable ReadProxyCSV(String strFile, boolean verbose, FileWriter LOG) throws IOException {
        //create BufferedReader to read csv file
        BufferedReader br;
        try {
            br = new BufferedReader( new FileReader(strFile));
        }
        catch (IOException ex) {
            throw ex;
        }

        String strLine = "";
        StringTokenizer st = null;
        int lineNumber = 0, tokenNumber = 0;

        HashMap years = new HashMap();
        HashMap objects = new HashMap();
        HashMap yearInd2objectInd2val = new HashMap();
        String var=null;

        try
        {
            //read comma separated file line by line
            while( (strLine = br.readLine()) != null)
            {
                lineNumber++;

                //break comma separated line using ","
                st = new StringTokenizer(strLine, ",");

                while(st.hasMoreTokens())
                {
                    //display csv values
                    tokenNumber++;

                    String s = st.nextToken();

                    if (lineNumber==1 && tokenNumber==1) {
                        var = s;
                    }
                    else if(lineNumber == 1 && tokenNumber > 1) {
                        objects.put(tokenNumber-2, s);
                    }
                    else if(lineNumber > 1 && tokenNumber == 1) {
                        years.put(lineNumber-2, s);
                    }
                    else {
                        HashMap objInd2val;
                        if ( ! yearInd2objectInd2val.containsKey(lineNumber-2)) {
                            objInd2val = new HashMap();
                            yearInd2objectInd2val.put(lineNumber-2, objInd2val);
                        }
                        objInd2val = (HashMap)yearInd2objectInd2val.get(lineNumber-2);
                        
                        Double val = Double.parseDouble(s);
                        objInd2val.put(tokenNumber-2, val);
                    }

                    /*
                    System.out.println("Line # " + lineNumber +
                                    ", Token # " + tokenNumber
                                    + ", Token : "+ s);
                     */
                }
                //reset token number
                tokenNumber = 0;
            }

            return new ProxyTable(var, objects, years, yearInd2objectInd2val, verbose, LOG);
        }
        catch(Exception e)
        {
            System.out.println("Exception while reading csv file: " + e);
            e.printStackTrace();
        }
        return null;
    }

    public static RatioTable ReadRatioCSV(String strFile, boolean verbose, FileWriter LOG) throws IOException {
        //create BufferedReader to read csv file
        BufferedReader br;
        try {
            br = new BufferedReader( new FileReader(strFile));
        }
        catch (IOException ex) {
            throw ex;
        }

        String strLine = "";
        StringTokenizer st = null;
        int lineNumber = 0, tokenNumber = 0;

        String var=null;

        RatioTable rt = null;
        
        try
        {
            //read comma separated file line by line
            while( (strLine = br.readLine()) != null)
            {
                lineNumber++;

                //break comma separated line using ","
                st = new StringTokenizer(strLine, ",");

                boolean isAlpha = false;
                String c1, c2, t1, t2;
                c1 = c2 = t1 = t2 = null;
                
                for (int f=0; f<5; f++) {
                    //display csv values
                    tokenNumber++;

                    String s = st.nextToken();

                    if (lineNumber==1 && tokenNumber==1) {
                        var = s;
                        rt = new RatioTable(var);
                    }
                    else if(lineNumber > 1 && tokenNumber == 1) {
                        if (s.equals("alpha")) {
                            isAlpha = true;
                        }
                        else if (s.equals("beta")) {
                            isAlpha = false;
                        }
                        else {
                            throw new IOException("Unknown ratio constraint type: "+s+" (only alpha and beta are supported).");
                        }
                    }
                    else if(lineNumber > 1 && tokenNumber > 1) {
                        if (isAlpha) {
                            if (tokenNumber==2) c1=s;
                            if (tokenNumber==3) c2=s;
                            if (tokenNumber==4) t1=s;
                            if (tokenNumber==5) {
                                double v = Double.parseDouble(s);
                                rt.addAlphaConstraint(c1,c2,t1,v,
                                        verbose,LOG);
                            }
                        }
                        else {
                            if (tokenNumber==2) t1=s;
                            if (tokenNumber==3) t2=s;
                            if (tokenNumber==4) c1=s;
                            if (tokenNumber==5) {
                                double v = Double.parseDouble(s);
                                rt.addBetaConstraint(t1,t2,c1,v,
                                        verbose,LOG);
                            }
                        }
                    }

                    /*
                    System.out.println("Line # " + lineNumber +
                                    ", Token # " + tokenNumber
                                    + ", Token : "+ s);
                     */
                }
                //reset token number
                tokenNumber = 0;
            }

            return rt;
        }
        catch(Exception e)
        {
            System.out.println("Exception while reading csv file: " + e);
            e.printStackTrace();
        }
        return null;
    }
}
