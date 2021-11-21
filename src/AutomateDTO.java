import java.util.Map;
import java.util.Set;

public class AutomateDTO {
    private final Set<Character> terminals;
    private final Set<String> states;
    private final String startState;
    private final Set<String> endStares;
    private final Map<String, Map<Character, String>> regulations;

    public AutomateDTO(Set<Character> terminals, Set<String> states, String startState, Set<String> endStares,
                       Map<String, Map<Character, String>> regulations) {
        this.terminals = terminals;
        this.states = states;
        this.startState = startState;
        this.endStares = endStares;
        this.regulations = regulations;
    }

    public Set<Character> getTerminals() {
        return terminals;
    }

    public Set<String> getStates() {
        return states;
    }

    public String getStartState() {
        return startState;
    }

    public Set<String> getEndStares() {
        return endStares;
    }

    public Map<String, Map<Character, String>> getRegulations() {
        return regulations;
    }
}
