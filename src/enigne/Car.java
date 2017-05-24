package enigne;

import java.awt.Color;
import static java.lang.Thread.sleep;
import objects.Alley;
import objects.Barrier;
import objects.Gate;
import objects.Pos;
import objects.Semaphore;

public class Car extends Thread {

    int basespeed = 100;             // Rather: degree of slowness
    int variation = 50;             // Percentage of base speed

    CarDisplayI cd;                  // GUI part

    public int no;                          // Car number
    Pos startpos;                    // Startpositon (provided by GUI)
    Pos barpos;                      // Barrierpositon (provided by GUI)
    Color col;                       // Car  color
    Gate mygate;                     // Gate at startposition
    private final Alley alley;					 // Step 4: Alley as a monitor
    Barrier barrier;

    int speed;                       // Current car speed
    public Pos curpos;                      // Current position 
    Pos newpos;                      // New position to go to

    boolean isMoving = false;
    public boolean inShortAlley = false;

    // Step 6: Removing car with monitors
    public boolean inAlley;				 // Flag representing if the car is in the alley;
    public Semaphore isBeingRemoved;

    public Car(int no, CarDisplayI cd, Gate g, Alley alley, Barrier barrier) { // Step 4: Alley as a monitor

        this.no = no;
        this.cd = cd;
        mygate = g;
        startpos = cd.getStartPos( no );
        barpos = cd.getBarrierPos( no );  // For later use

        this.alley = alley; 			// Step 4: Alley as a monitor
        this.barrier = barrier;			// Step 4: Barrier as a monitor
        inAlley = false;				// Step 6: Removing car with monitors

        col = chooseColor();

        // do not change the special settings for car no. 0
        if ( no == 0 ) {
            basespeed = 0;
            variation = 0;
            setPriority( Thread.MAX_PRIORITY );
        }
    }

    public synchronized void setSpeed(int speed) {
        if ( no != 0 && speed >= 0 ) {
            basespeed = speed;
        } else {
            cd.println( "Illegal speed settings" );
        }
    }

    public synchronized void setVariation(int var) {
        if ( no != 0 && 0 <= var && var <= 100 ) {
            variation = var;
        } else {
            cd.println( "Illegal variation settings" );
        }
    }

    synchronized int chooseSpeed() {
        double factor = (1.0D + (Math.random() - 0.5D) * 2 * variation / 100);
        return (int) Math.round( factor * basespeed );
    }

    private int speed() {
        // Slow down if requested
        final int slowfactor = 3;
        return speed * (cd.isSlow( curpos ) ? slowfactor : 1);
    }

    private Color chooseColor() {
        return Color.blue; // You can get any color, as longs as it's blue 
    }

    Pos nextPos(Pos pos) {
        // Get my track from display
        return cd.nextPos( no, pos );
    }

    boolean atGate(Pos pos) {
        return pos.equals( startpos );
    }

    @Override
    public void run() {
        try {

            speed = chooseSpeed();
            curpos = startpos;
            cd.mark( curpos, col, no );

            while ( true ) {
                sleep( speed() );

                if ( atGate( curpos ) ) {
                    mygate.pass();
                    speed = chooseSpeed();
                }

                newpos = nextPos( curpos );

                // Step 4: Alley as a monitor
                if ( alley.isEntryPoint( no, newpos ) ) {
                    synchronized ( this ) {
                        alley.enter( this );
                    }
                }

                // Step 3: enter barrier
                if ( curpos.equals( barpos ) ) {
                    barrier.sync();
                }

                if ( !isInterrupted() ) {
                    move();

                    // Step 4: Alley as a monitor
                    if ( alley.isExitPoint( no, curpos ) ) {
                        synchronized ( this ) {
                            alley.leave( this );
                        }
                    }
                }
            }
        } catch ( InterruptedException ex ) {
            releaseAcquiredResources();
        } catch ( Exception e ) {
            cd.println( "Exception in Car no. " + no );
            System.err.println( "Exception in Car no. " + no + ":" + e );
            e.printStackTrace();
        }
    }

    private void releaseAcquiredResources() {
        if ( inAlley ) {
            alley.leave( this );
        }

        if ( isMoving ) {
            CarControl.posSemaphoreMap.get( newpos ).V();
            cd.clear( newpos );
        }
        CarControl.posSemaphoreMap.get( curpos ).V();
        cd.clear( curpos );
        isBeingRemoved.V();
    }

    private void move() throws InterruptedException {
        // Step1: bumpin
        CarControl.posSemaphoreMap.get( newpos ).P();
        isMoving = true;

        //  Move to new position
        cd.clear( curpos );
        cd.mark( curpos, newpos, col, no );
        sleep( speed() );
        cd.clear( curpos, newpos );
        cd.mark( newpos, col, no );

        // Step1: bumping
        isMoving = false;
        CarControl.posSemaphoreMap.get( curpos ).V();

        curpos = newpos;
    }

    // Step 6: Removing car with monitors
    public void remove() {

        if ( isBeingRemoved != null ) {
            return; // remove or restore in progress
        }
        isBeingRemoved = new Semaphore( 0 );

        synchronized ( this ) {
            this.interrupt();
        }

        synchronized ( alley ) {
            alley.notifyAll();
        }
    }
}
