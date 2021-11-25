package graphics;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Graph extends JFrame {
    private final Map<String, Circle> circleMap = new HashMap<>();
    private final Stack<Transition> transitions = new Stack<>();

    public Graph(Set<String[]> demoRegulations){
        super("Graph");
        setBounds(300, 200, 600, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Set<String> allState = new HashSet<>();
        for (String[] r : demoRegulations){
            allState.add(r[0]);
            allState.add(r[2]);
        }

        // инициализация состояний
        int circleIndex = 0;
        double radiansIncrement = Math.PI * 2 / allState.size();
        for(String state: allState){
            circleMap.put(state, new Circle(state,
                    new Point((int)(Math.cos(circleIndex * radiansIncrement) * 450),
                            (int)(Math.sin(circleIndex * radiansIncrement) * 450))));
            circleIndex++;
        }

        //TODO: инициализация переходов

        repaint();
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        circleMap.values().forEach(c -> c.print(g));
    }
}
