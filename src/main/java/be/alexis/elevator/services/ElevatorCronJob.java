package be.alexis.elevator.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElevatorCronJob {

    Logger logger = LoggerFactory.getLogger(ElevatorCronJob.class);

    private final ElevatorService elevatorService;

    @Scheduled(fixedDelay = 3000) // trigger every 2 seconds
    public void runElevators(){
        elevatorService.moveElevatorsToNextFloor();
    }
}
