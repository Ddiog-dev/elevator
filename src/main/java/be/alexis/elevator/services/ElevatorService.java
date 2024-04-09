package be.alexis.elevator.services;

import be.alexis.elevator.models.Direction;
import be.alexis.elevator.models.Elevator;
import be.alexis.elevator.models.User;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ElevatorService {

    private final int MOVING_TIME = 1;

    Elevator[] elevators = new Elevator[]{new Elevator(0, Direction.STATIONARY, 0, 0, 0, new ArrayList<>())};


    public void callElevator(int floor, Direction direction) {
        Optional<Elevator> elevator = Arrays.stream(elevators)
                .filter(elevatorElement -> elevatorAvailable(elevatorElement, floor, direction))
                .findFirst();
        elevator
                .map(elevator1 -> {
                    moveToCallerFloor(elevator1, floor);
                    return elevator1;
                })
                .orElseThrow(() -> new IllegalArgumentException("No elevators available"));
    }

    /**
     * Move the elevator to the floor of a caller
     * @param elevator
     * @param floor
     */
    public void moveToCallerFloor(Elevator elevator, int floor) {
        if (elevator.getDirection().equals(Direction.STATIONARY)) {
            moveToFloor(elevator, floor);
        }
    }

    /**
     * The user in the elevator selects a floor
     * @param elevator
     * @param destinationFloor
     */
    public void selectFloor(Elevator elevator, int destinationFloor){
        elevator.getUsers().add(new User(destinationFloor));
        System.out.println("User enters elevator " + elevator.getElevatorId());
        moveToFloor(elevator, destinationFloor);
    }

    /**
     * Set the direction and the destination floor of the elevator
     * @param elevator
     * @param destinationFloor
     */
    private void moveToFloor(Elevator elevator, int destinationFloor) {
        elevator.setDirection(destinationFloor >= elevator.getCurrentPosition() ? Direction.UP : Direction.DOWN);
        if (elevator.getDirection().equals(Direction.UP)) {
            elevator.setMoveToFloor(Math.max(destinationFloor, elevator.getMoveToFloor()));
        } else {
            elevator.setMoveToFloor(Math.min(destinationFloor, elevator.getMoveToFloor()));
        }
        movingLoop(elevator);
    }

    public Optional<Elevator> getElevator(int id){
        return Arrays.stream(elevators).filter(elevator -> elevator.getElevatorId() == id).findFirst();
    }

    /**
     * Move the elevator to the destination floor
     * @param elevator
     */
    private void movingLoop(Elevator elevator) {
        while (elevator.getMoveToFloor() != elevator.getCurrentPosition()) {
            if (elevator.getDirection().equals(Direction.UP)) {
                elevator.setCurrentPosition(elevator.getCurrentPosition() + 1);
            } else if (elevator.getDirection().equals(Direction.DOWN)) {
                elevator.setCurrentPosition(elevator.getCurrentPosition() - 1);
            } else {
                System.err.println("Elevator with direction is marked stationary");
                break; // WRONG INPUT
            }
            try {
                TimeUnit.SECONDS.sleep(MOVING_TIME);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Elevator " + elevator.getElevatorId() + " is at floor " + elevator.getCurrentPosition());
            List<User> users = elevator.getUsers().stream().filter(user -> user.getDestinationFloor() == elevator.getCurrentPosition()).collect(Collectors.toList());
            if(!users.isEmpty()){
                elevator.getUsers().removeAll(users);
                System.out.println(users.size() + " users left the elevator, currently " + elevator.getUsers().size() + " users in the elevator");
            }

        }
        elevator.setDirection(Direction.STATIONARY);
    }

    /**
     * Find and elevator stationary or going the same way
     * @param elevatorElement the elevator we are inspection
     * @param floor floor the user called the elevator at
     * @param direction direction the user wants to go
     * @return true if the elevator is passing by the user and going the same way or is stationary, false otherwise
     */
    private boolean elevatorAvailable(Elevator elevatorElement, int floor, Direction direction) {
        if (elevatorElement.getDirection().equals(Direction.STATIONARY)) {
            return true;
        }

        int floorDifference = floor - elevatorElement.getCurrentPosition();
        if (floorDifference == 0) {
            return elevatorElement.getDirection().equals(direction); // elevator is called at its floor and going the same way
        }
        if (floorDifference > 0) {// elevator is higher in the building than the floor
            return direction.equals(Direction.DOWN) && floor > elevatorElement.getMoveToFloor(); // CHeck if elevator is going down and does not stop before
        } else {
            return direction.equals(Direction.UP) && floor < elevatorElement.getMoveToFloor();
        }
    }


}
