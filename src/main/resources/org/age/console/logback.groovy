def bySecond = timestamp("yyyyMMdd'T'HHmmss")

appender("FILE", FileAppender) {
    file = "console-${bySecond}.log"
    append = false

    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

root(DEBUG, ["FILE"])
logger("com.hazelcast", INFO)
logger("org.spring", INFO)
