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

public class DBUtility {

    // Update these with your MySQL credentials
    private static final String DB_URL = "jdbc:mysql://localhost/Cinema_DB";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // validate login, encrypt password and store data in userdata.json
    public static Boolean validateLogin(String email, String password) {
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
                try {
                    JSONUtility.storeUserDataFromResultSet(rs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
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
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT COUNT(*) AS count FROM booked_ticket";

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

    public static int[] countTicketsByType() {
        int[] ticketCounts = new int[3]; // Index 0: Premium, 1: VIP, 2: Standard
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT seatClass, SUM(numberOfSeats) AS totalSeats FROM booked_ticket GROUP BY seatClass";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String seatClass = rs.getString("seatClass");
                        int totalSeats = rs.getInt("totalSeats");

                        switch (seatClass) {
                            case "Premium":
                                ticketCounts[0] += totalSeats;
                                break;
                            case "VIP":
                                ticketCounts[1] += totalSeats;
                                break;
                            case "Standard":
                                ticketCounts[2] += totalSeats;
                                break;
                        }
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
        return ticketCounts;
    }

    public static boolean updateBookingData() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            JSONUtility json = new JSONUtility();
            User user = JSONUtility.getUserData();
            MovieData moviedata = json.getMovieJson();

            String seatsStr = String.join(", ", moviedata.selectedSeats);

            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String query = "INSERT INTO booked_ticket (userId, movieId, seatNumbers, seatClass, numberOfSeats, showTime, perPrice, totalPrice, currentStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, user.userId);
            pstmt.setInt(2, moviedata.id);
            pstmt.setString(3, seatsStr);
            pstmt.setString(4, "Normal");
            pstmt.setInt(5, moviedata.selectedSeats.length);
            pstmt.setString(6, moviedata.timing);
            pstmt.setInt(7, moviedata.basePrice);
            pstmt.setInt(8, moviedata.totalPrice);
            pstmt.setString(9, "Booked");

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;
        } catch (Exception e) {
            System.out.println(e.toString());
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
}