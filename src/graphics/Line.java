package graphics;

import java.awt.*;

class Line implements Transition{
    private final Point startPoint, endPoint;
    private final String title;

    Line(Point startPoint, Point endPoint, String title) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.title = title;
    }

    @Override
    public void print(Graphics g){
        g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);

        // считаем угол наклона для отрисовки стрелочек
        int deltaX = Math.abs(startPoint.x - endPoint.x);
        double injection = Math.asin(Math.sqrt(Math.pow(deltaX, 2) + Math.pow(Math.abs(startPoint.y - endPoint.y), 2)));
        g.drawLine(endPoint.x, endPoint.y,
                (int)(endPoint.x + Math.cos(injection + 1) * 10), (int)(endPoint.y + Math.sin(injection + 1) * 10));
        g.drawLine(endPoint.x, endPoint.y,
                (int)(endPoint.x + Math.cos(injection - 1) * 10), (int)(endPoint.y + Math.sin(injection - 1) * 10));

        g.drawChars(title.toCharArray(), 0, title.length(), (endPoint.x - startPoint.x) / 3 + startPoint.x,
                (endPoint.y - startPoint.y) / 3 + startPoint.y - 10);
    }
}
