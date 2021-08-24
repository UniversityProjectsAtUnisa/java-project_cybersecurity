package utils;

import java.util.List;
import java.util.stream.IntStream;

public class SimulationData {
    public static final List<String> VALID_CF_LIST = IntStream
            .range(1, Config.CLIENT_COUNT + 1)
            .mapToObj(i -> "CF" + i)
            .toList();
}
