package Cinema.util;

public class User {
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String cityName;
    private String quan;
    private String phuong;
    private String homeAddress;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getQuan() {
        return quan;
    }

    public void setQuan(String quan) {
        this.quan = quan;
    }

    public String getPhuong() {
        return phuong;
    }

    public void setPhuong(String phuong) {
        this.phuong = phuong;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }
}