package enigne;

//Implementation of Graphical User Interface class
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2015
//Hans Henrik Løvengreen     Oct 6,  2015
import test.CarTest;
import objects.Semaphore;
import objects.Pos;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;

@SuppressWarnings({"serial"})
public class Cars extends JFrame implements CarDisplayI {

    static final int width = 30;       // Width of text area
    static final int minhistory = 50;       // Min no. of lines kept

    public static final int initialBridgeLimit = 6;

    // Model
    private boolean[] gateopen = new boolean[9];
    private Pos[] startpos = new Pos[9];
    private Pos[] barrierpos = new Pos[9];

    private boolean barrieractive = false;
    private boolean bridgepresent = true;
    private volatile boolean slowdown = false;     // Flag read concurrently by isSlow()

    private CarControlI ctr;
    private CarTestWrapper testwrap;
    private Thread test;

    private Ground gnd;
    private JPanel gp;
    private ControlPanel cp;
    private JTextArea txt;
    private JScrollPane log;

    class LinePrinter implements Runnable {

        String m;

        public LinePrinter(String line) {
            m = line;
        }

        public void run() {
            int lines = txt.getLineCount();
            if ( lines > 2 * minhistory ) {
                try {
                    int cutpos = txt.getLineStartOffset( lines / 2 );
                    txt.replaceRange( "", 0, cutpos );
                } catch ( Exception e ) {
                }
            }
            txt.append( m + "\n" );
        }
    }


    /*
 *  NO blocking threshold setting in this version
 *
     
     // Thread to carry out barrier threshold setting since
     // it may be blocked by CarControl
     class SetThread extends Thread {
        int newval;

        public SetThread(int newval) {
            this.newval =  newval;
        }

        public void run() {
        	try {
        		ctr.barrierSet(newval);

        		// System.out.println("Barrier set returned");
        		EventQueue.invokeLater(new Runnable() {
        			public void run() { barrierSetDone(); }}
        				);

        	} catch (Exception e) {
        		System.err.println("Exception in threshold setting thread");
        		e.printStackTrace();
        	}

        }
    }
     */

 /*
 *  NO blocking limit setting in this version
 *
    // Variables used during limit setting
    private SetLimitThread limitThread; 
    private Semaphore      limitDone;
    private int            limitValue;
    
    
    // Thread to carry out change of bridge limit since
    // it may be blocked by CarControl
    class SetLimitThread extends Thread {
        int newmax;

        public SetLimitThread(int newmax) {
            this.newmax =  newmax;

        }

        public void run() {
            // ctr.setLimit(newmax);
            
            System.out.println("SetLimit returned");
            EventQueue.invokeLater(new Runnable() {
                public void run() { endSetLimit(); }}
            );
            
        }
    }
     */
    // Variables used during barrier shut down
    private ShutDownThread shutDownThread = null;
    private Semaphore shutDownSem;

    // Thread to carry out barrier off shut down since
    // it may be blocked by CarControl
    class ShutDownThread extends Thread {

        int newmax;

        public void run() {
            try {
                ctr.barrierShutDown();

                //System.out.println("Shut down returned");
                EventQueue.invokeLater( new Runnable() {
                    public void run() {
                        shutDownDone();
                    }
                }
                );
            } catch ( Exception e ) {
                System.err.println( "Exception in shut down thread" );
                e.printStackTrace();
            }

        }
    }

