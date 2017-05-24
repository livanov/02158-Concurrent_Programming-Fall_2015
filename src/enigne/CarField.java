package enigne;

import enigne.Cars;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import objects.Pos;

@SuppressWarnings("serial")
class CarField extends JPanel {

    final static int edge = 30;       // Field size

    // Colors
    final static Color defcolor = Color.blue;
    final static Color symbolcolor = new Color( 200, 200, 200 );
    final static Color blockcolor = new Color( 180, 180, 180 );
    final static Color bgcolor = new Color( 250, 250, 250 );  // Light grey
    final static Color slowcolor = new Color( 255, 200, 80 );   // Amber
    final static Color bridgecolor = new Color( 210, 210, 255 );  // Light blue
    final static Color overloadcolor = new Color( 255, 210, 240 );  // Pink
    final static Color opencolor = new Color( 0, 200, 0 );      // Dark green
    final static Color closedcolor = Color.red;
    final static Color barriercolor = Color.black;
    final static Color barriercolor2 = new Color( 255, 70, 70 );    // Emphasis colour - a kind of orange

    final static Font f = new Font( "SansSerif", Font.BOLD, 12 );

    final static int maxstaints = 10;

    static Color currentBridgeColor = bridgecolor;

    private Cars cars;

    // Model of field status
    // Modified through event-thread (except for Car no. 0)
    private int users = 0;             // No. of current users of field
    private Color c = defcolor;
    private char id = ' ';
    private char symbol = ' ';
    private int xoffset = 0;           // -1,0,1 Horizontal offset
    private int yoffset = 0;           // -1,0,1 Vertical offset

    private boolean isblocked = false; // Field can be used 
    private boolean hadcrash = false; // Car crash has occurred 
    private boolean keepcrash = false; // For detecting crashes
    private boolean slowdown = false; // Slow field
    private boolean onbridge = false; // Bridge field

    private boolean isstartpos = false;
    private int startposno = 0;
    private boolean startposopen = false;

    private boolean barriertop = false;
    private boolean barrieractive = false;
    private boolean barrieremph = false;

    private int staintx = 0;
    private int stainty = 0;
    private int staintd = 0;

    private static boolean light(Color c) {
        return (c.getRed() + 2 * c.getGreen() + c.getBlue()) > 600;
    }

    public CarField(Pos p, Cars c) {
        cars = c;
        setPreferredSize( new Dimension( edge, edge ) );
        setBackground( bgcolor );
        setOpaque( true );

        addMouseListener( new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if ( isstartpos ) {
                    if ( (e.getModifiers() & InputEvent.SHIFT_MASK) > 0 ) {
                        cars.removeCar( startposno );
                    } else if ( (e.getModifiers() & InputEvent.CTRL_MASK) > 0 ) {
                        cars.restoreCar( startposno );
                    } else {
                        cars.startFieldClick( startposno );
                    }
                }
            }
        } );
    }

    public void enter(int xoff, int yoff, Color newc, char ch) {
        users++;
        if ( users > 1 && keepcrash && !hadcrash ) {
            hadcrash = true;
            // Define a staint
            int dia = 7;
            staintx = (edge - 1 - dia) / 2 + (int) Math.round( Math.random() * 4 ) - 2;
            stainty = (edge - 1 - dia) / 2 + (int) Math.round( Math.random() * 4 ) - 2;
            staintd = dia;
        }
        c = newc;
        id = ch;
        xoffset = xoff;
        yoffset = yoff;
        repaint();
    }

    public void exit() {
        users--;
        repaint();
    }

    public void clean() {
        hadcrash = false;
        repaint();
    }

    public void setSymbol(char c) {
        symbol = c;
    }

    public void setBlocked() {
        isblocked = true;
        setBackground( blockcolor );
    }

    public void setStartPos(int no, boolean open) {
        setSymbol( (char) (no + (int) '0') );
        isstartpos = true;
        startposno = no;
        startposopen = open;
        repaint();
    }

    public void setStartPos(boolean open) {
        startposopen = open;
        repaint();
    }

    public void showBarrier(boolean active) {
        barrieractive = active;
        repaint();
    }

    public void emphasizeBarrier(boolean emph) {
        barrieremph = emph;
        repaint();
    }

    public void setBarrierPos(boolean top) {
        barriertop = top;                         //  Set only once
    }

    public void setKeep(boolean keep) {
        keepcrash = keep;
        if ( !keep && hadcrash ) {
            clean();
        }
    }

    public void setSlow(boolean slowdown) {
        this.slowdown = slowdown;
        setBackground( slowdown ? slowcolor : bgcolor );
        repaint();
    }

    public void setBridge(boolean onbridge) {
        this.onbridge = onbridge;
        setBackground( onbridge ? currentBridgeColor : bgcolor );
        repaint();
    }

    public static void setOverload(boolean overloaded) {
        currentBridgeColor = (overloaded ? overloadcolor : bridgecolor);
    }

    // This method may see transiently inconsistent states of the field if used by Car no. 0
    // This is considered acceptable
    public void paintComponent(Graphics g) {
        // System.out.println("Paints field: " + System.currentTimeMillis());
        g.setColor( isblocked ? blockcolor : (slowdown ? slowcolor : onbridge ? currentBridgeColor : bgcolor) );
        g.fillRect( 0, 0, edge, edge );

        if ( symbol != ' ' ) {
            g.setColor( symbolcolor );
            g.setFont( f );
            FontMetrics fm = getFontMetrics( f );
            int w = fm.charWidth( id );
            int h = fm.getHeight();
            g.drawString( "" + symbol, ((edge - w) / 2), ((edge + h / 2) / 2) );
        }

        if ( hadcrash ) {
            g.setColor( Color.red );
            g.fillOval( staintx, stainty, staintd, staintd );
        }

        if ( users > 1 || (users > 0 && isblocked) ) {
            g.setColor( Color.red );
            g.fillRect( 0, 0, edge, edge );
        }

        if ( users < 0 ) {
            g.setColor( Color.yellow );
            g.fillRect( 0, 0, edge, edge );
        }

        if ( users > 0 ) {
            g.setColor( c );
            int deltax = xoffset * (edge / 2);
            int deltay = yoffset * (edge / 2);
            g.fillOval( 3 + deltax, 3 + deltay, edge - 7, edge - 7 );
            if ( id != ' ' ) {
                if ( light( c ) ) {
                    g.setColor( Color.black );
                } else {
                    g.setColor( Color.white );
                }
                g.setFont( f );
                FontMetrics fm = getFontMetrics( f );
                int w = fm.charWidth( id );
                int h = fm.getHeight();
                g.drawString( "" + id, ((edge - w) / 2) + deltax, ((edge + h / 2) / 2) + deltay );
            }
        }

        if ( isstartpos ) {
            g.setColor( startposopen ? opencolor : closedcolor );
            g.drawRect( 1, 1, edge - 2, edge - 2 );

        }

        if ( barrieractive ) {
            if ( !barrieremph ) {
                g.setColor( barriercolor );
            } else {
                g.setColor( barriercolor2 );
            }
            if ( barriertop ) {
                g.fillRect( 0, 0, edge, 2 );
            } else {
                g.fillRect( 0, edge - 2, edge, 2 );
            }
        }

    }

}