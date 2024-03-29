package pt.isel.disco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DiscoApplication {

    public static void main(String[] args) {
        /**
         * Limit request handler threads to a single worker thread.
         * Thus, handlers should be non-blocking to allow multiple requests concurrently.
         * NOTICE minimum default to 4.
         */
        System.setProperty("reactor.netty.ioWorkerCount", "1"); // minimum default to 4
        SpringApplication.run(DiscoApplication.class, args);
    }
}