    void buildGUI(final Cars cars) {
        try {

            EventQueue.invokeAndWait( new Runnable() {

                public void run() {
                    gnd = new Ground( cars );
                    gp = new JPanel();
                    cp = new ControlPanel( cars );
                    txt = new JTextArea( "", 8, width );
                    txt.setEditable( false );
                    log = new JScrollPane( txt );
                    log.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );

                    setTitle( "Cars" );
                    setBackground( new Color( 200, 200, 200 ) );

                    gp.setLayout( new FlowLayout( FlowLayout.CENTER ) );
                    gp.add( gnd );

                    setLayout( new BorderLayout() );
                    add( "North", gp );
                    add( "Center", cp );
                    add( "South", log );

                    addWindowListener( new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            System.exit( 1 );
                        }
                    } );

                    // bridgepresent = ctr.hasBridge();
                    // gnd.showBridge(bridgepresent);
                    // cp.setBridge(bridgepresent);
                    // if (! bridgepresent) cp.disableBridge();
                    cp.setBridge( false );
                    cp.disableBridge();

                    pack();
                    setBounds( 100, 100, getWidth(), getHeight() );
                    setVisible( true );
                }
            } );

        } catch ( InvocationTargetException e ) {
            e.printStackTrace();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
    }

    public Cars() {

        startpos[0] = new Pos( 4, 2 );
        for ( int no = 1; no < 9; no++ ) {
            startpos[no] = new Pos( no < 5 ? 3 : 6, 3 + no );
        }

        for ( int no = 0; no < 9; no++ ) {
            barrierpos[no] = new Pos( no < 5 ? 4 : 5, 3 + no );
        }

        for ( int no = 0; no < 9; no++ ) {
            gateopen[no] = false;
        }

        buildGUI( this );

        // Add control
        testwrap = new CarTestWrapper( this );

        ctr = new CarControl( this );
    }

    public static void main(String[] args) {
        new Cars();
    }

    // High-level event handling -- to be called by gui thread only
    // The test thread activates these through the event queue via the
    // CarTestWrapper
    public void barrierOn() {
        gnd.showBarrier( true );
        barrieractive = true;
        ctr.barrierOn();
    }

    public void barrierOff() {
        ctr.barrierOff();
        gnd.showBarrier( false );
        barrieractive = false;
    }

    public void barrierShutDown(Semaphore done) {

        if ( shutDownThread != null ) {
            println( "WARNING: Barrier shut down already in progress" );
            if ( done != null ) {
                done.V();
            }
            return;
        }

        gnd.setBarrierEmphasis( true );
        cp.shutDownBegin();
        // Hold values for post-processing
        shutDownSem = done;
        shutDownThread = new ShutDownThread();
        shutDownThread.start();
    }

    // Called when Shut Down Thread has ended
    void shutDownDone() {

        // System.out.println(" start");
        cp.shutDownEnd();

        if ( shutDownSem != null ) {
            shutDownSem.V();
        }
        shutDownThread = null;
        shutDownSem = null;

        gnd.setBarrierEmphasis( false );
        gnd.showBarrier( false );
        barrieractive = false;
    }

    /*
    // Variables used during threshold setting
    private SetThread setThread = null;
    private Semaphore setSem = null;
    private int setVal;

    void  barrierSet(int k, Semaphore done) {

    	// System.out.println("Cars.barrierSet called");
    	
    	if (setThread != null ) {
    		println("WARNING: Threshold setting already in progress");
    		if (done != null) done.V();
    		return;
    	}

    	if (k < 2 || k > 9) {
    		println("WARNING: Threshold value out of range: " + k + " (ignored)");
    		if (done != null) done.V();
    		return;
    	}

        gnd.setBarrierEmphasis(true);
    	cp.barrierSetBegin();
    	// Hold values for post-processing
    	setSem = done;
    	setVal = k;
    	setThread = new SetThread(k);
    	setThread.start();
    }

    // Called when Set Thread has ended
    void barrierSetDone() {

    	// System.out.println("Cars.barrierSetDone called");
    	cp.barrierSetEnd(setVal);
        gnd.setBarrierEmphasis(false);

    	if (setSem != null ) setSem.V();
    	setThread = null;
    	setSem = null;
    }
     */
    void barrierClicked(boolean on) {
        if ( on ) {
            barrierOn();
        } else {
            barrierOff();
        }
    }

    public void setSlow(final boolean slowdown) {
        this.slowdown = slowdown;
        gnd.setSlow( slowdown );
    }

    public void startAll() {
        int first = barrieractive ? 0 : 1;
        // Should not start no. 0 if no barrier
        for ( int no = first; no < 9; no++ ) {
            startCar( no );
        }
    }

    public void stopAll() {
        for ( int no = 0; no < 9; no++ ) {
            stopCar( no );
        }
    }

    public void runTest(int i) {
        if ( test != null && test.isAlive() ) {
            println( "Test already running" );
            return;
        }
        println( "Run of test " + i );
        test = new CarTest( testwrap, i );
        test.start();
    }

    public void setKeep(final boolean keep) {
        gnd.setKeep( keep );
    }

    public void showBridge(boolean active) {
        gnd.showBridge( active );
        if ( active ) {
            cp.enableBridge();
        } else {
            cp.disableBridge();
        }
    }

    public void startFieldClick(int no) {
        if ( gateopen[no] ) {
            stopCar( no );
        } else {
            startCar( no );
        }
    }

    public void startCar(final int no) {
        if ( !gateopen[no] ) {
            gnd.setOpen( no );
            gateopen[no] = true;
            ctr.startCar( no );
        }
    }

    public void stopCar(final int no) {
        if ( gateopen[no] ) {
            ctr.stopCar( no );
            gnd.setClosed( no );
            gateopen[no] = false;
        }
    }

    public void setSpeed(int no, int speed) {
        ctr.setSpeed( no, speed );
    }

    public void setVariation(int no, int var) {
        ctr.setVariation( no, var );
    }

    public void removeCar(int no) {
        ctr.removeCar( no );
    }

    public void restoreCar(int no) {
        ctr.restoreCar( no );
    }

    public void setLimit(int max) {

        if ( !bridgepresent ) {
            println( "ERROR: No bridge at this playground!" );
            return;
        }

        if ( max < 2 || max > 6 ) {
            println( "ERROR: Illegal limit value" );
            return;
        }

        // cp.disableLimit();
        ctr.setLimit( max );
        gnd.setLimit( max );
        cp.setLimit( max );
        // cp.enableLimit(max);
    }

    /*
  *  No blocking of setLimit in this version
  *     
    void setLimit(int max, Semaphore done) {

        if (! bridgepresent) {
            println("ERROR: No bridge at this playground!");
            if (done != null) done.V();
            return;
        }
        
        if (max < 1 || max > 6) {
            println("ERROR: Illegal limit value");
            if (done != null) done.V();
            return;
        }

        if (limitThread != null ) {
            println("WARNING: Limit setting already in progress");
            if (done != null) done.V();
            return;
        }

        cp.disableLimit();
        // Hold values for post-processing
        limitValue = max;
        limitDone = done;
        limitThread = new SetLimitThread(max);
        limitThread.start();
    }
    
    // Called when SetLimitThread has ended
    void endSetLimit() {

        System.out.println("endSetLimit start");
        if (limitDone != null ) limitDone.V();
        
        gnd.setLimit(limitValue);
        cp.enableLimit(limitValue);
        limitThread = null;
        limitDone = null;
        System.out.println("endSetLimit end");
    }
     */
    // Implementation of CarDisplayI 
    // Mark and clear requests for car no. 0 are processed directly in order not
    // to fill the event queue (with risk of transiently inconsistent graphics)
    // Mark area at position p using color c and number no.
    public void mark(final Pos p, final Color c, final int no) {
        if ( no != 0 ) {
            EventQueue.invokeLater( new Runnable() {
                public void run() {
                    gnd.mark( p, c, no );
                }
            }
            );
        } else {
            gnd.mark( p, c, no );
        }
    }

    // Mark area between adjacent positions p and q 
    public void mark(final Pos p, final Pos q, final Color c, final int no) {
        if ( no != 0 ) {
            EventQueue.invokeLater( new Runnable() {
                public void run() {
                    gnd.mark( p, q, c, no );
                }
            }
            );
        } else {
            gnd.mark( p, q, c, no );
        }
    }

    // Clear area at position p
    public void clear(final Pos p) {
        if ( p.col < 2 || p.col > 3 || p.row < 4 || p.row > 5 ) {
            EventQueue.invokeLater( new Runnable() {
                public void run() {
                    gnd.clear( p );
                }
            }
            );
        } else // In toddlers' yard - call directly
        {
            gnd.clear( p );
        }
    }

    // Clear area between adjacent positions p and q.
    public void clear(final Pos p, final Pos q) {
        if ( p.col < 2 || p.col > 3 || p.row < 4 || p.row > 5 ) {
            EventQueue.invokeLater( new Runnable() {
                public void run() {
                    gnd.clear( p, q );
                }
            }
            );
        } else // In toddlers' yard - call directly
        {
            gnd.clear( p, q );
        }
    }

    public Pos getStartPos(int no) {      // Identify startposition of Car no.
        return startpos[no];
    }

    public Pos getBarrierPos(int no) {    // Identify pos. at barrier line
        return barrierpos[no];
    }

    public Pos nextPos(int no, Pos pos) {
        // Fixed tracks --- not to be modified.
        int mycol = 3 + no;
        Pos nxt = pos.copy();

        if ( no == 0 ) {   // No. 0 is special, running its own tiny track
            if ( pos.row == 5 && pos.col > 2 ) {
                nxt.col--;
            }
            if ( pos.col == 2 && pos.row > 4 ) {
                nxt.row--;
            }
            if ( pos.row == 4 && pos.col < 3 ) {
                nxt.col++;
            }
            if ( pos.col == 3 && pos.row < 5 ) {
                nxt.row++;
            }
        } else if ( no < 5 ) {  // Car going around clockwise (to the right)
            int myrow = (no < 3 ? 8 : 9);
            if ( pos.row == myrow && pos.col > 0 ) {
                nxt.col--;
            }
            if ( pos.col == 0 && pos.row > 1 ) {
                nxt.row--;
            }
            if ( pos.row == 1 && pos.col < mycol ) {
                nxt.col++;
            }
            if ( pos.col == mycol && pos.row < myrow ) {
                nxt.row++;
            }
        } else if ( no >= 5 ) {  // Car going around anti-clockwise (to the left)
            if ( pos.row == 0 && pos.col > 0 ) {
                nxt.col--;
            }
            if ( pos.col == 0 && pos.row < 9 ) {
                nxt.row++;
            }
            if ( pos.row == 9 && pos.col < 2 ) {
                nxt.col++;
            }
            // Round the corner
            if ( pos.row == 9 && pos.col == 2 ) {
                nxt.row++;
            }
            if ( pos.row == 10 && pos.col < mycol ) {
                nxt.col++;
            }
            if ( pos.col == mycol && pos.row > 0 ) {
                nxt.row--;
            }
        }

        return nxt;
    }

    public void println(String m) {
        // Print (error) message on screen 
        Runnable job = new LinePrinter( m );
        EventQueue.invokeLater( job );
    }

    public boolean isSlow(Pos pos) {
        return pos.col == 0 && slowdown;
    }
}
