package by.epam.courses.homeTask3.domain.initialization;

import by.epam.courses.homeTask3.domain.Container;
import by.epam.courses.homeTask3.domain.Port;
import by.epam.courses.homeTask3.domain.Ship;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Initializer {

    private final static int COUNT_SHIP_CONTAINERS = 5;
    private final static int COUNT_PORT_CONTAINERS = 20;

    public static ArrayList<Ship> initializeShips(int countShips) {
        Random random = new Random();
        ArrayList<Ship> ships = new ArrayList<>();
        for (int i = 0; i < countShips; i++) {
            ArrayList<Container> containers = new ArrayList<>();
            int value = random.nextInt(5);
            for (int j = 0; j < value; j++) {
                int idContainer = i * 10 + j;
                containers.add(new Container(idContainer));
            }

            Ship ship = new Ship(i, containers);
            ships.add(ship);

        }
        return ships;
    }

    public static void initializePort(int countShips) {
        List<Container> containers = new ArrayList<>();
        int id;
        for (int i = 0; i < COUNT_PORT_CONTAINERS; i++) {
            id = (countShips * 10) + i;
            containers.add(new Container(id));
        }
        Port.initPortStorage(containers);
    }

}
