package be.alexis.elevator

import be.alexis.elevator.models.Direction
import be.alexis.elevator.models.Elevator
import be.alexis.elevator.services.ElevatorService
import org.spockframework.spring.SpringBean
import spock.lang.Specification

class ElevatorApplicationTests extends Specification {

    @SpringBean
    ElevatorService elevatorService = new ElevatorService();

    void setup() {
        elevatorService.elevators = new Elevator[]{
                new Elevator(0, Direction.STATIONARY, 0, 0, 0, false, false, new ArrayList<>()),
                new Elevator(1, Direction.STATIONARY, 0, 0, 0, false, false, new ArrayList<>()),
                new Elevator(2, Direction.STATIONARY, 0, 0, 0, false, false, new ArrayList<>()),
        }
    }

    def "There is 3 elevators and they are stationary"() {
        when: "L'appel à effectuer"
        var returnValue = elevatorService.getElevators()

        then: "Vérification post appel"
        returnValue.size() == 3
        returnValue.toList().stream().each { elevator -> (elevator.direction == Direction.STATIONARY) }
    }

    def "call an elevator to the 5 floor"() {
        when:
        var id = elevatorService.callElevator(5, Direction.UP, false)
        var elevator = elevatorService.getElevator(id)

        then:
        id == 0
        if (elevator.present) {
            elevator.get().direction == Direction.UP
        }

    }

    def "a called elevator take N ticks to go to the Nth floor"() {
        given:
        elevatorService.getWaitingUsers().clear()
        elevatorService.callElevator(N, Direction.UP, false)
        var elevators = elevatorService.getElevators()

        when:
        N.times(floor -> {
            elevatorService.moveElevatorsToNextFloor()
            assert elevators[0].currentPosition == floor+1

        })

        then:
        elevators[0].currentPosition == N
        assert elevators[1].currentPosition == 0
        assert elevators[2].currentPosition == 0

        where: "Data table à 2 colonnes"

        // Spock lancera un sub test par ligne de la data table avec les valeurs de la ligne pour chaque variable
        N | _
        3 | _
        5 | _
        7 | _
    }

    def "an elevator take N ticks to go to the Nth floor when pressing the button"() {
        given:
        elevatorService.getWaitingUsers().clear()
        var elevators = elevatorService.getElevators()
        elevatorService.selectFloorForUser(elevators[0], N, false)

        when:
        elevators[0].users.size() == 1
        N.times(floor -> {
            elevatorService.moveElevatorsToNextFloor()
            assert elevators[0].currentPosition == floor+1
            assert elevators[1].currentPosition == 0
            assert elevators[2].currentPosition == 0
        })

        then:
        elevators[0].currentPosition == N
        elevators[0].users.size() == 0

        where: "Data table à 2 colonnes"

        // Spock lancera un sub test par ligne de la data table avec les valeurs de la ligne pour chaque variable
        N | _
        3 | _
        5 | _
        7 | _
    }

    def "call and elevator to the N floor then go to the X floor "() {
        given:
        elevatorService.getWaitingUsers().clear()
        var elevators = elevatorService.getElevators()
        elevatorService.callElevator(N, Direction.UP, false)


        when:
        elevators[0].users.size() == 1
        N.times(floor -> {
            elevatorService.moveElevatorsToNextFloor()
        })

        assert elevators[0].currentPosition == N
        assert elevators[1].currentPosition == 0
        assert elevators[2].currentPosition == 0

        elevatorService.selectFloorForUser(elevators[0], X, false)
        (X - N).times(floor -> {
            elevatorService.moveElevatorsToNextFloor()
        })

        then:
        elevators[0].currentPosition == X
        elevators[0].users.size() == 0
        elevators[1].currentPosition == 0
        elevators[2].currentPosition == 0


        where: "Data table à 2 colonnes"

        // Spock lancera un sub test par ligne de la data table avec les valeurs de la ligne pour chaque variable
        N | X
        3 | 5
        5 | 8
        7 | 10
    }

    def "a called elevator take N and X ticks to go to the Nth and X floor"() {
        given:
        elevatorService.getWaitingUsers().clear()
        elevatorService.callElevator(N, Direction.UP, false)
        elevatorService.callElevator(X, Direction.DOWN, false)
        var elevators = elevatorService.getElevators()

        when:
        X.times(floor -> {
            elevatorService.moveElevatorsToNextFloor()
        })

        then:
        elevators[0].currentPosition == N
        elevators[1].currentPosition == X
        elevators[2].currentPosition == 0

        where: "Data table à 2 colonnes"

        // Spock lancera un sub test par ligne de la data table avec les valeurs de la ligne pour chaque variable
        N | X
        3 | 5
        5 | 7
        7 | 9
    }

    def "a call a third elevator when first is stuck at floor N"() {
        given:
        elevatorService.getWaitingUsers().clear()
        elevatorService.callElevator(N, Direction.UP, false)
        elevatorService.callElevator(X, Direction.DOWN, false)
        var elevators = elevatorService.getElevators()

        when:
        N.times(floor -> {
            if(floor == N-1){
                elevatorService.callElevator(Z, Direction.DOWN, false)
            }
            elevatorService.moveElevatorsToNextFloor()
            elevators[0].currentPosition == floor
            elevators[1].currentPosition == floor
            elevators[2].currentPosition == 0
        })

        elevatorService.callElevator(Z, Direction.DOWN, false)
        (X-N).times(floor -> {
            elevatorService.moveElevatorsToNextFloor()

        })

        then:
        elevators[0].currentPosition == N
        elevators[1].currentPosition == X
        elevators[2].currentPosition == Z
        where: "Data table à 2 colonnes"

        // Spock lancera un sub test par ligne de la data table avec les valeurs de la ligne pour chaque variable
        N | X | Z
        3 | 5 | 2
        5 | 8 | 3
        7 | 12 | 5
    }


}
