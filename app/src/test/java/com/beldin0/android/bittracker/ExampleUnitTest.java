package com.beldin0.android.bittracker;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

class BitTrackerTest {

    @Test
    void testNewBitTrackerIsEmpty() {
        BitTrackerService pb = new BitTrackerService();
        assertEquals(0, pb.getCount());
    }
    void testBitTrackerCountWorksTo14() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        assertEquals(14, pb.getCount());
    }
    @Test
    void testBitTrackerCountWorksTo15() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        assertEquals(15, pb.getCount());
    }
    @Test
    void testBitTrackerCountStopsAt15() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.46);
        assertEquals(15, pb.getCount());
    }
    void testBitTrackerKeepsNewest() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(500.00);
        pb.add(400.00);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(123.47);
        pb.add(123.45);
        pb.add(123.46);
        pb.add(700.00);
        pb.add(600.00);
        assertEquals(700.00, pb.latest());
    }
    @Test
    void testBitTrackerAverage5WhenEmpty() {
        BitTrackerService pb = new BitTrackerService();
        assertEquals(0, pb.average(5));
    }
    @Test
    void testBitTrackerAverage10WhenEmpty() {
        BitTrackerService pb = new BitTrackerService();
        assertEquals(0, pb.average(10));
    }
    @Test
    void testBitTrackerAverage15WhenEmpty() {
        BitTrackerService pb = new BitTrackerService();
        assertEquals(0, pb.average(5));
    }
    @Test
    void testBitTrackerAverage5With4() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(50);
        pb.add(100);
        pb.add(50);
        pb.add(100);
        assertEquals(75, pb.average(5));
    }
    @Test
    void testBitTrackerAverage10With4() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(50);
        pb.add(100);
        pb.add(50);
        pb.add(100);
        assertEquals(75, pb.average(10));
    }
    @Test
    void testBitTrackerAverage15With4() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(50);
        pb.add(100);
        pb.add(50);
        pb.add(100);
        assertEquals(75, pb.average(15));
    }
    @Test
    void testBitTrackerAverage5With15() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(50);
        pb.add(100);
        pb.add(150);
        pb.add(200);
        pb.add(250);
        pb.add(300);
        pb.add(350);
        pb.add(400);
        pb.add(450);
        pb.add(500);
        pb.add(550);
        pb.add(600);
        pb.add(650);
        pb.add(700);
        pb.add(750);
        assertEquals(650, pb.average(5));
    }
    @Test
    void testBitTrackerAverage10With15() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(50);
        pb.add(100);
        pb.add(150);
        pb.add(200);
        pb.add(250);
        pb.add(300);
        pb.add(350);
        pb.add(400);
        pb.add(450);
        pb.add(500);
        pb.add(550);
        pb.add(600);
        pb.add(650);
        pb.add(700);
        pb.add(750);
        assertEquals(525, pb.average(10));
    }
    @Test
    void testBitTrackerAverage15With15() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(50);
        pb.add(100);
        pb.add(150);
        pb.add(200);
        pb.add(250);
        pb.add(300);
        pb.add(350);
        pb.add(400);
        pb.add(450);
        pb.add(500);
        pb.add(550);
        pb.add(600);
        pb.add(650);
        pb.add(700);
        pb.add(750);
        assertEquals(400, pb.average(15));
    }
    @Test
    void testBitTrackerAverage5With20() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(50);
        pb.add(100);
        pb.add(150);
        pb.add(200);
        pb.add(250);
        pb.add(300);
        pb.add(350);
        pb.add(400);
        pb.add(450);
        pb.add(500);
        pb.add(550);
        pb.add(600);
        pb.add(650);
        pb.add(700);
        pb.add(750);
        pb.add(800);
        pb.add(850);
        pb.add(900);
        pb.add(950);
        pb.add(1000);
        assertEquals(900, pb.average(5));
    }
    @Test
    void testBitTrackerAverage10With20() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(50);
        pb.add(100);
        pb.add(150);
        pb.add(200);
        pb.add(250);
        pb.add(300);
        pb.add(350);
        pb.add(400);
        pb.add(450);
        pb.add(500);
        pb.add(550);
        pb.add(600);
        pb.add(650);
        pb.add(700);
        pb.add(750);
        pb.add(800);
        pb.add(850);
        pb.add(900);
        pb.add(950);
        pb.add(1000);
        assertEquals(775, pb.average(10));
    }
    @Test
    void testBitTrackerAverage15With20() {
        BitTrackerService pb = new BitTrackerService();
        pb.add(50);
        pb.add(100);
        pb.add(150);
        pb.add(200);
        pb.add(250);
        pb.add(300);
        pb.add(350);
        pb.add(400);
        pb.add(450);
        pb.add(500);
        pb.add(550);
        pb.add(600);
        pb.add(650);
        pb.add(700);
        pb.add(750);
        pb.add(800);
        pb.add(850);
        pb.add(900);
        pb.add(950);
        pb.add(1000);
        assertEquals(650, pb.average(15));
    }
}