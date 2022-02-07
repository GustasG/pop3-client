package org.guge.pop3;

import java.io.*;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.guge.pop3.errors.ErrorResponseException;

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
        var usernameResponse = sendAndParse(String.format("USER %s", username));
        logger.debug("Server response after sending USER command: {}", usernameResponse);

        var passwordResponse = sendAndParse(String.format("PASS %s", password));
        logger.debug("Server response after sending PASS command: {}", passwordResponse);

        state = State.TRANSACTION;
        logger.info("User has been authenticated");
    }

    public void logout() throws IOException {
        var response = sendAndParse("QUIT");
        state = State.AUTHORIZATION;

        logger.debug("Quit response: {}", response);
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

    private String readResponseAndParse() throws IOException {
        var response = reader.readLine();
        validateResponse(response);

        return response.substring(Math.min(response.length(), 4));
    }

    private String sendAndParse(String command) throws IOException {
        sendCommand(command);
        return readResponseAndParse();
    }

    private void readResponseAndValidate() throws IOException {
        var response = reader.readLine();
        validateResponse(response);
    }

    private void sendAndValidate(String command) throws IOException {
        sendCommand(command);
        readResponseAndValidate();
    }
}