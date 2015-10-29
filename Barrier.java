// Step 4: Barrier as a monitor
class Barrier {

   public synchronized void sync() {  }  // Wait for others to arrive (if barrier active)

   public synchronized void on() {  }    // Activate barrier

   public synchronized void off() {  }   // Deactivate barrier 

}


//mutex.wait()
//	count += 1
//	if (count == n){
//		readyToGo.wait() # lock the second
//		waitingToArrive.signal() # unlock the first
//  }
//mutex.signal()
//
//waitingToArrive.wait() # first 
//waitingToArrive.signal()
//
//# critical point
//
//mutex.wait()
//	count -= 1
//	if (count == 0){
//		waitingToArrive.wait() # lock the first
//		// Extra B: Shutdown
//		if(barrierShutdownFlag) barrierOff();
//		readyToGo.signal() # unlock the second
// 	}
//mutex.signal()
//
//readyToGo.wait() # second 
//readyToGo.signal()
