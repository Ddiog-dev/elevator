package be.alexis.elevator.services;

import be.alexis.elevator.models.Direction;
import be.alexis.elevator.models.Elevator;
import be.alexis.elevator.models.ElevatorCall;
import be.alexis.elevator.models.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ElevatorService {

    private final int MAX_FLOOR = 50;

    private ExecutorService executor = Executors.newFixedThreadPool(3);

    Elevator[] elevators = new Elevator[]{new Elevator(0, Direction.STATIONARY, 0, 0, 0, new ArrayList<>())};

    List<ElevatorCall> waitingUsers = new ArrayList<>();

    /**
     * User calls the elevator to his floor and provides a direction
     *
     * @param floor
     * @param direction
     */
    public void callElevator(int floor, Direction direction) {
        if (floor == 0 && direction.equals(Direction.DOWN)) {
            System.err.println("user Cant go down when at bottom floor");
            return;
        }

        if (floor == MAX_FLOOR && direction.equals(Direction.UP)) {
            System.err.println("user Cant go up when at top floor");
            return;
        }

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
     * The user in the elevator selects a floor
     *
     * @param elevator
     * @param destinationFloor
     */
    public void selectFloor(Elevator elevator, int destinationFloor) {
        elevator.getUsers().add(new User(destinationFloor));
        System.out.println("User enters elevator " + elevator.getElevatorId());
        moveToFloor(elevator, destinationFloor);
    }


    public void workTheElevators() {
        Arrays.stream(elevators).forEach(elevator -> {
            if (!elevator.getDirection().equals(Direction.STATIONARY)) {
                movingElevator(elevator);
            }
        });
    }

    /**
     * Move the elevator to the floor of a caller
     *
     * @param elevator
     * @param floor
     */
    private void moveToCallerFloor(Elevator elevator, int floor) {
        if (elevator.getDirection().equals(Direction.STATIONARY)) {
            moveToFloor(elevator, floor);
        }
    }

    /**
     * Set the direction and the destination floor of the elevator
     *
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
    }

    public Optional<Elevator> getElevator(int id) {
        return Arrays.stream(elevators).filter(elevator -> elevator.getElevatorId() == id).findFirst();
    }

    /**
     * Move the elevator to the destination floor
     *
     * @param elevator
     */
    private void movingElevator(Elevator elevator) {

        if (elevator.getDirection().equals(Direction.UP)) {
            elevator.setCurrentPosition(elevator.getCurrentPosition() + 1);
        } else if (elevator.getDirection().equals(Direction.DOWN)) {
            elevator.setCurrentPosition(elevator.getCurrentPosition() - 1);
        }

        System.out.println("Elevator " + elevator.getElevatorId() + " is at floor " + elevator.getCurrentPosition());
        List<User> users = elevator.getUsers().stream().filter(user -> user.getDestinationFloor() == elevator.getCurrentPosition()).collect(Collectors.toList());
        if (!users.isEmpty()) {
            elevator.getUsers().removeAll(users);
            System.out.println(users.size() + " users left the elevator, currently " + elevator.getUsers().size() + " users in the elevator");
        }

        if (elevator.getMoveToFloor() == elevator.getCurrentPosition()) {
            elevator.setDirection(Direction.STATIONARY);
        }
    }

    /**
     * Find and elevator stationary or going the same way
     *
     * @param elevatorElement the elevator we are inspection
     * @param floor           floor the user called the elevator at
     * @param direction       direction the user wants to go
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
