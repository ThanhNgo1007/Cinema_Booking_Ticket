# 🎬 Cinema Booking System (Desktop Application)

> **Hệ thống quản lý và đặt vé xem phim trên nền tảng Desktop**, được xây dựng bằng **Java Core** và **JavaFX**, áp dụng mô hình kiến trúc **MVC** và tích hợp các công nghệ giao tiếp thời gian thực.

![Banner](https://via.placeholder.com/1000x400?text=Cinema+Booking+System+Preview)
*(Hãy thay thế link trên bằng ảnh chụp màn hình giao diện chính của ứng dụng)*

## 📋 Giới thiệu (Overview)

Dự án này là một ứng dụng Desktop hoàn chỉnh mô phỏng quy trình hoạt động thực tế của một rạp chiếu phim. Ứng dụng cung cấp giải pháp toàn diện cho cả hai đối tượng người dùng: **Khách hàng** (đặt vé, chọn ghế) và **Quản trị viên** (quản lý phim, suất chiếu, doanh thu).

Điểm nổi bật của dự án là việc xử lý các tác vụ phức tạp như **chọn ghế động (Dynamic Seat Selection)** và **Chat hỗ trợ trực tuyến** sử dụng Message Broker.

## 🛠 Công nghệ sử dụng (Tech Stack)

* **Ngôn ngữ & Nền tảng:** Java 17 (OpenJDK).
* **Giao diện (GUI):** JavaFX (FXML), CSS Styling.
* **Kiến trúc:** MVC (Model-View-Controller) Pattern.
* **Cơ sở dữ liệu:** MySQL (Kết nối qua JDBC - `mysql-connector-java`).
* **Real-time & Messaging:** RabbitMQ (Sử dụng thư viện `amqp-client`).
* **Tiện ích & Thư viện khác:**
    * **JavaMail API:** Gửi email xác thực OTP và vé điện tử.
    * **Gson:** Xử lý dữ liệu JSON.
    * **JBCrypt:** Mã hóa mật khẩu an toàn.
    * **Scene Builder:** Thiết kế giao diện.

## ✨ Tính năng chính (Key Features)

### 👤 Dành cho Khách hàng (User Client)
1.  **Đăng ký & Đăng nhập bảo mật:** Xác thực tài khoản qua Email OTP, hỗ trợ Quên mật khẩu.
2.  **Đặt vé trực quan:**
    * Xem danh sách phim đang chiếu/sắp chiếu.
    * **Giao diện chọn ghế Visual:** Hiển thị sơ đồ ghế thực tế, trạng thái ghế (Trống/Đang chọn/Đã bán) cập nhật theo thời gian thực.
3.  **Thanh toán & Vé điện tử:** Mô phỏng thanh toán và nhận vé qua Email.
4.  **Lịch sử giao dịch:** Xem lại các vé đã đặt.
5.  **Chat hỗ trợ:** Chat trực tiếp với Admin để được hỗ trợ (Real-time).

### 🛡 Dành cho Quản trị viên (Admin Dashboard)
1.  **Quản lý Phim (Movies):** Thêm, xóa, sửa thông tin phim, poster, trailer.
2.  **Quản lý Suất chiếu (Showtimes):** Sắp xếp lịch chiếu phim theo phòng và giờ.
3.  **Báo cáo & Thống kê:** Xem doanh thu, số lượng vé bán ra theo thời gian.
4.  **Hệ thống Chat Center:** Nhận và phản hồi tin nhắn từ nhiều khách hàng cùng lúc (sử dụng RabbitMQ).

## 🚀 Cài đặt & Chạy ứng dụng (Installation)

Để chạy được dự án này trên máy cá nhân, bạn cần cài đặt:
* **Java JDK 17+**
* **MySQL Server**
* **RabbitMQ Server** (Bắt buộc cho tính năng Chat)

### Bước 1: Clone dự án
```bash
git clone [https://github.com/your-username/cinema-booking-ticket.git](https://github.com/your-username/cinema-booking-ticket.git)
