package app;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("app")
@Component
@Data
public class AppProperties {
    String topic;
    /**
     * If greater than zero, then the consumer will pause after
     * the given number of messages is received.
     */
    int autoStopLimit;
}
