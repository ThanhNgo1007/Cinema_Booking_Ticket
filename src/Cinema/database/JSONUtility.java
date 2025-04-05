package Cinema.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JSONUtility {

    static String path_userdata = "/Users/ngochan0808/eclipse-workspace/HelloFX/src/Cinema/database/userdata.json";
    static String path_admindata = "/Users/ngochan0808/eclipse-workspace/HelloFX/src/Cinema/database/admindata.json";
    static String path_moviedata = "/Users/ngochan0808/eclipse-workspace/HelloFX/src/Cinema/database/moviedata.json";

    // Lưu thông tin người dùng vào userdata.json
    public static void storeUserDataFromResultSet(ResultSet rs) throws IOException, SQLException {
        int userId = rs.getInt("id");
        String phoneNumber = rs.getString("phone_num");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String cityName = rs.getString("city");
        String userEmail = rs.getString("email");
        String quan = rs.getString("quan");
        String phuong = rs.getString("phuong");
        String homeAddress = rs.getString("homeAddress");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneNumber = "";
        }
        if (firstName == null || firstName.isEmpty()) {
            firstName = "";
        }
        if (lastName == null || lastName.isEmpty()) {
            lastName = "";
        }
        if (cityName == null || cityName.isEmpty()) {
            cityName = "";
        }
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "";
        }
        if (quan == null || quan.isEmpty()) {
            quan = "";
        }
        if (phuong == null || phuong.isEmpty()) {
            phuong = "";
        }
        if (homeAddress == null || homeAddress.isEmpty()) {
            homeAddress = "";
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        User user = new User(userId, firstName, lastName, userEmail, phoneNumber, cityName, quan, phuong, homeAddress);
        String jsonString = gson.toJson(user);

        try (FileWriter writer = new FileWriter(path_userdata)) {
            writer.write(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lưu thông tin admin vào admindata.json
    public static void storeAdminDataFromResultSet(ResultSet rs) throws IOException, SQLException {
        int adminId = rs.getInt("id");
        String phoneNumber = rs.getString("phone_num");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String cityName = rs.getString("city");
        String adminEmail = rs.getString("email");
        String quan = rs.getString("quan");
        String huyen = rs.getString("phuong");
        String homeAddress = rs.getString("homeAddress");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneNumber = "";
        }
        if (firstName == null || firstName.isEmpty()) {
            firstName = "";
        }
        if (lastName == null || lastName.isEmpty()) {
            lastName = "";
        }
        if (cityName == null || cityName.isEmpty()) {
            cityName = "";
        }
        if (adminEmail == null || adminEmail.isEmpty()) {
            adminEmail = "";
        }
        if (quan == null || quan.isEmpty()) {
            quan = "";
        }
        if (huyen == null || huyen.isEmpty()) {
            huyen = "";
        }
        if (homeAddress == null || homeAddress.isEmpty()) {
            homeAddress = "";
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        User admin = new User(adminId, firstName, lastName, adminEmail, phoneNumber, cityName, quan, huyen, homeAddress);
        String jsonString = gson.toJson(admin);

        try (FileWriter writer = new FileWriter(path_admindata)) {
            writer.write(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Phương thức mới: Lưu dữ liệu người dùng trực tiếp từ tham số
    public static void storeUserData(int userId, String firstName, String lastName, String email, String phoneNumber, String cityName, String quan, String phuong, String homeAddress) throws IOException {
        // Xử lý giá trị null hoặc rỗng
        if (phoneNumber == null || phoneNumber.isEmpty()) phoneNumber = "";
        if (firstName == null || firstName.isEmpty()) firstName = "";
        if (lastName == null || lastName.isEmpty()) lastName = "";
        if (cityName == null || cityName.isEmpty()) cityName = "";
        if (email == null || email.isEmpty()) email = "";
        if (quan == null || quan.isEmpty()) quan = "";
        if (phuong == null || phuong.isEmpty()) phuong = "";
        if (homeAddress == null || homeAddress.isEmpty()) homeAddress = "";

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        User user = new User(userId, firstName, lastName, email, phoneNumber, cityName, quan, phuong, homeAddress);
        String jsonString = gson.toJson(user);

        try (FileWriter writer = new FileWriter(path_userdata)) {
            writer.write(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inner class representing User
    public static class User {
        public int userId;
        String firstName, lastName;
		public String email;
		String phoneNumber;
		String cityName;
		String quan;
		String phuong;
		String homeAddress;

        public User(int id, String fname, String lname, String email, String phoneNumber, String cityName, String quan, String phuong, String homeAddress) {
            this.userId = id;
            this.firstName = fname;
            this.lastName = lname;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.cityName = cityName;
            this.quan = quan;
            this.phuong = phuong;
            this.homeAddress = homeAddress;
        }

        public int getUserId() {
            return userId;
        }

        public String getUserName() {
            return firstName + " " + lastName;
        }

        public String getEmail() {
            return email;
        }
    }

    // Các phương thức khác giữ nguyên
    public static boolean userIsLoggedIn() {
        String filePath = "src/Cinema/database/userdata.json";
        try (FileReader reader = new FileReader(path_userdata)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.has("userId") && !jsonObject.get("userId").isJsonNull() && jsonObject.has("email")
                    && !jsonObject.get("email").isJsonNull() && !jsonObject.get("userId").getAsString().isEmpty()
                    && !jsonObject.get("email").getAsString().isEmpty();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeValuesAndSave() {
        String userFilePath = "src/Cinema/database/userdata.json";

        try {
            // Xóa userdata.json
            FileReader userReader = new FileReader(path_userdata);
            JsonObject userJsonObject = JsonParser.parseReader(userReader).getAsJsonObject();
            userReader.close();

            userJsonObject.addProperty("userId", "");
            userJsonObject.addProperty("email", "");
            userJsonObject.addProperty("firstName", "");
            userJsonObject.addProperty("lastName", "");
            userJsonObject.addProperty("phoneNumber", "");
            userJsonObject.addProperty("cityName", "");
            userJsonObject.addProperty("quan", "");
            userJsonObject.addProperty("phuong", "");
            userJsonObject.addProperty("homeAddress", "");

            FileWriter userWriter = new FileWriter(path_userdata);
            Gson gson = new Gson();
            gson.toJson(userJsonObject, userWriter);
            userWriter.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeValuesAndSaveAdmin() {
        String adminFilePath = "src/Cinema/database/admindata.json";

        try {
            Gson gson = new Gson();
            // Xóa admindata.json
            FileReader adminReader = new FileReader(path_admindata);
            JsonObject adminJsonObject = JsonParser.parseReader(adminReader).getAsJsonObject();
            adminReader.close();

            adminJsonObject.addProperty("userId", "");
            adminJsonObject.addProperty("email", "");
            adminJsonObject.addProperty("firstName", "");
            adminJsonObject.addProperty("lastName", "");
            adminJsonObject.addProperty("phoneNumber", "");
            adminJsonObject.addProperty("cityName", "");
            adminJsonObject.addProperty("quan", "");
            adminJsonObject.addProperty("phuong", "");
            adminJsonObject.addProperty("homeAddress", "");

            FileWriter adminWriter = new FileWriter(path_admindata);
            gson.toJson(adminJsonObject, adminWriter);
            adminWriter.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static User getUserData() {
        try (FileReader reader = new FileReader(path_userdata)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            int userId = jsonObject.has("userId") && !jsonObject.get("userId").isJsonNull() ? jsonObject.get("userId").getAsInt() : 0;
            String firstName = jsonObject.has("firstName") && !jsonObject.get("firstName").isJsonNull() ? jsonObject.get("firstName").getAsString() : "";
            String lastName = jsonObject.has("lastName") && !jsonObject.get("lastName").isJsonNull() ? jsonObject.get("lastName").getAsString() : "";
            String email = jsonObject.has("email") && !jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : "";
            String phoneNumber = jsonObject.has("phoneNumber") && !jsonObject.get("phoneNumber").isJsonNull() ? jsonObject.get("phoneNumber").getAsString() : "";
            String cityName = jsonObject.has("cityName") && !jsonObject.get("cityName").isJsonNull() ? jsonObject.get("cityName").getAsString() : "";
            String quan = jsonObject.has("quan") && !jsonObject.get("quan").isJsonNull() ? jsonObject.get("quan").getAsString() : "";
            String huyen = jsonObject.has("phuong") && !jsonObject.get("phuong").isJsonNull() ? jsonObject.get("phuong").getAsString() : "";
            String homeAddress = jsonObject.has("homeAddress") && !jsonObject.get("homeAddress").isJsonNull() ? jsonObject.get("homeAddress").getAsString() : "";
            return new User(userId, firstName, lastName, email, phoneNumber, cityName, quan, huyen, homeAddress);
        } catch (IOException e) {
            System.err.println("Lỗi lấy dữ liệu người dùng: " + e.getMessage());
            return null;
        }
    }

    public static String getUserFirstName() {
        try {
            FileReader reader = new FileReader(path_userdata);
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            reader.close();

            return jsonObject.get("firstName").getAsString();
        } catch (IOException e) {
            System.out.println("Error getting user data: " + e.getMessage());
            return "Guest";
        }
    }

    public class MovieData {
        public String id;
        public int basePrice, totalPrice = 0, numberOfSeats = 0; // Xóa price
        public String name, timing, selected, screen;
        public String[] selectedSeats = {}; // Xóa bookedSeats

        public MovieData(String id, String name, String timing, int basePrice, String screen) {
            this.id = id;
            this.name = name;
            this.timing = timing;
            this.basePrice = basePrice;
            this.screen = screen;
        }
    }

    // Sửa phương thức createMovieJson để không sử dụng price và bookedSeats
    public void createMovieJson(String id_lichchieu, String movieName, String timing, String seatInfo, int basePrice, String screen) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        MovieData movieData = new MovieData(id_lichchieu, movieName, timing, basePrice, screen);
        movieData.totalPrice = 0;
        movieData.numberOfSeats = 0;
        movieData.selected = "";
        movieData.selectedSeats = new String[]{};

        try (FileWriter file = new FileWriter(path_moviedata)) {
            gson.toJson(movieData, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MovieData getMovieJson() {
        try {
            FileReader reader = new FileReader(path_moviedata);
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            MovieData movieData = new MovieData(
                jsonObject.get("id").getAsString(),
                jsonObject.get("name").getAsString(),
                jsonObject.get("timing").getAsString(),
                jsonObject.get("basePrice").getAsInt(),
                jsonObject.has("screen") ? jsonObject.get("screen").getAsString() : ""
            );
            movieData.totalPrice = jsonObject.get("totalPrice").getAsInt();
            movieData.numberOfSeats = jsonObject.has("numberOfSeats") ? jsonObject.get("numberOfSeats").getAsInt() : 0;
            movieData.selected = jsonObject.has("selected") ? jsonObject.get("selected").getAsString() : "";
            JsonArray selectedArr = jsonObject.getAsJsonArray("selectedSeats");
            movieData.selectedSeats = new String[selectedArr.size()];
            for (int i = 0; i < selectedArr.size(); i++) {
                movieData.selectedSeats[i] = selectedArr.get(i).getAsString();
            }
            reader.close();
            return movieData;
        } catch (IOException e) {
            System.out.println("Error getting movie data: " + e.getMessage());
            return null;
        }
    }

    public boolean updateMovieJson(String[] seats, int price) {
        try {
            FileReader reader = new FileReader(path_moviedata);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            MovieData movieData = gson.fromJson(reader, MovieData.class);
            reader.close();

            movieData.selected = String.join(", ", seats);
            movieData.selectedSeats = seats;
            movieData.totalPrice = price;
            movieData.numberOfSeats = seats.length;

            FileWriter writer = new FileWriter(path_moviedata);
            gson.toJson(movieData, writer);
            writer.close();
            return true;
        } catch (IOException e) {
            System.out.println("Error updating movie data: " + e.getMessage());
            return false;
        }
    }

    // Sửa phương thức clearMovieData để không sử dụng price và bookedSeats
    public boolean clearMovieData() {
        try {
            FileReader reader = new FileReader(path_moviedata);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            MovieData movieData = gson.fromJson(reader, MovieData.class);
            reader.close();

            // Xóa các trường liên quan đến vé đã đặt
            movieData.selected = "";
            movieData.selectedSeats = new String[]{};
            movieData.totalPrice = 0;
            movieData.numberOfSeats = 0;

            FileWriter writer = new FileWriter(path_moviedata);
            gson.toJson(movieData, writer);
            writer.close();
            return true;
        } catch (IOException e) {
            System.out.println("Error clearing movie data: " + e.getMessage());
            return false;
        }
    }
    public boolean updateMoviePrice(int newTotalPrice) {
        try {
            FileReader reader = new FileReader(path_moviedata);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            MovieData movieData = gson.fromJson(reader, MovieData.class);
            reader.close();

            movieData.totalPrice = newTotalPrice;

            FileWriter writer = new FileWriter(path_moviedata);
            gson.toJson(movieData, writer);
            writer.close();
            return true;
        } catch (IOException e) {
            System.out.println("Error updating movie price: " + e.getMessage());
            return false;
        }
    }
}