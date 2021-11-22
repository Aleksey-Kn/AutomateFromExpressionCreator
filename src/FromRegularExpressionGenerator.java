import java.util.*;
import java.util.stream.Collectors;

public class FromRegularExpressionGenerator {
    private final String regular;
    private int maxSize;

    public FromRegularExpressionGenerator(String regular) {
        this.regular = regular.replace("\n", "");
        if (regular.chars().filter(c -> c == '(').count() != regular.chars().filter(c -> c == ')').count())
            throw new IllegalArgumentException("Count '(' not equal count ')'");
        if (regular.chars().anyMatch(Character::isUpperCase))
            throw new IllegalArgumentException("Regular expression must not contain uppercase symbol");
    }

    public List<String> generateChains(int minLength, int maxLength) {
        maxSize = maxLength;
        ArrayList<String> results = new ArrayList<>();
        results.add("");
        if (regular.charAt(0) == '('
                && regular.charAt(regular.length() - 1) == '*'
                && indexCloseFor(regular) == regular.length() - 2)
            results = manyStep(results, split(regular.substring(1, regular.length() - 2)));
        else {
            if (regular.charAt(0) == '(' && indexCloseFor(regular) == regular.length() - 1)
                results = oneStep(results, split(regular.substring(1, regular.length() - 1)));
            else
                results = oneStep(results, split(regular));
        }
        return results.stream()
                .filter(s -> s.length() <= maxLength)
                .filter(s -> s.length() >= minLength)
                .collect(Collectors.toList());
    }

    public AutomateDTO generateAutomate() {
        Set<Character> terminals = regular.chars()
                .filter(c -> Character.isDigit(c) || Character.isLowerCase(c))
                .mapToObj(i -> (char) i)
                .collect(Collectors.toSet());
        Map<String, Map<Character, String>> regulations = new TreeMap<>();
        Set<String> endStares = generateRegulations(regular, Collections.singleton("q0"), regulations, null);
        Set<String> states = new TreeSet<>(regulations.keySet());
        regulations.values().forEach(values -> states.addAll(values.values()));
        return new AutomateDTO(terminals, states, "q0", endStares, regulations);
    }

    private Set<String> generateRegulations(String nowString, Set<String> nowState,
                                             Map<String, Map<Character, String>> regulations, Set<String> inState) {
        String[] splittingString = split(nowString);
        if (splittingString.length == 1) {
            String nextState;
            int indexCloseFor;
            boolean isMultiplicity;
            for (int i = 0; i < nowString.length(); i++) {
                if (nowString.charAt(i) == '(') { // обработка конкатинации выражения
                    indexCloseFor = indexCloseFor(nowString, i);
                    isMultiplicity = indexCloseFor + 1 < nowString.length()
                            && nowString.charAt(indexCloseFor + 1) == '*';
                    nowState = generateRegulations(searchNextBlock(nowString, i), new HashSet<>(nowState), regulations,
                            calculateReturnStates(isMultiplicity, nowState, inState));
                    i = (isMultiplicity? indexCloseFor: indexCloseFor + 1);
                } else { // обработка конкатинации терминалов
                    nextState = "q" + regulations.size();
                    for (String ns : nowState) {
                        if (!regulations.containsKey(ns))
                            regulations.put(ns, new HashMap<>());
                        regulations.get(ns).put(nowString.charAt(i), nextState);
                        if (i == nowString.length() - 1 && inState != null) {
                            for (String is : inState) {
                                regulations.get(ns).put(nowString.charAt(i), is);
                            }
                        }
                    }
                    nowState = Collections.singleton(nextState);
                }
            }
            return nowState;
        } else { // обработка логического сложения
            Set<String> result = new HashSet<>();
            for (String s : splittingString) {
                result.addAll(generateRegulations(s, new HashSet<>(nowState), regulations, new HashSet<>(inState)));
            }
            return result;
        }
    }

