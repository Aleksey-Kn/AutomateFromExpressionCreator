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
        g.drawArc(point.x - 12, point.y - 25, 30, 25, -100, 200);
        g.drawChars(title.toCharArray(), 0, title.length(), point.x + 10, point.y - 40);
    }
}
