package utils;

import java.util.List;

public class RandomUtils {

    public static int randomIntFromInterval(int min, int max) { // min and max included 
        return (int) Math.floor(Math.random() * (max - min + 1) + min);
    }

    public static <T> T pickOne(List<T> coll) {
        if (coll.isEmpty()) {
            return null;
        }
        int randomIndex = randomIntFromInterval(0, coll.size() - 1);
        return coll.get(randomIndex);
    }
}