    private Set<String> calculateReturnStates(boolean isMultiplicity, Set<String> nowState, Set<String> inState){
        Set<String> result = new HashSet<>();
        if(isMultiplicity)
            result.addAll(nowState);
        if(inState != null)
            result.addAll(inState);
        return result;
    }

    private String searchNextBlock(String s, int startInd) {
        int endInd;
        startInd = s.indexOf('(', startInd);
        if (startInd == -1)
            return null;
        endInd = indexCloseFor(s, startInd);
        return s.substring(startInd + 1, endInd);
    }

    private ArrayList<String> oneStep(ArrayList<String> previousResult, String[] options) { // возвращает входные цепочки, на которые навешены варианты из входных options
        ArrayList<String> tempResult;
        ArrayList<String> newResult = new ArrayList<>();
        StringBuilder state;
        int ind;
        for (String nowOptions : options) {
            tempResult = new ArrayList<>(previousResult);
            state = new StringBuilder(nowOptions);
            while (!state.isEmpty()) {
                if (state.charAt(0) == '(') {
                    ind = indexCloseFor(state.toString()); // индекс конца обрабатываемых скобок
                    String now = state.substring(1, ind);
                    if (ind != state.length() - 1 && state.charAt(ind + 1) == '*') {
                        tempResult = manyStep(tempResult, split(now));
                        state.delete(0, ind + 2);
                    } else {
                        tempResult = oneStep(tempResult, split(now));
                        state.delete(0, ind + 1);
                    }
                } else { // когда нет ветвления
                    ind = state.indexOf("(");
                    if (ind == -1)
                        ind = state.length();
                    String now = state.substring(0, ind);
                    state.delete(0, ind);
                    for (int i = 0; i < tempResult.size(); i++)
                        tempResult.set(i, tempResult.get(i) + now);
                }
            }
            newResult.addAll(tempResult);
        }
        return newResult;
    }

    private ArrayList<String> manyStep(ArrayList<String> previousResult, String[] options) {
        ArrayList<String> newResult = new ArrayList<>(previousResult);
        ArrayList<String> tempResult = new ArrayList<>(previousResult);
        while (true) {
            tempResult = oneStep(tempResult, options);
            if (tempResult.stream().allMatch(e -> e.length() > maxSize)) {
                return newResult.stream()
                        .filter(e -> e.length() <= maxSize)
                        .collect(Collectors.toCollection(ArrayList::new));
            }
            newResult.addAll(tempResult);
        }
    }

    private int indexCloseFor(String nowRegular) {
        int count = 1;
        char[] reg = nowRegular.toCharArray();
        for (int i = 1; i < nowRegular.length(); i++) {
            if (reg[i] == '(')
                count++;
            else if (reg[i] == ')') {
                count--;
                if (count == 0)
                    return i;
            }
        }
        return -1;
    }

    private String[] split(String s) {
        LinkedList<String> spliting = new LinkedList<>();
        int count = 0;
        int prev = 0;
        char[] reg = s.toCharArray();
        for (int i = 0; i < s.length(); i++) {
            if (reg[i] == '+') {
                if (count == 0) {
                    spliting.add(s.substring(prev, i));
                    prev = i + 1;
                }
            } else if (reg[i] == '(')
                count++;
            else if (reg[i] == ')') {
                count--;
            }
        }
        spliting.add(s.substring(prev));
        return spliting.toArray(String[]::new);
    }

    private int indexCloseFor(String nowRegular, int startIndex) {
        if (nowRegular.charAt(startIndex) != '(')
            throw new IllegalArgumentException("String must have '(' on current index");
        int count = 1;
        char[] reg = nowRegular.toCharArray();
        for (int i = 1 + startIndex; i < nowRegular.length(); i++) {
            if (reg[i] == '(')
                count++;
            else if (reg[i] == ')') {
                count--;
                if (count == 0)
                    return i;
            }
        }
        return -1;
    }
}
