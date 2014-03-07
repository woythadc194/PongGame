/*
 * Author:Dylan Woythal
 */

import java.awt.event.*;
import java.awt.*;
import java.util.Random;

import javax.swing.*;

/* 
 * THINGS THAT NEED TO BE DONE STILL....
 * 
 * DONE		1) Ball can be grabbed back into play even after it has passed paddle..
 *		
 * DONE		2) Make New game work
 *
 * DONE		3) Add beginning game options
 *  	Done	Board Size
 *		Done	Paddle sizes
 *  			(ball size, ball speed, paddle speeds) <--All optional
 *  
 * Fixing 	4) Add AI
 *  			Always follows ball
 *  			always goes back to center
 *  
 * DONE		5) Listener for Pause/resume Game
 */

/*
 * Pong game
 */

//Eclipse Demands that the next line be there... Idk why
@SuppressWarnings("serial")
public class Pong extends JPanel implements KeyListener{
	
	/*
	 * Instance Vars
	 */
	public int boardW, boardH;	//JPanel size
	public int LPaddleX, LPaddleY, RPaddleX, RPaddleY, ballX, ballY;//Location
	public double LPaddleVY, RPaddleVY, ballVX, ballVY, ballVDelta;	//Speeds
	public int LPaddleW, LPaddleH, RPaddleW, RPaddleH, ballW, ballH;//Sizes
	public int sleepTime, movementX, movementY, movementReset;		//Logic aids
	public int boundsU, boundsD, boundsR, boundsL;					//boundaries
	public int hitCounter;
	public boolean inZone;
	public boolean rUp, rDown, lUp, lDown; //KeyListener Booleans
	public boolean gameWon, playAgain, paused, AI;
	public int upperLimit, lowerLimit;
	public Pong game = this;
	public JFrame tempFrame = new JFrame(), mainFrame = new JFrame();
	public double english;

	/*
	 * Makes new Pong Game
	 */
	public static void main(String[] args){
		new Pong();
	}
	
	/*
	 * Constructor needed for adding keylistener as 'this' to JFrame
	 * 		'this' can not be used from a static context hence why constructor 
	 * 		is needed to make non static calls
	 */
	public Pong(){
		super();
		showInstructions();
		starter();
	}
	
	public void starter(){

		setStats();
		
		mainFrame.dispose();
		mainFrame = new JFrame( "Test Frame" );
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		mainFrame.setLayout( gridbag );
		
		addPanels( mainFrame, gridbag, c );
		
		mainFrame.addKeyListener( this );
		mainFrame.setSize( boardW+2, boardH+50 );
		mainFrame.setVisible( true );
		mainFrame.setResizable( false );
		mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		mainFrame.setLocation( 100, 100 );
		
		playGame( this );
	}

	public void tempFreeze(){
		game.paused = true;
		long start = System.currentTimeMillis();
		while( System.currentTimeMillis()-start < 1500){
			setStats();
			repaint();
		}
		game.paused = false;
	}
	
	/*
	 * Starts the game
	 */
	public void playGame(Pong game){
		try{
			Thread.sleep(1500);
		}catch(Exception e2){}
		while(!gameWon){
			if( paused ){
				game.repaint();
				try{
					Thread.sleep(sleepTime);
				}catch(Exception e2){}
				continue;
			}
			//System.out.println( RPaddleX + ", " + RPaddleY );
			ballX += ballVX;
			ballY+=ballVY;
			if( lUp==true )
				LPaddleY -= LPaddleVY;
			if( lDown==true )
				LPaddleY += LPaddleVY;
			if( rUp==true )
				RPaddleY -= RPaddleVY;
			if( rDown==true )
				RPaddleY += RPaddleVY;

			if( RPaddleY + RPaddleH >= boundsD )
				rDown = false;
			if( RPaddleY <= boundsU )
				rUp = false;
			if( LPaddleY + LPaddleH >= boundsD )
				lDown = false;
			if( LPaddleY <= boundsU )
				lUp = false;
			
			checkBounce();
			game.repaint();
			try{
				Thread.sleep(sleepTime);
			}catch(Exception e2){}
		}
		if( playAgain ){
			starter();
		} else
			System.exit(0);
	}
	
