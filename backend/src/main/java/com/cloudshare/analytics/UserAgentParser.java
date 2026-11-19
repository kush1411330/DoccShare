package com.cloudshare.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

@Component
public class UserAgentParser {

    private static final Logger log = LoggerFactory.getLogger(UserAgentParser.class);

    private final Parser uaParser;

    public UserAgentParser() {
        this.uaParser = new Parser();
    }

    public record UserAgentInfo(String browser, String os, String deviceType) {}

    public UserAgentInfo parse(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return new UserAgentInfo("Unknown", "Unknown", "desktop");
        }

        try {
            Client client = uaParser.parse(userAgentString);
            String browser = client.userAgent != null && client.userAgent.family != null
                    ? client.userAgent.family
                    : "Unknown";

            String os = client.os != null && client.os.family != null
                    ? client.os.family
                    : "Unknown";

            String device = client.device != null && client.device.family != null
                    ? client.device.family
                    : "Unknown";

            String deviceType = detectDeviceType(userAgentString, device, os);

            return new UserAgentInfo(browser, os, deviceType);
        } catch (Exception e) {
            log.warn("Error parsing user-agent {}: {}", userAgentString, e.getMessage());
            return new UserAgentInfo("Unknown", "Unknown", "desktop");
        }
    }

    private String detectDeviceType(String ua, String deviceFamily, String osFamily) {
        String lowerUa = ua.toLowerCase();
        String lowerOs = osFamily.toLowerCase();

        if (lowerUa.contains("bot") || lowerUa.contains("crawler") || lowerUa.contains("spider") || lowerUa.contains("curl") || lowerUa.contains("wget")) {
            return "bot";
        }
        if (lowerUa.contains("ipad") || lowerUa.contains("tablet") || lowerUa.contains("kindle") || (lowerOs.contains("android") && !lowerUa.contains("mobile"))) {
            return "tablet";
        }
        if (lowerUa.contains("mobi") || lowerUa.contains("iphone") || lowerUa.contains("android") || lowerUa.contains("phone")) {
            return "mobile";
        }
        return "desktop";
    }
}
