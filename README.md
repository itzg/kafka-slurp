## Configuration

### Environment variables

#### Required
- `APP_TOPIC`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` : comma separated list of host:port

#### Optional for SSL
- `SPRING_KAFKA_SECURITY_PROTOCOL` : typically "SSL"
- `SPRING_KAFKA_SSL_KEY_PASSWORD`
- `SPRING_KAFKA_SSL_KEY_STORE_PASSWORD`
- `SPRING_KAFKA_SSL_KEY_STORE_TYPE` : typically "PKCS12"
- `SPRING_KAFKA_SSL_KEY_STORE_LOCATION` : `file:///` location of the mounted key store file
- `SPRING_KAFKA_SSL_TRUST_STORE_LOCATION` : `file:///` location of the mounted trust store file
- `SPRING_KAFKA_SSL_TRUST_STORE_PASSWORD`
