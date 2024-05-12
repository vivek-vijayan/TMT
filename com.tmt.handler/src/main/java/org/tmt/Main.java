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
import java.io.BufferedReader;
import javax.swing.border.EmptyBorder;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.awt.*;

class ProcessHandler extends JOptionPane {
    ProcessHandler() throws IOException, InterruptedException {
        System.out.println("Process handler initiated");
        // Execute command
        Process process = Runtime.getRuntime().exec(detect_operating_system());
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        reader.close();
        process.waitFor();
    }

    String detect_operating_system() {
        String command = "";
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")) {
            command = "tasklist";
        } else if (os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            command = "ps -e";
        } else {
            System.out.println("No supporting operating system found, quitting the application");
            showMessageDialog(this, "No supporting operating system found, quitting the application",
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        return command;
    }
}

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        JFrame mainframe = new JFrame("Task Manager Tool");
        mainframe.setLayout(new BorderLayout());
        mainframe.setSize(800,600);
        mainframe.setResizable(false);
        mainframe.setMinimumSize(new Dimension(800,600));
        /*
        Application Design of main frame
        NORTH :
            Usage Panel (GRID layout)
                CPU Usage graph
                CPU details
                RAM Usage Graph
                RAM details
         */
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
        cpu_percent.setForeground(Color.GREEN);

        cpu_percent.setSize(100,gh);
        cpu_status_panel.add(cpu_percent);

        JLabel heap_memory = new JLabel("Loading...");
        heap_memory.setForeground(new Color(0 ,227 ,245));
        heap_memory.setBorder(new EmptyBorder(10, 0, 10, 10));
        heap_memory.setSize(100,gh);
        cpu_status_panel.add(heap_memory);

        JLabel non_heap_memory = new JLabel("Loading...");
        non_heap_memory.setForeground(new Color(0 ,227 ,245));
        non_heap_memory.setBorder(new EmptyBorder(0, 0, 10, 10));
        non_heap_memory.setSize(100,gh);
        cpu_status_panel.add(non_heap_memory);


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

                    heap_memory.setText("HEAP : Used Memory: " + usedHeapMemory / (1024 * 1024) + " MB" + " / Max Memory : " + maxHeapMemory / (1024 * 1024) + " MB" );
                    non_heap_memory.setText("NON HEAP : Used Memory: " + usedNonHeapMemory / (1024 * 1024) + " MB / " + " Max Memory : " + maxNonHeapMemory / (1024 * 1024) + " MB");

                    double cpuUsage = osBean.getSystemLoadAverage() * 10;
                    ImageIcon icon2 = new ImageIcon(Objects.requireNonNull(Main.class.getClassLoader().getResource("sample.jpg")));
                    OperatingSystemMXBean sa = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                    double cpuTemperature = sa.getSystemLoadAverage();

                    String text = "<html><b>CPU Utilization : " +  Math.min(Math.round(cpuUsage),100) + "% </b></html>";

                    if(cpu_usage_data_.size() > 100) {
                        cpu_usage_data_.remove(0);
                    }

                    // Adding the data to the List  - CPU usage
                    cpu_usage_data_.add((int)Math.min(cpuUsage,99));

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

        // setting the window size

        mainframe.setVisible(true);
        mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
}