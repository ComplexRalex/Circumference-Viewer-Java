import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <h1>Circumference test frame</h1>
 * This program shows up a frame with a circumference in it which
 * its radius can be adjustable. Also, it has an orthogonal line on
 * the surface of the circumference which its angle can be
 * adjustable as well.
 * <p>
 * Note: Not every calculation is completely accurate due to the
 * generated floating point numbers by the sinusoidal functions.
 * 
 * @author Alejandro Batres
 * @since 02/27/2020
 * @version 1.0
 */
@SuppressWarnings("serial")
public class TestCircleWindow extends JFrame{
	
	public TestCircleWindow(){

		setTitle("A simple circumference viewer");
		// Set window size to be the 70% size of the screen
		setSize(new Dimension((int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.70),(int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight()*0.70)));
		setMinimumSize(new Dimension(750,480));
		setResizable(true);
		setLocationRelativeTo(null);
		DrawArea canvas = new DrawArea(getWidth(),getHeight(),this);
		setContentPane(canvas);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent componentEvent){
				canvas.resizeGraphics(getWidth(), getHeight());
			}
		});
	}
	
	public static void main(String args[]){

		//Anti-aliasing properties for the text
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");
		
		new TestCircleWindow();
	}
}

/**
 * <h1>Graphics canvas</h1>
 * Every component (related to calculate stuff and show up graphics)
 * initializes its values inside this class.
 * 
 * Note: Almost every change on the window or any interaction with
 * the visual components, runs the "paintComponent" function.
 * 
 * @author Alejandro Batres
 * @since 02/27/2020
 * @version 1.0
 */
@SuppressWarnings("serial")
class DrawArea extends JPanel implements ChangeListener, KeyListener, FocusListener, MouseWheelListener{
	private Point center, point, corner;
	private int radius, angle, width, height;
	private final int lowRadius = 100, highRadius = 350;
	private JFrame parent;
	
	private JPanel pOptions, pComponents;
	private JLabel lbInfoMSG;
	private JTextField txtRadius, txtAngle;
	private JSlider slRadius, slAngle;
	private JButton btRadius, btAngle;
	private JRadioButton btYes, btNo;
	private ButtonGroup btsComponents;
	private boolean showRadiusComponents, focusRadius, focusAngle;
	
	private final Color
		transparent = new Color(0f,0f,0f,0.00001f),
		notLightGray = new Color(80,80,80),
		notGray = new Color(112,128,144),
		notGreen = new Color(143,188,143);
	
	private final Font
		titleCoords = new Font("Dialog",0,16),
		pointCoords = new Font("Dialog",0,14),
		defaultGraphicsFont = new Font("Dialog",0,12),
		creditsName = new Font("Dialog",0,16);
	
	public DrawArea(int w, int h, JFrame p) {
		this.width = w;
		this.height = h;
		parent = p;
		center = new Point();
		point = new Point();
		corner = new Point();
		radius = 100;
		angle = 45;
		showRadiusComponents = focusRadius = focusAngle = false;
		setLayout(null);
		
		initComplements();
		addListenerToAll();
	}
	
	/**
	 * Relocates every component inside the panel that
	 * depends on the size of the frame
	 * 
	 * @param w window width in pixels
	 * @param h window height in pixels
	 */
	public void resizeGraphics(int w, int h){
		this.width = w;
		this.height = h;
		relocateOptions();
		relocateOptionComponents();
	}
	
	/**
	 * Relocates the "pOptions" sub-panel which contains
	 * the radius and angle modifiers
	 */
	private void relocateOptions(){
		pOptions.setBounds(30,height-335,230,280);
	}
	
	/**
	 * Relocates the "pComponents" sub-panel which contains
	 * the radius components and its options
	 */
	private void relocateOptionComponents(){
		pComponents.setBounds(width-230,height-120,190,50);
	}
	
	/**
	 * Displays the "lbInfoMSG" label with in the <b>m</b>
	 * message and colored with <b>c</b> color
	 * 
	 * @param m error message
	 * @param c text color
	 */
	private void enableInfoMSG(String m, Color c){
		lbInfoMSG.setText(m);
		lbInfoMSG.setForeground(c);
	}
	
	/**
	 * Hides the "lbInfoMSG" label
	 * 
	 * Note: It actually colors the label with a translucent
	 * black color
	 */
	private void disableInfoMSG(){
		lbInfoMSG.setText("This might not display");
		lbInfoMSG.setForeground(transparent);
	}
	
