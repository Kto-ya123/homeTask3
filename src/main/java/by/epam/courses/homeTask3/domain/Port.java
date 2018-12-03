package by.epam.courses.homeTask3.domain;

import by.epam.courses.homeTask3.domain.exceptions.PortException;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Port {

    private static Logger logger = Logger.getLogger(Port.class);

    private static Port instance;
    private static AtomicBoolean instanceCreated = new AtomicBoolean(false);
    private static Lock lock = new ReentrantLock();
    private static Condition notEmpty = lock.newCondition();

    private final static int CAPACITY_PORT_STORAGE = 200;
    private final static int COUNT_DOCKS = 4;
    private static List<Container> portContainers;
    private Queue<Dock> dockQueue;

    private Map<Ship, Dock> usedDock;


    private Port() {

        dockQueue = new ArrayDeque<>(COUNT_DOCKS);
        for (int i = 0; i < COUNT_DOCKS; i++) {
            dockQueue.add(new Dock(i, CAPACITY_PORT_STORAGE, portContainers));
        }

        usedDock = new HashMap<>();

    }

    public static List<Container> getPortContainers() {
        return portContainers;
    }

    public static void setPortContainers(List<Container> portContainers) {
        Port.portContainers = portContainers;
    }

    public static void initPortStorage(List<Container> containers) {
        portContainers=containers;
    }

    static Port getInstance() {

        if (!instanceCreated.get()) {
            lock.lock();
            try {
                if (!instanceCreated.get()) {
                    instance = new Port();
                    instanceCreated.set(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }


    public boolean lockDock(Ship ship) {
        Dock dock;
        boolean result = false;
        lock.lock();
        try {
            while (!result) {
                if (dockQueue.size() > 0) {
                    dock = dockQueue.element();
                    dockQueue.remove(dock);
                    //dock.setPortContainers(portContainers);
                    usedDock.put(ship, dock);
                    result = true;
                } else {
                    notEmpty.await();
                }
            }
        } catch (InterruptedException e) {
            logger.error("Ship " + ship.getShipId() + " couldn't lock dock");
        } finally {
            lock.unlock();

        }
        return result;
    }


    public void unlockDock(Ship ship) {
        lock.lock();
        Dock dock;
        try {
            dock = usedDock.get(ship);
            //dock.setPortContainers(portContainers);
            dockQueue.add(dock);
            usedDock.remove(ship);
            notEmpty.signal();
        } catch (Exception e) {
            logger.error("Ship " + ship.getShipId() + " couldn't moore from port");
        } finally {
            lock.unlock();
        }
    }


    public Dock getDock(Ship ship) throws PortException {
        lock.lock();
        Dock dock;
        try {
            dock = usedDock.get(ship);
            if (dock == null) {
                throw new PortException("Try to use dock without blocking.");
            }
        } finally {
            lock.unlock();
        }
        return dock;
    }

}
