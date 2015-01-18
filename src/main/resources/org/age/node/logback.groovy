package org.age.node

def bySecond = timestamp("yyyyMMdd'T'HHmmss")

appender("FILE", FileAppender) {
    file = "node-${bySecond}.log"
    append = false
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{40} - %msg%n"
    }
}

appender("CONSOLE", ConsoleAppender) {
    filter(ch.qos.logback.classic.filter.ThresholdFilter) {
        level = INFO
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%highlight(%.-1level) %green(%-40logger{39}) : %msg%n"
    }
}

root(ALL, ["FILE"])
logger("org.age", DEBUG, ["CONSOLE"])
logger("com.hazelcast", INFO)
logger("org.springframework", INFO)
