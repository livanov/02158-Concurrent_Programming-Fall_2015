//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2015

//Hans Henrik Løvengreen    Oct 6,  2015


import java.awt.Color;
import java.lang.InterruptedException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

class Gate {

    Semaphore g = new Semaphore(0);
    Semaphore e = new Semaphore(1);
    boolean isopen = false;

    public void pass() throws InterruptedException {
        g.P(); 
        g.V();
    }

    public void open() {
        try { e.P(); } catch (InterruptedException e) {}
        if (!isopen) { g.V();  isopen = true; }
        e.V();
    }

    public void close() {
        try { e.P(); } catch (InterruptedException e) {}
        if (isopen) { 
            try { g.P(); } catch (InterruptedException e) {}
            isopen = false;
        }
        e.V();
    }

}

class Car extends Thread {

    int basespeed = 100;             // Rather: degree of slowness
    int variation =  50;             // Percentage of base speed

    CarDisplayI cd;                  // GUI part

    int no;                          // Car number
    Pos startpos;                    // Startpositon (provided by GUI)
    Pos barpos;                      // Barrierpositon (provided by GUI)
    Color col;                       // Car  color
    Gate mygate;                     // Gate at startposition
	Alley alley;					 // Step 4: Alley as a monitor
	Barrier barrier;

	boolean isMoving = false;
	boolean inShortAlley = false;
	
    int speed;                       // Current car speed
    Pos curpos;                      // Current position 
    Pos newpos;                      // New position to go to
	
	// Step 6: Removing car with monitors
	boolean inAlley;				 // Flag representing if the car is in the alley;
	boolean isBeingRemoved;

    public Car(int no, CarDisplayI cd, Gate g, Alley alley, Barrier barrier) { // Step 4: Alley as a monitor

        this.no = no;
        this.cd = cd;
        mygate = g;
        startpos = cd.getStartPos(no);
        barpos = cd.getBarrierPos(no);  // For later use
		
		this.alley = alley; 			// Step 4: Alley as a monitor
		this.barrier = barrier;			// Step 4: Barrier as a monitor
		inAlley = false;				// Step 6: Removing car with monitors
		
        col = chooseColor();

        // do not change the special settings for car no. 0
        if (no==0) {
            basespeed = 0;  
            variation = 0; 
            setPriority(Thread.MAX_PRIORITY); 
        }
    }

    public synchronized void setSpeed(int speed) { 
        if (no != 0 && speed >= 0) {
            basespeed = speed;
        }
        else
            cd.println("Illegal speed settings");
    }

    public synchronized void setVariation(int var) { 
        if (no != 0 && 0 <= var && var <= 100) {
            variation = var;
        }
        else
            cd.println("Illegal variation settings");
    }

    synchronized int chooseSpeed() { 
        double factor = (1.0D+(Math.random()-0.5D)*2*variation/100);
        return (int)Math.round(factor*basespeed);
    }

    private int speed() {
        // Slow down if requested
        final int slowfactor = 3;  
        return speed * (cd.isSlow(curpos)? slowfactor : 1);
    }

    Color chooseColor() { 
        return Color.blue; // You can get any color, as longs as it's blue 
    }

    Pos nextPos(Pos pos) {
        // Get my track from display
        return cd.nextPos(no,pos);
    }

    boolean atGate(Pos pos) {
        return pos.equals(startpos);
    }
	
    public void run() {
        try {

            speed = chooseSpeed();
            curpos = startpos;
            cd.mark(curpos,col,no);

            while (true) {
                sleep(speed());

                if (atGate(curpos)) {
                    mygate.pass();
                    speed = chooseSpeed();
                }

                newpos = nextPos(curpos);

                // Step 4: Alley as a monitor
                if (alley.isEntryPoint(no, newpos)) {
                    synchronized (this) {
                        alley.enter(this);
                    }
                }

                // Step 3: enter barrier
                if (curpos.equals(barpos)) {
                    barrier.sync();
                }

                move();

                // Step 4: Alley as a monitor
                if (alley.isExitPoint(no, curpos)) {
                    synchronized (this) {
                        alley.leave(this);
                    }
                }
            }
		} catch(InterruptedException ex){
            releaseAquiredResources();
		} catch (Exception e) {
            cd.println("Exception in Car no. " + no);
            System.err.println("Exception in Car no. " + no + ":" + e);
            e.printStackTrace();
        }
    }

