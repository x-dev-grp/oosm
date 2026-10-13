package com.xdev.ooms.production.storageunit.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageUnitStockTest {

    @Test
    void reversingSourceMovementAcceptsMissingUnitPrice() {
        StorageUnit unit = unit(100.0, 4.0, 400.0);

        unit.updateDeletedCurrentVolume(10.0, 0, null);

        assertEquals(110.0, unit.getCurrentVolume());
        assertEquals(440.0, unit.getTotalCost());
        assertEquals(4.0, unit.getAvgCost());
    }

    @Test
    void reversingDestinationMovementAcceptsMissingUnitPrice() {
        StorageUnit unit = unit(100.0, 4.0, 400.0);

        unit.updateDeletedCurrentVolume(10.0, 1, null);

        assertEquals(90.0, unit.getCurrentVolume());
        assertEquals(360.0, unit.getTotalCost());
        assertEquals(4.0, unit.getAvgCost());
    }

    private StorageUnit unit(double volume, double averageCost, double totalCost) {
        StorageUnit unit = new StorageUnit();
        unit.setCurrentVolume(volume);
        unit.setAvgCost(averageCost);
        unit.setTotalCost(totalCost);
        return unit;
    }
}
