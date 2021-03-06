package by.epam.courses.homeTask3.domain;

import org.apache.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Dock {

    private static Logger logger = Logger.getLogger(Dock.class);

    private int id;
    private int portCapacity;
    private List<Container> portContainers;
    private static Lock portStorageLock = new ReentrantLock();
    private static Lock lockOfLoadingStorage = new ReentrantLock();
    private static Lock lockOfUploadingStorage = new ReentrantLock();
    private static Queue<Ship> queueLoadingShip = new ArrayDeque<>();
    private static Queue<Ship> queueUploadingShip = new ArrayDeque<>();

    public Dock(int id, int portCapacity, List<Container> portContainers) {
        this.id = id;
        this.portCapacity = portCapacity;
        this.portContainers = portContainers;
    }

    //public void setPortContainers(List<Container> portContainers) {
    //    this.portContainers = portContainers;
    //}

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

        lockOfUploadingStorage.lock();
        try {
            queueUploadingShip.add(ship);
        } finally {
            lockOfUploadingStorage.unlock();
        }
        portStorageLock.lock();
        try {
            ship.takeLock();
            if (requestedContainersCount <= getFreeSpace()) {
                List<Container> containers = ship.pickUpContainers(requestedContainersCount);
                result = this.portContainers.addAll(containers);
            }
            ship.giveLock();

        } finally {
            queueUploadingShip.remove(ship);
            portStorageLock.unlock();
        }
        return result;
    }


    public boolean loadContainersFromPort(Ship ship, int requestedContainersCount) {
        boolean result = false;

        lockOfLoadingStorage.lock();
        try {
            queueLoadingShip.add(ship);
        } finally {
            lockOfLoadingStorage.unlock();
        }

        portStorageLock.lock();
        try {
            ship.takeLock();
            if (requestedContainersCount >= portContainers.size() && !portContainers.isEmpty()) {
                List<Container> containers = pickUpContainers(requestedContainersCount);
                result = ship.addContainers(containers);
            }
            ship.giveLock();
        } finally {
            queueLoadingShip.remove(ship);
            portStorageLock.unlock();
        }
        return result;
    }
}
