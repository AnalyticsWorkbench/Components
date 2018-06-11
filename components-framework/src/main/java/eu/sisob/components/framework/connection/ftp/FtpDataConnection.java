package eu.sisob.components.framework.connection.ftp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import eu.sisob.components.framework.connection.interfaces.DataConnection;

/**
 * Created by stefan on 27.06.15.
 */
public class FtpDataConnection implements DataConnection {

    private FTPClient ftpClient;
    private String url;
    private int port;
    private String username;
    private String password;
    protected static Logger logger = Logger.getLogger(FtpDataConnection.class.getName());

    {
        boolean found = false;
        for (Handler h : logger.getHandlers()) {
            if (h instanceof ConsoleHandler) {
                found = true;
                h.setLevel(Level.ALL);
            }
        }
        if (!found) {
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.ALL);
            logger.addHandler(ch);
        }
        logger.setLevel(Level.ALL);

    }


    public FtpDataConnection(String url, int port, String username, String password) {
        this.url = url;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean setupConnection() {

        if (ftpClient == null)
            ftpClient = new FTPClient();

        try {
            // connect and login
            ftpClient.connect(url, port);
            ftpClient.login(username, password);

            // set to passive mode because most of us are behind a firewall :)
            ftpClient.enterLocalPassiveMode();

        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Write a file to a ftp server.
     * @return
     */
    @Override
    public void writeData(String path, String payload) {

        InputStream in = new ByteArrayInputStream(payload.getBytes());
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.storeFile(path, in);
            ftpClient.disconnect();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Load a file from a ftp server.
     * @return base64 converted data
     */
    @Override
    public String readData(String path) {
        InputStream inputStream;
        String data = null;

        try {
            inputStream = ftpClient.retrieveFileStream(path);
            // download file and covert it to an base64 string
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream,
                    StandardCharsets.UTF_8));
            String str = null;
            StringBuilder sb = new StringBuilder(8192);
            while ((str = r.readLine()) != null) {
                sb.append(str);
            }
            data = sb.toString();
            ftpClient.disconnect();

        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return data;
    }

    @Override
    public void removeData(String path) {

        try {
            ftpClient.deleteFile(path);
            ftpClient.disconnect();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public void shutdown() {
        ftpClient = null;
    }
}
