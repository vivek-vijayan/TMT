package org.tmt;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LinearGraph extends JPanel {

    private final List<Integer> cpu_usage_data;
    private final int width;
    private final int height;

    public LinearGraph(List<Integer> cpu_usage, int width, int height) {
        this.cpu_usage_data = cpu_usage;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        // Draw graph paper background
        drawGraphPaper(g2d);

        // Set drawing color for the graph
        g2d.setColor(Color.GREEN);
        setBackground(new Color(0,30,0));

        // Calculate scaling factors
        double maxX = cpu_usage_data.size() - 1;
        double maxY = 100;

        double scaleX = width / maxX;
        double scaleY = height / maxY;

        // Draw lines connecting data points
        for (int i = 0; i < cpu_usage_data.size() - 1; i++) {
            int x1 = (int) (i * scaleX);
            int y1 = (int) ((maxY - cpu_usage_data.get(i)) * scaleY);
            int x2 = (int) ((i + 1) * scaleX);
            int y2 = (int) ((maxY - cpu_usage_data.get(i + 1)) * scaleY);
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.dispose();
    }

    private void drawGraphPaper(Graphics2D g2d) {
        // Set the color for the grid lines
        g2d.setColor(new Color(0,81,0));

        // Draw vertical grid lines
        int stepX = width / 10; // Number of vertical lines
        for (int i = 0; i <= 10; i++) {
            int x = i * stepX;
            g2d.drawLine(x, 0, x, height);
        }

        // Draw horizontal grid lines
        int stepY = height / 10; // Number of horizontal lines
        for (int i = 0; i <= 10; i++) {
            int y = i * stepY;
            g2d.drawLine(0, y, width, y);
        }
    }

    private int getMax(List<Integer> data) {
        int max = Integer.MIN_VALUE;
        for (int value : data) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
