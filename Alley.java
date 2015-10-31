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
	private final Pos exit1to4 = new Pos(1, 1);
	private final Pos exit5to8 = new Pos(10, 2);
	private final Pos secondExit5to8 = new Pos(9, 0);
	
	public Alley(CarDisplayI cd){
		nrShortS = 0;
		nrLongS = 0;
		nrN = 0;
		this.cd = cd;
	}

	public synchronized void enter(Car car){
		int no = car.getNo();
		if(1 <= no && no <= 2){  // car going north
			while(nrShortS > 0){
				try{ wait(); } catch(InterruptedException ex){ /*if(car.amIDying) return; */}
			}
			nrN++;
		} else if(3 <= no && no <= 4){ // car going north
			while(nrLongS > 0){
				try{ wait(); } catch(InterruptedException ex){ /*if(car.amIDying) return; */}
			}
			nrN++;
		} else { // car going south
			while(nrN > 0) {
				try{ wait(); } catch(InterruptedException ex){ /*if(car.amIDying) return; */}
			}
			nrShortS++;
			nrLongS++;
		}
		
		//cd.println("nrN = " + nrN + " ; nrS = " + nrS);
	}
	
	public synchronized void leave(int no, Pos exitPoint) {
		if(1 <= no && no <= 4){ // car going north
			nrN--;
			if(nrN == 0) notifyAll();
		} else { // car going south
			if(exitPoint.equals(exit5to8)){
				nrLongS--;
			} else if(exitPoint.equals(secondExit5to8)){
				nrShortS--;
			} else{
			}
			
			if(nrShortS == 0) notifyAll();
		}
		//cd.println("nrN = " + nrN + " ; nrS = " + nrS);
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
		
        if( 1 <= no && no <= 4 && pos.equals(exit1to4) )
            return true;
        if( 5 <= no && no <= 8 && ( pos.equals(exit5to8) || pos.equals(secondExit5to8) ) )
            return true;

        return false;
    }

}