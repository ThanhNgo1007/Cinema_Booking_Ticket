package Cinema.controller;

import Cinema.database.mysqlconnect;
import com.rabbitmq.client.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ServerFormController {
    @FXML
    private ListView<String> userListView;
    @FXML
    private ScrollPane scrollPain;
    @FXML
    private VBox chatVBox;
    @FXML
    private TextField txtMsg;

    private String adminId;
    private RabbitMQService rabbitMQ;
    private Map<String, String> userMap = new HashMap<>(); // Key: userId, Value: username
    private String selectedUser; // Lưu username của người dùng hiện tại

    private static final String DB_URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public void initialize() {
        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD)) {
            if (conn != null) {
                String sql = "SELECT id FROM users WHERE isSuperUser = 1 LIMIT 1";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    adminId = rs.getString("id");
                    System.out.println("Admin ID retrieved from database: " + adminId);
                } else {
                    adminId = "1";
                    System.err.println("Không tìm thấy tài khoản admin trong CSDL, sử dụng ID mặc định: " + adminId);
                }
            } else {
                adminId = "1";
                System.err.println("Không thể kết nối CSDL, sử dụng ID admin mặc định: " + adminId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            adminId = "1";
            System.err.println("Lỗi khi lấy ID admin từ CSDL, sử dụng ID mặc định: " + adminId);
        }

        loadUserList();
        chatVBox.heightProperty().addListener((observable, oldValue, newValue) -> scrollPain.setVvalue(1.0)); // Cuộn xuống dưới cùng

        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedUser = newValue; // Lưu username được chọn
                loadChatHistory(newValue);
            } else {
                selectedUser = null;
                chatVBox.getChildren().clear();
            }
        });

        try {
            rabbitMQ = new RabbitMQService("admin_queue");
            Consumer consumer = new DefaultConsumer(rabbitMQ.getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    receiveMessage(message);
                }
            };
            rabbitMQ.receiveMessages(consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendButtonOnAction(ActionEvent event) {
        String msgToSend = txtMsg.getText();
        String selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (!msgToSend.isEmpty() && selectedUser != null) {
            LocalDateTime timestamp = LocalDateTime.now();
            displayMessage("Admin", msgToSend, timestamp);

            try {
                String userId = getUserIdFromUsername(selectedUser);
                System.out.println("Sending message to client: " + adminId + "-" + msgToSend + " with routing key: user_" + userId);
                rabbitMQ.sendMessage(adminId + "-" + msgToSend, "user_" + userId); // Gửi với adminId
                saveMessageToDatabase(adminId, userId, msgToSend, timestamp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            txtMsg.clear();
        }
    }

    private void loadUserList() {
        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD)) {
            if (conn == null) {
                System.err.println("Failed to connect to database");
                return;
            }
            String sql = "SELECT id, first_name, last_name FROM users WHERE isSuperUser = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            userListView.getItems().clear();
            while (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String fullName = (firstName != null ? firstName : "") + (lastName != null ? lastName : "");
                fullName = fullName.trim();
                if (fullName.isEmpty()) fullName = "Unknown User";
                String id = rs.getString("id");
                userMap.put(id, fullName); // Key: userId, Value: username
                userListView.getItems().add(fullName);
                System.out.println("Added to userMap: " + id + " -> " + fullName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadChatHistory(String selectedUser) {
        chatVBox.getChildren().clear();
        LocalDate previousDate = null;
        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD)) {
            if (conn == null) {
                System.err.println("Failed to connect to database");
                return;
            }
            String userId = getUserIdFromUsername(selectedUser);
            String sql = "SELECT sender_id, message_text, send_at FROM messages " +
                         "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                         "ORDER BY send_at ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setString(2, adminId);
            stmt.setString(3, adminId);
            stmt.setString(4, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String senderId = rs.getString("sender_id");
                String message = rs.getString("message_text");
                Timestamp sendAt = rs.getTimestamp("send_at");
                LocalDateTime sentTime = sendAt != null ? sendAt.toLocalDateTime() : LocalDateTime.now();
                LocalDate currentDate = sentTime.toLocalDate();

                if (previousDate == null || !previousDate.equals(currentDate)) {
                    addDateSeparator(currentDate);
                    previousDate = currentDate;
                }

                displayMessage(senderId.equals(adminId) ? "Admin" : userMap.get(senderId), message, sentTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> scrollPain.setVvalue(1.0));
    }

    private String getUserIdFromUsername(String username) {
        for (Map.Entry<String, String> entry : userMap.entrySet()) {
            if (entry.getValue().equals(username)) {
                return entry.getKey();
            }
        }
        return null; // Trả về null nếu không tìm thấy
    }

    private void addDateSeparator(LocalDate date) {
        HBox dateBox = new HBox();
        dateBox.setAlignment(Pos.CENTER);
        dateBox.setPadding(new Insets(10, 0, 10, 0));
        Text dateText = new Text(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateText.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-background-color: #f0f0f0; -fx-padding: 5px 10px; -fx-background-radius: 10px;");
        dateBox.getChildren().add(dateText);
        chatVBox.getChildren().add(dateBox);
    }

    private void receiveMessage(String msg) {
        System.out.println("Received message in receiveMessage: " + msg);
        Platform.runLater(() -> {
            String[] parts = msg.split("-", 2);
            if (parts.length < 2) {
                System.err.println("Invalid message format: " + msg);
                return;
            }

            String senderId = parts[0].trim();
            String message = parts[1].trim();
            String senderName = userMap.get(senderId);

            if (senderName == null) {
                System.err.println("Error: Sender ID '" + senderId + "' not found in userMap.");
                senderId = "0";
                senderName = "Unknown User";
            }

            LocalDateTime timestamp = LocalDateTime.now();
            displayMessage(senderName, message, timestamp);

            // Đưa người gửi lên đầu danh sách
            userListView.getItems().remove(senderName);
            userListView.getItems().add(0, senderName);
        });
    }


    private void displayMessage(String sender, String message, LocalDateTime timestamp) {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(5, 10, 5, 10));

        if (sender.equals("Admin")) {
            hBox.setAlignment(Pos.CENTER_RIGHT);
            textFlow.setStyle("-fx-background-color: #0693e3; -fx-background-radius: 20px");
            text.setFill(Color.WHITE);
        } else {
            hBox.setAlignment(Pos.CENTER_LEFT);
            textFlow.setStyle("-fx-background-color: #abb8c3; -fx-background-radius: 20px");
            text.setFill(Color.BLACK);
        }

        hBox.getChildren().add(textFlow);

        HBox hBoxTime = new HBox();
        hBoxTime.setAlignment(hBox.getAlignment());
        hBoxTime.setPadding(new Insets(0, 5, 5, 10));
        String stringTime = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
        Text time = new Text(stringTime);
        time.setStyle("-fx-font-size: 8");
        hBoxTime.getChildren().add(time);

        chatVBox.getChildren().add(hBox);
        chatVBox.getChildren().add(hBoxTime);
    }

    private void saveMessageToDatabase(String senderId, String receiverId, String messageText, LocalDateTime timestamp) {
        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD)) {
            if (conn == null) {
                System.err.println("Failed to connect to database");
                return;
            }
            String sql = "INSERT INTO messages (sender_id, receiver_id, message_text, send_at) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, senderId);
            stmt.setString(2, receiverId);
            stmt.setString(3, messageText);
            stmt.setTimestamp(4, Timestamp.valueOf(timestamp));
            stmt.executeUpdate();
            System.out.println("Message saved: SenderId=" + senderId + ", ReceiverId=" + receiverId + ", Message=" + messageText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class RabbitMQService {
        private static final String EXCHANGE_NAME = "chat_exchange";
        private final Channel channel;
        private final String queueName;

        public RabbitMQService(String queueName) throws Exception {
            this.queueName = queueName;
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            com.rabbitmq.client.Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.queueDeclare(queueName, false, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, "admin");
            System.out.println("Queue " + queueName + " bound to routing key: admin");
        }

        public void sendMessage(String message, String routingKey) throws IOException {
            System.out.println("Sending message: " + message + " with routing key: " + routingKey);
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
        }

        public void receiveMessages(Consumer consumer) throws IOException {
            channel.basicConsume(queueName, true, consumer);
            System.out.println("Started consuming from queue: " + queueName);
        }

        public Channel getChannel() {
            return channel;
        }
    }
}