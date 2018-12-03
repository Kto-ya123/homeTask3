package by.epam.courses.homeTask3.domain;

import by.epam.courses.homeTask3.domain.exceptions.PortException;
import by.epam.courses.homeTask3.domain.exceptions.ShipException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Ship extends Thread {

    private static Logger logger = Logger.getLogger(Ship.class);

    private static final int SIZE_SHIP_STORAGE = 5;
    private Integer id;
    private List<Container> containers;
    private Port port;
    private Lock lock;

    public Ship(Integer id, List<Container> containers) {
        this.id = id;
        this.containers = containers;
        lock = new ReentrantLock();
    }

    @Override
    public void run() {
        try {
            port = Port.getInstance();
            while (true) {
                swim();
                goToPort();
            }
        } catch (PortException e) {
            logger.error("Ship was destroyed..", e);
        }
    }


    private void swim() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            logger.error("Ship was destroyed at time of swim", e);
        }
    }


    private void goToPort() throws PortException {

        boolean isLockedDock = false;
        Dock dock = null;
        try {
            isLockedDock = port.lockDock(this);

            if (isLockedDock) {
                dock = port.getDock(this);
                logger.info("The ship " + id + " moored to the pier " + dock.getId());
                ShipAction action = getNewShipAction();
                executeAction(action, dock);
            } else {
                logger.info("Ship " + id + " couldn't moored to dock ");
            }
        } catch (ShipException e) {
            logger.warn(e.toString());
        } finally {
            if (isLockedDock) {
                port.unlockDock(this);
                logger.info("The ship " + id + " moved away from the pier " + dock.getId());
            }
        }
    }

    public int getShipId() {

        return id;
    }

    public int getFreeSpace() {
        return SIZE_SHIP_STORAGE - containers.size();
    }

    public void takeLock() {
        lock.lock();
    }

    public void giveLock() {
        lock.unlock();
    }

    public boolean takeLockForOtherShip() {
        return lock.tryLock();
    }

    public List<Container> getContainers() {
        return containers;
    }

    public List<Container> pickUpContainers(int numberOfContainers) {
        List<Container> pickedUpContainers = new ArrayList<>(this.containers.subList(0, numberOfContainers));
        this.containers.removeAll(pickedUpContainers);
        return pickedUpContainers;
    }

    public boolean addContainers(List<Container> containers) {
        return this.containers.addAll(containers);
    }


    private void loadToPort(Dock dock) throws ShipException {

        int containersNumberToMove = containers.size();//containersCount(containers.size());
        boolean result=false;

        logger.info("The ship " + id + " is going to upload " + containersNumberToMove + " containers to port storage.");
        if (!containers.isEmpty())
            result = dock.addContainerToPort(this, containersNumberToMove);

        if (result) {
            logger.info("Port received " + containersNumberToMove + " containers from ship " + id + ", port size: " + dock.getPortContainers().size());
        } else {
            logger.warn("Port haven't place to load containers from ship" + id + " or this ship haven't any containers");
        }
    }

    private void loadFromPort(Dock dock) throws ShipException {

        int containersNumberToMove = getFreeSpace();//containersCount(getFreeSpace());

        boolean result;

        logger.info("The ship " + id + " want to load " + containersNumberToMove + " containers from port storage.");

        result = dock.loadContainersFromPort(this, containersNumberToMove);

        if (result) {
            logger.info("The ship " + id + " loaded " + containersNumberToMove + " containers from port, port size: " + dock.getPortContainers().size());
        } else {
            logger.warn("Port haven't containers to load ship " + id + " or this ship is maximally loaded");
        }

    }

    private void loadToOtherShip(Dock dock) throws ShipException {
        boolean result;
        int containersNumberToMove = containersCount(containers.size());
        logger.info("Ship " + id + " want to upload " + containersNumberToMove + " on other dock.");
        result = dock.addContainerToOtherShip(this,containersNumberToMove);

        if (!result) {
            logger.info("The dock " + id + " is going to upload " + containersNumberToMove + " containers to port storage.");
            if (dock.addContainerToPort(this,containersNumberToMove)) {
                logger.info("Port storage  load containers from dock " + id);
            } else {
                logger.warn("Port haven't place to load containers from dock" + id);
            }
        }

    }

    private void loadFromOtherShip(Dock dock) throws ShipException {
        int containersNumberToMove = containersCount(getFreeSpace());
        logger.info("Ship " + id + " want to load " + containersNumberToMove + " from other ship.");

        if (!dock.pickUpContainerFromOtherShip(this, containersNumberToMove)) {
            logger.info("Ship " + id + " want to load " + containersNumberToMove + " from port.");
            if (!dock.loadContainersFromPort(this, containersNumberToMove)) {
                logger.info("Port received containers to ship " + id);
            } else {
                logger.warn("Port haven't containers to load ship " + id);
            }
        }
    }


    private void executeAction(ShipAction action, Dock dock) throws ShipException {
        switch (action) {
            case LOAD_TO_PORT:
                loadToPort(dock);
                break;
            case LOAD_FROM_PORT:
                loadFromPort(dock);
                break;
            case LOAD_TO_OTHER_SHIP:
                loadToOtherShip(dock);
               break;
            case LOAD_FROM_OTHER_SHIP:
                loadFromOtherShip(dock);
                break;
        }
    }

    private ShipAction getNewShipAction() {
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


    private int containersCount(int freeSpaceSize) throws ShipException {
        if (freeSpaceSize > 0) {
            Random random = new Random();
            int result = random.nextInt(freeSpaceSize);
            if (result == 0) {
                result++;
            }
            logger.info(result + " containers will be used");
            return result;
        } else {
            throw new ShipException("Invalid action in ship " + id);
        }

    }
}
