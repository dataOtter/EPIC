/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jintangible;

import no.uib.cipr.matrix.*;
import java.io.*;


/**
 *
 * @author grouptheory
 */
public class InstanceSpec {
    protected final int _numprops, _numobjs, _numtimes;
    
    public InstanceSpec(int numprops, int numobjs, int numtimes) {

        _numprops = numprops;
        _numobjs = numobjs;
        _numtimes = numtimes;
    }

    public int NumProps() {
        return _numprops;
    }

    public int NumObjs() {
        return _numobjs;
    }
    
    public int NumTimes() {
        return _numtimes;
    }

    public int index_val(int prop, int obj, int time) {
        int index = prop*(_numobjs*_numtimes) + obj*_numtimes + time;

        int prop2 = inv_index_prop(index);
        if (prop != prop2) throw new Error("Inviant index_val <--> inv_index_prop");

        int obj2 = inv_index_obj(index);
        if (obj != obj2) throw new Error("Inviant index_val <--> inv_index_obj");

        int time2 = inv_index_time(index);
        if (time != time2) throw new Error("Inviant index_val <--> inv_index_time");

        return index;
    }

    public int inv_index_prop(int index) {
        int prod1 = index - inv_index_obj(index)*_numtimes - inv_index_time(index);
        int quot1 = prod1 / (_numobjs*_numtimes);
        return quot1;
    }

    public int inv_index_obj(int index) {
        int rem1 = index % (_numobjs*_numtimes);
        int rem2 = rem1 % _numtimes;
        int prod2 = rem1 - rem2;
        int quot2 = prod2 / _numtimes;
        return quot2;
    }
    
    public int inv_index_time(int index) {
        int rem1 = index % (_numobjs*_numtimes);
        int rem2 = rem1 % _numtimes;
        return rem2;
    }
}
