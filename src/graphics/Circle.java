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
        inPoint = new Point(position.x + 20, position.y + 20);
        outPoint = new Point(position.x + 10, position.y + 10);
    }

    void print(Graphics g){
        g.drawOval(position.x, position.y, 30, 30);
        g.drawChars(name.toCharArray(), 0, name.length(), position.x + 10, position.y + 12);
    }

    public Point getInPoint() {
        return inPoint;
    }

    public Point getOutPoint() {
        return outPoint;
    }
}
