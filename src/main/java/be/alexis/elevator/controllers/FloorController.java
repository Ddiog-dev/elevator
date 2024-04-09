package be.alexis.elevator.controllers;

import be.alexis.elevator.models.Direction;
import be.alexis.elevator.models.Elevator;
import be.alexis.elevator.services.ElevatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FloorController {

    private final ElevatorService elevatorService;

    @GetMapping("/call-elevator")
    public String callElevator(@RequestParam int floor, @RequestParam String direction) {
        elevatorService.callElevator(floor, Direction.valueOf(direction));
        return "Hello world!";
    }

    @GetMapping("/set-floor")
    public String callElevator(@RequestParam int floor, @RequestParam int elevatorId) {
        Optional<Elevator> elevator = elevatorService.getElevator(elevatorId);
        elevator.map(elevator1 -> {
                    elevatorService.selectFloor(elevator1, floor);
                    return elevator1;
                })
                .orElseThrow(IllegalArgumentException::new);
        return "Hello world!";
    }
}