	/**
	 * Converts the given angle <b>alpha</b> to an equivalent
	 * angle.
	 * 
	 * Note: This would not be necessary if Java could obtain
	 * a positive module with the expression {@code value%quotient}
	 * 
	 * @param alpha
	 * @return <b>alpha</b> if it belongs [0,360]. In other
	 * case, it will return the positive module of <b>{@code alpha%360}</b>
	 */
	private int equivalentAngle(int alpha){
		if(alpha > 360 || alpha < 0)
			return 360*(1-1*(int)Math.signum(alpha))/2 + alpha%360;
		else
			return alpha;
	}
	
	/**
	 * Initializes every visual component inside the JPanels
	 * "pOptions" and "pComponents"
	 */
	private void initComplements(){
		
		// Initializing JPanels and every JComponent
		
		GridBagConstraints c = new GridBagConstraints();
		
		// -> Initializing everything about "pOptions" JPanel
		
		pOptions = new JPanel(new GridBagLayout());
		pOptions.setBackground(transparent);
		relocateOptions();
		
		lbInfoMSG = new JLabel("This might not display", SwingConstants.CENTER);
		lbInfoMSG.setFont(new Font("Arial",2,16));
		enableInfoMSG("Radius range: ["+lowRadius+","+highRadius+"]",Color.green);
		
		txtRadius = new JTextField("100",12);
		txtRadius.setFont(new Font("Arial",0,14));
		txtRadius.setBorder(BorderFactory.createEmptyBorder());
		txtRadius.setHorizontalAlignment(JTextField.RIGHT);
		txtRadius.setBackground(Color.lightGray);
		
		btRadius = new JButton("Change radius");
		btRadius.setFont(new Font("Arial",1,15));
		btRadius.setBorder(BorderFactory.createEmptyBorder());
		btRadius.setForeground(Color.white);
		btRadius.setBackground(Color.gray);
		
		slRadius = new JSlider(JSlider.HORIZONTAL,lowRadius,highRadius,lowRadius);
		slRadius.setBackground(Color.darkGray);
		slRadius.setForeground(Color.lightGray);
		slRadius.setMajorTickSpacing(50);
		slRadius.setMinorTickSpacing(1);
		slRadius.setPaintTicks(true);
		slRadius.setPaintLabels(true);
		
		txtAngle = new JTextField("45",12);
		txtAngle.setFont(txtRadius.getFont());
		txtAngle.setBorder(BorderFactory.createEmptyBorder());
		txtAngle.setHorizontalAlignment(JTextField.RIGHT);
		txtAngle.setBackground(txtRadius.getBackground());

		btAngle = new JButton("Change angle");
		btAngle.setFont(btRadius.getFont());
		btAngle.setBorder(BorderFactory.createEmptyBorder());
		btAngle.setForeground(btRadius.getForeground());
		btAngle.setBackground(btRadius.getBackground());
		
		slAngle = new JSlider(JSlider.HORIZONTAL,0,360,45);
		slAngle.setBackground(Color.darkGray);
		slAngle.setForeground(Color.lightGray);
		slAngle.setMajorTickSpacing(90);
		slAngle.setMinorTickSpacing(1);
		slAngle.setPaintTicks(true);
		slAngle.setPaintLabels(true);
		
		// -> Initializing everything about "pComponents" JPanel
		
		pComponents = new JPanel(new GridBagLayout());
		pComponents.setBackground(transparent);
		relocateOptionComponents();
		
		JLabel lbShow = new JLabel("Show components?", SwingConstants.CENTER);
		lbShow.setFont(new Font("Arial",2,16));
		lbShow.setForeground(notGreen);

		btYes = new JRadioButton("Show");
		btYes.setForeground(Color.white);
		btYes.setBackground(Color.darkGray);
		btNo = new JRadioButton("Don't show");
		btNo.setForeground(btYes.getForeground());
		btNo.setBackground(Color.darkGray);
		btNo.setSelected(true);
		
		btsComponents = new ButtonGroup();
		btsComponents.add(btYes);
		btsComponents.add(btNo);
		
		// Adding stuff to JPanels
		
		c.insets = new Insets(5,5,5,5);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 100;
		c.weighty = 50;
		
		// -> Adding everything about "pOptions" JPanel
		
		c.gridx = 0;
		c.gridy = 0;
		pOptions.add(lbInfoMSG,c);
		c.gridy++;
		pOptions.add(txtRadius,c);
		c.gridy++;
		pOptions.add(btRadius,c);
		c.gridy++;
		pOptions.add(slRadius,c);
		c.gridy++;
		pOptions.add(txtAngle,c);
		c.gridy++;
		pOptions.add(btAngle,c);
		c.gridy++;
		pOptions.add(slAngle,c);
		
		// -> Adding everything about "pComponents" JPanel
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		pComponents.add(lbShow,c);
		c.gridwidth = 1;
		c.gridy++;
		pComponents.add(btYes,c);
		c.gridx++;
		pComponents.add(btNo,c);
		
		// Adding JPanels to JFrame
		
		add(pOptions);
		add(pComponents);
	}
	
