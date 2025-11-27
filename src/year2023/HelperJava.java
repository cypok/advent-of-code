package year2023;

import java.util.Comparator;

public class HelperJava {
    static Comparator<Arrow> CC4 = Comparator.comparing(x -> x.getHeat());
}
