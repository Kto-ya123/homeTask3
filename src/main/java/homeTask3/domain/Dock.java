package homeTask3.domain;

import org.apache.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Dock {

    private static Logger logger = Logger.getLogger(Dock.class);

    private int id;
    private int portCapacity;
    private List<Container> portContainers;
    private static Queue<Ship> queueLoadingShip = new ArrayDeque<>();
    private static Queue<Ship> queueUploadingShip = new ArrayDeque<>();

    public Dock(int id, int portCapacity, List<Container> portContainers) {
        this.id = id;
        this.portCapacity = portCapacity;
        this.portContainers = portContainers;
    }

    public List<Container> getPortContainers() {
        return portContainers;
    }

    public int getId() {
        return id;
    }

    public int getFreeSpace() {
        return portCapacity - portContainers.size();
    }

    public List<Container> pickUpContainers(int numberOfContainers) {
        List<Container> pickedUpContainers = new ArrayList<>(this.portContainers.subList(0, numberOfContainers));
        this.portContainers.removeAll(pickedUpContainers);
        return pickedUpContainers;
    }

    public boolean addContainerToPort(Ship ship, int requestedContainersCount) {
        boolean result = false;

        synchronized (queueUploadingShip){
            queueUploadingShip.add(ship);
        }

        synchronized (portContainers){
            synchronized (queueUploadingShip) {
                if (requestedContainersCount <= getFreeSpace()) {
                    List<Container> containers = ship.pickUpContainers(requestedContainersCount);
                    result = this.portContainers.addAll(containers);
                }
            }
        }
        queueUploadingShip.remove(ship);
        return result;
    }


    public boolean loadContainersFromPort(Ship ship, int requestedContainersCount) {
        boolean result = false;

        synchronized (queueLoadingShip){
            queueLoadingShip.add(ship);
        }

        synchronized (portContainers){
            if (requestedContainersCount >= portContainers.size() && !portContainers.isEmpty()) {
                List<Container> containers = pickUpContainers(requestedContainersCount);
                result = ship.addContainers(containers);
            }
        }
        queueLoadingShip.remove(ship);
        return result;
    }
}
