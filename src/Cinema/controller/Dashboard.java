package Cinema.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import Cinema.database.mysqlconnect;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.Node;

public class Dashboard implements Initializable {

    @FXML
    private ComboBox<String> filterTypeCombo, monthCombo, yearComboForMonth, yearCombo, dataTypeCombo;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Button refreshButton;
    @FXML
    private PieChart pieChart;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private Label pieChartTitle, barChartTitle, totalLabel; // Thêm totalLabel

    private static final String DB_URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private FadeTransition currentPieFadeOut, currentPieFadeIn;
    private FadeTransition currentBarFadeOut, currentBarFadeIn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filterTypeCombo.setItems(FXCollections.observableArrayList("Theo ngày", "Theo tháng/năm", "Theo năm"));
        monthCombo.setItems(FXCollections.observableArrayList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"));
        yearComboForMonth.setItems(FXCollections.observableArrayList("2023", "2024", "2025"));
        yearCombo.setItems(FXCollections.observableArrayList("2023", "2024", "2025"));
        dataTypeCombo.setItems(FXCollections.observableArrayList("Số vé bán được", "Doanh thu"));

        filterTypeCombo.setOnAction(event -> updateFilterVisibility());

        filterTypeCombo.getSelectionModel().select("Theo ngày");
        dataTypeCombo.getSelectionModel().select("Số vé bán được");
        datePicker.setValue(LocalDate.now());

        updateFilterVisibility();
        yAxis.setAutoRanging(false);

        customizeCategoryAxis();

        refreshCharts();
        dataTypeCombo.setOnAction(event -> refreshCharts());
        refreshButton.setOnAction(event -> refreshCharts());
    }

    private void updateFilterVisibility() {
        String filterType = filterTypeCombo.getValue();
        datePicker.setVisible("Theo ngày".equals(filterType));
        datePicker.setManaged("Theo ngày".equals(filterType));
        monthCombo.setVisible("Theo tháng/năm".equals(filterType));
        monthCombo.setManaged("Theo tháng/năm".equals(filterType));
        yearComboForMonth.setVisible("Theo tháng/năm".equals(filterType));
        yearComboForMonth.setManaged("Theo tháng/năm".equals(filterType));
        yearCombo.setVisible("Theo năm".equals(filterType));
        yearCombo.setManaged("Theo năm".equals(filterType));
    }

    private void customizeCategoryAxis() {
        xAxis.setTickLabelRotation(0);
        xAxis.setTickLabelGap(10);
        xAxis.setTickLabelFill(javafx.scene.paint.Color.BLACK);
        xAxis.setTickLabelFont(javafx.scene.text.Font.font(10));
    }

    private String wrapMovieName(String movieName) {
        return movieName.replaceAll("(.{21})", "$1\n");
    }

    private void setupYAxis(double maxValue, boolean isTicketCount) {
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);

        if (isTicketCount) {
            int maxTickets = (int) Math.ceil(maxValue);
            int upperBound = maxTickets + (int) (maxTickets * 0.2);
            upperBound = Math.max(upperBound, 5);
            yAxis.setUpperBound(upperBound);
            yAxis.setTickUnit(Math.max(1, upperBound / 10));
            yAxis.setMinorTickVisible(false);
        } else {
            double step = 100000;
            double upperBound = Math.ceil(maxValue / step) * step;
            upperBound = upperBound + (step * 2);
            upperBound = Math.max(upperBound, step * 2);
            yAxis.setUpperBound(upperBound);
            yAxis.setTickUnit(step);
            yAxis.setMinorTickVisible(false);
        }

