package enigne;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
class ControlPanel extends JPanel {

    private final int test_count = 20;

    Cars cars;

    JPanel button_panel = new JPanel();

    JCheckBox keep = new JCheckBox( "Keep crash", false );
    JCheckBox slow = new JCheckBox( "Slowdown", false );

    JPanel barrier_panel = new JPanel();

    // JCheckBox barrier_on = new JCheckBox("Active", false);
    JButton barrier_on = new JButton( "On" );
    JButton barrier_off = new JButton( "Off" );
    JButton barrier_shutdown = new JButton( "Shut down" );

    JPanel bridge_panel = new JPanel();   // Combined with barrier panel

    JCheckBox bridge_on = new JCheckBox( "Show", false );

    // JLabel     threshold_label = new JLabel("Threshold:");
    // JComboBox<String> barrier_threshold = new JComboBox<String>();
    //  int currentThreshold = 9;
    int currentLimit = Cars.initialBridgeLimit;
    // JLabel     limit_label  = new JLabel("Bridge limit:");
    JComboBox<String> bridge_limit = new JComboBox<>();

    JPanel test_panel = new JPanel();

    JComboBox<String> test_choice = new JComboBox<>();

    public ControlPanel(Cars c) {
        cars = c;

        Insets bmargin = new Insets( 2, 5, 2, 5 );

        setLayout( new GridLayout( 3, 1 ) );

        JButton start_all = new JButton( "Start all" );
        start_all.setMargin( bmargin );
        start_all.addActionListener((ActionEvent e) -> {
            cars.startAll();
        });

        JButton stop_all = new JButton( "Stop all" );
        stop_all.setMargin( bmargin );
        stop_all.addActionListener((ActionEvent e) -> {
            cars.stopAll();
        });

        keep.addItemListener((ItemEvent e) -> {
            cars.setKeep( keep.isSelected() );
        });

        slow.addItemListener((ItemEvent e) -> {
            cars.setSlow( slow.isSelected() );
        });

        button_panel.add( start_all );
        button_panel.add( stop_all );
        button_panel.add( new JLabel( "  " ) );
        button_panel.add( keep );
        button_panel.add( new JLabel( "" ) );
        button_panel.add( slow );

        add( button_panel );

        barrier_panel.add( new JLabel( "Barrier:" ) );

        barrier_on.setMargin( bmargin );
        barrier_off.setMargin( bmargin );
        // barrier_shutdown.setMargin(bmargin);

        barrier_on.addActionListener((ActionEvent e) -> {
            cars.barrierOn();
        });

        barrier_off.addActionListener((ActionEvent e) -> {
            cars.barrierOff();
        });

        barrier_shutdown.addActionListener((ActionEvent e) -> {
            cars.barrierShutDown( null );
        });

        barrier_panel.add( barrier_on );
        barrier_panel.add( barrier_off );
        barrier_panel.add( barrier_shutdown );

        /*
        barrier_panel.add(barrier_on);
        
        barrier_on.addItemListener( new ItemListener () {
            public void itemStateChanged(ItemEvent e) {
                cars.barrierClicked(barrier_on.isSelected());
            }
        });
         */
 /*
        barrier_panel.add(new JLabel("   "));


        for (int i = 0; i <= 7; i++) 
            barrier_threshold.addItem(""+(i+2));
        barrier_threshold.setSelectedIndex(currentThreshold - 2);

        barrier_threshold.addActionListener( new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                int t = barrier_threshold.getSelectedIndex() + 2; 
                // Ignore internal changes
                if (t != currentThreshold) {
                	cars.barrierSet(t, null);
                }
            }
        });

        barrier_panel.add(threshold_label);
        barrier_panel.add(barrier_threshold);
         */
        barrier_panel.add( new JLabel( "Bridge:" ) );
        barrier_panel.add( bridge_on );

        bridge_on.addItemListener((ItemEvent e) -> {
            boolean isOn = bridge_on.isSelected();
            cars.showBridge( isOn );
        });

        for ( int i = 0; i < 5; i++ ) {
            bridge_limit.addItem( "" + (i + 2) );
        }
        bridge_limit.setSelectedIndex( currentLimit - 2 );

        bridge_limit.addActionListener((ActionEvent e) -> {
            int i = bridge_limit.getSelectedIndex();
            // System.out.println("Select event " + i);
            // Ignore internal changes
            if ( i + 2 != currentLimit ) {
                // System.out.println("Calling setLimit");
                cars.setLimit( i + 2 );
            }
        });

        barrier_panel.add( bridge_limit );

        add( barrier_panel );

        for ( int i = 0; i < test_count; i++ ) {
            test_choice.addItem( "" + i );
        }

        JButton run_test = new JButton( "Run test no." );
        run_test.setMargin( bmargin );
        run_test.addActionListener((ActionEvent e) -> {
            int i = test_choice.getSelectedIndex();
            cars.runTest( i );
        });

        test_panel.add( run_test );
        test_panel.add( test_choice );
        add( test_panel );

    }

    /*
    public void barrierSetBegin() {
    	// barrier_on.setEnabled(false);
       	// barrier_off.setEnabled(false);
       	barrier_threshold.setEnabled(false);
    }
    
    public void barrierSetEnd(int k) {
    	// barrier_on.setEnabled(true);
    	// barrier_off.setEnabled(true);
        if (k !=currentThreshold ) {
        	currentThreshold = k;
        	barrier_threshold.setSelectedIndex(k - 2);
        }
    	barrier_threshold.setEnabled(true);
    }
     */
    public void shutDownBegin() {
        barrier_on.setEnabled( false );
        barrier_off.setEnabled( false );
        barrier_shutdown.setEnabled( false );
    }

    public void shutDownEnd() {
        barrier_on.setEnabled( true );
        barrier_off.setEnabled( true );
        barrier_shutdown.setEnabled( true );
    }

    public void disableBridge() {
        // limit_label.setEnabled(false);
        bridge_limit.setEnabled( false );
    }

    public void enableBridge() {
        // limit_label.setEnabled(false);
        bridge_limit.setEnabled( true );
    }

    public void disableLimit() {
        bridge_limit.setEnabled( false );
    }

    public void setLimit(int k) {
        currentLimit = k;
        if ( k - 2 != bridge_limit.getSelectedIndex() ) {
            bridge_limit.setSelectedIndex( k - 2 );
        }
    }

    public void enableLimit(int k) {
        setLimit( k );
        bridge_limit.setEnabled( true );
    }

    public void setBridge(boolean active) {
        // Precaution to avoid infinite event sequence of events
        if ( active != bridge_on.isSelected() ) {
            bridge_on.setSelected( active );
        }
    }

}