package test;

//Prototype implementation of Car Test class
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2015

//Hans Henrik Løvengreen    Oct 6,  2015


import java.util.*;

public class CarTest extends Thread {

    CarTestingI cars;
    int testno;

    public CarTest(CarTestingI ct, int no) {
        cars = ct;
        testno = no;
    }

    public void run() {
        try {
			
			List<Integer> solution;
			
            switch (testno) { 
            case 0:
                // Demonstration of startAll/stopAll.
                // Should let the cars go one round (unless very fast)
                cars.startAll();
                sleep(3000);
                cars.stopAll();
                break;

			case 1:
			
				cars.println("Test to show barrier synchronization");
				
				cars.barrierOn();
				
				solution = getRandomOrder();
				
				for (int i = 0; i <= 8; i++)
				{
					cars.startCar(solution.get(i));
					sleep(500);
				}
				
				cars.barrierOff();
				cars.stopAll();
				break;
				
			case 2:
			
				cars.println("Test to show car release when barrierOff is called");
				
				cars.barrierOn();
				solution = getRandomOrder();
				
				for (int i = 0; i <= 7; i++){
					cars.startCar(solution.get(i));
					sleep(500);
				}
				
				sleep(3000);
				
				cars.barrierOff();
				cars.stopAll();
				break;


			case 3:
			
				cars.println("Test to show barrier off and on called immediatelly after each other");
				
				cars.barrierOn();
				
				solution = getRandomOrder();
				
				for (int i = 0; i <= 8; i++)
				{
					cars.startCar(solution.get(i));
					sleep(500);
					if(i == 3 || i == 6){
						cars.barrierOff();
						cars.barrierOn();
					}						
				}
				
				cars.barrierOff();
				cars.stopAll();
				break;
				
				
			case 4:
					
				cars.println("Test to show barrier shutdown using semaphores.");
				
				cars.startAll();
				sleep(500);
				cars.barrierOn();
				cars.startCar(0);
				cars.barrierShutDown();
				cars.stopAll();
				break;
				
			case 5:
				
				cars.println("Test to show 1 and 2 are able to enter alley w/o delay.");
				
				cars.setSlow(true);
				
				for(int i = 5;i <= 8;i++){
					cars.startCar(i);
				}
				
				sleep(5000);
				
				cars.startAll();
				cars.stopAll();
				
				sleep(5000);
				
				cars.setSlow(false);
				
				break;
			
			case 6:
			
				cars.println("Test to show removal car.");
				cars.startAll();
				
				for (int i = 0; i <= 8; i++){
					cars.removeCar(i);
					sleep(500);
				}
				
				solution = getRandomOrder();
				
				for (int i = 0; i <= 8; i++){
					cars.restoreCar(solution.get(i));
					sleep(500);
				}
				
				cars.stopAll();
				break;
				
			case 7:
			
				cars.println("Test to show removing cars waiting for the alley");
				
				cars.setSpeed(6,200);
				cars.setSpeed(7,250);
				cars.setSpeed(8,300);
				
				cars.setSlow(true);
				cars.startAll();
				sleep(6000);
				
				cars.setSlow(false);
				
				cars.removeCar(5);
				sleep(2000);
				cars.removeCar(6);
				sleep(2000);
				cars.removeCar(7);
				
				sleep(5000);
				
				cars.stopAll();
				
				cars.restoreCar(5);
				cars.restoreCar(6);
				cars.restoreCar(7);
				
				break;
				
			case 8:
			
				cars.println("Test to show removing cars being in the alley");
				
				cars.setSlow(true);
				cars.startAll();
				
				sleep(4000);
				
				for(int i = 1; i <= 4; i++){
					cars.removeCar(i);
					sleep(500);
				}
				
				cars.stopAll();
				
				for(int i = 1; i <= 4; i++){
					cars.restoreCar(i);
				}
				
				cars.setSlow(false);
				
				break;
				
			case 9:
				cars.println("Test to show removing and immediate restoring of a car.");
				
				cars.startAll();
				cars.startCar(0);
				
				solution = getRandomOrder();
				
				for(int i = 0; i < 9; i++){
					sleep(1000);
					cars.removeCar(solution.get(i));
					cars.restoreCar(solution.get(i));
					cars.removeCar(solution.get(i));
					cars.restoreCar(solution.get(i));
					cars.removeCar(solution.get(i));
					cars.restoreCar(solution.get(i));
					cars.removeCar(solution.get(i));
					cars.restoreCar(solution.get(i));
				}
				
				sleep(1000);
				cars.stopAll();
				
				break;
				
            case 19:
                // Demonstration of speed setting.
                // Change speed to double of default values
                cars.println("Doubling speeds");
                for (int i = 1; i < 9; i++) {
                    cars.setSpeed(i,50);
                };
                break;

            default:
                cars.println("Test " + testno + " not available");
            }

            cars.println("Test ended");

        } catch (Exception e) {
            System.err.println("Exception in test: "+e);
        }
    }

	public List<Integer> getRandomOrder(){
		List<Integer> solution = new ArrayList<>();
		for (int i = 0; i <= 8; i++)
		{
			solution.add(i);
		}
		Collections.shuffle(solution);
				
		return solution;
	}
	
}



