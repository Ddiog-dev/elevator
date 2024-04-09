package be.alexis.elevator.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Elevator {

    private int elevatorId;

    private Direction direction;

    private int currentPosition;

    private int calledFromFloor;

    private int moveToFloor;

    private List<User> users;
}
