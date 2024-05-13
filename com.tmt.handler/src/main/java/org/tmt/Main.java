package org.tmt;
/*
Author: Vivek Vijayan
Date: May 11, 2024
GitHub Repository: github.com/vivek-vijayan/tmt

Description:
    This Java application is a Task Manager Tool (TMT) created by Vivek Vijayan. It provides a user-friendly interface
    for monitoring and managing running processes on the system. The application is built using Java and Java Swing,
    making it cross-platform and easy to use.

Usage:
    To use the TMT tool, simply run this application. It will display a window with a list of running processes,
    including their Process ID (PID), name, CPU percentage, and memory percentage.

Dependencies:
    - Java
    - Java Swing

License:
    This script is licensed under the MIT License. See the LICENSE file in the GitHub repository for more details.

GitHub Repository:
    github.com/vivek-vijayan/tmt
*/

import org.w3c.dom.css.RGBColor;

import javax.swing.*;
import javax.swing.border.Border;
import java.io.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import java.util.*;
import java.awt.*;
import java.util.List;



public class Main {
    public static void listRunningApps(DefaultTableModel model) {
        try {
            Vector<Vector<String>> data = new Vector<>();
            Vector<String> columnNames = new Vector<>();
            columnNames.addElement("Name");
            columnNames.addElement("PID");

            ProcessBuilder processBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").startsWith("Windows")) {
                processBuilder.command("tasklist", "/fo", "csv", "/nh");
            } else {
                processBuilder.command("ps", "-e", "-o", "pid,args");
            }
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    Vector<String> row = new Vector<>();
                    row.addElement(parts[0].replaceAll("\"", ""));
                    row.addElement(parts[1].trim());
                    data.addElement(row);
                }
            }

            process.waitFor();

            // Update model without flickering
            model.setDataVector(data, columnNames);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, FontFormatException {
        SwingUtilities.invokeLater(() -> {
            try {
                startGUI();
            } catch (IOException | FontFormatException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public static void startGUI() throws IOException, FontFormatException {
        JFrame mainframe = new JFrame("Task Manager Tool");
        mainframe.setLayout(new BorderLayout());
        mainframe.setSize(800,600);
        mainframe.setResizable(false);
        mainframe.setMinimumSize(new Dimension(800,600));

        // Loading Font
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        InputStream is = Main.class.getResourceAsStream("/fonts/Heebo.ttf");
        assert is != null;
        ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));

        // Usage Panel Creation
        JPanel usagePanel = new JPanel();
        int gw = 400;
        int gh = 100;
        usagePanel.setLayout(new GridLayout());
        usagePanel.setSize(800,100);
        usagePanel.setPreferredSize(new Dimension(800,100));
        usagePanel.setMinimumSize(new Dimension(gw,gh));

        // CPU Usage graph area
        List<Integer> cpu_usage_data = List.of(0);
        List<Integer> ram_usage_data = List.of(0);
        LinearGraph cpu_usage_graph = new LinearGraph(cpu_usage_data,  gw, gh);
        usagePanel.add(cpu_usage_graph);

        // CPU stat area
        JPanel cpu_status_panel = new JPanel();
        cpu_status_panel.setLayout (new BoxLayout (cpu_status_panel, BoxLayout.Y_AXIS));
        cpu_status_panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        cpu_status_panel.setBackground(new Color(0 ,31, 0));

        JLabel cpu_percent = new JLabel("Loading...");
        cpu_percent.setFont(new Font("Heebo",Font.PLAIN, 15));
        cpu_percent.setForeground(Color.GREEN);

        cpu_percent.setSize(100,gh);
        cpu_status_panel.add(cpu_percent);

        JLabel heap_memory = new JLabel("Loading...");
        heap_memory.setForeground(new Color(0 ,227 ,245));
        heap_memory.setBorder(new EmptyBorder(10, 0, 10, 10));
        heap_memory.setFont(new Font("Jersey 15",Font.PLAIN, 10));
        heap_memory.setSize(100,gh);
        cpu_status_panel.add(heap_memory);

        JLabel non_heap_memory = new JLabel("Loading...");
        non_heap_memory.setForeground(new Color(0 ,227 ,245));
        non_heap_memory.setBorder(new EmptyBorder(0, 0, 10, 10));
        non_heap_memory.setSize(100,gh);
        non_heap_memory.setFont(new Font("Jersey 15",Font.PLAIN, 10));
        cpu_status_panel.add(non_heap_memory);

        // Running process list
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Name");
        model.addColumn("PID");

        JTable table = new JTable(model);
        // Set custom cell renderer to change background and foreground colors
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        table.setBorder(null);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800,600));
        scrollPane.setBorder(null);
        mainframe.add(scrollPane, BorderLayout.CENTER);

        listRunningApps(model);


        usagePanel.add(cpu_status_panel);
        mainframe.add(usagePanel, BorderLayout.NORTH);
        Thread cpu_usage_handler = new Thread(() -> {
            List<Integer> cpu_usage_data_ = new ArrayList<>(List.of(0));
            try {
                while(true) {
                    OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

                    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
                    MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
                    long usedHeapMemory = heapMemoryUsage.getUsed();
                    long maxHeapMemory = heapMemoryUsage.getMax();

                    MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
                    long usedNonHeapMemory = nonHeapMemoryUsage.getUsed();
                    long maxNonHeapMemory = nonHeapMemoryUsage.getMax();

                    heap_memory.setText("Memory used : " + usedHeapMemory / (1024 * 1024) + " MB" + " / " + maxHeapMemory / (1024 * 1024) + " MB" );
                    non_heap_memory.setText("NON HEAP : Used Memory: " + usedNonHeapMemory / (1024 * 1024) + " MB / " + " Max Memory : " + maxNonHeapMemory / (1024 * 1024) + " MB");

                    double cpuUsage = osBean.getSystemLoadAverage() * 10;
                    //ImageIcon icon2 = new ImageIcon(Objects.requireNonNull(Main.class.getClassLoader().getResource("sample.jpg")));
                    OperatingSystemMXBean sa = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                    double cpuTemperature = sa.getSystemLoadAverage();

                    String text = "CPU Utilization : " +  Math.min(Math.round(cpuUsage),100) + "%";

                    if(cpu_usage_data_.size() > 100) {
                        cpu_usage_data_.remove(0);
                    }

                    // Adding the data to the List  - CPU usage
                    cpu_usage_data_.add((int)Math.min(cpuUsage,99));

                    // Updating the content in the process table
                    listRunningApps(model);

                    // Create the line graph panel
                    LinearGraph updated_graph = new LinearGraph(cpu_usage_data_,  gw, gh);
                    usagePanel.removeAll();
                    usagePanel.setBounds(0,0,800,100);
                    usagePanel.add(updated_graph);
                    cpu_percent.setText(text);

                    usagePanel.add(cpu_status_panel);
                    usagePanel.setBackground(new Color(0 ,31, 0));
                    usagePanel.setSize(gw,gh);
                    usagePanel.setMinimumSize(new Dimension(gw,gh));
                    usagePanel.revalidate();

                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                // Handle interrupted exception
                System.out.println(e.toString());
            }
        });

        cpu_usage_handler.start();

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        List<OperatingSystemMXBean> list = ManagementFactory.getPlatformMXBeans(OperatingSystemMXBean.class);

        for (OperatingSystemMXBean os : list) {
            System.out.println("Name: " + os.getName());
            System.out.println("Arch: " + os.getArch());
            System.out.println("Version: " + os.getVersion());
            System.out.println("Available processors: " + os.getAvailableProcessors());
            System.out.println("System load average: " + os.getSystemLoadAverage());
        }

        // setting the window size

        mainframe.setVisible(true);
        mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    // Custom TableCellRenderer to set background and foreground colors and cell borders
    static class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent cellComponent = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cellComponent.setBackground(new Color(0, 31, 0)); // Set background color
            cellComponent.setForeground(Color.GREEN); // Set foreground color
            cellComponent.setBorder(new EmptyBorder(5,5,5,5));

            // Set cell border color
            if (isSelected) {
                cellComponent.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            } else {
                cellComponent.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0, 81, 0))); // Set border color
            }
            return cellComponent;
        }
    }
}