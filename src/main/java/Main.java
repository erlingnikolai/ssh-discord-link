import com.jcraft.jsch.*;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.Properties;

public class Main {


    private static String host;
    private static String user;
    private static String password;
    private static Discord discord;

    static Boolean pendingResponse = false;




    private static PipedOutputStream pin;


    /**
     * Gets data from the properties file and links the with the objects requires
     * Doing this we encapsulate important code like tokens etc from the java files itself and it can be hidden on git.
     *
     * @param prop with all the properties values
     */
    private static void addFromProperties(Properties prop) {
        discord = new Discord(prop.getProperty("discord.token"));

        host = prop.getProperty("ssh.ip");
        password = prop.getProperty("ssh.password");
        user = prop.getProperty("ssh.userName");
    }


    /**
     * creates a Properties object
     *
     * @return the properties object
     * @throws IOException if it cant load the file for any reason
     */
    private static Properties loadProperties() throws IOException {
        InputStream input = new FileInputStream("src/main/resources/config.properties");
        Properties prop = new Properties();
        prop.load(input);
        return prop;
    }


    public static void main(String[] args) throws JSchException, IOException, InterruptedException, LoginException {
        addFromProperties(loadProperties());
        PipedInputStream in1 = new PipedInputStream();
        pin = new PipedOutputStream(in1);
        JDA jda = new JDABuilder(discord.getToken()).addEventListener(discord).build();
        jda.awaitReady();
        Session session = connect();
        session.connect(30000);
        Channel channel = session.openChannel("shell");
        channel.setInputStream(in1);
        BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        channel.connect(1000);
        gatherMessage(in, jda);
    }

    private static Session connect() throws JSchException {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig(config);
        return session;
    }


    private static void gatherMessage(BufferedReader in, JDA jda) throws IOException {
        String line;
        StringBuffer content = new StringBuffer();
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    if (content.length() == 0) {
                        Thread.sleep(500);
                    } else {
                        pendingResponse = true;
                        String hallo = content.toString();
                        Thread.sleep(2000);
                        if (hallo.equals(content.toString())) {
                            jda.getTextChannelsByName("ssh-plex-51", true).get(0).sendMessage(content).queue();
                            content.delete(0, content.length());
                            pendingResponse = false;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        while ((line = in.readLine()) != null) {
            if (!line.isBlank() || !line.isEmpty()) {
                content.append(line.replace("https://", "")).append("\n");
                System.out.println(line);
            }
        }

    }


    static void sendMessage(String message) {
        try {
            pin.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}



