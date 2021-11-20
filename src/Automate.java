import java.util.*;
import java.util.stream.Collectors;

public class Automate {
    static class Rules{
        private final char terminal;
        private final String state;

        Rules(String s){
            s = s.trim();
            terminal = s.charAt(0);
            state = s.substring(1);
        }
    }

    private final Set<Character> terminal;
    private final Map<String, Set<Rules>> regulations = new HashMap<>();
    private final String startState;
    private final Set<String> endState;

    public Automate(Set<Character> terminal, Set<String> state, Set<String> stringRules, String startState,
                    Set<String> endState){
        this.terminal = terminal.stream().map(Character::toLowerCase).collect(Collectors.toSet());
        this.startState = startState;
        this.endState = endState;

        String[] t;
        String key;
        Rules value;
        for(String r: stringRules){
            t = r.split("->");
            key = t[0].trim();
            value = new Rules(t[1]);
            regulations.putIfAbsent(key, new HashSet<>());
            regulations.get(key).add(value);
            if(!state.contains(key))
                throw new IllegalArgumentException("Use not exist non terminal symbol: " + key);
            if(!this.terminal.contains(value.terminal) || !state.contains(value.state)) {
                throw new IllegalArgumentException("Incorrect rules: " + r);
            }
        }
    }

    public boolean canCreate(String lang, StringBuilder cause, List<String> logs){
        char[] language = lang.toCharArray();
        cause.delete(0, cause.length());
        logs.clear();
        for(char c: language){
            if(!terminal.contains(c)){
                cause.append("Language exist unacceptable symbol");
                return false;
            }
        }
        String nowState = startState;
        boolean wasSearch;
        for(int i = 0; i < language.length; i++){
            wasSearch = false;
            logs.add(nowState + ": " + lang.substring(i));
            for(Rules nowVal: regulations.get(nowState)){
                if(language[i] == nowVal.terminal){
                    nowState = nowVal.state;
                    wasSearch = true;
                    break;
                }
            }
            if(!wasSearch){
                cause.append("The final state is unattainable");
                return false;
            }
        }
        if(endState.contains(nowState)) {
            logs.add(nowState + ": ");
            return true;
        } else {
            cause.append("End of the chain, but the end state has not been reached");
            return false;
        }
    }
}