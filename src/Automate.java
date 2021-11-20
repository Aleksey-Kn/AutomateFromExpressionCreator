import java.util.*;
import java.util.stream.Collectors;

public class Automate {
    private final Set<Character> terminal;
    private final Map<String, Map<Character, String>> regulations = new HashMap<>();
    private final String startState;
    private final Set<String> endState;

    public Automate(Set<Character> terminal, Set<String> state, Set<String[]> rules, String startState,
                    Set<String> endState) {
        this.terminal = terminal.stream().map(Character::toLowerCase).collect(Collectors.toSet());
        this.startState = startState;
        this.endState = endState;

        for (String[] r : rules) {
            if (r[1].length() > 1)
                throw new IllegalArgumentException("Terminal symbol in rules must be one symbol");
            if (!state.contains(r[0]))
                throw new IllegalArgumentException("Use not exist state: " + r[0]);
            if (!this.terminal.contains(r[1].charAt(0)))
                throw new IllegalArgumentException("Use not exist terminal: " + r[1]);
            if (!state.contains(r[2])) {
                throw new IllegalArgumentException("Use not exist state: " + r[2]);
            }
            if (!regulations.containsKey(r[0]))
                regulations.put(r[0], new HashMap<>());
            regulations.get(r[0]).put(r[1].charAt(0), r[2]);
        }
    }

    public boolean canCreate(String lang, StringBuilder cause, List<String> logs) {
        char[] language = lang.toCharArray();
        cause.delete(0, cause.length());
        logs.clear();
        for (char c : language) {
            if (!terminal.contains(c)) {
                cause.append("Language exist unacceptable symbol");
                return false;
            }
        }
        String nowState = startState;
        for (int i = 0; i < language.length; i++) {
            logs.add(nowState + ": " + lang.substring(i));
            if (regulations.containsKey(nowState) && regulations.get(nowState).containsKey(language[i])) {
                nowState = regulations.get(nowState).get(language[i]);
            } else {
                cause.append("The final state is unattainable");
                return false;
            }
        }
        if (endState.contains(nowState)) {
            logs.add(nowState + ": ");
            return true;
        } else {
            cause.append("End of the chain, but the end state has not been reached");
            return false;
        }
    }
}