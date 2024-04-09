package be.alexis.elevator.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ElevatorCall {
    int floor;
    Direction direction;
}
