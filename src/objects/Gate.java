package objects;


public class Gate {

    Semaphore g = new Semaphore( 0 );
    Semaphore e = new Semaphore( 1 );
    boolean isopen = false;

    public void pass() throws InterruptedException {
        g.P();
        g.V();
    }

    public void open() {
        try {
            e.P();
        } catch ( InterruptedException ex ) {
        }
        if ( !isopen ) {
            g.V();
            isopen = true;
        }
        e.V();
    }

    public void close() {
        try {
            e.P();
        } catch ( InterruptedException ex ) {
        }
        if ( isopen ) {
            try {
                g.P();
            } catch ( InterruptedException ex ) {
            }
            isopen = false;
        }
        e.V();
    }
}
