package ru.rozvezev;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Программа сопоставления похожих строк. Сопоставление по сходству Джаро — Винклера
 * (https://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance).
 */
public class Main {

    private static final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

    public static void main(String[] args) {

        if (args.length == 0){
            System.out.println("Укажите путь к файлу в качестве аргумента.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]))) {

            int n = Integer.parseInt(reader.readLine());

            // Похожость строк определяем по сходству Джаро — Винклера и заносим число для каждой пары строк в мап-у.
            Map<String, List<String[]>> firstListWithDistances = new LinkedHashMap<>(n);
            for (int i = 0; i < n; i++) {
                firstListWithDistances.put(reader.readLine(), new ArrayList<>());
            }

            int m = Integer.parseInt(reader.readLine());
            List<String> secondList = new ArrayList<>(m);
            Map<String, Double> columnsMaxDistances = new HashMap<>();

            for (int i = 0; i < m; i++) {
                String columnString = reader.readLine();
                secondList.add(columnString);
                double columnMaxDistance = 0.0;
                for (Map.Entry<String, List<String[]>> entry: firstListWithDistances.entrySet()){

                    //откругляем до 10 знака после запятой, для более точного сравнения double чисел.
                    double distance = BigDecimal.valueOf(similarity.apply(entry.getKey(), columnString))
                            .setScale(10, RoundingMode.CEILING)
                            .doubleValue();
                    entry.getValue().add(new String[]{columnString, String.valueOf(distance)});
                    if (distance > columnMaxDistance) {
                        columnMaxDistance = distance;
                    }
                }
                columnsMaxDistances.put(columnString, columnMaxDistance);
            }

            // Сортируем List дистаниций для каждого слова из первого списка.
            firstListWithDistances.values().
                    forEach(list -> {
                        list.sort(Comparator.comparingDouble(array -> Double.parseDouble(array[1])));
                        Collections.reverse(list);
                    });


            List<String> resultList = new ArrayList<>(n);
            boolean foundPair;
            for (Map.Entry<String, List<String[]>> entry: firstListWithDistances.entrySet()){
                foundPair = false;
                String key = entry.getKey();
                List<String[]> value = entry.getValue();
                for (String[] column : value){
                    Double columnMaxDistance = columnsMaxDistances.get(column[0]);
                    if (Double.parseDouble(column[1]) >= columnMaxDistance){
                        resultList.add(key + ":" + column[0]);
                        foundPair = true;
                        secondList.remove(column[0]);
                        break;
                    }
                }
                if (!foundPair) {
                    resultList.add(key + ":?");
                }
            }

            // Если m оказалось больше n, то список будет не пуст, заполняем его оставшимися строками.
            secondList.forEach(str -> resultList.add(str + ":?"));

            System.out.println(resultList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
