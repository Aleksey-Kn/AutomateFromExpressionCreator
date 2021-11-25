package graphics;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Graph extends JFrame {
    private final Map<String, Circle> circleMap = new HashMap<>();
    private final Stack<Transition> transitions = new Stack<>();

    public Graph(Set<String[]> regulations){
        super("Graph");
        setBounds(200, 10, 800, 800);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Set<String> allState = new HashSet<>();
        for (String[] r : regulations){
            allState.add(r[0]);
            allState.add(r[2]);
        }

        // инициализация состояний
        int circleIndex = 0;
        double radiansIncrement = Math.PI * 2 / allState.size();
        for(String state: allState){
            circleMap.put(state, new Circle(state,
                    new Point((int)(Math.cos(circleIndex * radiansIncrement) * 300 + 400),
                            (int)(Math.sin(circleIndex * radiansIncrement) * 300 + 400))));
            circleIndex++;
        }

        for(String[] rules: regulations){
            if(rules[0].equals(rules[2])){
                transitions.add(new Cycle(circleMap.get(rules[0]).getOutPoint(), rules[1]));
            } else{
                transitions.add(
                        new Line(circleMap.get(rules[0]).getOutPoint(), circleMap.get(rules[2]).getInPoint(), rules[1]));
            }
        }

        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.clearRect(0, 0, getWidth(), getHeight());
        circleMap.values().forEach(c -> c.print(g));
        transitions.forEach(t -> t.print(g));
    }
}
