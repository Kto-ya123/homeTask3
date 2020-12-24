package homeTask3.domain;

import homeTask3.domain.exceptions.PortException;
import homeTask3.domain.exceptions.ShipException;
import homeTask3.service.ShipActionManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class Ship extends Thread {

    private static Logger logger = Logger.getLogger(Ship.class);

    private static final int SIZE_SHIP_STORAGE = 5;
    private Integer id;
    private List<Container> containers;
    private Port port;
    private Integer importance;

    public Ship(Integer id, List<Container> containers, Integer priority, Integer importance) {
        this.setPriority(priority);
        this.importance = importance;
        this.id = id;
        this.containers = containers;
    }

    @Override
    public void run() {
        try {
            port = Port.getInstance();
            while (true) {
                swim();
                System.out.println("Initial + " + this.getShipId());
                goToPort();
            }
        } catch (PortException e) {
            logger.error("Ship was destroyed..", e);
        }
    }

    private void swim() {
        try {
            Thread.sleep(1000 - importance);
            if(port.isInMulctShip(this)){
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            logger.error("Ship was destroyed at time of swim", e);
        }
    }


    private void goToPort() throws PortException {
        ShipActionManager actionManager=new ShipActionManager();
        boolean isLockedDock = false;
        Dock dock = null;
        try {
            isLockedDock = port.lockDock(this);

            if (isLockedDock) {
                dock = port.getDock(this);
                logger.info("The ship " + id + " go to the dock " + dock.getId());
                ShipAction action = actionManager.getNewShipAction();
                if(actionManager.executeAction(action, dock,this)){
                    port.addMulctShip(this);
                    logger.error("The ship " + getId() + " late; Dock id:  " + dock.getId());
                }

            } else {
                logger.info("Ship " + id + " couldn't got to dock ");
            }
        } catch (ShipException | InterruptedException e) {
            logger.warn(e.toString());
        } finally {
            if (isLockedDock) {
                port.unlockDock(this);
                logger.info("The ship " + id + " go away from the dock " + dock.getId());
            }
        }
    }

    public int getShipId() {

        return id;
    }

    public int getFreeSpace() {
        return SIZE_SHIP_STORAGE - containers.size();
    }

    public List<Container> getContainers() {
        return containers;
    }

    List<Container> pickUpContainers(int numberOfContainers) {
        List<Container> pickedUpContainers = new ArrayList<>(this.containers.subList(0, numberOfContainers));
        this.containers.removeAll(pickedUpContainers);
        return pickedUpContainers;
    }

    boolean addContainers(List<Container> containers) {
        return this.containers.addAll(containers);
    }





}
