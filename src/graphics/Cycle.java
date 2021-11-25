package graphics;

import java.awt.*;

class Cycle implements Transition {
    private final Point point;
    private final String title;

    Cycle(Point point, String title) {
        this.point = point;
        this.title = title;
    }

    @Override
    public void print(Graphics g) {
        g.drawArc(point.x - 20, point.y - 20, 20, 20, 45, -270);
        g.drawChars(title.toCharArray(), 0, title.length(), point.x, point.y - 30);
    }
}
