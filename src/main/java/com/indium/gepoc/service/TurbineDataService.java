package com.indium.gepoc.service;

import com.indium.gepoc.dto.TurbineDataDTO;
import com.indium.gepoc.util.IoTDataGenerator;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Service
public class TurbineDataService {

    private final InfluxDBClient influxDBClient;

    public TurbineDataService(
            @Value("${influxdb.url}") String url,
            @Value("${influxdb.token}") String token,
            @Value("${influxdb.org}") String org
    ) {
        this.influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org);
    }

    public List<TurbineDataDTO> fetchDataByTag(String measurement, String tagName, String tagValue, String timeRange) {
        String fluxQuery = String.format(
                "from(bucket: \"GE\")\n" +
                        "  |> range(start: -%s)\n" +
                        "  |> filter(fn: (r) => r._measurement == \"%s\" and r[\"%s\"] == \"%s\")",
                timeRange, measurement, tagName, tagValue
        );

        List<FluxRecord> records;
        List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery);

        List<TurbineDataDTO> turbineDataDTOS = new ArrayList<>();

        if(tables.size()==0){
            return turbineDataDTOS;
        }

        for (int i = 0; i < 7; i++) {
            records = tables.get(i).getRecords();
            int count = 0;
            if (i == 0) {
                for (FluxRecord record : records) {
                    TurbineDataDTO data = new TurbineDataDTO();
                    data.setAssetId(tagValue);
                    // data.setAnomaly(Boolean.parseBoolean(record.getValue().toString()));
                    data.setDateTime(Date.from(record.getTime()));
                    turbineDataDTOS.add(data);
                }
            } else {
                for (FluxRecord record : records) {
                    TurbineDataDTO data = turbineDataDTOS.get(count);
                    count++;
                    if (record.getField().equals("rotorSpeed")) {
                        data.setRotorSpeed(Integer.parseInt(record.getValue().toString()));
                    } else if (record.getField().equals("vibration")) {
                        data.setVibration(Double.parseDouble(record.getValue().toString()));
                    } else if (record.getField().equals("temperature")) {
                        data.setTemperature(Integer.parseInt(record.getValue().toString()));
                    } else if (record.getField().equals("powerOutput")) {
                        data.setPowerOutput(Double.parseDouble(record.getValue().toString()));
                    } else if (record.getField().equals("windSpeed")) {
                        data.setWindSpeed(Double.parseDouble(record.getValue().toString()));
                    } else if (record.getField().equals("windDirection")) {
                        data.setWindDirection(Integer.parseInt(record.getValue().toString()));
                    }
                }
            }
        }
        return turbineDataDTOS;
    }
    public static List<TurbineDataDTO> downsampleData(List<TurbineDataDTO> turbineDataDTOList, String splitTime) {
        // Group data by timestamp (assuming timestamps are in milliseconds)
        long splitTimeMillis = parseTime(splitTime);
        Map<Long, List<TurbineDataDTO>> groupedData = turbineDataDTOList.stream()
                .collect(Collectors.groupingBy(dto -> dto.getDateTime().getTime() / splitTimeMillis));

        // Aggregate data within each group
        List<TurbineDataDTO> aggregatedData = groupedData.values().stream()
                .map(group -> {
                    TurbineDataDTO aggregatedDTO = new TurbineDataDTO();
                    aggregatedDTO.setAssetId(group.get(0).getAssetId());
                    aggregatedDTO.setDateTime(new Date(group.get(0).getDateTime().getTime())); // Use the timestamp of the first item in the group

                    // Aggregate integer and float fields
                    aggregatedDTO.setRotorSpeed(averageInt(group, TurbineDataDTO::getRotorSpeed));
                    aggregatedDTO.setTemperature(averageInt(group, TurbineDataDTO::getTemperature));
                    aggregatedDTO.setWindDirection(averageInt(group, TurbineDataDTO::getWindDirection));
                    aggregatedDTO.setPowerOutput(averageDouble(group, TurbineDataDTO::getPowerOutput));
                    aggregatedDTO.setVibration(averageDouble(group, TurbineDataDTO::getVibration));
                    aggregatedDTO.setWindSpeed(averageDouble(group, TurbineDataDTO::getWindSpeed));

                    return aggregatedDTO;
                })
                .collect(Collectors.toList());

        return aggregatedData;
    }

    // Helper method to calculate the average of integer values in a list
    private static int averageInt(List<TurbineDataDTO> data, ToIntFunction<TurbineDataDTO> mapper) {
        return (int) data.stream()
                .mapToInt(mapper)
                .average()
                .orElse(0.0);
    }

    // Helper method to calculate the average of double values in a list
    private static double averageDouble(List<TurbineDataDTO> data, ToDoubleFunction<TurbineDataDTO> mapper) {
        return data.stream()
                .mapToDouble(mapper)
                .average()
                .orElse(0.0);
    }

    public List<TurbineDataDTO> fetchDataForAllAssets(String timeRange, String splitTime, String assetId, String api) {
        List<String> allAssetIds = new ArrayList<>();

        if(assetId.equals("all")){
            allAssetIds = new ArrayList<String>(Arrays.asList("T001","T002","T003","T004","T005","T006","T007","T008","T009","T0010"));
        }
        else {
            allAssetIds = new ArrayList<String>(Arrays.asList(assetId));
        }

        //List<TurbineDataDTO> allTurbineData = new ArrayList<>();
        List<TurbineDataDTO> downsampledData = new ArrayList<>();

        for (String asset : allAssetIds) {
            List<TurbineDataDTO> assetData = fetchDataByTag("iot", "assetId", asset, timeRange);
            //allTurbineData.addAll(assetData);
            if(api.equals("liveData")){
                downsampledData.addAll(assetData);
            }
            else {
                downsampledData.addAll(downsampleData(assetData, splitTime));
            }
        }

        return downsampledData;
    }

    private static long parseTime(String time) {
        return Long.parseLong(time.substring(0, time.length() - 1)) * 60000;
    }

}
