package com.main.lms.utility;

import com.main.lms.dtos.StudentPerformanceDTO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChartUtility {

    public void generateBarChart(String title, String xAxisLabel, String yAxisLabel, Map<String, Double> data, String filename) throws IOException {
        // Create the dataset
        CategoryDataset dataset = createCategoryDataset(data);
        JFreeChart barChart = null;
        try {
            // Create the chart
            barChart = ChartFactory.createBarChart(
                    title,
                    xAxisLabel,
                    yAxisLabel,
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, true, false);
        }catch (Exception e){
            System.out.println("Error in creating chart");
        }

        // Save the chart as an image file
        String filePath = "charts/" + filename;
        File chartFile = new File(filePath);
        ChartUtils.saveChartAsPNG(chartFile, barChart, 800, 600);
    }

    public void generatePieChart(String title, Map<String, Double> data, String filename) throws IOException {
        // Create the dataset
        DefaultPieDataset dataset = createPieDataset(data);

        // Create the chart
        JFreeChart pieChart = ChartFactory.createPieChart(
                title,
                dataset,
                true, true, false);

        // Save the chart as an image file
        String filePath = "charts/" + filename;
        File chartFile = new File(filePath);
        ChartUtils.saveChartAsPNG(chartFile, pieChart, 800, 600);
    }

    public void generateCompletionChart(String title, List<StudentPerformanceDTO> dataList, String filename) throws IOException {
        // Prepare data
        Map<String, Integer> completionData = dataList.stream()
                .collect(Collectors.toMap(
                        StudentPerformanceDTO::getStudentName,
                        dto -> dto.getIsCourseCompleted() ? 1 : 0
                ));

        // Create the dataset
        CategoryDataset dataset = createCategoryDatasetFromCompletionData(completionData);

        // Create the chart
        JFreeChart barChart = ChartFactory.createBarChart(
                title,
                "Students",
                "Completion Status",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        // Customize the chart (e.g., setting the range to 0-1 for completion status)
        barChart.getCategoryPlot().getRangeAxis().setRange(0.0, 1.0);

        // Save the chart as an image file
        String filePath = "charts/" + filename;
        File chartFile = new File(filePath);
        ChartUtils.saveChartAsPNG(chartFile, barChart, 800, 600);
    }

    // Helper method to create CategoryDataset from data
    private CategoryDataset createCategoryDataset(Map<String, Double> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series = "Score";
        data.forEach((studentName, value) -> dataset.addValue(value, series, studentName));
        return dataset;
    }

    // Helper method to create PieDataset from data
    private DefaultPieDataset createPieDataset(Map<String, Double> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        data.forEach(dataset::setValue);
        return dataset;
    }

    // Helper method to create CategoryDataset for completion data
    private CategoryDataset createCategoryDatasetFromCompletionData(Map<String, Integer> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series = "Completion";
        data.forEach((studentName, value) -> dataset.addValue(value, series, studentName));
        return dataset;
    }
}