package app;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Slurper {

    private static final String LISTENER_ID = "slurper";

    private final Semaphore autoStopCountdown;
    private final String topic;
    private final KafkaListenerEndpointRegistry listenerEndpointRegistry;
    private final Instant startTime;
    private final AtomicInteger messageCount;
    private final AtomicLong serializedBytesCount;

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

        startTime = Instant.now();
        messageCount = new AtomicInteger();
        serializedBytesCount = new AtomicLong();
    }

    public String getTopic() {
        return topic;
    }

    @Scheduled(fixedDelayString = "#{appProperties.progressLogsInterval}")
    void logProgress() {
        final int currentMessageCount = messageCount.get();
        final long currentBytes = serializedBytesCount.get();

        final long duration = Duration.between(startTime, Instant.now()).toSeconds();
        log.info("PROGRESS: {} messages at {} msg/sec, {} byte/sec",
            currentMessageCount,
            currentMessageCount / duration,
            currentBytes / duration
        );
    }

    @KafkaListener(id = LISTENER_ID, idIsGroup = false,
        topics = "#{__listener.topic}"
    )
    public void consume(String message, ConsumerRecordMetadata metadata) {
        if (log.isTraceEnabled()) {
            log.trace("Consumed messages={}", message);
        } else if (log.isDebugEnabled()) {
            log.debug("Consumed message len={}", message.length());
        }

        if (autoStopCountdown != null && !autoStopCountdown.tryAcquire()) {
            log.info("Got enough messages. Pausing our listener container");
            final MessageListenerContainer container = listenerEndpointRegistry.getListenerContainer(LISTENER_ID);
            if (container == null) {
                throw new IllegalStateException("Unable to find our listener container");
            }

            container.pause();
        }

        messageCount.incrementAndGet();
        serializedBytesCount.addAndGet(
            (long)metadata.serializedKeySize() + metadata.serializedValueSize()
        );
    }
}
