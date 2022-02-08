package org.guge.pop3;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.guge.pop3.models.StatsModel;
import org.guge.pop3.models.MessageInfoModel;
import org.guge.pop3.errors.ErrorResponseException;
import org.guge.pop3.errors.InvalidSpecificationException;

public class Pop3Client implements Closeable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Pop3Client.class);

    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private State state;

    public Pop3Client(Socket socket) throws IOException {
        this.socket = socket;

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        var greeting = readResponseAndParse();
        this.state = State.AUTHORIZATION;

        logger.info("Successfully connected. Server greeting: {}", greeting);
    }

    public Pop3Client(Socket socket, String username, String password) throws IOException {
        this(socket);
        authenticate(username, password);
    }

    @Override
    public void close() throws IOException {
        logout();
        socket.close();
        reader.close();
        writer.close();
    }

    public void authenticate(String username, String password) throws IOException {
        sendAndValidate(String.format("USER %s", username));
        sendAndValidate(String.format("PASS %s", password));

        state = State.TRANSACTION;
        logger.info("User has been authenticated");
    }

    public void logout() throws IOException {
        sendAndValidate("QUIT");
        logger.info("User has logged out");
    }

    public StatsModel stats() throws IOException {
        var response = sendAndParse("STAT");

        try {
            var stats = response.split(" ");

            return new StatsModel(Integer.parseInt(stats[0]), Integer.parseInt(stats[1]) / 8);
        } catch (NumberFormatException e) {
            throw new InvalidSpecificationException("Stats response provided incorrect response format", e);
        }
    }

    public MessageInfoModel[] info() throws IOException {
        var response = sendAndParse("LIST");

        try {
            var listing = response.split(" ");
            var messages = Integer.parseInt(listing[0]);
            var infos = new ArrayList<MessageInfoModel>(messages);

            for (int i = 0; i < messages; i++) {
                var info = readResponse()
                        .split(" ");

                infos.add(new MessageInfoModel(Integer.parseInt(info[0]), Integer.parseInt(info[1]) / 8));
            }

            readResponse();
            return infos.toArray(new MessageInfoModel[0]);
        } catch (NumberFormatException e) {
            throw new InvalidSpecificationException("Info response provided incorrect response format", e);
        }
    }

    public MessageInfoModel info(int messageNumber) throws IOException {
        var response = sendAndParse(String.format("LIST %d", messageNumber));

        try {
            var listing = response.split(" ");

            return new MessageInfoModel(Integer.parseInt(listing[0]), Integer.parseInt(listing[1]) / 8);
        } catch (NumberFormatException e) {
            throw new InvalidSpecificationException("Info response provided incorrect response format", e);
        }
    }

    public void delete(int messageNumber) throws IOException {
        sendAndValidate(String.format("DELE %d", messageNumber));
    }

    public void reset() throws IOException {
        sendAndValidate("RSET");
    }

    private void sendCommand(String command) throws IOException {
        writer.write(command);
        writer.write("\r\n");
        writer.flush();
    }

    private static void validateResponse(String response) {
        if (response.startsWith("-ERR")) {
            throw new ErrorResponseException(response.substring(Math.min(response.length(), 5)));
        }
    }

    private String readResponse() throws IOException {
        return reader.readLine();
    }

    private String readResponseAndParse() throws IOException {
        var response = readResponse();
        validateResponse(response);

        return response.substring(Math.min(response.length(), 4));
    }

    private String sendAndParse(String command) throws IOException {
        sendCommand(command);
        return readResponseAndParse();
    }

    private void readResponseAndValidate() throws IOException {
        var response = readResponse();
        validateResponse(response);
    }

    private void sendAndValidate(String command) throws IOException {
        sendCommand(command);
        readResponseAndValidate();
    }
}