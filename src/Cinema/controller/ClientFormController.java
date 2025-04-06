package Cinema.controller;

import Cinema.database.JSONUtility;
import Cinema.util.User;
import Cinema.database.mysqlconnect;
import com.rabbitmq.client.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClientFormController {
    @FXML
    private Label txtLabel;
    @FXML
    private ScrollPane scrollPain;
    @FXML
    private VBox vBox;
    @FXML
    private TextField txtMsg;
    @FXML
    private VBox sampleQuestionsVBox;
    @FXML
    private Button toggleQuestionsButton;

    private String clientName;
    private String userId;
    private String adminId;
    private RabbitMQService rabbitMQ;

    private static final String DB_URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public void initialize() {
        Cinema.util.User userData = JSONUtility.getUserData();
        if (userData != null) {
            clientName = userData.getUserName();
            userId = String.valueOf(userData.getUserId());
        } else {
            try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD)) {
                if (conn != null) {
                    String sql = "SELECT id, first_name, last_name FROM users WHERE isSuperUser = 0 LIMIT 1";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String firstName = rs.getString("first_name");
                        String lastName = rs.getString("last_name");
                        userId = rs.getString("id");
                        clientName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                        clientName = clientName.trim();
                        if (clientName.isEmpty()) clientName = "Unknown Client";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                clientName = "Unknown Client";
                userId = "0";
            }
        }

        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD)) {
            if (conn != null) {
                String sql = "SELECT id FROM users WHERE isSuperUser = 1 LIMIT 1";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    adminId = rs.getString("id");
                    System.out.println("Admin ID retrieved from database: " + adminId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            adminId = "1";
            System.err.println("Lỗi khi lấy ID admin từ CSDL, sử dụng ID mặc định: " + adminId);
        }

        txtLabel.setText(clientName);
        if (vBox != null) {
            vBox.heightProperty().addListener((observable, oldValue, newValue) -> scrollPain.setVvalue(1.0));
        }

        if (txtMsg != null) {
            txtMsg.setOnAction(event -> sendMsg(txtMsg.getText()));
        }

        try {
            String queueName = "user_" + userId;
            System.out.println("Client queue name: " + queueName);
            rabbitMQ = new RabbitMQService(queueName);
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

            displayWelcomeMessage();
            loadSampleQuestions();
        sampleQuestionsVBox.setManaged(false);
    }

    private void displayWelcomeMessage() {
        String welcomeMessage = "Xin chào! " +
                "Bạn cần hỗ trợ về hệ thống đặt vé xem phim.\n" +
                "Vui lòng đợi trong giây lát! Nhân viên của chúng tôi sẽ trả lời bạn trong thời gian sớm nhất !!!";
        displaySystemMessage("Hệ thống", welcomeMessage);
    }

    private void displaySystemMessage(String sender, String message) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 20px; -fx-border-color: #cccccc; -fx-border-radius: 20px");
        textFlow.setPadding(new Insets(5, 10, 5, 10));
        text.setFill(Color.BLACK);

        hBox.getChildren().add(textFlow);

        HBox hBoxTime = new HBox();
        hBoxTime.setAlignment(Pos.CENTER_LEFT);
        hBoxTime.setPadding(new Insets(0, 5, 5, 10));
        String stringTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Text time = new Text(stringTime);
        time.setStyle("-fx-font-size: 8");
        hBoxTime.getChildren().add(time);

        vBox.getChildren().add(hBox);
        vBox.getChildren().add(hBoxTime);
        scrollPain.setVvalue(1.0);
    }

    private void loadSampleQuestions() {
        List<String> sampleQuestions = new ArrayList<>();
        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD)) {
            if (conn != null) {
                String sql = "SELECT question_text FROM sample_questions LIMIT 3";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    sampleQuestions.add(rs.getString("question_text"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sampleQuestions.add("Tôi có thể đặt vé xem phim ở đâu?");
            sampleQuestions.add("Làm thế nào để hủy vé đã đặt?");
            sampleQuestions.add("Giá vé được tính như thế nào?");
        }

        for (String question : sampleQuestions) {
            Button questionButton = new Button(question);
            questionButton.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 10px; -fx-padding: 5 10 5 10;");
            questionButton.setOnAction(event -> sendMsg(question)); // Sử dụng sendMsg thay vì sendSampleQuestion
            HBox buttonBox = new HBox(questionButton);
            buttonBox.setAlignment(Pos.CENTER_LEFT);
            buttonBox.setPadding(new Insets(5, 0, 5, 10));
            sampleQuestionsVBox.getChildren().add(buttonBox);
        }
    }

    @FXML
    private void sendButtonOnAction(MouseEvent event) {
        System.out.println("sendButtonOnAction called with event: " + event);
        if (txtMsg == null) {
            System.err.println("Error: txtMsg is null. Check FXML mapping.");
            return;
        }
        sendMsg(txtMsg.getText());
    }

    private void sendMsg(String msgToSend) {
        if (!msgToSend.isEmpty()) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(5, 5, 0, 10));

            Text text = new Text(msgToSend);
            text.setStyle("-fx-font-size: 14");
            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle("-fx-background-color: #0693e3; -fx-font-weight: bold; -fx-background-radius: 20px");
            textFlow.setPadding(new Insets(5, 10, 5, 10));
            text.setFill(Color.WHITE);

            hBox.getChildren().add(textFlow);

            HBox hBoxTime = new HBox();
            hBoxTime.setAlignment(Pos.CENTER_RIGHT);
            hBoxTime.setPadding(new Insets(0, 5, 5, 10));
            String stringTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            Text time = new Text(stringTime);
            time.setStyle("-fx-font-size: 8");
            hBoxTime.getChildren().add(time);

            vBox.getChildren().add(hBox);
            vBox.getChildren().add(hBoxTime);

            try {
                System.out.println("Sending message to admin: " + userId + "-" + msgToSend + " with routing key: admin");
                rabbitMQ.sendMessage(userId + "-" + msgToSend, "admin");
                saveMessageToDatabase(userId, adminId, msgToSend);
            } catch (Exception e) {
                e.printStackTrace();
            }

            txtMsg.clear();
        }
    }

    private void receiveMessage(String msg) {
        System.out.println("Client received message: " + msg);
        Platform.runLater(() -> {
            String[] parts = msg.split("-", 2);
            if (parts.length < 2) {
                System.err.println("Invalid message format: " + msg);
                return;
            }
            String senderId = parts[0];
            String message = parts[1];
            String senderName = senderId.equals(adminId) ? "Admin" : senderId;
            displayMessage(senderName, message);
        });
    }

    private void displayMessage(String sender, String message) {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(5, 10, 5, 10));

        if (sender.equals(clientName)) {
            hBox.setAlignment(Pos.CENTER_RIGHT);
            textFlow.setStyle("-fx-background-color: #0693e3; -fx-background-radius: 20px");
            text.setFill(Color.WHITE);
        } else {
            hBox.setAlignment(Pos.CENTER_LEFT);
            textFlow.setStyle("-fx-background-color: #abb8c3; -fx-background-radius: 20px");
            text.setFill(Color.BLACK);
            HBox hBoxName = new HBox();
            hBoxName.setAlignment(Pos.CENTER_LEFT);
            Text textName = new Text(sender);
            TextFlow textFlowName = new TextFlow(textName);
            hBoxName.getChildren().add(textFlowName);
            vBox.getChildren().add(hBoxName);
        }

        hBox.getChildren().add(textFlow);

        HBox hBoxTime = new HBox();
        hBoxTime.setAlignment(hBox.getAlignment());
        hBoxTime.setPadding(new Insets(0, 5, 5, 10));
        String stringTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Text time = new Text(stringTime);
        time.setStyle("-fx-font-size: 8");
        hBoxTime.getChildren().add(time);

        vBox.getChildren().add(hBox);
        vBox.getChildren().add(hBoxTime);
        scrollPain.setVvalue(1.0);
    }

    private void saveMessageToDatabase(String senderId, String receiverId, String messageText) {
        try (Connection conn = mysqlconnect.ConnectDb(DB_URL, DB_USER, DB_PASSWORD)) {
            if (conn == null) {
                System.err.println("Failed to connect to database");
                return;
            }
            String sql = "INSERT INTO messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, senderId);
            stmt.setString(2, receiverId);
            stmt.setString(3, messageText);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleSampleQuestions(MouseEvent event) {
        boolean isVisible = sampleQuestionsVBox.isVisible();
        sampleQuestionsVBox.setVisible(!isVisible);
        sampleQuestionsVBox.setManaged(!isVisible);
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
            channel.queueBind(queueName, EXCHANGE_NAME, queueName);
        }

        public void sendMessage(String message, String routingKey) throws IOException {
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
        }

        public void receiveMessages(Consumer consumer) throws IOException {
            channel.basicConsume(queueName, true, consumer);
        }

        public Channel getChannel() {
            return channel;
        }
    }
}