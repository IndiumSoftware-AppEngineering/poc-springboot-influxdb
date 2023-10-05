package com.indium.gepoc;

import com.indium.gepoc.dto.TurbineDataDTO;
import com.indium.gepoc.util.IoTDataGenerator;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.query.FluxTable;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import com.influxdb.client.write.Point;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.indium.*")
public class GepocApplication {
	public static void main(String[] args) {
		SpringApplication.run(GepocApplication.class, args);
		IoTDataGenerator ioTDataGenerator = new IoTDataGenerator();
		long endTime = System.currentTimeMillis() + (6 * 60 * 1000);
		while (System.currentTimeMillis() < endTime) {
			try {
				ioTDataGenerator.getData();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done: "+System.currentTimeMillis());

	}

}