        yAxis.requestAxisLayout();
    }

    @FXML
    private void refreshCharts() {
        stopAllAnimations();

        String filterType = filterTypeCombo.getValue();
        String dataType = dataTypeCombo.getValue();
        boolean isTicketCount = "Số vé bán được".equals(dataType);

        pieChartTitle.setText("Tỷ lệ " + dataType.toLowerCase() + " theo phim");
        barChartTitle.setText(dataType + " theo thời gian");
        yAxis.setLabel(dataType + (isTicketCount ? " (vé)" : " (VND)"));

        // Clear charts data
        pieChart.getData().clear();
        barChart.getData().clear();

        // Reset xAxis completely
        xAxis.setTickLabelsVisible(false);
        xAxis.setCategories(FXCollections.<String>observableArrayList());
        clearAxisLabels();
        xAxis.requestAxisLayout();
        barChart.requestLayout();

        totalLabel.setText("Tổng: ");

        // Load new data based on filter type
        if ("Theo ngày".equals(filterType)) {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null) {
                loadDataForDay(selectedDate, isTicketCount);
            } else {
                datePicker.setValue(LocalDate.now());
                loadDataForDay(LocalDate.now(), isTicketCount);
            }
        } else if ("Theo tháng/năm".equals(filterType)) {
            String month = monthCombo.getValue();
            String year = yearComboForMonth.getValue();
            if (month != null && year != null) {
                loadDataForMonth(month, year, isTicketCount);
            }
        } else if ("Theo năm".equals(filterType)) {
            String year = yearCombo.getValue();
            if (year != null) {
                loadDataForYear(year, isTicketCount);
            }
        }
    }

    private void clearAxisLabels() {
        if (xAxis == null) return;
        
        // Clear all existing tick labels
        for (Node node : xAxis.getChildrenUnmodifiable()) {
            if (node instanceof Text) {
                ((Text) node).setText("");
                node.setVisible(false);
            }
        }
        
        // Also clear through lookup if needed
        for (Node node : xAxis.lookupAll(".chart-category-tick-label")) {
            if (node instanceof Label) {
                ((Label) node).setText("");
                node.setVisible(false);
            }
        }
    }

    private void stopAllAnimations() {
        if (currentPieFadeOut != null) currentPieFadeOut.stop();
        if (currentPieFadeIn != null) currentPieFadeIn.stop();
        if (currentBarFadeOut != null) currentBarFadeOut.stop();
        if (currentBarFadeIn != null) currentBarFadeIn.stop();
    }

    private void loadDataForDay(LocalDate date, boolean isTicketCount) {
        String dateStr = date.toString();
        String query = "SELECT m.name, b.seatNumbers, b.totalPrice " +
                      "FROM bookedTickets b " +
                      "JOIN movies m ON b.movieID = m.id " +
                      "WHERE DATE(b.createDate) = ? AND b.currentStatus = 1";

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(dateStr);

        Map<String, Integer> ticketCountByMovie = new HashMap<>();
        Map<String, Double> revenueByMovie = new HashMap<>();
        double maxValue = 0;
        double totalValue = 0; // Tổng số vé hoặc doanh thu

        ObservableList<String> categories = FXCollections.observableArrayList();
        xAxis.setCategories(categories);

        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, dateStr);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String movieName = rs.getString("name");
                String seatNumbers = rs.getString("seatNumbers");
                double totalPrice = rs.getDouble("totalPrice");

                int ticketCount = seatNumbers.split(",").length;
                ticketCountByMovie.put(movieName, ticketCountByMovie.getOrDefault(movieName, 0) + ticketCount);
                revenueByMovie.put(movieName, revenueByMovie.getOrDefault(movieName, 0.0) + totalPrice);
            }

            for (String movieName : ticketCountByMovie.keySet()) {
                int ticketCount = ticketCountByMovie.get(movieName);
                double revenue = revenueByMovie.get(movieName);

                String wrappedMovieName = wrapMovieName(movieName);
                categories.add(wrappedMovieName);

                double value = isTicketCount ? ticketCount : revenue;
                PieChart.Data pieData = new PieChart.Data(movieName, value);
                pieChartData.add(pieData);

                Tooltip tooltip = new Tooltip(movieName);
                Tooltip.install(pieData.getNode(), tooltip);

                series.getData().add(new XYChart.Data<>(wrappedMovieName, value));
                maxValue = Math.max(maxValue, value);
                totalValue += value; // Cộng vào tổng
            }

            xAxis.setCategories(categories);
            xAxis.setTickLabelsVisible(true);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dữ liệu: " + e.getMessage());
        }

        setupYAxis(maxValue, isTicketCount);

        // Cập nhật totalLabel
        String totalText = isTicketCount ? String.format("Tổng: %d vé", (int) totalValue)
                                       : String.format("Tổng: %.2f VND", totalValue);
        totalLabel.setText(totalText);

        currentPieFadeOut = new FadeTransition(Duration.millis(500), pieChart);
        currentPieFadeOut.setFromValue(1.0);
        currentPieFadeOut.setToValue(0.0);
        currentPieFadeOut.setOnFinished(e -> {
            if (pieChartData.isEmpty()) {
                pieChart.setTitle("Không có dữ liệu");
            } else {
                pieChart.setData(pieChartData);
                pieChart.setTitle("");
                for (PieChart.Data data : pieChart.getData()) {
                    Tooltip tooltip = new Tooltip(data.getName());
                    Tooltip.install(data.getNode(), tooltip);
                }
            }
            currentPieFadeIn = new FadeTransition(Duration.millis(500), pieChart);
            currentPieFadeIn.setFromValue(0.0);
            currentPieFadeIn.setToValue(1.0);
            currentPieFadeIn.play();
        });

        currentBarFadeOut = new FadeTransition(Duration.millis(500), barChart);
        currentBarFadeOut.setFromValue(1.0);
        currentBarFadeOut.setToValue(0.0);
        currentBarFadeOut.setOnFinished(e -> {
            if (series.getData().isEmpty()) {
                barChart.setTitle("Không có dữ liệu");
                xAxis.setCategories(FXCollections.observableArrayList());
                clearAxisLabels();
            } else {
                barChart.getData().removeIf(s -> s.getName().equals(series.getName()));
                barChart.getData().add(series);
                barChart.setTitle("");
            }
            Platform.runLater(() -> {
                barChart.requestLayout();
                xAxis.requestAxisLayout();
                customizeTickLabels();
            });
            currentBarFadeIn = new FadeTransition(Duration.millis(500), barChart);
            currentBarFadeIn.setFromValue(0.0);
            currentBarFadeIn.setToValue(1.0);
            currentBarFadeIn.play();
        });

        currentPieFadeOut.play();
        currentBarFadeOut.play();
        if (categories.isEmpty()) {
            categories.add("No Data"); // Thêm nhãn mặc định nếu không có dữ liệu
        }

        xAxis.setCategories(categories);
        xAxis.setTickLabelsVisible(true);


        xAxis.setLabel("Phim");
    }

    private void customizeTickLabels() {
        for (Node node : xAxis.lookupAll(".axis-tick-mark")) {
            node.setVisible(false);
        }

        for (Node node : xAxis.lookupAll(".chart-category-tick-label")) {
            if (node instanceof Label) {
                Label label = (Label) node;
                label.setWrapText(true);
                label.setMaxWidth(150);
                label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                label.setStyle("-fx-font-size: 10px;");
                label.setVisible(!barChart.getData().isEmpty() && xAxis.getCategories().contains(label.getText()));
            }
        }
    }

    private void loadDataForMonth(String month, String year, boolean isTicketCount) {
        String startDate = year + "-" + month + "-01";
        String endDate = year + "-" + month + "-31";
        String query = "SELECT m.name, b.seatNumbers, b.totalPrice " +
                      "FROM bookedTickets b " +
                      "JOIN movies m ON b.movieID = m.id " +
                      "WHERE DATE(b.createDate) BETWEEN ? AND ? AND b.currentStatus = 1";

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tháng " + month + "/" + year);

        Map<String, Integer> ticketCountByMovie = new HashMap<>();
        Map<String, Double> revenueByMovie = new HashMap<>();
        double totalValue = 0; // Tổng số vé hoặc doanh thu

        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String movieName = rs.getString("name");
                String seatNumbers = rs.getString("seatNumbers");
                double totalPrice = rs.getDouble("totalPrice");

                int ticketCount = seatNumbers.split(",").length;
                ticketCountByMovie.put(movieName, ticketCountByMovie.getOrDefault(movieName, 0) + ticketCount);
                revenueByMovie.put(movieName, revenueByMovie.getOrDefault(movieName, 0.0) + totalPrice);
            }

            for (String movieName : ticketCountByMovie.keySet()) {
                int ticketCount = ticketCountByMovie.get(movieName);
                double revenue = revenueByMovie.get(movieName);

                double value = isTicketCount ? ticketCount : revenue;
                PieChart.Data pieData = new PieChart.Data(movieName, value);
                pieChartData.add(pieData);

                Tooltip tooltip = new Tooltip(movieName);
                Tooltip.install(pieData.getNode(), tooltip);
                totalValue += value; // Cộng vào tổng
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dữ liệu: " + e.getMessage());
        }

        String queryByDay = "SELECT DAY(b.createDate) as day, " +
                           "SUM(LENGTH(b.seatNumbers) - LENGTH(REPLACE(b.seatNumbers, ',', '')) + 1) as ticket_count, " +
                           "SUM(b.totalPrice) as revenue " +
                           "FROM bookedTickets b " +
                           "WHERE DATE(b.createDate) BETWEEN ? AND ? AND b.currentStatus = 1 " +
                           "GROUP BY DAY(b.createDate) " +
                           "ORDER BY DAY(b.createDate)";
        double maxValue = 0;
        ObservableList<String> categories = FXCollections.observableArrayList();

        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(queryByDay)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String day = String.format("%02d", rs.getInt("day"));
                int ticketCount = rs.getInt("ticket_count");
                double revenue = rs.getDouble("revenue");

                double value = isTicketCount ? ticketCount : revenue;
                series.getData().add(new XYChart.Data<>(day, value));
                categories.add(day);
                maxValue = Math.max(maxValue, value);
            }

            xAxis.setCategories(categories);
            xAxis.setTickLabelsVisible(true);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dữ liệu: " + e.getMessage());
        }

        setupYAxis(maxValue, isTicketCount);

        // Cập nhật totalLabel
        String totalText = isTicketCount ? String.format("Tổng: %d vé", (int) totalValue)
                                       : String.format("Tổng: %.2f VND", totalValue);
        totalLabel.setText(totalText);

        currentPieFadeOut = new FadeTransition(Duration.millis(500), pieChart);
        currentPieFadeOut.setFromValue(1.0);
        currentPieFadeOut.setToValue(0.0);
        currentPieFadeOut.setOnFinished(e -> {
            if (pieChartData.isEmpty()) {
                pieChart.setTitle("Không có dữ liệu");
            } else {
                pieChart.setData(pieChartData);
                pieChart.setTitle("");
                for (PieChart.Data data : pieChart.getData()) {
                    Tooltip tooltip = new Tooltip(data.getName());
                    Tooltip.install(data.getNode(), tooltip);
                }
            }
            currentPieFadeIn = new FadeTransition(Duration.millis(500), pieChart);
            currentPieFadeIn.setFromValue(0.0);
            currentPieFadeIn.setToValue(1.0);
            currentPieFadeIn.play();
        });

        currentBarFadeOut = new FadeTransition(Duration.millis(500), barChart);
        currentBarFadeOut.setFromValue(1.0);
        currentBarFadeOut.setToValue(0.0);
        currentBarFadeOut.setOnFinished(e -> {
            if (series.getData().isEmpty()) {
                barChart.setTitle("Không có dữ liệu");
                xAxis.setCategories(FXCollections.observableArrayList());
                clearAxisLabels();
            } else {
                barChart.getData().removeIf(s -> s.getName().equals(series.getName()));
                barChart.getData().add(series);
                barChart.setTitle("");
            }
            Platform.runLater(() -> {
                barChart.requestLayout();
                xAxis.requestAxisLayout();
                customizeTickLabels();
            });
            currentBarFadeIn = new FadeTransition(Duration.millis(500), barChart);
            currentBarFadeIn.setFromValue(0.0);
            currentBarFadeIn.setToValue(1.0);
            currentBarFadeIn.play();
        });

        currentPieFadeOut.play();
        currentBarFadeOut.play();

        xAxis.setLabel("Ngày trong tháng");
    }

    private void loadDataForYear(String year, boolean isTicketCount) {
        String startDate = year + "-01-01";
        String endDate = year + "-12-31";
        String query = "SELECT m.name, b.seatNumbers, b.totalPrice " +
                      "FROM bookedTickets b " +
                      "JOIN movies m ON b.movieID = m.id " +
                      "WHERE DATE(b.createDate) BETWEEN ? AND ? AND b.currentStatus = 1";

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Năm " + year);

        Map<String, Integer> ticketCountByMovie = new HashMap<>();
        Map<String, Double> revenueByMovie = new HashMap<>();
        double totalValue = 0; // Tổng số vé hoặc doanh thu

        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String movieName = rs.getString("name");
                String seatNumbers = rs.getString("seatNumbers");
                double totalPrice = rs.getDouble("totalPrice");

                int ticketCount = seatNumbers.split(",").length;
                ticketCountByMovie.put(movieName, ticketCountByMovie.getOrDefault(movieName, 0) + ticketCount);
                revenueByMovie.put(movieName, revenueByMovie.getOrDefault(movieName, 0.0) + totalPrice);
            }

            for (String movieName : ticketCountByMovie.keySet()) {
                int ticketCount = ticketCountByMovie.get(movieName);
                double revenue = revenueByMovie.get(movieName);

                double value = isTicketCount ? ticketCount : revenue;
                PieChart.Data pieData = new PieChart.Data(movieName, value);
                pieChartData.add(pieData);

                Tooltip tooltip = new Tooltip(movieName);
                Tooltip.install(pieData.getNode(), tooltip);
                totalValue += value; // Cộng vào tổng
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dữ liệu: " + e.getMessage());
        }

        String queryByMonth = "SELECT MONTH(b.createDate) as month, " +
                             "SUM(LENGTH(b.seatNumbers) - LENGTH(REPLACE(b.seatNumbers, ',', '')) + 1) as ticket_count, " +
                             "SUM(b.totalPrice) as revenue " +
                             "FROM bookedTickets b " +
                             "WHERE DATE(b.createDate) BETWEEN ? AND ? AND b.currentStatus = 1 " +
                             "GROUP BY MONTH(b.createDate) " +
                             "ORDER BY MONTH(b.createDate)";
        double maxValue = 0;
        ObservableList<String> categories = FXCollections.observableArrayList();

        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(queryByMonth)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String month = String.format("%02d", rs.getInt("month"));
                int ticketCount = rs.getInt("ticket_count");
                double revenue = rs.getDouble("revenue");

                double value = isTicketCount ? ticketCount : revenue;
                series.getData().add(new XYChart.Data<>(month, value));
                categories.add(month);
                maxValue = Math.max(maxValue, value);
            }

            xAxis.setCategories(categories);
            xAxis.setTickLabelsVisible(true);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy dữ liệu: " + e.getMessage());
        }

        setupYAxis(maxValue, isTicketCount);

        // Cập nhật totalLabel
        String totalText = isTicketCount ? String.format("Tổng: %d vé", (int) totalValue)
                                       : String.format("Tổng: %.2f VND", totalValue);
        totalLabel.setText(totalText);

        currentPieFadeOut = new FadeTransition(Duration.millis(500), pieChart);
        currentPieFadeOut.setFromValue(1.0);
        currentPieFadeOut.setToValue(0.0);
        currentPieFadeOut.setOnFinished(e -> {
            if (pieChartData.isEmpty()) {
                pieChart.setTitle("Không có dữ liệu");
            } else {
                pieChart.setData(pieChartData);
                pieChart.setTitle("");
                for (PieChart.Data data : pieChart.getData()) {
                    Tooltip tooltip = new Tooltip(data.getName());
                    Tooltip.install(data.getNode(), tooltip);
                }
            }
            currentPieFadeIn = new FadeTransition(Duration.millis(500), pieChart);
            currentPieFadeIn.setFromValue(0.0);
            currentPieFadeIn.setToValue(1.0);
            currentPieFadeIn.play();
        });

        currentBarFadeOut = new FadeTransition(Duration.millis(500), barChart);
        currentBarFadeOut.setFromValue(1.0);
        currentBarFadeOut.setToValue(0.0);
        currentBarFadeOut.setOnFinished(e -> {
            if (series.getData().isEmpty()) {
                barChart.setTitle("Không có dữ liệu");
                xAxis.setCategories(FXCollections.observableArrayList());
                clearAxisLabels();
            } else {
                barChart.getData().removeIf(s -> s.getName().equals(series.getName()));
                barChart.getData().add(series);
                barChart.setTitle("");
            }
            Platform.runLater(() -> {
                barChart.requestLayout();
                xAxis.requestAxisLayout();
                customizeTickLabels();
            });
            currentBarFadeIn = new FadeTransition(Duration.millis(500), barChart);
            currentBarFadeIn.setFromValue(0.0);
            currentBarFadeIn.setToValue(1.0);
            currentBarFadeIn.play();
        });

        currentPieFadeOut.play();
        currentBarFadeOut.play();

        xAxis.setLabel("Tháng trong năm");
    }
}