	/**
	 * Graphics every shape (and some text) showed on the frame
	 */
	@Override
	protected void paintComponent(Graphics g){
		
		// Defining the corner of the circumference
		corner.x = width/2 - radius;
		corner.y = height/2 - radius;
		
		// Defining the center of the circumference
		center.x = corner.x+radius;
		center.y = corner.y+radius;
		
		// Defining the coordinates of the point from the orthogonal line
		point.x = (int)(Math.round(center.x + radius*Math.cos(Math.toRadians(angle))));
		point.y = (int)(Math.round(center.y + radius*Math.sin(Math.toRadians(-angle))));
		
		// Coloring background
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, width, height);
		
		// Setting axes
		g.setColor(notLightGray);
		g.drawLine(0,center.y,width,center.y);
		g.drawLine(center.x,0,center.x,height);
		
		// Generating more axis and radius components
		if(showRadiusComponents){
			
			// Drawing axes of multiples of 45 degrees
			g.setColor(notGray);
			g.drawLine(center.x-radius,center.y,center.x+radius,center.y);
			g.drawLine(center.x,center.y-radius,center.x,center.y+radius);
			
			// Drawing axes of multiples of 90 degrees
			g.setColor(notLightGray);
			g.drawLine(center.x-radius,center.y-radius,center.x+radius,center.y+radius);
			g.drawLine(center.x+radius,center.y-radius,center.x-radius,center.y+radius);
			g.drawRect(center.x-radius, center.y-radius, 2*radius, 2*radius);
			
			// Drawing axes of the rest multiples of 60 degrees
			g.drawLine(center.x-radius, center.y-(int)Math.round(radius*(Math.tan(Math.toRadians(60)))),center.x+radius,center.y+(int)Math.round(radius*(Math.tan(Math.toRadians(60)))));
			g.drawLine(center.x+radius, center.y-(int)Math.round(radius*(Math.tan(Math.toRadians(60)))),center.x-radius,center.y+(int)Math.round(radius*(Math.tan(Math.toRadians(60)))));
			g.drawRect(center.x-radius, center.y-(int)Math.round(radius*(Math.tan(Math.toRadians(60)))), 2*radius, (int)Math.round(2*radius*(Math.tan(Math.toRadians(60)))));
			
			// Drawing axes of the rest multiples of 30 degrees
			g.drawLine(center.x-(int)Math.round(radius*(1/Math.tan(Math.toRadians(30)))), center.y-radius,center.x+(int)Math.round(radius*(1/Math.tan(Math.toRadians(30)))),center.y+radius);
			g.drawLine(center.x+(int)Math.round(radius*(1/Math.tan(Math.toRadians(30)))), center.y-radius,center.x-(int)Math.round(radius*(1/Math.tan(Math.toRadians(30)))),center.y+radius);
			g.drawRect(center.x-(int)Math.round(radius*(1/Math.tan(Math.toRadians(30)))), center.y-radius, (int)Math.round(2*radius*(1/Math.tan(Math.toRadians(30)))), 2*radius);
			
			// Writing number of degrees on each position
			g.setColor(notGray);
			for(int i = 0; i < 360; i++)
				if(i%45 == 0 || i%30 == 0)
					g.drawString(i+" d", center.x-16+(int)Math.round((radius+25)*(Math.cos(Math.toRadians(i)))), center.y+7-(int)Math.round((radius+15)*(Math.sin(Math.toRadians(i)))));
			
			// Drawing x and y radius components
			g.setColor(notGreen);
			g.drawLine(point.x,center.y,point.x,point.y);
			g.drawLine(center.x,point.y,point.x,point.y);
			
			// Writing information about the magnitude of the components
			g.setColor(Color.green);
			g.setFont(titleCoords);
			g.drawString("Components (magnitude):", width-225, height-190);
			g.setFont(pointCoords);
			g.drawString("x component: "+Math.abs(point.x-center.x)+" px", width-225, height-170);
			g.drawString("y component: "+Math.abs(point.y-center.y)+" px", width-225, height-150);
		}
		
		// Drawing circumference (actually two)
		g.setColor(Color.white);
		g.drawOval(corner.x, corner.y, radius*2, radius*2);
		g.drawOval(corner.x-1, corner.y-1, radius*2+2, radius*2+2);
		
