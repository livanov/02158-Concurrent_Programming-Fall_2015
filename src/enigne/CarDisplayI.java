package enigne;

//Specification of Car Display interface 
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2015

//Hans Henrik Løvengreen    Oct 6,  2015

import objects.Pos;
import java.awt.Color;

public interface CarDisplayI {

    // May be called concurrently 

    // Mark area at position p using color c and number no.
    void mark(Pos p, Color c, int no); 

    // Mark area between adjacent positions p and q 
    void mark(Pos p, Pos q, Color c, int no); 

    // Clear area at position p
    void clear(Pos p);    

    // Clear area between adjacent positions p and q.
    void clear(Pos p, Pos q);

    void println(String message);  // Print (error) message on screen

    Pos getStartPos(int no);       // Get start/gate position of Car no.
                                          // (on private part of track)

    Pos getBarrierPos(int no);     // Get position right before 
                                          // barrier line for Car no.
                                          // (on private part of track).

    Pos nextPos(int no, Pos pos);  // Get next position for Car no.
    
    boolean isSlow(Pos pos);       // Get slow-down state

}



