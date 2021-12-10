package app;

import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Slurper {

    private static final String LISTENER_ID = "slurper";

    private final Semaphore autoStopCountdown;
    private final String topic;
    private final KafkaListenerEndpointRegistry listenerEndpointRegistry;

    public Slurper(AppProperties appProperties, KafkaListenerEndpointRegistry listenerEndpointRegistry) {
        this.listenerEndpointRegistry = listenerEndpointRegistry;
        if (appProperties.getAutoStopLimit() > 0) {
            autoStopCountdown = new Semaphore(appProperties.getAutoStopLimit());
            log.info("Will stop after consuming {} messages",
                appProperties.getAutoStopLimit());
        }
        else {
            autoStopCountdown = null;
        }
        topic = appProperties.getTopic();
    }

    public String getTopic() {
        return topic;
    }

    @KafkaListener(id = LISTENER_ID, idIsGroup = false,
        topics = "#{__listener.topic}"
    )
    public void consume(byte[] rawMessage) {
        log.info("Consumed message len={}", rawMessage.length);

        if (autoStopCountdown != null && !autoStopCountdown.tryAcquire()) {
            log.info("Got enough messages. Pausing our listener container");
            final MessageListenerContainer container = listenerEndpointRegistry.getListenerContainer(LISTENER_ID);
            if (container == null) {
                throw new IllegalStateException("Unable to find our listener container");
            }

            container.pause();
        }
    }
}
