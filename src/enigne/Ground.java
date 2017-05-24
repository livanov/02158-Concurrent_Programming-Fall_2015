package enigne;

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import objects.Pos;

@SuppressWarnings("serial")
class Ground extends JPanel {

    private final int n = 11;   // Rows
    private final int m = 12;   // Columns
    private Cars cars;

    private CarField[][] area;

    // Checking for bridge overload is done in this class
    // Initial values must correspond to control panel defaults
    private int onbridge = 0;
    private boolean checkBridge = false;
    private int limit = Cars.initialBridgeLimit;

    public Ground(Cars c) {
        cars = c;
        area = new CarField[n][m];
        setLayout( new GridLayout( n, m ) );
        setBorder( BorderFactory.createLineBorder( new Color( 180, 180, 180 ) ) );

        for ( int i = 0; i < n; i++ ) {
            for ( int j = 0; j < m; j++ ) {
                area[i][j] = new CarField( new Pos( i, j ), cars );
                add( area[i][j] );
            }
        }

        // Define Hut area
        for ( int i = 2; i <= 7; i++ ) {
            for ( int j = 1; j <= 3; j++ ) {
                if ( i < 4 || i > 5 || j < 2 ) {
                    area[i][j].setBlocked();
                }
            }
        }

        // Define Shed area
        for ( int i = 10; i <= 10; i++ ) {
            for ( int j = 0; j < 2; j++ ) {
                area[i][j].setBlocked();
            }
        }

        // Set start/gate positions
        for ( int i = 0; i < 9; i++ ) {
            Pos startpos = cars.getStartPos( i );
            area[startpos.row][startpos.col].setStartPos( i, false );
        }

        // Set barrier fields (both adjacent fields)
        for ( int i = 0; i < 9; i++ ) {
            area[4][i + 3].setBarrierPos( false );
            area[5][i + 3].setBarrierPos( true );
        }

    }

    boolean isOnBridge(Pos pos) {
        return pos.col >= 1 && pos.col <= 3 && pos.row >= 9 && pos.row <= 2;
    }

    // The following methods are normally called through the event-thread.
    // May also be called directly by Car no. 0, but then for private fields.
    // Hence no synchronization is necessary
    public void mark(Pos p, Color c, int no) {
        CarField f = area[p.row][p.col];
        f.enter( 0, 0, c, (char) (no + (int) '0') );
        if ( isOnBridge( p ) ) {
            onbridge++;
        }
        bridgeCheck();
        // repaint();
    }

    public void mark(Pos p, Pos q, Color c, int no) {
        CarField fp = area[p.row][p.col];
        CarField fq = area[q.row][q.col];
        char marker = (char) (no + (int) '0');
        fp.enter( q.col - p.col, q.row - p.row, c, marker );
        fq.enter( p.col - q.col, p.row - q.row, c, marker );
        if ( isOnBridge( p ) || isOnBridge( q ) ) {
            onbridge++;
        }
        bridgeCheck();
        // repaint();
    }

    public void clear(Pos p) {
        CarField f = area[p.row][p.col];
        f.exit();
        if ( isOnBridge( p ) ) {
            onbridge--;
        }
        // repaint();
    }

    public void clear(Pos p, Pos q) {
        CarField fp = area[p.row][p.col];
        CarField fq = area[q.row][q.col];
        fp.exit();
        fq.exit();
        if ( isOnBridge( p ) || isOnBridge( q ) ) {
            onbridge--;
        }
        // repaint();
    }

    // The following internal graphical methods are only called via the event-thread
    void setOpen(int no) {
        Pos p = cars.getStartPos( no );
        area[p.row][p.col].setStartPos( true );
        // repaint();
    }

    void setClosed(int no) {
        Pos p = cars.getStartPos( no );
        area[p.row][p.col].setStartPos( false );
        // repaint();
    }

    void showBarrier(boolean active) {
        for ( int no = 0; no < 9; no++ ) {
            Pos p = cars.getBarrierPos( no );
            area[p.row][p.col].showBarrier( active );
            area[p.row + (no < 5 ? 1 : -1)][p.col].showBarrier( active );
        }
        // repaint();
    }

    void setBarrierEmphasis(boolean emph) {
        for ( int no = 0; no < 9; no++ ) {
            Pos p = cars.getBarrierPos( no );
            area[p.row][p.col].emphasizeBarrier( emph );
            area[p.row + (no < 5 ? 1 : -1)][p.col].emphasizeBarrier( emph );
        }
        // repaint();
    }

    void setKeep(boolean keep) {
        for ( int i = 0; i < n; i++ ) {
            for ( int j = 0; j < m; j++ ) {
                area[i][j].setKeep( keep );
            }
        }
    }

    void setSlow(boolean slowdown) {
        for ( int i = 0; i < n; i++ ) {
            area[i][0].setSlow( slowdown );
        }
        // repaint();
    }

    void showBridge(boolean active) {
        for ( int i = 0; i < 2; i++ ) {
            for ( int j = 1; j < 4; j++ ) {
                area[i][j].setBridge( active );
            }
        }
        checkBridge = active;
        bridgeCheck();
    }

    void setLimit(int max) {
        limit = max;
        bridgeCheck();
    }

    void bridgeCheck() {
        if ( checkBridge ) {
            CarField.setOverload( onbridge > limit );
            for ( int i = 9; i < 11; i++ ) {
                for ( int j = 1; j < 4; j++ ) {
                    area[i][j].repaint();
                }
            }
        }
    }

}