package com.hospital.utils;

import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

/**
 * Utility class for chart styling and configuration across the application.
 * Provides consistent colors and styling for charts used in different dashboards.
 */
public class ChartUtils {
    
    // Define consistent colors for urgency levels
    private static final String EMERGENCY_COLOR = "#ff3333"; // Red
    private static final String HIGH_COLOR = "#ff9900";      // Orange
    private static final String MEDIUM_COLOR = "#ffcc00";    // Yellow
    private static final String LOW_COLOR = "#66cc66";       // Green
    
    /**
     * Applies consistent styling to treatment urgency chart data.
     * Sets specific colors for each urgency level (Emergency, High, Medium, Low).
     * 
     * @param series The XYChart.Series to style
     */
    public static void styleUrgencyChart(XYChart.Series<String, Number> series) {
        if (series == null || series.getData() == null) return;
        
        for (XYChart.Data<String, Number> data : series.getData()) {
            String urgencyLevel = data.getXValue();
            String color;
            
            switch (urgencyLevel) {
                case "Emergency":
                    color = EMERGENCY_COLOR;
                    break;
                case "High":
                    color = HIGH_COLOR;
                    break;
                case "Medium":
                    color = MEDIUM_COLOR;
                    break;
                case "Low":
                    color = LOW_COLOR;
                    break;
                default:
                    color = "#999999"; // Default gray
                    break;
            }
            
            // Apply the color immediately if node exists
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-bar-fill: " + color + ";");
            }
            
            // Also set up a listener for when the node is created/changed
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + color + ";");
                }
            });
        }
    }
    
    /**
     * Applies CSS styling directly to the chart to ensure colors are applied correctly.
     * This is an alternative approach that can be used if the node property approach doesn't work.
     * 
     * @param chart The BarChart to style
     */
    public static void applyUrgencyChartCSS(BarChart<String, Number> chart) {
        if (chart == null || chart.getData() == null || chart.getData().isEmpty()) return;
        
        // Add CSS style classes to the chart
        chart.getStylesheets().add(ChartUtils.class.getResource("/com/hospital/chart-styles.css").toExternalForm());
        
        // Apply specific styles to each bar based on its category
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                String urgencyLevel = data.getXValue();
                
                // Apply appropriate style class
                switch (urgencyLevel) {
                    case "Emergency":
                        data.getNode().getStyleClass().add("emergency-bar");
                        break;
                    case "High":
                        data.getNode().getStyleClass().add("high-bar");
                        break;
                    case "Medium":
                        data.getNode().getStyleClass().add("medium-bar");
                        break;
                    case "Low":
                        data.getNode().getStyleClass().add("low-bar");
                        break;
                    default:
                        break;
                }
            }
        }
    }
} 