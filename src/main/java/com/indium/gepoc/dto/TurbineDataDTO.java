package com.indium.gepoc.dto;

import lombok.*;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
@Measurement(name = "iot")
public class TurbineDataDTO {

    @Column(name = "assetId")
    private String assetId;
    @Column(name = "rotorSpeed")
    private int rotorSpeed;
    @Column(name = "vibration")
    private double vibration;
    @Column(name = "temperature")
    private int temperature;
    @Column(name = "powerOutput")
    private double powerOutput;
    @Column(name = "windSpeed")
    private double windSpeed;
    @Column(name = "windDirection")
    private int windDirection;
    @Column(name="dateTime")
    private Date dateTime;

}
