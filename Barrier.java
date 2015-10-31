import java.util.Set;
import java.util.HashSet;

// Step 4: Barrier as a monitor
class Barrier {
	
	private int count = 0;
	private boolean barrierOn = false;
	private boolean freeToGo = false;
	private boolean isAboutToShutDown = false;
	private CarDisplayI cd;
	private Set<Pos> behindBarrierPositions;
	
	public Barrier(CarDisplayI cd){
		count = 0;
		barrierOn = false;
		behindBarrierPositions = new HashSet<Pos>();
		this.cd = cd;
		
		behindBarrierPositions.add(new Pos(5, 3));  // Car No 0
		behindBarrierPositions.add(new Pos(5, 4));  // Car No 1
		behindBarrierPositions.add(new Pos(5, 5));  // Car No 2
		behindBarrierPositions.add(new Pos(5, 6));  // Car No 3
		behindBarrierPositions.add(new Pos(5, 7));  // Car No 4
		behindBarrierPositions.add(new Pos(4, 8));  // Car No 5
		behindBarrierPositions.add(new Pos(4, 9));  // Car No 6
		behindBarrierPositions.add(new Pos(4, 10)); // Car No 7
		behindBarrierPositions.add(new Pos(4, 11)); // Car No 8
	}

    public synchronized void sync() {  // Wait for others to arrive (if barrier active)
		if(barrierOn){
			while(freeToGo){
				try { wait(); } catch (InterruptedException ex) {}
			}
				
			count++;
			
			if(count == 9){
				if(isAboutToShutDown){
					off();
					notifyAll();
				}

				freeToGo = true;
				notifyAll();
			}
			else{
				while(barrierOn && !freeToGo){
					try{ wait(); } catch (InterruptedException ex) {}
				}
			}
			
			count--;
			
			if(count == 0){
				freeToGo = false;
				notifyAll();
			}
		}
    }  

    public synchronized void on() { // Activate barrier
		if(!barrierOn) {
			barrierOn = true;
			freeToGo = false;
		}
	}

    public synchronized void off() {  // Deactivate barrier 
		if(barrierOn) {
			barrierOn = false;
			freeToGo = false;
			isAboutToShutDown = false;
			notifyAll();
		}
	}
	
    public synchronized void shutDown(){ // Shutdown barrier
		if(!isAboutToShutDown){
			isAboutToShutDown = true;
			while(isAboutToShutDown) { 
				try { wait(); } catch (InterruptedException ex) {}
			}
		}
	}

	public boolean isBehindBarrier(Pos pos){
		
		return behindBarrierPositions.contains(pos);
	}
}