package com.doccshare.analytics;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.InetAddress;

@Service
public class GeoIpService {

    private static final Logger log = LoggerFactory.getLogger(GeoIpService.class);

    private final ResourceLoader resourceLoader;
    private final String geoipDbPath;
    private DatabaseReader reader;

    public GeoIpService(
            ResourceLoader resourceLoader,
            @Value("${DoccShare.app.geoip-db-path:}") String geoipDbPath
    ) {
        this.resourceLoader = resourceLoader;
        this.geoipDbPath = geoipDbPath;
    }

    public record GeoLocation(String countryCode, String countryName, String city) {}

    @PostConstruct
    public void init() {
        if (geoipDbPath == null || geoipDbPath.isBlank()) {
            log.info("GeoIP database path not specified. GeoIP lookup will be disabled (fallback to Unknown).");
            return;
        }

        try {
            Resource resource = resourceLoader.getResource(geoipDbPath);
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    this.reader = new DatabaseReader.Builder(inputStream).build();
                    log.info("Successfully loaded MaxMind GeoIP database from: {}", geoipDbPath);
                }
            } else {
                log.info("MaxMind GeoIP database file not found at: {}. Fallback enabled.", geoipDbPath);
            }
        } catch (Exception e) {
            log.warn("Could not initialize MaxMind GeoIP database from {}: {}. Fallback enabled.", geoipDbPath, e.getMessage());
        }
    }

    @PreDestroy
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                log.error("Error closing GeoIP reader", e);
            }
        }
    }

    public GeoLocation resolve(String ipAddress) {
        if (reader == null || ipAddress == null || ipAddress.isBlank()) {
            return new GeoLocation("XX", "Unknown", "Unknown");
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            if (inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isLinkLocalAddress()) {
                return new GeoLocation("LOCAL", "Local Network", "Localhost");
            }

            CityResponse response = reader.city(inetAddress);
            String countryCode = response.getCountry() != null && response.getCountry().getIsoCode() != null
                    ? response.getCountry().getIsoCode()
                    : "XX";

            String countryName = response.getCountry() != null && response.getCountry().getName() != null
                    ? response.getCountry().getName()
                    : "Unknown";

            String city = response.getCity() != null && response.getCity().getName() != null
                    ? response.getCity().getName()
                    : "Unknown";

            return new GeoLocation(countryCode, countryName, city);
        } catch (Exception e) {
            log.debug("GeoIP lookup failed for {}: {}", ipAddress, e.getMessage());
            return new GeoLocation("XX", "Unknown", "Unknown");
        }
    }
}

