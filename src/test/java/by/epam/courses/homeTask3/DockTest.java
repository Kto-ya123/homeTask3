package by.epam.courses.homeTask3;

import by.epam.courses.homeTask3.domain.Container;
import by.epam.courses.homeTask3.domain.Dock;
import by.epam.courses.homeTask3.domain.initialization.Initializer;
import by.epam.courses.homeTask3.domain.Ship;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class DockTest {

    @Test
    public void addContainerToPortTest() {
        List<Container> shipContainers = new ArrayList<>();
        shipContainers.add(new Container(11));
        shipContainers.add(new Container(12));
        shipContainers.add(new Container(13));
        Ship ship = new Ship(1, shipContainers, 9, 1000);
        List<Container> portContainers = new ArrayList<>();
        portContainers.add(new Container(1));
        portContainers.add(new Container(2));
        portContainers.add(new Container(3));
        Dock dock = new Dock(1, 20, portContainers);
        System.out.println("free space before:" + dock.getFreeSpace());
        boolean result = dock.addContainerToPort(ship, ship.getContainers().size());
        System.out.println("free space after:" + dock.getFreeSpace());
        Assert.assertTrue(result);
    }

    @Test
    public void loadContainersFromPortTest() {
        List<Container> shipContainers = new ArrayList<>();
        shipContainers.add(new Container(11));
        shipContainers.add(new Container(12));
        shipContainers.add(new Container(13));
        Ship ship = new Ship(1, shipContainers, 9, 1000);
        List<Container> portContainers = new ArrayList<>();
        portContainers.add(new Container(1));
        portContainers.add(new Container(2));
        Dock dock = new Dock(1, 20, portContainers);
        System.out.println("free space before:" + ship.getFreeSpace());
        boolean result = dock.loadContainersFromPort(ship, ship.getFreeSpace());
        System.out.println("free space after:" + ship.getFreeSpace());
        Assert.assertTrue(result);
    }

}
