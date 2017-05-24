package enigne;

//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2015

//Hans Henrik Løvengreen    Oct 6,  2015
import objects.Semaphore;
import objects.Gate;
import objects.Pos;
import java.util.Map;
import java.util.HashMap;
import objects.Alley;
import objects.Barrier;

public class CarControl implements CarControlI {

    // Step1: bumping
    public static Map<Pos, Semaphore> posSemaphoreMap;     // Semaphores for each position
    CarDisplayI cd;                         // Reference to GUI
    Car[] car;                             // Cars
    Gate[] gate;                            // Gates
    Alley alley;							// Step 4: Alley as a monitor
    Barrier barrier;						// Step 4: Barrier as a monitor

    public CarControl(CarDisplayI cd) {
        this.cd = cd;
        this.alley = new Alley( cd );		// Step 4: Alley as a monitor
        this.barrier = new Barrier();	// Step 4: Barrier as a monitor

        car = new Car[9];
        gate = new Gate[9];

        // Step1: bumping
        InitializePositionSemaphores();

        for ( int no = 0; no < 9; no++ ) {
            gate[no] = new Gate();
            car[no] = new Car( no, cd, gate[no], alley, barrier );		// Step 4: Alley and barrier as monitors
            // Step1: bumping
            try {
                posSemaphoreMap.get( car[no].startpos ).P();
            } catch ( InterruptedException e ) {

            }
            car[no].start();
        }

    }

    // Step1: bumping
    private void InitializePositionSemaphores() {
        posSemaphoreMap = new HashMap<>();

        for ( int i = 0; i < 11; i++ ) {
            for ( int j = 0; j < 12; j++ ) {
                posSemaphoreMap.put( new Pos( i, j ), new Semaphore( 1 ) );
            }
        }
    }

    @Override
    public void startCar(int no) {
        gate[no].open();
    }

    @Override
    public void stopCar(int no) {
        gate[no].close();
    }

    @Override
    public void barrierOn() {
        barrier.on();
        //cd.println("Barrier On not implemented in this version");
    }

    public void barrierOff() {
        barrier.off();
        //cd.println("Barrier Off not implemented in this version");
    }

    @Override
    public void barrierShutDown() {
        barrier.shutDown();
    }

    @Override
    public void setLimit(int k) {
        cd.println( "Setting of bridge limit not implemented in this version" );
    }

    // Step 6: Removing car with monitors
    @Override
    public synchronized void removeCar(int no) {
        car[no].remove();
    }

    // Step 6: Removing car with monitors
    @Override
    public synchronized void restoreCar(int no) {

        if ( car[no].isBeingRemoved == null ) {
            return;
        }

        try {
            car[no].isBeingRemoved.P();
        } catch ( InterruptedException ex ) {
        }

        car[no] = new Car( no, cd, gate[no], alley, barrier );
        try {
            posSemaphoreMap.get( car[no].startpos ).P();
        } catch ( InterruptedException ex ) {
        }
        car[no].start();
    }

    /* Speed settings for testing purposes */
    @Override
    public void setSpeed(int no, int speed) {
        car[no].setSpeed( speed );
    }

    @Override
    public void setVariation(int no, int var) {
        car[no].setVariation( var );
    }

}
