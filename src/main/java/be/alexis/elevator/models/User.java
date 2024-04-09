package be.alexis.elevator.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class User implements Comparable {
    int destinationFloor;

    @Override
    public int compareTo(Object o) {
        return Integer.compare(destinationFloor, ((User)o).getDestinationFloor());
    }
}
