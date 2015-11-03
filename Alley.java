import java.lang.InterruptedException;

// Step 4: alley as a monitor
class Alley{
	
	//Extra D: Priority for 1 and 2 with monitor
	private int nrShortS;
	private int nrLongS;
	private int nrN;
	private CarDisplayI cd;
	
	private final Pos entry1to2 = new Pos(8, 0);
	private final Pos entry3to4 = new Pos(9, 2);
	private final Pos entry5to8 = new Pos(1, 0);
	private final Pos exitPointNorth = new Pos(1, 1);
	private final Pos longAlleyExitPointSouth = new Pos(10, 2);
	private final Pos shortAlleyExitPointSouth = new Pos(9, 0);
	
	public Alley(CarDisplayI cd){
		nrShortS = 0;
		nrLongS = 0;
		nrN = 0;
		this.cd = cd;
	}

	public synchronized void enter(Car car){
		int no = car.no;
		if(1 <= no && no <= 2){  // car going north
			while(nrShortS > 0){
				try{ wait(); if(car.isBeingRemoved != null) return; } catch(InterruptedException ex){ }
			}
			nrN++;	
		} else if(3 <= no && no <= 4){ // car going north
			while(nrLongS > 0){
				try{ wait(); if(car.isBeingRemoved != null) return; } catch(InterruptedException ex){ }
			}
			nrN++;
		} else { // car going south
			while(nrN > 0) {
				try{ wait(); if(car.isBeingRemoved != null) return; } catch(InterruptedException ex){ }
			}
			nrShortS++;
			nrLongS++;
			car.inShortAlley = true;
		}
		
		car.inAlley = true;
	}
	
	public synchronized void leave(Car car) {
		int no = car.no;
		
		if(1 <= no && no <= 4){ // car going north
			nrN--;
			car.inAlley = false;
			if(nrN == 0) notifyAll();
		} else { // car going south
			if(car.inShortAlley && car.curpos.equals(shortAlleyExitPointSouth)){
				nrShortS--;
				car.inShortAlley = false;
			} else {
				nrLongS--;
				car.inAlley = false;
				if(car.inShortAlley) {
					nrShortS--;
				}
			}
			if(nrShortS == 0) notifyAll();
		}
	}
	
	 // Step1: alley sync
    public boolean isEntryPoint(int no, Pos pos){
		
        if( 1 <= no && no <= 2 && pos.equals(entry1to2) )
            return true;
        if( 3 <= no && no <= 4 && pos.equals(entry3to4) )
            return true;
        if( 5 <= no && no <= 8 && pos.equals(entry5to8) )
            return true;

        return false;
    }

    // Step1: alley sync
    public boolean isExitPoint(int no, Pos pos){
		
        if( 1 <= no && no <= 4 && pos.equals(exitPointNorth) )
            return true;
        if( 5 <= no && no <= 8 && ( pos.equals(longAlleyExitPointSouth) || pos.equals(shortAlleyExitPointSouth) ) )
            return true;

        return false;
    }
}