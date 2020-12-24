package homeTask3.domain;

import homeTask3.domain.exceptions.PortException;
import org.apache.log4j.Logger;

import java.util.*;

public class Port {

    private static Logger logger = Logger.getLogger(Port.class);

    private static Port instance;

    private final static int CAPACITY_PORT_STORAGE = 250;
    private final static int COUNT_DOCKS = 4;
    private static List<Container> portContainers;
    private final Queue<Dock> dockQueue;
    private Set<Ship> mulctShips;

    private Map<Ship, Dock> usedDock;


    private Port() {
        mulctShips = new HashSet<>();
        dockQueue = new ArrayDeque<>(COUNT_DOCKS);
        for (int i = 0; i < COUNT_DOCKS; i++) {
            dockQueue.add(new Dock(i, CAPACITY_PORT_STORAGE, portContainers));
        }

        usedDock = new HashMap<>();

    }

    public static void initPortStorage(List<Container> containers) {
        portContainers=containers;
    }

    public static synchronized Port getInstance() {
        if(instance == null){
            instance = new Port();
        }
        return instance;
    }


    public boolean lockDock(Ship ship) throws InterruptedException {
        Dock dock;
        boolean result = false;
        while (!result) {
            synchronized (dockQueue){
                if (dockQueue.size() > 0) {
                    dock = dockQueue.element();
                    dockQueue.remove(dock);
                    //dock.setPortContainers(portContainers);
                    usedDock.put(ship, dock);
                    result = true;
                }
            }
            if(!result){
                Thread.sleep(1000);
            }
        }
        return result;
    }


    public void unlockDock(Ship ship) {
        Dock dock;

        synchronized (dockQueue){
            dock = usedDock.get(ship);
            dockQueue.add(dock);
            usedDock.remove(ship);
        }
    }


    public Dock getDock(Ship ship) throws PortException {
        Dock dock;
        synchronized (usedDock){
            dock = usedDock.get(ship);
            if (dock == null) {
                throw new PortException("Try to use dock without blocking.");
            }
        }
        return dock;
    }

    public synchronized void addMulctShip(Ship ship){
        mulctShips.add(ship);
    }

    public boolean isInMulctShip(Ship ship){
        return mulctShips.contains(ship);
    }

}
