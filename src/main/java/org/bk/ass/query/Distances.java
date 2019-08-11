package org.bk.ass.query;

public class Distances {
    public static final DistanceProvider EUCLIDEAN_DISTANCE =
            (ax, ay, bx, by) -> (int) Math.sqrt((float) (bx - ax) * (bx - ax) + (by - ay) * (by - ay));

    /**
     * When using this, all radius queries need to be made with the squared radius
     */
    public static final DistanceProvider EUCLIDEAN_DISTANCE_SQUARED =
            (ax, ay, bx, by) -> (bx - ax) * (bx - ax) + (by - ay) * (by - ay);

    /**
     * Use this to query using the distance approximation used in OpenBW
     */
    public static final DistanceProvider BW_DISTANCE_APPROXIMATION =
            (ax, ay, bx, by) -> {
                int min = Math.abs(ax - bx);
                int max = Math.abs(ay - by);
                int minCalc;
                if (max < min) {
                    minCalc = max;
                    max = min;
                    min = minCalc;
                }

                if (min < max >> 2) {
                    return max;
                } else {
                    minCalc = 3 * min >> 3;
                    return (minCalc >> 5) + minCalc + max - (max >> 4) - (max >> 6);
                }
            };

    private Distances() {
        // Utility class
    }
}
