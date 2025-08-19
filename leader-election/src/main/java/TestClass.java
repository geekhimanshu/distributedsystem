import java.util.HashMap;
import java.util.Map;

public class TestClass {

    public static void main(String [] args) {
        //Java java Java java Spring spring Hibernate
        final String input = "Java java Java java Spring spring Hibernate";
        countAndPrintWords(input);
    }

    private static void countAndPrintWords(String input) {

        Map<String, Integer> wordCountMap = new HashMap<>();
        String[] words = input.split(" ");
        for(String word : words) {
            wordCountMap.putIfAbsent(word, 0);
            wordCountMap.put(word, wordCountMap.get(word)+1);
        }

        for(Map.Entry<String, Integer> e: wordCountMap.entrySet()) {
            System.out.println(e.getKey() + " " + e.getValue());
        }
    }

}
