package by.epam.courses.homeTask3.service;

import by.epam.courses.homeTask3.domain.Dock;
import by.epam.courses.homeTask3.domain.ShipAction;
import by.epam.courses.homeTask3.domain.exceptions.ShipException;

import java.util.Random;
import by.epam.courses.homeTask3.domain.Ship;

public class ShipActionManager {

    private final ShipService service=new ShipService();

    public void executeAction(ShipAction action, Dock dock,Ship ship) throws ShipException {
        switch (action) {
            case LOAD_TO_PORT:
                service.loadToPort(dock,ship);
                break;
            case LOAD_FROM_PORT:
                service.loadFromPort(dock, ship);
                break;
            case LOAD_TO_OTHER_SHIP:
                service.loadToOtherShip(dock, ship);
                break;
            case LOAD_FROM_OTHER_SHIP:
                service.loadFromOtherShip(dock, ship);
                break;
        }
    }

    public ShipAction getNewShipAction() {
        Random random = new Random();
        int value = random.nextInt(10000);
        //if (value < 5) {
        //    return ShipAction.LOAD_TO_PORT;
        //}
        //return ShipAction.LOAD_FROM_PORT;
        if (value < 3000) {
            return ShipAction.LOAD_TO_PORT;
        } else if (value < 6000) {
            return ShipAction.LOAD_FROM_PORT;
        } else if (value < 8000)
            return ShipAction.LOAD_TO_OTHER_SHIP;
        return ShipAction.LOAD_FROM_OTHER_SHIP;
    }
}
