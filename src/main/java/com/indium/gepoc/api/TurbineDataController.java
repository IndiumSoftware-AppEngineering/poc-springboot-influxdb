package com.indium.gepoc.api;


import com.indium.gepoc.dto.TurbineDataDTO;
import com.indium.gepoc.service.TurbineDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.time.Duration;
import java.util.List;



@RestController
public class TurbineDataController {

    @Autowired
    private TurbineDataService service;

    @GetMapping("/fetchByTag")
    public List<TurbineDataDTO> fetchDataByTag(
            @RequestParam(name = "timeRange") String timeRange,
            @RequestParam(name = "splitTime") String splitTime,
            @RequestParam(name = "assetId") String assetId
    ) {
        //return Flux.interval(Duration.ofSeconds(1)).flatMap(ignore -> (Publisher<? extends List<TurbineDataDTO>>) service.fetchDataForAllAssets(timeRange, splitTime, assetId));
        return service.fetchDataForAllAssets(timeRange, splitTime, assetId, "fetchByTag");
    }


    @GetMapping("/liveData")
    public Flux<TurbineDataDTO> streamTurbineData(
            @RequestParam(name = "assetId") String assetId
    ) {
        return Flux.interval(Duration.ofSeconds(1)) // Emit data every 1 second
                .flatMap(tick -> {
                    // Fetch turbine data from InfluxDB here and convert it to TurbineData objects
                    List<TurbineDataDTO> turbineDataList = service.fetchDataForAllAssets("1s", "1s", assetId, "liveData");
                    return Flux.fromIterable(turbineDataList);
                });
    }
}
