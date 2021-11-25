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
        int deltaY = startPoint.y - endPoint.y;
        double injection = Math
                .asin(deltaY / Math.sqrt(Math.pow(Math.abs(startPoint.x - endPoint.x), 2) + Math.pow(Math.abs(deltaY), 2)));
        if(endPoint.x > startPoint.x){ // инверсия по оси y
            if(injection < 0){
                injection = Math.toRadians(-90) - (injection - Math.toRadians(-90));
            } else{
                injection = Math.toRadians(90) + (Math.toRadians(90) - injection);
            }
        }
        g.drawLine(endPoint.x, endPoint.y,
                (int)(endPoint.x + Math.cos(injection + 0.3) * 15), (int)(endPoint.y + Math.sin(injection + 0.3) * 15));
        g.drawLine(endPoint.x, endPoint.y,
                (int)(endPoint.x + Math.cos(injection - 0.3) * 15), (int)(endPoint.y + Math.sin(injection - 0.3) * 15));

        double titleRandomDelta = Math.random() / 3 + 0.1;
        g.drawChars(title.toCharArray(), 0, title.length(),
                (int)((endPoint.x - startPoint.x) * titleRandomDelta + startPoint.x),
                (int)((endPoint.y - startPoint.y) * titleRandomDelta + startPoint.y - 10));
    }
}
