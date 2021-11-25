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
        if(endPoint.x > startPoint.x){
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

        g.drawChars(title.toCharArray(), 0, title.length(), (endPoint.x - startPoint.x) / 3 + startPoint.x,
                (endPoint.y - startPoint.y) / 3 + startPoint.y - 10);
    }
}
