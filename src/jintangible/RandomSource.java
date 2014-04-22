/*
 * A centralized source of randomness.
 */

package jintangible;

import java.util.*;

/**
 *
 * @author emisshula
 */
public class RandomSource {

    private static Random _rng = new Random();

    public static void setSeed(long s) {
        _rng = new Random(s);
    }

    public static double GetUniformRandom(double a, double b) {
        return a + _rng.nextDouble() * (b-a);
    }

    public static boolean GetBernoulliRandom(double p) {
        return _rng.nextDouble() < p;
    }

    public static double GetGaussianRandom(double mean, double stddev) {
        double r, x, y;
        do {
            x = GetUniformRandom(-1.0, 1.0);
            y = GetUniformRandom(-1.0, 1.0);
            r = x*x + y*y;
        } while (r >= 1 || r == 0);
        return mean + stddev * (x * Math.sqrt(-2 * Math.log(r) / r));
    }

    public static double GetGaussianPositiveRandom(double mean, double stddev) {
        double ans;
        do {
            ans = GetGaussianRandom(mean, stddev);
        }
        while (ans<0.0);
        return ans;
    }
    public static int GetGeometricRandom(double p) {
        return (int) Math.ceil(Math.log(GetUniformRandom(0.0, 1.0)) / Math.log(1.0 - p));
    }

    public static int GetPoissonRandom(double lambda) {
        int k = 0;
        double p = 1.0;
        double L = Math.exp(-lambda);
        do {
            k++;
            p *= GetUniformRandom(0.0, 1.0);
        } while (p >= L);
        return k-1;
    }

    public static double GetParetoRandom(double alpha) {
        return Math.pow(1 - GetUniformRandom(0.0, 1.0), -1.0/alpha) - 1.0;
    }

    public static int GetDiscreteRandom(double[] a) {
        // precondition: sum of array entries equals 1
        double r = _rng.nextDouble();
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum = sum + a[i];
            if (sum >= r) return i;
        }
        return -1;
    }

    public static double exp(double lambda) {
        return -Math.log(1 - _rng.nextDouble()) / lambda;
    }
}


