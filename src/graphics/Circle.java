package graphics;

import java.awt.*;

class Circle {
    private final String name;
    private final Point position;
    private final Point inPoint;
    private final Point outPoint;

    Circle(String name, Point position){
        this.name = name;
        this.position = position;
        inPoint = new Point(position.x + 5, position.y + 5);
        outPoint = new Point(position.x + 30, position.y + 30);
    }

    void print(Graphics g){
        g.drawOval(position.x, position.y, 40, 40);
        g.setColor(Color.BLUE);
        g.drawChars(name.toCharArray(), 0, name.length(), position.x + 15, position.y + 15);
        g.setColor(Color.BLACK);
    }

    public Point getInPoint() {
        return inPoint;
    }

    public Point getOutPoint() {
        return outPoint;
    }
}