    private void releaseAquiredResources() {
        if(inAlley) {
            alley.leave(this);
        }

        if(isMoving) {
            CarControl.posSemaphoreMap.get(newpos).V();
            cd.clear(newpos);
        }
        CarControl.posSemaphoreMap.get(curpos).V();
        cd.clear(curpos);
    }

    private void move() throws InterruptedException{
        // Step1: bumpin
        CarControl.posSemaphoreMap.get(newpos).P();
        isMoving = true;

        //  Move to new position
        cd.clear(curpos);
        cd.mark(curpos, newpos, col, no);
        sleep(speed());
        cd.clear(curpos, newpos);
        cd.mark(newpos, col, no);

        // Step1: bumping
        isMoving = false;
        CarControl.posSemaphoreMap.get(curpos).V();

        curpos = newpos;
    }

    // Step 6: Removing car with monitors
    public void remove(){
		
		isBeingRemoved = true;
		synchronized(alley){
			alley.notifyAll();
		}

		synchronized(this){
			this.interrupt();
		}		
	}
}

public class CarControl implements CarControlI{

    // Step1: bumping
    public static Map<Pos, Semaphore> posSemaphoreMap;     // Semaphores for each position
    CarDisplayI cd;                         // Reference to GUI
    Car[]  car;                             // Cars
    Gate[] gate;                            // Gates
	Alley alley;							// Step 4: Alley as a monitor
	Barrier barrier;						// Step 4: Barrier as a monitor

    public CarControl(CarDisplayI cd){
        this.cd = cd;
		this.alley = new Alley(cd);		// Step 4: Alley as a monitor
		this.barrier = new Barrier(cd);	// Step 4: Barrier as a monitor
		
        car  = new  Car[9];
        gate = new Gate[9];

        // Step1: bumping
        InitializePositionSemaphores();

        for (int no = 0; no < 9; no++) {
            gate[no] = new Gate();
            car[no] = new Car(no, cd, gate[no], alley, barrier);		// Step 4: Alley and barrier as monitors
            // Step1: bumping
            try{
                posSemaphoreMap.get(car[no].startpos).P();
            }catch(InterruptedException e){

            }
            car[no].start();
        }

    }

    // Step1: bumping
    private void InitializePositionSemaphores(){
        posSemaphoreMap = new HashMap<Pos, Semaphore>();

        for (int i = 0; i < 11; i++){
            for (int j = 0; j < 12; j++){
                posSemaphoreMap.put(new Pos(i,j), new Semaphore(1));
            }
        }
    }

   public void startCar(int no) {
        gate[no].open();
    }

    public void stopCar(int no) {
        gate[no].close();
    }

    public void barrierOn() { 
		barrier.on();
        //cd.println("Barrier On not implemented in this version");
    }

    public void barrierOff() { 
		barrier.off();
        //cd.println("Barrier Off not implemented in this version");
    }

    public void barrierShutDown() { 
		barrier.shutDown();
    }

    public void setLimit(int k) { 
        cd.println("Setting of bridge limit not implemented in this version");
    }

	// Step 6: Removing car with monitors
    public void removeCar(int no) { 
		
		if(car[no].isAlive()){
			car[no].remove();
		}
    }

	// Step 6: Removing car with monitors
    public void restoreCar(int no) { 
	
		if(!car[no].isAlive()){
			car[no] = new Car(no, cd, gate[no], alley, barrier);
			try{
				posSemaphoreMap.get(car[no].startpos).P();
			} catch(InterruptedException ex){
			}
			car[no].start();
		}
    }

    /* Speed settings for testing purposes */

    public void setSpeed(int no, int speed) { 
        car[no].setSpeed(speed);
    }

    public void setVariation(int no, int var) { 
        car[no].setVariation(var);
    }

}