	/*
	 * Returns MenuBar object with menus
	 */
	public JMenuBar getMenus(){
		JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder( BorderFactory.createEmptyBorder() );
        menuBar.setBackground( Color.DARK_GRAY );

		JMenu fileMenu = new JMenu( "File" );
		fileMenu.setBackground( Color.DARK_GRAY );
        fileMenu.setForeground( Color.WHITE );

        JMenuItem newGame = new JMenuItem( "New Game" );
        newGame.setBackground( Color.DARK_GRAY );
        newGame.setForeground( Color.WHITE );
        newGame.addActionListener( 
    			new ActionListener() { 
    				public void actionPerformed(ActionEvent e){ 
    					try{
    						mainFrame.dispose();
    						game.playAgain = true;
    						game.gameWon = true;
    					} catch(Exception exc){}
    				}
    			} );
		JMenuItem quit = new JMenuItem( "Quit" );
		quit.setBackground( Color.DARK_GRAY );
		quit.setForeground( Color.WHITE );
		quit.addActionListener( 
			new ActionListener() { 
				public void actionPerformed(ActionEvent e){ 
					System.exit(0); 
        		} 
        	}
		);
		
		fileMenu.add( newGame );
		fileMenu.add( quit );
		menuBar.add( fileMenu );
		return menuBar;
	}
	
	/*
	 * Sets all vars with values
	 */
	public void setStats(){
			
		getNumPlayers();
		
		//LOGIC AIDS
		sleepTime = 50;
		movementReset = 10;
		hitCounter = 0;
		movementY = 0;
		movementX = 0;
		gameWon = false;
		playAgain = false;
		inZone = true;
		paused = false;
		
		//BOUNDARIES
		
		//getBoardDim();
		boardW = 600;
		boardH = 400;
		boundsL = 10;
		boundsU = 10;
		boundsR = boardW - 10;
		boundsD = boardH - 10;
		
		//LEFT PADDLE
		getLPadW();
		
		LPaddleW = boardW/30;
		LPaddleVY = boardH/100;
		if( AI )
			LPaddleVY*=2;
		LPaddleX = boundsL + boardW/15-LPaddleW;
		LPaddleY = boardH/2-LPaddleH/2;
		
		upperLimit = LPaddleH/3;
		lowerLimit = LPaddleH/3*2;
		
		//RIGHT PADDLE
		getRPadW();
		
		RPaddleW = boardW/30;
		RPaddleVY = boardH/100;
		RPaddleX = boardW-10-boardW/15;
		RPaddleY = boardH/2-RPaddleH/2;
		
		//BALL
		ballW = boardH/20;
		ballH = boardH/20;
		ballVX = boardW/100.0;						//Changeable
		ballVY = boardW/100.0;						//Changeable
		ballVDelta = boardH/400.0;					//Changeable
		ballX = boardW/2 - ballW/2;
		ballY = getBallStartPoint();
		
		//English
		getEnglish();
	}	
	
	public void showInstructions(){
		
		JPanel jp1 = new JPanel();
		jp1.setLayout( new BorderLayout() );
		JPanel jp2 = new JPanel();
		
		tempFrame = new JFrame( "Game of Pong" );
		tempFrame.setLayout( new BorderLayout() );

		JTextField jf1 = new JTextField();
		jf1.setText("Left Controls: W/S (or COMP if 1 player)");
		JTextField jf2 = new JTextField();
		jf2.setText("Right Controls: <UArrow>/<DArrow>");
		

		jp1.add( jf1, BorderLayout.NORTH );
		jp1.add( jf2, BorderLayout.SOUTH );
		
		JRadioButton ready = new JRadioButton("Okay!", false);
		jp2.add( ready );
		
		tempFrame.add( jp1, BorderLayout.NORTH );
		tempFrame.add( jp2, BorderLayout.SOUTH );
		tempFrame.setLocation( 350, 300 );
		tempFrame.setSize( 250, 100 );
		tempFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		tempFrame.setVisible( true );
		
		while(true)
			if( ready.isSelected() ){
				tempFrame.dispose();
				return;
			}else
				try{
					Thread.sleep(100);
				}catch(Exception e2){}
		
	}
	
