package Cinema.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import Cinema.database.JSONUtility.MovieData;
import Cinema.database.JSONUtility.User;
import Cinema.util.Movie;

public class DBUtility {

    private static final String DB_URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // Kết quả trả về của validateLogin
    public static class LoginResult {
        private boolean isValid;
        private int isSuperUser;

        public LoginResult(boolean isValid, int isSuperUser) {
            this.isValid = isValid;
            this.isSuperUser = isSuperUser;
        }

        public boolean isValid() {
            return isValid;
        }

        public int getIsSuperUser() {
            return isSuperUser;
        }
    }

    // validate login, encrypt password and store data in appropriate JSON file
    public static LoginResult validateLogin(String email, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String encryptedPassword = EncryptionDecryption.encrypt(password);

        try {
            conn = mysqlconnect.ConnectDb(DB_URL, USERNAME, PASSWORD);

            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, email);
            pstmt.setString(2, encryptedPassword);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                int isSuperUser = rs.getInt("isSuperUser");
                try {
                    // Chỉ lưu vào userdata.json nếu không phải admin
                    if (isSuperUser == 0) {
                        JSONUtility.storeUserDataFromResultSet(rs);
                    }
                    // Lưu vào admindata.json nếu là admin
                    if (isSuperUser == 1) {
                        JSONUtility.storeAdminDataFromResultSet(rs);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new LoginResult(true, isSuperUser);
            } else {
                return new LoginResult(false, 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new LoginResult(false, 0);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Các phương thức khác giữ nguyên
    public static Boolean checkExistinguserEmailAddress(String email) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String query = "SELECT * FROM users WHERE email = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, email);

            rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Boolean updateUsersPassword(String email, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String encryptedPassword = EncryptionDecryption.encrypt(password);

            String updateQuery = "UPDATE USERS SET password = ? WHERE email = ?";
            pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, encryptedPassword);
            pstmt.setString(2, email);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Boolean createNewUserAccount(String fullName, String email, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = mysqlconnect.ConnectDb(DB_URL, USERNAME, PASSWORD);

            String encryptedPassword = EncryptionDecryption.encrypt(password);
            String[] parsedName = Form.parseFullName(fullName);
            String firstName = (parsedName != null && parsedName.length > 0) ? parsedName[0] : null;
            String lastName = (parsedName != null && parsedName.length > 1) ? String.join(" ", Arrays.copyOfRange(parsedName, 1, parsedName.length)) : null;

            String insertQuery = "INSERT INTO users (first_name, last_name, email, password) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, encryptedPassword);

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void getMoviesData(ResultSet rs, List<Movie> movies) {
        try {
            while (rs.next()) {
                String getMovieName = rs.getString("name");
                String getMovieID = rs.getString("id");
                String getMovieDescription = rs.getString("description");
                String getMovieRating = rs.getString("ratings");
                String getMovieGener = rs.getString("gener");
                InputStream getMoviePoster = rs.getBinaryStream("posterImage");
                String getActorsList = rs.getString("actorList");
                String getMovieRealeseDate = rs.getString("releaseDate");
                String getDirector = rs.getString("director");
                String getMovieDuration = rs.getString("duration");
                String getMovieTrailer = rs.getString("trailer");
                String getMoviePrice = rs.getString("basePrice");

                Movie movie = new Movie();
                movie.setMovieName(getMovieName);
                movie.setMovieDescription(getMovieDescription);
                movie.setMovieRating(getMovieRating);
                movie.setMovieGener(getMovieGener);
                movie.setMovieRealeseDate(getMovieRealeseDate);
                movie.setMoviePosterFromBlob(getMoviePoster);
                movie.setMovieActor(getActorsList);
                movie.setMovieID(getMovieID);
                movie.setDirector(getDirector);
                movie.setMovieTime(getMovieDuration);
                movie.setMovieTrailer(getMovieTrailer);
                movie.setPrice(getMoviePrice);

                movies.add(movie);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static double calculateTotalPrice() {
        double total = 0;
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String query = "SELECT totalPrice FROM booked_ticket";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    total += resultSet.getDouble("totalPrice");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error calculating total price: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return total;
    }

    public static int countMovies() {
        int movieCount = 0;
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT COUNT(*) AS count FROM movies";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        movieCount = rs.getInt("count");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return movieCount;
    }

    public static int totalTicketsSold() {
        int ticketCount = 0;
        Connection conn = null;

        try {
            conn = mysqlconnect.ConnectDb(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT COUNT(*) AS count FROM bookedTickets";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        ticketCount = rs.getInt("count");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ticketCount;
    }
}