// Step 3: Barrier
class Barrier {

   public void sync() {  }  // Wait for others to arrive (if barrier active)

   public void on() {  }    // Activate barrier

   public void off() {  }   // Deactivate barrier 

}


//mutex.wait()
//	count += 1
//	if count == n:
//		turnstile2.wait() # lock the second
//		turnstile.signal() # unlock the first
//mutex.signal()
//
//turnstile.wait() # first turnstile
//turnstile.signal()
//
//# critical point
//
//mutex.wait()
//	count -= 1
//	if count == 0:
//		turnstile.wait() # lock the first
//		// Extra B: Shutdown
//		if(barrierShutdownFlag) barrierOff();
//		turnstile2.signal() # unlock the second
//mutex.signal()
//
//turnstile2.wait() # second turnstile
//turnstile2.signal()
