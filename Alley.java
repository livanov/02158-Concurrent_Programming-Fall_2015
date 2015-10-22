import java.lang.InterruptedException;

// Step1: alley sync
class Alley{

    private static Semaphore qN = new Semaphore(1);
    private static Semaphore qS = new Semaphore(1);
    private static Semaphore alley = new Semaphore(1);

    private static int nrN = 0;
    private static int nrS = 0;

    public static void enter(int no, CarDisplayI cd) throws InterruptedException{

        if(1 <= no && no <= 4){ // car going north
            qN.P();
            if(nrN == 0) {
                alley.P();
            }
            nrN++;
            qN.V();
        } else if(5 <= no && no <= 8){ // car going south
            qS.P();
            if(nrS == 0) {
                alley.P();
            }
            nrS++;
            qS.V();
        } else {
            // TODO: exception ?
        }
    }

    public static void leave(int no, CarDisplayI cd) throws InterruptedException{

        if(1 <= no && no <= 4){ // car going north
            qN.P();
            nrN--;
            if(nrN == 0) {
                alley.V();
            }
            qN.V();
        } else if(5 <= no && no <= 8){ // car going south
            qS.P();
            nrS--;
            if(nrS == 0) {
                alley.V();
            }
            qS.V();
        } else {
            // TODO: exception ?
        }
    }
}