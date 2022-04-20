package org.example;

import java.time.Instant;
import java.util.List;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.WritePrecision;

public class ClientExample {

    @Measurement(name = "temperature")
    public static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;

        @Override
        public String toString() {
            return "Temperature{" +
                    "location='" + location + '\'' +
                    ", value=" + value +
                    ", time=" + time +
                    '}';
        }
    }

    public static void main(final String[] args) {

        InfluxDBClientOptions options = InfluxDBClientOptions
                .builder()
                .url("http://localhost:8086")
                .bucket("my-bucket")
                .org("my-org")
                .authenticateToken("my-token".toCharArray())
                .build();

        try (InfluxDBClient client = InfluxDBClientFactory.create(options)) {

            //
            // Write data
            //
            Temperature temperature = new Temperature();
            temperature.location = "south";
            temperature.value = 62D;
            temperature.time = Instant.now();
            client.getWriteApiBlocking().writeMeasurement(WritePrecision.NS, temperature);

            //
            // Read data
            //
            String flux = "from(bucket:\"" + options.getBucket() + "\") |> range(start: 0) |> filter(fn: (r) => r._measurement == \"temperature\")";
            List<Temperature> temperatures = client.getQueryApi().query(flux, Temperature.class);
            temperatures.forEach(it -> System.out.println("temperature = " + temperature));
        }
    }
}
