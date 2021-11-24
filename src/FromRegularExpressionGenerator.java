import java.util.*;
import java.util.stream.Collectors;

public class FromRegularExpressionGenerator {
    private final String regular;
    private int maxSize;
    private int stateCount;
    private final Vector<Set<String>> preState = new Vector<>(); // для хранения состояний циклщейся подцепочки

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
                .sorted()
                .collect(Collectors.toList());
    }

    public AutomateDTO generateAutomate() {
        stateCount = 1;
        Set<Character> terminals = regular.chars()
                .filter(c -> Character.isDigit(c) || Character.isLowerCase(c))
                .mapToObj(i -> (char) i)
                .collect(Collectors.toSet());
        Map<String, Map<Character, String>> regulations = new TreeMap<>();
        Set<String> endStares = generateRegulations(regular, Collections.singleton("q0"), regulations, null, 0);
        Set<String> states = new TreeSet<>(regulations.keySet());
        regulations.values().forEach(value -> states.addAll(value.values()));
        String lastMultiplicity = regular.substring(indexOpenFor(regular, regular.lastIndexOf(')')) + 1,
                regular.lastIndexOf(')'));
        boolean onlyConcatenation = split(lastMultiplicity).length == 1;
        List<String> addedState = new ArrayList<>();
        String baseState = null;
        int partCount = (int) lastMultiplicity.chars().filter(c -> (char) c == '(').count();

        //добавление недостающих переходов
        if (regular.contains("*") && !regular.substring(regular.lastIndexOf('*')).contains("(")
                && !regular.substring(regular.lastIndexOf('*')).contains(")")) {
            String requiredSubstring = regular.substring(regular.lastIndexOf('*') + 1);
            if (requiredSubstring.length() > 0) {
                boolean startRequiredChain = true;
                List<String> lastStates = states.stream()
                        .sorted(Comparator.comparing(s -> s.chars().reduce(0, (res, now) -> res * 10 + now)))
                        .skip(states.size() - requiredSubstring.length() - 1)
                        .collect(Collectors.toList());
                lastStates.set(0, preState.get(0).iterator().next()); // начальное состояние обязательной цеочки совпадает с началом последних циклящийся скобок
                Set<Character> terminalUndoLastPart = lastMultiplicity.chars()
                        .filter(Character::isLowerCase)
                        .mapToObj(c -> (char)c)
                        .collect(Collectors.toSet());
                for (char t : terminalUndoLastPart) {
                    if (requiredSubstring.charAt(1) != t) {
                        if (requiredSubstring.charAt(0) == t) { // петля по первому состоянию
                            if (onlyConcatenation) {
                                regulations.get(lastStates.get(1)).put(t, "qa");
                                baseState = lastStates.get(1);
                                for (int i = 0; i < partCount - 2; i++) {
                                    addedState.add("q" + (char) ('a' + i));
                                    regulations.put("q" + (char) ('a' + i),
                                            new HashMap<>(regulations.get(lastStates.get(1))));
                                    regulations.get("q" + (char) ('a' + i)).put(t, "q" + (char) ('a' + i + 1));
                                }
                                addedState.add("q" + (char) ('a' + partCount - 2));
                                regulations.put("q" + (char) ('a' + partCount - 2),
                                        new HashMap<>(regulations.get(lastStates.get(1))));
                                regulations.get("q" + (char) ('a' + partCount - 2)).put(t, lastStates.get(1));
                            } else {
                                regulations.get(lastStates.get(1)).put(t, lastStates.get(1));
                            }
                        } else { // первое состояние конечной подцепочки переходит в нулевое
                            if (baseState != null) {
                                for (int i = 0; i < addedState.size(); i++) {
                                    regulations.get(addedState.get(i)).put(t,
                                            preState.get((i + 3) % preState.size()).iterator().next());
                                }
                            }
                            regulations.get(lastStates.get(1)).put(t, preState.get(2 % preState.size()).iterator().next());
                        }
                    }
                }
                if (requiredSubstring.charAt(0) != requiredSubstring.charAt(1))
                    startRequiredChain = false;
                for (int rsi = 1, lsi = 2; rsi < requiredSubstring.length(); rsi++, lsi++) { // rsi -- символ перехода в текущее правило, rsi + 1 -- символ перехода из текущего
                    if (!regulations.containsKey(lastStates.get(lsi))) {
                        regulations.put(lastStates.get(lsi), new HashMap<>());
                    }
                    if (requiredSubstring.charAt(rsi) != requiredSubstring.charAt(rsi - 1))
                        startRequiredChain = false;
                    for (char t : terminalUndoLastPart) {  // создаём переход по кажому символу
                        if ((requiredSubstring.length() == rsi + 1
                                || requiredSubstring.charAt(rsi + 1) != requiredSubstring.charAt(rsi))
                                && requiredSubstring.charAt(rsi) == t && startRequiredChain) {// для петли
                            if (onlyConcatenation) {
                                regulations.get(lastStates.get(lsi)).put(t, "qa");
                                baseState = lastStates.get(lsi);
                                for (int i = 0; i < partCount - 2; i++) {
                                    addedState.add("q" + (char) ('a' + i));
                                    regulations.put("q" + (char) ('a' + i),
                                            new HashMap<>(regulations.get(lastStates.get(lsi))));
                                    regulations.get("q" + (char) ('a' + i)).put(t, "q" + (char) ('a' + i + 1));
                                }
                                addedState.add("q" + (char) ('a' + partCount - 2));
                                regulations.put("q" + (char) ('a' + partCount - 2),
                                        new HashMap<>(regulations.get(lastStates.get(lsi))));
                                regulations.get("q" + (char) ('a' + partCount - 2)).put(t, lastStates.get(lsi));
                            } else {
                                regulations.get(lastStates.get(lsi)).put(t, lastStates.get(lsi));
                            }
                        } else {
                            boolean b = requiredSubstring.length() == rsi + 1 || requiredSubstring.charAt(rsi + 1) != t;
                            if (b && requiredSubstring.charAt(0) == t) { // для перехода в начальное состояние обязательной подцепочки
                                regulations.get(lastStates.get(lsi)).put(t, lastStates.get(1));
                            } else if (b) { // для перехода в последнюю цклящюся скобку
                                if (baseState != null && baseState.equals(lastStates.get(lsi))) {
                                    for (int i = 0; i < addedState.size(); i++) {
                                        regulations.get(addedState.get(i)).put(t,
                                                preState.get((i + 3 + rsi) % preState.size()).iterator().next());
                                    }
                                }
                                regulations.get(lastStates.get(lsi)).put(t, preState.get((rsi + 2) % preState.size()).iterator().next());
                            }
                        }
                    }
                }
            }
        }

        states.addAll(addedState);
        return new AutomateDTO(terminals, states, "q0", endStares, regulations);
    }

    private Set<String> generateRegulations(String nowString, Set<String> nowState,
                                            Map<String, Map<Character, String>> regulations, Set<String> inState,
                                            int external) {
        String[] splittingString = split(nowString);
        if (external == 1 && inState != null && !inState.isEmpty()) { // если встечаем более поздние повторяющееся подвыражения
            preState.clear();
            preState.add(nowState);
        }
        if (splittingString.length == 1) {
            String nextState;
            int indexCloseFor;
            boolean isMultiplicity;
            for (int i = 0; i < nowString.length(); i++) {
                if (nowString.charAt(i) == '(') { // обработка конкатинации выражения
                    if (external == 1 && inState != null && !inState.isEmpty() && i != 0) { // если встечаем более поздние повторяющееся подвыражения
                        preState.add(nowState);
                    }
                    indexCloseFor = indexCloseFor(nowString, i);
                    isMultiplicity = indexCloseFor + 1 < nowString.length()
                            && nowString.charAt(indexCloseFor + 1) == '*';
                    if (isMultiplicity) {
                        generateRegulations(searchNextBlock(nowString, i), new TreeSet<>(nowState), regulations, // если скобки повторяющиеся, не меняем текущее состояние
                                calculateReturnStates(true, nowState,
                                        (indexCloseFor + 2 == nowString.length() ? inState : null)), external + 1); // не null только если последнее выражение подстроки и оно должно выходить в начало
                        i = indexCloseFor + 1;
                    } else {
                        nowState = generateRegulations(searchNextBlock(nowString, i), new TreeSet<>(nowState), regulations,
                                calculateReturnStates(false, nowState,
                                        (indexCloseFor + 1 == nowString.length() ? inState : null)), external + 1);
                        i = indexCloseFor;
                    }
                } else { // обработка конкатинации терминалов
                    nextState = "q" + stateCount++;
                    for (String ns : nowState) {
                        if (!regulations.containsKey(ns))
                            regulations.put(ns, new HashMap<>());
                        if (i == nowString.length() - 1 && inState != null && !inState.isEmpty()) {
                            for (String is : inState) {
                                regulations.get(ns).put(nowString.charAt(i), is);
                            }
                        } else {
                            regulations.get(ns).put(nowString.charAt(i), nextState);
                        }
                    }
                    nowState = Collections.singleton(nextState);
                }
            }
            return nowState;
        } else { // обработка логического сложения
            Set<String> result = new HashSet<>();
            for (String s : splittingString) {
                result.addAll(generateRegulations(s, new HashSet<>(nowState), regulations,
                        (inState == null ? null : new HashSet<>(inState)), external));
            }
            return result;
        }
    }

    private Set<String> calculateReturnStates(boolean isMultiplicity, Set<String> nowState, Set<String> inState) {
        Set<String> result = new HashSet<>();
        if (inState != null)
            result.addAll(inState);
        if (isMultiplicity)
            result.addAll(nowState);
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
                if (--count == 0)
                    return i;
            }
        }
        return -1;
    }

    private int indexOpenFor(String nowRegular, int index) {
        int count = 1;
        char[] reg = nowRegular.toCharArray();
        for (int i = index - 1; i >= 0; i--) {
            if (reg[i] == ')') {
                count++;
            } else if (reg[i] == '(') {
                if (--count == 0) {
                    return i;
                }
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
