package org.guge.pop3;

public class Main {
    public static void main(String[] args) {
        try {
            var client = new Pop3ClientBuilder()
                    .useHost("pop.gmail.com")
                    .usePort(995)
                    .useCredentials(System.getenv("USERNAME"), System.getenv("PASSWORD"))
                    .useSSL(true)
                    .build();

            for (var u : client.info()) {
                System.out.println(u);
            }

            for (var u : client.uidl()) {
                System.out.println(u);
            }

            System.out.println(client.stats());
            System.out.println(client.uidl(1));

            client.noop();

            client.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}