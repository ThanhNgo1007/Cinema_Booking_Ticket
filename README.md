# 🎬 CINEMA BOOKING MANAGEMENT SYSTEM

> **Hệ thống đặt vé và quản lý rạp chiếu phim** (Desktop Application) được xây dựng dựa trên kiến trúc **MVC**, sử dụng **JavaFX** cho giao diện, **JDBC** để tối ưu hóa thao tác dữ liệu và **RabbitMQ** cho tính năng giao tiếp thời gian thực.

![Banner Project](https://via.placeholder.com/1000x400?text=Cinema+Booking+System+Preview)

## 📋 Giới thiệu (Overview)

Dự án là giải pháp phần mềm toàn diện mô phỏng quy trình vận hành thực tế của một cụm rạp chiếu phim. Hệ thống phục vụ hai đối tượng người dùng chính: **Khách hàng** (User) và **Quản trị viên** (Admin), đảm bảo tính toàn vẹn dữ liệu và trải nghiệm người dùng mượt mà thông qua giao diện trực quan.

Dự án tập trung giải quyết các bài toán kỹ thuật về:
* **Concurrency:** Xử lý đồng bộ trạng thái ghế khi nhiều người cùng đặt.
* **Real-time Communication:** Hỗ trợ khách hàng trực tuyến thông qua Message Broker.
* **Performance:** Tối ưu hóa truy vấn dữ liệu lớn bằng JDBC thuần.

## 🛠 Công nghệ sử dụng (Tech Stack)

| Category | Technology | Description |
| :--- | :--- | :--- |
| **Language** | **Java 17 (OpenJDK)** | Sử dụng các tính năng mới của Java Core & OOP. |
| **Frontend/GUI** | **JavaFX / FXML** | Xây dựng giao diện Desktop hiện đại, Responsive. |
| **Architecture** | **MVC Pattern** | Tách biệt Model, View, Controller giúp code dễ bảo trì. |
| **Database** | **MySQL & JDBC** | Sử dụng `mysql-connector` để thực thi Raw SQL hiệu năng cao. |
| **Messaging** | **RabbitMQ** | Message Broker xử lý Chat bất đồng bộ (Asynchronous). |
| **Security** | **JBCrypt** | Mã hóa mật khẩu (Hashing & Salting). |
| **Utilities** | **JavaMail API** | Gửi Email xác thực OTP và vé điện tử. |
| **Libraries** | **Gson, SLF4J** | Xử lý JSON và Logging hệ thống. |

## ✨ Tính năng chính (Key Features)

### 👤 Dành cho Khách hàng (Client Application)
1.  **Hệ thống xác thực:** Đăng ký/Đăng nhập, Quên mật khẩu qua OTP Email.
2.  **Chọn phim & Suất chiếu:** Xem danh sách phim đang chiếu, sắp chiếu với thông tin chi tiết (Trailer, Poster).
3.  **Đặt ghế trực quan (Visual Seat Selection):**
    * Hiển thị sơ đồ ghế theo phòng chiếu.
    * Cập nhật trạng thái ghế: *Trống (Available), Đang chọn (Selected), Đã bán (Sold)*.
4.  **Thanh toán giả lập:** Tích hợp quy trình thanh toán và gửi vé điện tử qua Email.
5.  **Live Chat:** Nhắn tin trực tiếp với Admin để được hỗ trợ (sử dụng RabbitMQ).

### 🛡 Dành cho Quản trị viên (Admin Dashboard)
1.  **Quản lý Phim (Movies CRUD):** Thêm, sửa, xóa phim, upload poster.
2.  **Quản lý Lịch chiếu (Showtimes):** Sắp xếp suất chiếu, phòng chiếu, tránh trùng lặp khung giờ.
3.  **Báo cáo doanh thu (Analytics):** Biểu đồ thống kê doanh thu theo phim, theo ngày.
4.  **Chat Support Center:** Giao diện nhận tin nhắn từ nhiều khách hàng cùng lúc.

## 🚀 Hướng dẫn Cài đặt & Chạy (Installation)

### 1. Yêu cầu hệ thống (Prerequisites)
* Java JDK 17+
* MySQL Server (8.0+)
* RabbitMQ Server (Đang chạy ở port 5672)
* IDE: IntelliJ IDEA hoặc Eclipse (khuyên dùng IntelliJ)

### 2. Cấu hình Database
1.  Mở công cụ quản lý MySQL (Workbench/HeidiSQL).
2.  Tạo database mới: `cinema_booking`.
3.  Import file SQL trong thư mục `database/cinema_db.sql` (hoặc tên file sql tương ứng trong source).
4.  Cập nhật cấu hình trong file `src/Cinema/database/DBUtility.java`:
    ```java
    private static final String URL = "jdbc:mysql://localhost:3306/cinema_booking";
    private static final String USER = "root";
    private static final String PASS = "your_password";
    ```

### 3. Cấu hình RabbitMQ & Email
* Đảm bảo **RabbitMQ Service** đã được start.
* Cập nhật thông tin gửi mail trong `EmailUtility.java`:
    ```java
    private static final String EMAIL_FROM = "your_email@gmail.com";
    private static final String APP_PASSWORD = "your_app_password"; // Lấy từ Google App Password
    ```

### 4. Cài đặt thư viện (Dependencies)
* Dự án sử dụng các file `.jar` trong thư mục `src/dbexample`.
* **IntelliJ:** File -> Project Structure -> Libraries -> Nhấn dấu `+` -> Chọn folder `src/dbexample` -> Apply.

### 5. Chạy ứng dụng
* Chạy class `src/Cinema/UI/Main.java` để khởi động ứng dụng.

## 📸 Hình ảnh minh họa (Screenshots)

| Màn hình đăng nhập | Chọn ghế |
|:---:|:---:|
| ![Login](link_anh_1) | ![Seat](link_anh_2) |

| Dashboard Admin | Chat Support |
|:---:|:---:|
| ![Dashboard](link_anh_3) | ![Chat](link_anh_4) |

## 📞 Liên hệ
* **Developer:** [Tên của bạn]
* **Email:** [Email của bạn]
* **LinkedIn:** [Link Profile của bạn]
