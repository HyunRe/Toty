package config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class TestDatabaseConfig {

    @Value("${ssh.host}")
    private String sshHost;

    @Value("${ssh.port}")
    private int sshPort;

    @Value("${ssh.user}")
    private String sshUser;

    @Value("${ssh.private-key}")
    private String privateKey;

    @Value("${ssh.remote-host}")
    private String remoteHost;

    @Value("${ssh.remote-port}")
    private int remotePort;

    @Value("${db.username}")
    private String dbUsername;

    @Value("${db.password}")
    private String dbPassword;

    @Bean
    public DataSource dataSource() throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity(privateKey);
//        jsch.addIdentity("my-ssh-key", privateKey.getBytes(), null, null);
        Session session = jsch.getSession(sshUser, sshHost, sshPort);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        int localPort = 21212;
        session.setPortForwardingL(localPort, remoteHost, remotePort);

        // DB 연결
        String dbUrl = "jdbc:mysql://localhost:" + localPort + "/toty_test?serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}
