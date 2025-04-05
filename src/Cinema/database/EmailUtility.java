package Cinema.database;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtility {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587; // Chuyển sang 587 để dùng STARTTLS
    private static final String EMAIL_USERNAME = "lckchaser1007@gmail.com";
    private static final String EMAIL_PASSWORD = "mvuq rmzq fhwc mqxk"; // 🔴 Dùng App Password!

    public static String validOtpCode;

    public static Boolean sendVerificationEmail(String emailAddress) {
        String otp = generateOTP();
        validOtpCode = otp;
        String message = "Your verification code is: " + otp;
        String subject = "Account Verification";
        return sendEmail(message, subject, emailAddress);
    }

    private static String generateOTP() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    public static Boolean sendEmail(String message, String subject, String to) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true"); // ✅ Bật STARTTLS
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(EMAIL_USERNAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setText(message);

            Transport.send(msg);
            System.out.println("Email sent successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
