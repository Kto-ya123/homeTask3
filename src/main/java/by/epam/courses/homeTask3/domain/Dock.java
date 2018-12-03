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


    public boolean loadContainerFromOtherShip(Ship ship, int containersNumberToMove) {
        boolean result = false;
        lockOfUploadingStorage.lock();
        try {
            if (!queueUploadingShip.isEmpty()) {
                Ship uploadingShip = queueUploadingShip.poll();
                logger.info("Ship " + ship.getShipId() + " get ship " + uploadingShip.getShipId() + " for loading");
                result = moveFromOtherShip(ship, uploadingShip, containersNumberToMove);
                queueUploadingShip.add(uploadingShip);
            } else {
                logger.warn("Ship " + ship.getShipId() + " doesn't get ship for loading");
            }
        } finally {
            lockOfUploadingStorage.unlock();
        }
        return result;
    }

    private boolean moveFromOtherShip(Ship loadingShip, Ship uploadingShip, int containersNumberToMove) {
        boolean result = false;
        boolean IsShipLocked = uploadingShip.takeLockForOtherShip();
        if (IsShipLocked) {
            try {
                if (loadingShip.getFreeSpace() >= containersNumberToMove && loadingShip.getFreeSpace() != 0 && !uploadingShip.getContainers().isEmpty()) {
                    loadingShip.addContainers(uploadingShip.pickUpContainers(containersNumberToMove));
                    logger.info("Ship " + loadingShip.getShipId() + " loaded all portContainers from " + uploadingShip.getShipId());
                    result = true;
                } else if (loadingShip.getFreeSpace() != 0 && !uploadingShip.getContainers().isEmpty()) {
                    loadingShip.addContainers(uploadingShip.pickUpContainers(loadingShip.getFreeSpace()));
                    logger.info("Ship " + loadingShip.getShipId() + " loaded only part portContainers from " + uploadingShip.getShipId());
                } else
                    logger.warn("Ship" + loadingShip.getShipId() + "cannot be loaded from ship " + uploadingShip.getShipId() +
                            ", because loading ship haven't any free space" + loadingShip.getFreeSpace() + "or uploading ship haven't any containers"
                            + uploadingShip.getContainers().size());
            } finally {
                uploadingShip.giveLock();
            }
            return result;

        } else {
            return result;
        }
    }

    public boolean addContainerToOtherShip(Ship ship, int containersNumberToMove) {
        boolean result = false;
        lockOfLoadingStorage.lock();
        try {
            if (!queueLoadingShip.isEmpty()) {
                Ship loadingShipStorage = queueLoadingShip.poll();
                logger.info("Ship " + ship.getShipId() + " get ship " + loadingShipStorage.getShipId() + " for uploading");
                result = moveToOtherShip(ship, loadingShipStorage, containersNumberToMove);
                queueLoadingShip.add(loadingShipStorage);
            } else {
                logger.warn("Ship " + ship.getShipId() + " doesn't get ship for uploading");
            }
        } finally {
            lockOfLoadingStorage.unlock();
        }
        return result;
    }

    private boolean moveToOtherShip(Ship uploadingShip, Ship loadingShip, int containersNumberToMove) {
        boolean result = false;
        boolean IsShipLocked = loadingShip.takeLockForOtherShip();
        if (IsShipLocked) {
            try {
                if (loadingShip.getFreeSpace() >= containersNumberToMove && loadingShip.getFreeSpace() != 0 && !uploadingShip.getContainers().isEmpty()) {
                    loadingShip.addContainers(uploadingShip.pickUpContainers(containersNumberToMove));
                    logger.info("Ship " + uploadingShip.getShipId() + " uploaded all portContainers to ship " + loadingShip.getShipId());
                    result = true;
                } else if (loadingShip.getFreeSpace() != 0 && !uploadingShip.getContainers().isEmpty()) {
                    loadingShip.addContainers(uploadingShip.pickUpContainers(loadingShip.getFreeSpace()));
                    logger.info("Ship " + uploadingShip.getShipId() + " uploaded only part of portContainers to ship" + loadingShip.getShipId());
                } else logger.warn("Ship" + uploadingShip.getShipId() + "cannot load containers to ship " + loadingShip.getShipId() +
                        ", because loading ship haven't any free space" + loadingShip.getFreeSpace() + "or uploading ship haven't any containers"
                        + uploadingShip.getContainers().size());
            } finally {
                loadingShip.giveLock();
            }
            return result;

        } else {
            return result;
        }
    }
}
