package Cinema.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Cinema.database.mysqlconnect;

public class Voucher {
    private String voucherId;
    private int discountAmount;
    private String discountDetails;
    private boolean isValid;

    // Constructor mặc định
    public Voucher() {
        this.voucherId = null;
        this.discountAmount = 0;
        this.discountDetails = "";
        this.isValid = false;
    }

    // Phương thức kiểm tra và áp dụng mã giảm giá
    public void applyVoucher(String voucherCode, int totalPrice) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            this.discountDetails = "Vui lòng nhập mã giảm giá";
            this.isValid = false;
            return;
        }

        String url = "jdbc:mysql://localhost/Cinema_DB";
        String username = "root";
        String password = "";

        try (Connection conn = mysqlconnect.ConnectDb(url, username, password)) {
            if (conn == null) {
                this.discountDetails = "Lỗi kết nối cơ sở dữ liệu";
                this.isValid = false;
                return;
            }

            String sql = "SELECT id_voucher, discountAmount, discountDetails FROM vouchers " +
                         "WHERE id_voucher = ? AND status = 1 AND start_date <= CURDATE() AND end_date >= CURDATE()";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, voucherCode);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    this.voucherId = rs.getString("id_voucher");
                    String discountStr = rs.getString("discountAmount");
                    this.discountDetails = rs.getString("discountDetails");

                    // Tính toán số tiền giảm giá
                    if (discountStr.contains("%")) {
                        // Giảm giá theo phần trăm
                        int percent = Integer.parseInt(discountStr.replace("%", "").trim());
                        this.discountAmount = (int) (totalPrice * (percent / 100.0));
                    } else {
                        // Giảm giá số tiền cố định
                        this.discountAmount = Integer.parseInt(discountStr);
                    }

                    this.isValid = true;
                } else {
                    this.voucherId = null;
                    this.discountAmount = 0;
                    this.discountDetails = "Mã giảm giá không hợp lệ hoặc đã hết hạn";
                    this.isValid = false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra voucher: " + e.getMessage());
            this.discountDetails = "Lỗi kiểm tra voucher";
            this.voucherId = null;
            this.discountAmount = 0;
            this.isValid = false;
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Lỗi định dạng discountAmount: " + e.getMessage());
            this.discountDetails = "Lỗi định dạng mã giảm giá";
            this.voucherId = null;
            this.discountAmount = 0;
            this.isValid = false;
        }
    }

    // Getter cho các thuộc tính
    public String getVoucherId() {
        return voucherId;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }

    public String getDiscountDetails() {
        return discountDetails;
    }

    public boolean isValid() {
        return isValid;
    }

    // Reset voucher về trạng thái ban đầu
    public void reset() {
        this.voucherId = null;
        this.discountAmount = 0;
        this.discountDetails = "";
        this.isValid = false;
    }
}