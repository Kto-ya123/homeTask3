package by.epam.courses.homeTask3;

import by.epam.courses.homeTask3.domain.Ship;
import by.epam.courses.homeTask3.domain.initialization.Initializer;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<Ship> ships = Initializer.initializeShips(20);
        Initializer.initializePort(ships.size());
        ships.forEach(Ship::start);
    }
}
