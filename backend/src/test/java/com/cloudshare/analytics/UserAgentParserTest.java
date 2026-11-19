package com.cloudshare.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAgentParserTest {

    private UserAgentParser userAgentParser;

    @BeforeEach
    void setUp() {
        userAgentParser = new UserAgentParser();
    }

    @Test
    void parse_desktopChrome() {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        UserAgentParser.UserAgentInfo info = userAgentParser.parse(ua);

        assertNotNull(info);
        assertEquals("Chrome", info.browser());
        assertEquals("Windows", info.os());
        assertEquals("desktop", info.deviceType());
    }

    @Test
    void parse_mobileSafari() {
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";
        UserAgentParser.UserAgentInfo info = userAgentParser.parse(ua);

        assertNotNull(info);
        assertEquals("Mobile Safari", info.browser());
        assertEquals("iOS", info.os());
        assertEquals("mobile", info.deviceType());
    }

    @Test
    void parse_bot() {
        String ua = "curl/7.88.1";
        UserAgentParser.UserAgentInfo info = userAgentParser.parse(ua);

        assertNotNull(info);
        assertEquals("bot", info.deviceType());
    }

    @Test
    void parse_emptyOrNull() {
        UserAgentParser.UserAgentInfo info = userAgentParser.parse(null);
        assertNotNull(info);
        assertEquals("Unknown", info.browser());
        assertEquals("desktop", info.deviceType());
    }
}
