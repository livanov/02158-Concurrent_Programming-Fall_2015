package objects;

// Step 4: Barrier as a monitor

public class Barrier {

    private int count = 0;
    private boolean barrierOn = false;
    private boolean freeToGo = false;
    private boolean isAboutToShutDown = false;

    public Barrier() {
        count = 0;
        barrierOn = false;
    }

    public synchronized void sync() {  // Wait for others to arrive (if barrier active)
        if ( barrierOn ) {
            while ( freeToGo ) {
                try {
                    wait();
                } catch ( InterruptedException ex ) {
                }
            }

            count++;

            if ( count == 9 ) {
                if ( isAboutToShutDown ) {
                    off();
                    notifyAll();
                }

                freeToGo = true;
                notifyAll();
            } else {
                while ( barrierOn && !freeToGo ) {
                    try {
                        wait();
                    } catch ( InterruptedException ex ) {
                    }
                }
            }

            count--;

            if ( count == 0 ) {
                freeToGo = false;
                notifyAll();
            }
        }
    }

    public synchronized void on() { // Activate barrier
        if ( !barrierOn ) {
            barrierOn = true;
            freeToGo = false;
        }
    }

    public synchronized void off() {  // Deactivate barrier 
        if ( barrierOn ) {
            barrierOn = false;
            freeToGo = false;
            isAboutToShutDown = false;
            notifyAll();
        }
    }

    public synchronized void shutDown() { // Shutdown barrier
        if ( barrierOn && !isAboutToShutDown ) {
            if ( count == 0 ) {
                barrierOn = false;
                return;
            }
            isAboutToShutDown = true;
            while ( isAboutToShutDown ) {
                try {
                    wait();
                } catch ( InterruptedException ex ) {
                }
            }
        }
    }
}
