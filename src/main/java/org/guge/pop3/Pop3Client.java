package org.guge.pop3;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.guge.pop3.models.Message;
import org.guge.pop3.models.UIDLModel;
import org.guge.pop3.models.StatsModel;
import org.guge.pop3.models.MessageInfoModel;
import org.guge.pop3.errors.ErrorResponseException;
import org.guge.pop3.errors.InvalidSpecificationException;

public class Pop3Client implements Closeable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Pop3Client.class);

    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public Pop3Client(Socket socket) throws IOException {
        this.socket = socket;

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        var greeting = readResponseAndTrim();
        logger.debug("Connection established. Server greeting: {}", greeting);
    }

    @Override
    public void close() throws IOException {
        socket.close();
        reader.close();
        writer.close();
    }

    public void authenticate(String username, String password) throws IOException {
        sendCommandAndValidate(String.format("USER %s", username));
        sendCommandAndValidate(String.format("PASS %s", password));

        logger.debug("User has been authenticated");
    }

    public void logout() throws IOException {
        sendCommandAndValidate("QUIT");
        logger.debug("User has logged out");
    }

    public void noop() throws IOException {
        sendCommandAndValidate("NOOP");
    }

    public StatsModel stats() throws IOException {
        var response = sendCommandAndTrim("STAT");

        try {
            var listing = response.split(" ");

            return new StatsModel(Integer.parseInt(listing[0]), Integer.parseInt(listing[1]));
        } catch (NumberFormatException e) {
            throw new InvalidSpecificationException("Server provided incorrect response format", e);
        }
    }

    private static MessageInfoModel createInfoModel(String response) {
        try {
            var listing = response.split(" ");

            return new MessageInfoModel(Integer.parseInt(listing[0]), Integer.parseInt(listing[1]));
        } catch (NumberFormatException e) {
            throw new InvalidSpecificationException("Server provided incorrect response format", e);
        }
    }

    public MessageInfoModel[] info() throws IOException {
        var response = sendCommandAndTrim("LIST");

        try {
            var listing = response.split(" ");
            var messages = Integer.parseInt(listing[0]);
            var infos = new ArrayList<MessageInfoModel>(messages);

            for (int i = 0; i < messages; i++) {
                var currentListing = readResponse();
                infos.add(createInfoModel(currentListing));
            }

            validateTerminatingOctet();
            return infos.toArray(new MessageInfoModel[0]);
        } catch (NumberFormatException e) {
            throw new InvalidSpecificationException("Server provided incorrect response format", e);
        }
    }

    public MessageInfoModel info(int messageNumber) throws IOException {
        var response = sendCommandAndTrim(String.format("LIST %d", messageNumber));

        return createInfoModel(response);
    }

    private Map<String, String> readHeaders() throws IOException {
        var lines = new ArrayList<String>();
        String line;

        while (!(line = readResponse()).isEmpty()) {
            if (Character.isLetter(line.charAt(0))) {
                lines.add(line);
            } else {
                var currentLine = lines.get(lines.size() - 1);
                lines.set(lines.size() - 1, currentLine + line.strip());
            }
        }

        return lines.stream()
                .map(l -> l.split(": ", 2))
                .collect(Collectors.toMap(s -> s[0], s -> s[1], (s1, s2) -> s1));
    }

    public Message message(int messageNumber) throws IOException {
        sendCommandAndValidate(String.format("RETR %d", messageNumber));
        var headers = readHeaders();
        var builder = new StringBuilder();
        String line;

        while (!(line = readResponse()).equals(".")) {
            builder.append(line);
        }

        return new Message(headers, builder.toString());
    }

    public void delete(int messageNumber) throws IOException {
        sendCommandAndValidate(String.format("DELE %d", messageNumber));
    }

    public void reset() throws IOException {
        sendCommandAndValidate("RSET");
    }

    private UIDLModel createUidlModel(String response) {
        try {
            var listing = response.split(" ");
            return new UIDLModel(Integer.parseInt(listing[0]), listing[1]);
        } catch (NumberFormatException e) {
            throw new InvalidSpecificationException("Server provided incorrect response format", e);
        }
    }

    public UIDLModel[] uidl() throws IOException {
        sendCommandAndValidate("UIDL");
        var uidls = new ArrayList<UIDLModel>();

        try {
            String response;

            while (!(response = readResponse()).equals(".")) {
                uidls.add(createUidlModel(response));
            }

            return uidls.toArray(new UIDLModel[0]);
        } catch (NumberFormatException e) {
            throw new InvalidSpecificationException("Server provided incorrect response format", e);
        }
    }

    public UIDLModel uidl(int messageNumber) throws IOException {
        var response = sendCommandAndTrim(String.format("UIDL %d", messageNumber));

        return createUidlModel(response);
    }

    public void setTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
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

    private String readResponseAndTrim() throws IOException {
        var response = readResponse();
        validateResponse(response);

        return response.substring(Math.min(response.length(), 4));
    }

    private String sendCommandAndTrim(String command) throws IOException {
        sendCommand(command);
        return readResponseAndTrim();
    }

    private void readResponseAndValidate() throws IOException {
        var response = readResponse();
        validateResponse(response);
    }

    private void sendCommandAndValidate(String command) throws IOException {
        sendCommand(command);
        readResponseAndValidate();
    }

    private void validateTerminatingOctet() throws IOException {
        var response = readResponse();

        if (!response.equals(".")) {
            throw new InvalidSpecificationException("Server stream did not end with termination octet");
        }
    }
}