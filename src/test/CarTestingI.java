package test;

//Specification of Car Testing interface 
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2015

//Hans Henrik Løvengreen    Oct 6,  2015


public interface CarTestingI {
    // Corresponding GUI event

    void startCar(int no);             // Click at red   gate no.
    void stopCar(int no);              // Click at green gate no.

    void startAll();                   // Click at Start All button
    void stopAll();                    // Click at Stop  All button

    void removeCar(int no);            // Click+shift at gate no.
    void restoreCar(int no);           // Click+ctr   at gate no.

    void barrierOn();                  // Click on On button
    void barrierOff();                 // Click on Off button (asynchronous)
    void barrierShutDown();            // Click on Shut down button (synchronous)

    void setLimit(int k);              // Set bridge limit value

    void setSlow(boolean slowdown);    // Set slow-down
    void println(String message);      // Print (error) message on GUI

    void setSpeed(int no, int speed);  // Set base speed (no GUI)
    void setVariation(int no,int var); // Set variation  (no GUI)
}

