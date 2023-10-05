package com.indium.gepoc.util;

import com.indium.gepoc.dto.TurbineDataDTO;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import io.gsonfire.util.JsonUtils;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class IoTDataGenerator {
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final int TOTAL_ASSETS = 10;  // Adjust this to the number of turbines you want

    private static char[] token = "rEf2n9Ndc7i_PaGEYyeUQMTULxBZ_J6_vyyyvcBOJnl0uNDcL1FjzZF5nYbBScdRf6wpb19FPqIg4MEndYJwLA==".toCharArray();
    private static String org = "26fd3628f583796e";
    private static String bucket = "GE";

    public void getData() {
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);

            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            for (int asset = 1; asset <= TOTAL_ASSETS; asset++) {
                for(int i=0;i<1;i++) {
                    generateRecord(("T00" + asset), writeApi);
                }
            }
    }

    private void generateRecord(String assetId, WriteApiBlocking api) {
        Random rand = new Random();


        Date timestamp = new Date();
        int rotorSpeed = 45 + rand.nextInt(20); // Random rotor speed between 45 and 65
        double vibration = 1 + 2 * rand.nextDouble(); // Random vibration between 1 and 3
        int temperature = 20 + rand.nextInt(10); // Random temperature between 20 and 30
        double powerOutput = 1.5 + rand.nextDouble(); // Random power output between 1.5 and 2.5
        double windSpeed = 5 + 2 * rand.nextDouble(); // Random wind speed between 5 and 7
        int windDirection = rand.nextInt(360); // Random wind direction between 0 and 360
        boolean isAnomaly = rand.nextDouble() < 0.1;  // 10% chance for an anomaly

        Point point = Point.measurement("iot")
                .addTag("assetId", assetId)
                .addField("rotorSpeed", rotorSpeed)
                .addField("vibration", vibration)
                .addField("temperature", temperature)
                .addField("powerOutput",powerOutput)
                .addField("windSpeed",windSpeed)
                .addField("windDirection",windDirection)
                .addField("_time", String.valueOf(timestamp));
               // .addField("isAnomaly",isAnomaly);

        api.writePoint(point);
        //return new TurbineDataDTO(assetId, rotorSpeed, vibration, temperature, powerOutput, windSpeed, windDirection,anomaly);
    }
}