	/*
	 * Option menus
	 */
	public void getNumPlayers(){

		JPanel jp1 = new JPanel();
		jp1.setSize(boardW, boardH/2);
		JPanel jp2 = new JPanel();
		jp2.setSize(boardW, boardH/2);

		tempFrame = new JFrame( "Number of Players" );
		tempFrame.setLayout( new BorderLayout() );
		
		ButtonGroup bDim = new ButtonGroup();
		JRadioButton b1 = new JRadioButton( "One", false );
		JRadioButton b2 = new JRadioButton( "Two", false );
		b1.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					AI = true;
				}
			});
		b2.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					AI = false;
				}
			});
		bDim.add( b1 );
		bDim.add( b2 );
		jp1.add( b1 );
		jp1.add( b2 );
		
		JRadioButton ready = new JRadioButton("Okay!", false);
		jp2.add( ready ); 

		tempFrame.add( jp1, BorderLayout.NORTH );
		tempFrame.add( jp2, BorderLayout.SOUTH );
		tempFrame.setLocation( 350, 300 );
		tempFrame.setSize( 250, 100 );
		tempFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		tempFrame.setVisible( true );
		
		while(true)
			if( ready.isSelected() && !b1.isSelected() && !b2.isSelected() )
		    	ready.setSelected( false );
			else if( ready.isSelected() && (b1.isSelected() || b2.isSelected()) ){
				tempFrame.dispose();
				return;
			}else
				try{
					Thread.sleep(100);
				}catch(Exception e2){}
	}
	public void getBoardDim(){

		JPanel jp1 = new JPanel();
		jp1.setSize(boardW, boardH/2);
		JPanel jp2 = new JPanel();
		jp2.setSize(boardW, boardH/2);

		tempFrame = new JFrame( "Board Size" );
		tempFrame.setLayout( new BorderLayout() );
		
		ButtonGroup bDim = new ButtonGroup();
		JRadioButton b1 = new JRadioButton( "Small", false );
		JRadioButton b2 = new JRadioButton( "Medium", false );
		JRadioButton b3 = new JRadioButton( "Large", false );
		b1.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					boardW=400;
					boardH=266;
				}
			});
		b2.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					boardW=600;
					boardH=400;
				}
			});
		b3.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					boardW=800;
					boardH=800/3*2;
				}
			});
		bDim.add(b1);
		bDim.add(b2);
		bDim.add(b3);
		jp1.add(b1);
		jp1.add(b2);
		jp1.add(b3);
		
		JRadioButton ready = new JRadioButton("Okay!", false);
		jp2.add( ready ); 

		tempFrame.add( jp1, BorderLayout.NORTH );
		tempFrame.add( jp2, BorderLayout.SOUTH );
		tempFrame.setLocation( 350, 300 );
		tempFrame.setSize( 250, 100 );
		tempFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		tempFrame.setVisible( true );
		
		while(true)
			if( ready.isSelected()  && 
			    !b1.isSelected()    && 
		        !b2.isSelected()    && 
		        !b3.isSelected() )
		    	ready.setSelected( false );
			else if( ready.isSelected() && 
			        (b1.isSelected()    || 
			         b2.isSelected()    || 
			         b3.isSelected()) ){
				tempFrame.dispose();
				return;
			}else
				try{
					Thread.sleep(100);
				}catch(Exception e2){}
	}
	public void getLPadW(){
		
		JPanel jp1 = new JPanel();
		jp1.setSize(boardW, boardH/2);
		JPanel jp2 = new JPanel();
		jp2.setSize(boardW, boardH/2);
		
		tempFrame = new JFrame( "Left Paddle Size" );
		tempFrame.setLayout( new BorderLayout () );
		
		ButtonGroup btnGrp = new ButtonGroup();
		JRadioButton b1 = new JRadioButton( "Small", false );
		JRadioButton b2 = new JRadioButton( "Medium", false );
		JRadioButton b3 = new JRadioButton( "Large", false );
		b1.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					LPaddleH = (boundsD-boundsU)/7*3;
				}
			});
		b2.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					LPaddleH = (boundsD-boundsU)/7*4;
				}
			});
		b3.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					LPaddleH = (boundsD-boundsU)/7*5;
				}
			});
		btnGrp.add(b1);
		btnGrp.add(b2);
		btnGrp.add(b3);
		jp1.add(b1);
		jp1.add(b2);
		jp1.add(b3);
		
		JRadioButton ready = new JRadioButton("Okay!", false);
		jp2.add( ready ); 
		

		tempFrame.add( jp1, BorderLayout.NORTH );
		tempFrame.add( jp2, BorderLayout.SOUTH );
		tempFrame.setLocation( 350, 300 );
		tempFrame.setSize( 250, 100 );
		tempFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		tempFrame.setVisible( true );
		
		while(true)
			if( ready.isSelected()  && 
			    !b1.isSelected()    && 
			    !b2.isSelected()    && 
			    !b3.isSelected() )
		    	ready.setSelected( false );
			else if( ready.isSelected() && 
			        (b1.isSelected()    || 
			         b2.isSelected()    || 
			         b3.isSelected()) ){
				tempFrame.dispose();
				return;
			}else
				try{
					Thread.sleep(100);
				}catch(Exception e2){}
	}	
	public void getRPadW(){
		
		JPanel jp1 = new JPanel();
		jp1.setSize(boardW, boardH/2);
		JPanel jp2 = new JPanel();
		jp2.setSize(boardW, boardH/2);
		
		tempFrame = new JFrame( "Right Paddle Size" );
		tempFrame.setLayout( new BorderLayout() );
		
		ButtonGroup btnGrp = new ButtonGroup();
		JRadioButton b1 = new JRadioButton( "Small", false );
		JRadioButton b2 = new JRadioButton( "Medium", false );
		JRadioButton b3 = new JRadioButton( "Large", false );
		b1.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					RPaddleH = (boundsD-boundsU)/7*3;
				}
			});
		b2.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					RPaddleH = (boundsD-boundsU)/7*4;
				}
			});
		b3.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					RPaddleH = (boundsD-boundsU)/7*5;
				}
			});
		btnGrp.add(b1);
		btnGrp.add(b2);
		btnGrp.add(b3);
		jp1.add(b1);
		jp1.add(b2);
		jp1.add(b3);
		
		JRadioButton ready = new JRadioButton("Okay!", false);
		jp2.add( ready ); 

		tempFrame.add( jp1, BorderLayout.NORTH );
		tempFrame.add( jp2, BorderLayout.SOUTH );
		tempFrame.setLocation( 350, 300 );
		tempFrame.setSize( 250, 100 );
		tempFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		tempFrame.setVisible( true );
		
		while(true)
			if( ready.isSelected()  && 
			    !b1.isSelected()    && 
			    !b2.isSelected()    && 
			    !b3.isSelected() )
		    	ready.setSelected( false );
			else if( ready.isSelected() && 
			        (b1.isSelected()    || 
			         b2.isSelected()    || 
			         b3.isSelected()) ){
				tempFrame.dispose();
				return;
			}else
				try{
					Thread.sleep(100);
				}catch(Exception e2){}
	}	
	public void getEnglish(){

		JPanel jp1 = new JPanel();
		jp1.setSize(boardW, boardH/2);
		JPanel jp2 = new JPanel();
		jp2.setSize(boardW, boardH/2);

		tempFrame = new JFrame( "Add English Spin?" );
		tempFrame.setLayout( new BorderLayout() );
		
		ButtonGroup bDim = new ButtonGroup();
		JRadioButton b1 = new JRadioButton( "Yes", false );
		JRadioButton b2 = new JRadioButton( "no", false );
		b1.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					english = 7;
				}
			});
		b2.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					english = 0;
				}
			});
		bDim.add( b1 );
		bDim.add( b2 );
		jp1.add( b1 );
		jp1.add( b2 );
		
		JRadioButton ready = new JRadioButton("Okay!", false);
		jp2.add( ready ); 

		tempFrame.add( jp1, BorderLayout.NORTH );
		tempFrame.add( jp2, BorderLayout.SOUTH );
		tempFrame.setLocation( 350, 300 );
		tempFrame.setSize( 250, 100 );
		tempFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		tempFrame.setVisible( true );
		
		while(true)
			if( ready.isSelected() && !b1.isSelected() && !b2.isSelected() )
		    	ready.setSelected( false );
			else if( ready.isSelected() && (b1.isSelected() || b2.isSelected()) ){
				tempFrame.dispose();
				return;
			}else
				try{
					Thread.sleep(100);
				}catch(Exception e2){}
	}

	/*
	 * Starting point for ball
	 */
	public int getBallStartPoint(){
		Random rand = new Random();
		int i = boardH/5;
		int j = rand.nextInt( i*3) + i;
		return j;
	}

	/*
	 * Sets up the GridBag Layout with JFrame and JPanel
	 */
	public void addPanels( JFrame frame, 
			   GridBagLayout gridbag,
			   GridBagConstraints c ){
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		JMenuBar menus = getMenus();
		gridbag.setConstraints( menus, c );
		frame.add( menus );
		c.weighty = 1.0;
		c.gridheight = GridBagConstraints.REMAINDER;
		gridbag.setConstraints( game, c );
		frame.add( game );
	}

	/*
	 * Controls the movement and speed of the ball
	 */
	public void checkBounce(){
		movementX--;
		movementY--;
		
		//For checking paddle bounce logic
		//System.out.println( "[("+(ballX<=LPaddleX+LPaddleW)+" && "+(LPaddleY<=ballY+ballH)+" && "+(ballY<=LPaddleY+LPaddleH)+")  ||  ("+(ballX+ballW>=RPaddleX)+" && "+(RPaddleY<=ballY+ballH)+" && "+(ballY<=RPaddleY+RPaddleH)+")]  && "+(movementX<=0)+" && "+inZone);
		
		if( ( ( ( ballX <= LPaddleX + LPaddleW )    &&
		        ( LPaddleY <= ballY + ballH )       &&
		        ( ballY <= LPaddleY + LPaddleH ))   || 
			    ( ( ballX + ballW >= RPaddleX )	    && 
			    ( RPaddleY <= ballY + ballH )       && 
			    ( ballY <= RPaddleY + RPaddleH )))  && 
			    movementX<= 0                       && 
			    inZone ){
			
			//For checking of ball speed after bounce
			//System.out.println( "Ball Speed: " + Math.abs( ballVY ) );
			movementX = movementReset;
			ballVX = -ballVX;
			hitCounter += 1;
			
			//English
			if( ballVX > 0 ){               //collision at right paddle
				if( rUp ){                      //right paddle moving up
        			System.out.println( "VY Made Steeper" );
					ballVY -= english;              //decrese VY for steeper incline
				}else if( rDown ){              //right paddle moving down
        			System.out.println( "VY Made Steeper" );			
        			ballVY += english;              //increase VY for less incline
				}
			}else{                          //collision at left paddle
    			if( lUp ){                      //left paddle moving up
        			System.out.println( "VY Made Steeper" );
    				ballVY -= english;              //decrese VY for steeper incline
	    		}else if( lDown ){              //left paddle moving down
        			System.out.println( "VY Made Steeper" );
    				ballVY += english;              //increse VY for steeper incline
			    }
		    }
		    
			//Changing velocities
			if( ballVX > 0 ){
				ballVX += ballVDelta;
				if( ballVY > 0 )
					ballVY += ballVDelta;
				else
					ballVY -= ballVDelta;
			} else {
				ballVX -= ballVDelta;
				if( ballVY > 0 )
					ballVY += ballVDelta;
				else
					ballVY -= ballVDelta;
			}
			
		}else if( ((ballX <= LPaddleX + LPaddleW)||(RPaddleX <= ballX+ballW)) && movementX!=9){
		//Gets rid of double velocity change for accidental double tap on paddle
			inZone = false;
		}
		
		//Game over Status
		if( ballX <= boundsL ){
			gameWon = true;
			JOptionPane.showMessageDialog(null,  "Right Won! After " + hitCounter + " hits!", "GameOver", JOptionPane.PLAIN_MESSAGE);
    		if( JOptionPane.showConfirmDialog( null, "Play Again?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION )
    		    playAgain = true;
		}
		if( ballX + ballW >= boundsR ){
			gameWon = true;
			JOptionPane.showMessageDialog(null,  "Left Won! After " + hitCounter + " hits!", "GameOver", JOptionPane.PLAIN_MESSAGE);
    		if( JOptionPane.showConfirmDialog( null, "Play Again?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION )
    		    playAgain = true;
		}
		//For Tracking wall bounce logic
		//System.out.println( "("+(ballY<=boundsU)+" || "+(ballY+ballH>=boundsD)+") && "+(movementY <= 0) );
		if( (( ballY <= boundsU )||( ballY + ballH >= boundsD )) && movementY <= 0){
			//System.out.println( "Mx:" + movementX + "  My:" + movementY );
			movementY = movementReset;
			ballVY = -ballVY;
		}
	}

	
/////////////////////////////
/////////OVERRIDDEN//////////
/////////////////////////////
	
	
	/*
	 * Overridden method that controls Painting
	 * super.paint( g ) = clears the screen of previous paint
	 */
	public void paint( Graphics g ){
		
		checkBounce();
		
		//background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, boardW, boardH);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor( Color.WHITE );
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		//borders
		g2.drawRect(boundsL, boundsU, boundsR-10, boundsD-10);

		//center line
		g2.drawLine(boardW/2, boundsU, boardW/2, boundsD);
		
		//centerCircle
		g2.drawOval( (boardW/2)-(boundsD/3)/2, (boardH/2)-(boundsD/3)/2, boundsD/3, boundsD/3);
		
		//Paddles		
		g2.fillRect(RPaddleX, RPaddleY, RPaddleW, RPaddleH);
		if( AI ){
			g2.setColor( Color.GRAY );
			if( ballVY > 0 ){
			    if( ballY < LPaddleY && LPaddleY>boundsU ){
			    	LPaddleY -= LPaddleVY;
			    }else if ( ballY < LPaddleY+upperLimit ){
			    	;
			    }else if ( ballY < LPaddleY+lowerLimit && LPaddleY+LPaddleH<boundsD ){
			    	LPaddleY += LPaddleVY;
			    }else if ( ballY < LPaddleY+LPaddleH && LPaddleY+LPaddleH<boundsD ){
			    	LPaddleY += LPaddleVY;
			    }else if ( ballY < boardH && LPaddleY+LPaddleH<boundsD ){
			    	LPaddleY += LPaddleVY;
			    }
			}else{
				if( ballY < LPaddleY && LPaddleY>boundsU ){
			    	LPaddleY -= LPaddleVY;
			    }else if ( ballY < LPaddleY+upperLimit && LPaddleY>boundsU ){
			    	LPaddleY -= LPaddleVY;
			    }else if ( ballY < LPaddleY+lowerLimit && LPaddleY>boundsU ){
			    	LPaddleY -= LPaddleVY;
			    }else if ( ballY < LPaddleY+LPaddleH ){
			    	;
			    }else if ( ballY < boardH && LPaddleY+LPaddleH<boundsD ){
			    	LPaddleY += LPaddleVY;
			    }
			}
			/*
			 * 	DOWN
			 * 		Above
			 *      	move Up
			 *      first third
			 *      	stay
			 * 	    middle
			 *      	move down
			 *      last third
			 *      	move down
			 *      Below
			 *      	move down
			 *	UP
    		 *		Above
        	 * 			move up
        	 *		first third
        	 *			move up
    		 *		middle
        	 *			move up
    		 *		last third
        	 *			stay
    		 *		Below
        	 *			move down
			 */
		}
		g2.fillRect(LPaddleX, LPaddleY, LPaddleW, LPaddleH);		
		//ball
		g2.setColor( Color.RED );
		g2.fillOval(ballX, ballY, ballW, ballH);
	}
	
	/*
	 * Key pressed to control paddles
	 */
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == 38 && ( RPaddleY > boundsU ) )
			rUp = true;
		if(e.getKeyCode() == 40 && ( RPaddleY + RPaddleH < boundsD ) )
			rDown = true;
		if(!AI){
			if(e.getKeyCode() == 87 && ( LPaddleY > boundsU ) )
				lUp = true;
			if(e.getKeyCode() == 83 && ( LPaddleY + LPaddleH < boundsD ) )
				lDown = true;
		}
		if(e.getKeyCode() == 32){
			paused = true;
			rUp=false; rDown=false; lUp=false; lDown=false;
			JOptionPane.showMessageDialog(null,  "Game Paused. 'OK' to resume", "", JOptionPane.PLAIN_MESSAGE);
			paused = false;
		}
	}
	
	/*
	 * Key released to control paddles
	 */
	public void keyReleased(KeyEvent e) {
		if( e.getKeyCode() == 38 )
			rUp = false;
		if( e.getKeyCode() == 40 )
			rDown = false;
		if( e.getKeyCode() == 87 )
			lUp = false;
		if( e.getKeyCode() == 83 )
			lDown = false;
		
	}
	
	/*
	 * Does Nothing, needed for implemented method
	 */
	public void keyTyped(KeyEvent arg0) {	
		;
	}
}
