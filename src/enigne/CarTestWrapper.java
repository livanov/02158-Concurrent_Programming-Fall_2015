package enigne;


import java.awt.EventQueue;
import objects.Semaphore;
import test.CarTestingI;

/**
 * For the methods of the CarTestI interface this class wraps them to similar events to be processed
 * by the gui thread.
 */
class CarTestWrapper implements CarTestingI {

    Cars cars;

    public CarTestWrapper(Cars cars) {
        this.cars = cars;
    }

    @Override
    public void startCar(final int no) {
        EventQueue.invokeLater(() -> {
            cars.startCar( no );
        });
    }

    @Override
    public void stopCar(final int no) {
        EventQueue.invokeLater(() -> {
            cars.stopCar( no );
        });
    }

    @Override
    public void startAll() {
        EventQueue.invokeLater(cars::startAll);
    }

    @Override
    public void stopAll() {
        EventQueue.invokeLater(cars::stopAll);
    }

    @Override
    public void barrierOn() {
        EventQueue.invokeLater(cars::barrierOn);
    }

    @Override
    public void barrierOff() {
        EventQueue.invokeLater(cars::barrierOff);
    }

    // This should wait until barrier is off
    // For this, a one-time semaphore is used (as simple Future)
    @Override
    public void barrierShutDown() {
        final Semaphore done = new Semaphore( 0 );
        EventQueue.invokeLater(() -> {
            cars.barrierShutDown( done );
        });
        try {
            done.P();
        } catch ( InterruptedException e ) {
        }

    }

    /*
    // This should wait until barrier has been set
    // For this, a one-time semaphore is used (as simple Future)
    public void barrierSet(final int k) {
        final Semaphore done = new Semaphore(0);
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.barrierSet(k, done); }}
        );
        try {
            done.P();
        } catch (InterruptedException e) {}

    }
     */
    @Override
    public void setLimit(final int k) {
        EventQueue.invokeLater(() -> {
            cars.setLimit( k );
        });
    }

    @Override
    public void setSlow(final boolean slowdown) {
        EventQueue.invokeLater(() -> {
            cars.setSlow( slowdown );
        });
    }

    @Override
    public void removeCar(final int no) {
        EventQueue.invokeLater(() -> {
            cars.removeCar( no );
        });
    }

    @Override
    public void restoreCar(final int no) {
        EventQueue.invokeLater(() -> {
            cars.restoreCar( no );
        });
    }

    @Override
    public void setSpeed(final int no, final int speed) {
        EventQueue.invokeLater(() -> {
            cars.setSpeed( no, speed );
        });
    }

    @Override
    public void setVariation(final int no, final int var) {
        EventQueue.invokeLater(() -> {
            cars.setVariation( no, var );
        });
    }

    /*
    // This should wait until limit change carried out
    // For this, a one-time semaphore is used (as simple Future)
    public void setLimit(final int k) {
        final Semaphore done = new Semaphore(0);
        EventQueue.invokeLater(new Runnable() {
            public void run() { cars.setLimit(k, done); }}
        );
        try {
            done.P();
        } catch (InterruptedException e) {}

    }
     */
    // Println already wrapped in Cars
    @Override
    public void println(final String message) {
        cars.println( message );
    }

}