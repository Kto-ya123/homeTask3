package homeTask3.service;

import homeTask3.domain.Dock;
import homeTask3.domain.Ship;
import homeTask3.domain.ShipAction;
import homeTask3.domain.exceptions.ShipException;

import java.util.Random;

public class ShipActionManager {

    private final ShipService service=new ShipService();

    public boolean executeAction(ShipAction action, Dock dock, Ship ship) throws ShipException {
        Random random = new Random();
        int timeInPort = random.nextInt(10);
        long start = System.currentTimeMillis();
        switch (action) {
            case LOAD_TO_PORT:
                service.loadToPort(dock,ship);
                break;
            case LOAD_FROM_PORT:
                service.loadFromPort(dock, ship);
                break;
        }
        long end = System.currentTimeMillis();
        if(end - start > timeInPort){
            return true;
        }
        return false;
    }

    public ShipAction getNewShipAction() {
        Random random = new Random();
        int value = random.nextInt(2000);
        if (value < 1000) {
            return ShipAction.LOAD_TO_PORT;
        } else  {
            return ShipAction.LOAD_FROM_PORT;
        }
    }
}
