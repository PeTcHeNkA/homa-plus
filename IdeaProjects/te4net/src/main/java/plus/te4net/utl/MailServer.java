package plus.te4net.utl;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import plus.tson.TsonBool;
import plus.tson.TsonMap;
import plus.util.SimpleSrcGetter;
import java.util.Properties;
import static plus.te4net.core.node.HWorkerRepository.pool;


public class MailServer implements SimpleSrcGetter {
    public interface CallBack{
        void onSuccess();
        void onError(Exception e);
    }
    private final Session session;
    private final String user, password;
    private final boolean virtual;

    public MailServer(TsonMap map){
        if(!(this.virtual = map.getOrDefault("virtual", new TsonBool(false)).getBool())) {
            boolean debug = map.getOrDefault("debug", new TsonBool(false)).getBool();
            this.user = map.getStr("mail-login");
            this.password = map.getStr("mail-password");
            String server = map.getStr("server");
            //"smtp.yandex.ru"
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "smtps");
            props.setProperty("mail.smtp.host", server);
            props.setProperty("mail.smtp.port", "465");
            props.setProperty("mail.smtp.user", user);
            props.setProperty("mail.smtp.ssl.enable", "true");
            props.setProperty("mail.debug", debug ? "true" : "false");

            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            };
            session = Session.getInstance(props, auth);
        } else {
            this.user = null;
            this.session = null;
            this.password = null;
        }
    }


    public void send(String mail, String content, CallBack cl){
        if(virtual){
            Logger.get().info("EMULATE SEND MAIL TO ["+mail
                    +"]:\n---------------\n"+content+"\n---------------");
            pool.put(cl::onSuccess);
        } else {
            if (user == null || password == null) {
                cl.onError(new NumberFormatException("Mail server not configured!"));
            } else {
                pool.put(() -> send0(mail, content, cl));
            }
        }
    }


    private void send0(String mail, String content, CallBack cl) {
        try {
            InternetAddress email = new InternetAddress(mail);
            email.validate();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipient(Message.RecipientType.TO, email);
            MimeBodyPart mailBody = new MimeBodyPart();
            mailBody.setText(content, "utf-8", "html");
            message.setContent(new MimeMultipart(mailBody));
            Transport.send(message, user, password);
            cl.onSuccess();
        } catch (Exception e){
            System.out.println("Error sending to "+mail);
            cl.onError(e);
        }
    }
}