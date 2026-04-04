package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.ReportsService;
import model.User;
import security.AuditService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;

@SuppressWarnings("unchecked")  // ADD THIS TO SUPPRESS UNCHECKED WARNINGS
public class ReportsController {

    @FXML private DatePicker dailyDate;
    @FXML private TextArea dailyReportArea;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private TextArea revenueReportArea;

    private ReportsService service;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        service = new ReportsService();
        dailyDate.setValue(LocalDate.now());
        startDate.setValue(LocalDate.now().minusDays(7));
        endDate.setValue(LocalDate.now());
    }

    public void generateDailyReport() {
        try {
            Date date = Date.valueOf(dailyDate.getValue());
            Map<String, Object> report = service.getDailyReport(date);

            StringBuilder sb = new StringBuilder();
            sb.append("=== DAILY REPORT FOR ").append(date).append(" ===\n\n");
            sb.append("Arrivals Today: ").append(report.get("arrivals")).append("\n");
            sb.append("Departures Today: ").append(report.get("departures")).append("\n");
            sb.append("In-House Guests: ").append(report.get("in_house")).append("\n");
            sb.append(String.format("Occupancy Rate: %.1f%%\n", report.get("occupancy_rate")));
            sb.append(String.format("Revenue: $%.2f\n", report.get("revenue")));

            dailyReportArea.setText(sb.toString());

            if (currentUser != null) {
                AuditService.log(currentUser.getId(), "Generated daily report for " + date);
            }

        } catch (Exception e) {
            dailyReportArea.setText("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void generateRevenueReport() {
        try {
            Date start = Date.valueOf(startDate.getValue());
            Date end = Date.valueOf(endDate.getValue());
            Map<String, Object> report = service.getRevenueReport(start, end);

            StringBuilder sb = new StringBuilder();
            sb.append("=== REVENUE REPORT (").append(start).append(" to ").append(end).append(") ===\n\n");
            sb.append(String.format("Total Revenue: $%.2f\n\n", report.get("total_revenue")));
            sb.append("Daily Breakdown:\n");
            sb.append("------------------------\n");

            Map<Date, Double> dailyRevenue = (Map<Date, Double>) report.get("daily_revenue");
            for (Map.Entry<Date, Double> entry : dailyRevenue.entrySet()) {
                sb.append(String.format("%s: $%.2f\n", entry.getKey(), entry.getValue()));
            }

            revenueReportArea.setText(sb.toString());

            if (currentUser != null) {
                AuditService.log(currentUser.getId(), "Generated revenue report from " + start + " to " + end);
            }

        } catch (Exception e) {
            revenueReportArea.setText("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}