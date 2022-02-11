package org.guge.pop3;

public class Main {
    public static void main(String[] args) {
        var builder = new Pop3ClientBuilder()
                .useHost("pop.gmail.com")
                .usePort(995)
                .useCredentials(System.getenv("USERNAME"), System.getenv("PASSWORD"))
                .useSSL(true);

        try (var client = builder.build()) {
            var message = client.message(1);
            System.out.println(message);

            client.logout();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}