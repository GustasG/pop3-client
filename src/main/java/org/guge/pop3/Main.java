package org.guge.pop3;

public class Main {
    public static void main(String[] args) {
        try {
            var client = new Pop3ClientFactory()
                    .useHost("pop.gmail.com")
                    .usePort(995)
                    .useCredentials(System.getenv("USERNAME"), System.getenv("PASSWORD"))
                    .useSSL(true)
                    .build();

            client.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}