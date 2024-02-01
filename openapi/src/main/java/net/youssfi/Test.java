package net.youssfi;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mohamedyoussfi
 **/
public class Test {
    public static void main(String[] args) {
        Map<String, ArrayList<String>> listMap1 =new HashMap<>();
        Map<String, ArrayList<String>> listMap2 =new HashMap<>();
        listMap1.put("category",new ArrayList<>());
        listMap1.put("polarity", new ArrayList<>());
        listMap1.get("category").add("screen");
        listMap1.get("category").add("gpu");
        listMap1.get("category").add("hardware");
        listMap1.get("category").add("software");
        listMap1.get("polarity").add("neutral");
        listMap1.get("polarity").add("neutral");
        listMap1.get("polarity").add("positive");
        listMap1.get("polarity").add("neutral");


        listMap2.put("category",new ArrayList<>());
        listMap2.put("polarity", new ArrayList<>());
        listMap2.get("category").add("screen");
        listMap2.get("category").add("gpu");
        listMap2.get("category").add("hardware");
        listMap2.get("category").add("software");
        listMap2.get("polarity").add("neutral");
        listMap2.get("polarity").add("neutral");
        listMap2.get("polarity").add("positive");
        listMap2.get("polarity").add("neutral");

        System.out.println(listMap1);
        System.out.println(listMap2);
        System.out.println(listMap1.equals(listMap2));

    }
}