		// Writing the word "center" at the center lmao
		g.setFont(defaultGraphicsFont);
		g.drawString("Center",center.x-18,center.y+15);	
		g.drawLine(center.x,center.y,point.x,point.y);
		
		// Writing angle of the orthogonal line
		g.setColor(Color.lightGray);
		g.drawString(angle+" d", center.x-15, center.y-8);	
		g.drawArc(center.x-32, center.y-32, 64, 64, 0, angle);
		
		// Writing the coordinates of the point and center
		
		// -> Coordinates with the center at (0,0)
		g.setFont(titleCoords);
		g.drawString("Coordinates (center at the origin):", 30, 35);
		g.setFont(pointCoords);
		g.drawString("P ("+(point.x-center.x)+" px, "+(-point.y+center.y)+" px)", 30, 55);
		g.drawString("C ("+(center.x-center.x)+" px, "+(-center.y+center.y)+" px)", 30, 75);
		
		// -> Real frame coordinates
		g.setFont(titleCoords);
		g.setColor(Color.orange);
		g.drawString("Coordinates (in frame):", 30, 100);
		g.setFont(pointCoords);
		g.drawString("P ("+(point.x)+" px, "+(point.y)+" px)", 30, 120);
		g.drawString("C ("+(center.x)+" px, "+(center.y)+" px)", 30, 140);
		
		// Writing radius magnitude of the circumference
		g.setFont(defaultGraphicsFont);
		g.setColor(Color.red);
		g.drawString("r = "+radius+" px",center.x+radius/2-14,center.y-8);
		g.drawLine(center.x, center.y, center.x+radius, center.y);
		
		// Showing credits
		g.setFont(creditsName);
		g.setColor(Color.lightGray);
		g.drawString("Programmed by Alejandro Batres", width-280, 30);
		
	}
	
	/**
	 * Appends "Listeners" to the necessary components
	 */
	private void addListenerToAll(){
		parent.addMouseWheelListener(this);
		txtRadius.addFocusListener(this);
		txtRadius.addKeyListener(this);
		btRadius.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					int r = Integer.parseInt(txtRadius.getText().trim());
					if(r < lowRadius || r > highRadius) throw new OutOfRangeException();
					slRadius.setValue(radius = r);
					disableInfoMSG();
				}catch(NumberFormatException ex){
					enableInfoMSG("Enter an integer number",Color.red);
				}catch(OutOfRangeException ex) {
					enableInfoMSG("Enter an integer from the range",Color.red);
				}finally{repaint();}
			}
			
		});
		slRadius.addChangeListener(this);
		txtAngle.addFocusListener(this);
		txtAngle.addKeyListener(this);
		btAngle.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					angle = equivalentAngle(Integer.parseInt(txtAngle.getText().trim()));
					txtAngle.setText(Integer.toString(angle));
					slAngle.setValue(angle);
					disableInfoMSG();
				}catch(NumberFormatException ex){
					enableInfoMSG("Enter an integer number",Color.red);
				}finally{repaint();}
			}
			
		});
		slAngle.addChangeListener(this);
		btYes.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				showRadiusComponents = true;
				repaint();
			}
			
		});	
		btNo.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				showRadiusComponents = false;
				repaint();
			}
			
		});
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		if(source == slRadius){
			txtRadius.setText(Integer.toString(source.getValue()));
			radius = source.getValue();
		}else{
			txtAngle.setText(Integer.toString(source.getValue()));
			angle = source.getValue();
		}
		disableInfoMSG();
		repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if(e.getKeyChar() == KeyEvent.VK_ENTER){
			if(focusRadius)
				btRadius.doClick();
			else if(focusAngle)
				btAngle.doClick();
		}
	}
	@Override
	public void keyPressed(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void focusGained(FocusEvent e) {
		if((JTextField)e.getSource() == txtRadius)
			focusRadius = true;
		else
			focusAngle = true;
	}

	@Override
	public void focusLost(FocusEvent e) {
		if((JTextField)e.getSource() == txtRadius)
			focusRadius = false;
		else
			focusAngle = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// Adds degrees to the current angle depending on the rotation
		angle = equivalentAngle(angle-e.getWheelRotation());
		txtAngle.setText(Integer.toString(angle));
		slAngle.setValue(angle);
		disableInfoMSG();
		repaint();
	}
	
}

/**
 * Necessary for understanding what is going on with this code!
 * 
 * @author Alejandro Batres
 */
@SuppressWarnings("serial")
class OutOfRangeException extends Exception{
	public OutOfRangeException(){ super("Value out of range."); }
	public OutOfRangeException(String msg){ super(msg); }
}
