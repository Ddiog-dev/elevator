package be.alexis.elevator.controllers;

import be.alexis.elevator.models.Direction;
import be.alexis.elevator.models.Elevator;
import be.alexis.elevator.services.ElevatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FloorController {

    private final ElevatorService elevatorService;

    @GetMapping("/call-elevator")
    public int callElevator(@RequestParam int floor, @RequestParam String direction) {
        return elevatorService.callElevator(floor, Direction.valueOf(direction), false);
    }

    @GetMapping("/set-floor")
    public void chooseFloor(@RequestParam int floor, @RequestParam int elevatorId) {
        Optional<Elevator> elevator = elevatorService.getElevator(elevatorId);
        elevator.map(elevator1 -> {
                    elevatorService.selectFloorForUser(elevator1, floor, false);
                    return elevator1;
                })
                .orElseThrow(IllegalArgumentException::new);
    }
    @PreAuthorize("hasRole('FIREFIGHTER')")
    @GetMapping("/firefighter/call-elevator")
    public int callElevatorFireFighter(@RequestParam int floor, @RequestParam String direction) {
        return elevatorService.callElevator(floor, Direction.valueOf(direction), true);
    }

     @PreAuthorize("hasRole('FIREFIGHTER')")
    @GetMapping("/firefighter/set-floor")
    public void chooseFloorFireFighter(@RequestParam int floor, @RequestParam int elevatorId) {
        Optional<Elevator> elevator = elevatorService.getElevator(elevatorId);
        elevator.map(elevator1 -> {
                    elevatorService.selectFloorForUser(elevator1, floor, true);
                    return elevator1;
                })
                .orElseThrow(IllegalArgumentException::new);
    }


}
