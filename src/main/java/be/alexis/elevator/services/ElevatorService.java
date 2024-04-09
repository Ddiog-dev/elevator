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

    Elevator[] elevators = new Elevator[]{
            new Elevator(0, Direction.STATIONARY, 0, 0, 0, false, new ArrayList<>()),
            new Elevator(1, Direction.STATIONARY, 0, 0, 0, false, new ArrayList<>()),
            new Elevator(2, Direction.STATIONARY, 0, 0, 0, false, new ArrayList<>()),
    };

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
                    if (elevator1.getDirection().equals(Direction.STATIONARY)) {
                        moveToFloor(elevator1, floor);
                    }
                    waitingUsers.add(new ElevatorCall(floor, direction));
                    System.out.println("User waiting elevator " + elevator1.getElevatorId() + " at floor " + floor + " to go " + direction.name());
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
    public void selectFloorForUser(Elevator elevator, int destinationFloor) {
        elevator.getUsers().add(new User(destinationFloor));
        System.out.println("User enters elevator " + elevator.getElevatorId());
        int newDestinationFloor = elevator.getDirection().equals(Direction.UP) ?
                Collections.max(elevator.getUsers()).getDestinationFloor() : // if we go up, we take the highest floor as destination
                Collections.min(elevator.getUsers()).getDestinationFloor(); // otherwise we take the min (if stationary there is probably only one user
        moveToFloor(elevator, newDestinationFloor);
    }


    public void moveElevatorsToNextFloor() {
        Arrays.stream(elevators).forEach(elevator -> {
            if (!elevator.getDirection().equals(Direction.STATIONARY) && !elevator.isWaitingForFloorInput()) {
                movingElevator(elevator);
            }
        });
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
        elevator.setWaitingForFloorInput(false);
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
        /* MOVE ELEVATOR TO NEXT FLOOR */
        if (elevator.getDirection().equals(Direction.UP)) {
            elevator.setCurrentPosition(elevator.getCurrentPosition() + 1);
        } else if (elevator.getDirection().equals(Direction.DOWN)) {
            elevator.setCurrentPosition(elevator.getCurrentPosition() - 1);
        }
        /* FIND USERS LEAVING AT THIS FLOOR*/
        System.out.println("Elevator " + elevator.getElevatorId() + " is at floor " + elevator.getCurrentPosition() + " going to " + elevator.getMoveToFloor());
        List<User> users = elevator.getUsers().stream().filter(user -> user.getDestinationFloor() == elevator.getCurrentPosition()).collect(Collectors.toList());
        if (!users.isEmpty()) {
            elevator.getUsers().removeAll(users);
            System.out.println(users.size() + " users left the elevator, currently " + elevator.getUsers().size() + " users in the elevator");
        }

        /* STOP FOR WAITING USER */
        List<ElevatorCall> calls = waitingUsers.stream()
                .filter(user -> user.getFloor() == elevator.getCurrentPosition() && elevatorAvailable(elevator, user.getFloor(), user.getDirection())).collect(Collectors.toList());
        if (!calls.isEmpty()){
            elevator.setWaitingForFloorInput(true); // Wait input from new user
            System.out.println("Waiting for user floor  choice");
            waitingUsers.removeAll(calls);
        }

        /* IF WE REACH DIRECTION FLOOR, ELEVATOR IS STATIONARY */
        if (elevator.getMoveToFloor() == elevator.getCurrentPosition() && !elevator.isWaitingForFloorInput()) {
            elevator.setDirection(Direction.STATIONARY);
            waitingUsers.stream().findFirst().map(elevatorCall -> {
                moveToFloor(elevator, elevatorCall.getFloor());
                return true;
            });
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
        if (elevatorElement.getDirection().equals(Direction.STATIONARY) || elevatorElement.getMoveToFloor() == floor) {
            return true;
        }

        int floorDifference = floor - elevatorElement.getCurrentPosition();
        if (floorDifference == 0) {
            return elevatorElement.getDirection().equals(direction); // elevator is called at its floor and going the same way
        }

        return floorDifference > 0 ? (direction.equals(Direction.UP)): direction.equals(Direction.DOWN);
    }



}
