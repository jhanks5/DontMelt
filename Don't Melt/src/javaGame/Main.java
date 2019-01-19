package javaGame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat; //needed for speed
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

/* Comments:
 *  -Vision for the game is an ice cube picking up snow in order to stay cold while dodging fire
 * 	-Added a score counter that continually increases as long as the player is alive
 * 	-Added a counter that indicates how many times player has jumped
 * 	-Speed is now displayed and increases each time space bar is pressed by 0.10
 * 	-Added a tracker for the player's best speed across all runs in that session
 *  -Added a variable that keeps track of the highest score player has earned
 *  -Added a tracker for score earned across entire session
 *  -Added a points system based on intersecting the gold coins, with a tracker that indicates highest points earned in a single run
 *  -Added a health system where players are damaged when intersecting with fire balls rather than losing instantly
 *  -Added a health pickup mechanic where players can intersect with snow balls to restore their health
 *  -Added pixel art for player character, the hazard below, the obstacles, the health drops, and the gold coins
 *   -Note: Pixel art is a design choice, going for a retro, Atari style
 * 	-Applicable variables reset at the end of a run (speed, score, jumps; contest for highScore, bestSpeed, and bestPoints)
 *  -Statistics displayed on-screen include: score, health, jumps, points, speed, most points, high score, best speed, and total score for session
 * 	-Cleaned up code at various locations, most notably with for loops for applicable add___ functions and nesting in actionPerformed
 *  ---10 total all-new features and numerous quality of life improvements within the code
/* Rules:
 * 	-Press space to jump.
 *  -Speed increases with each jump.
 *  -Health depletes while intersecting with the fire balls, and falling into the lava below is an instant death.
 * 	-Intersect with the coins to gain Gold, a quantitative measure of each run beyond just distance.
 *  -Pass over the snow balls to gain Health, designed to reward players who play for pickups.
 *  -Best strategy is to focus on avoiding fire balls.
 *  -Don't melt!
 */ 

public class Main implements ActionListener, KeyListener{
	
    public static Main main;
    public final int WIDTH = 800, HEIGHT = 600;
    public final double DEFAULT_SPEED = 10.0;
    public final int DEFAULT_HEALTH = 30;
    public Renderer renderer; //Renderer.java runs the repaint method
    public Rectangle character; 
    public ArrayList<Rectangle> fireBall, gold, healthDrop; //on-screen elements besides player character
    public Random rand; //for randomly generating location of on-screen elements
    public boolean start = false, gameover = false;
    public int tick, score, spacePressed, highScore, sessionScore, goldCounter, bestGold, health = DEFAULT_HEALTH;
    public double speed = DEFAULT_SPEED, bestSpeed;

    public Main()
    {
        JFrame jFrame = new JFrame();
        Timer timer = new Timer(20, this);
      
        renderer = new Renderer();
        rand = new Random();

        jFrame.setTitle("Don't Melt!");
        jFrame.add(renderer); //add Renderer object to jFrame
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(WIDTH, HEIGHT);
        jFrame.addKeyListener(this);
        jFrame.setVisible(true); //performed last

        fireBall = new ArrayList<Rectangle>();
        gold = new ArrayList<Rectangle>();
        healthDrop = new ArrayList<Rectangle>();
      
        character = new Rectangle(200, 220, 110, 110); //Rectangle(int x, int y, int width, int height)
        
        for (int i = 0; i < 5; i++)
        {
        	addfireBall(true); //boolean start is criteria for add____ methods
        }
        
        for (int i = 0; i < 2; i++)
        {
        	addGold(true);
        }
        	
        	addHealth(true); //only one per cycle
       
        timer.start();
    } 
    
    public void repaint(Graphics g) //fills frame with Renderer.java
    {
        g.setColor(Color.black); 
        g.fillRect(0,0, WIDTH, HEIGHT); //background 
        
        Rectangle hazard = new Rectangle(0, HEIGHT - 200, WIDTH, 100); //lava at the bottom
        Image hazardPic = Toolkit.getDefaultToolkit().getImage("pixelLava.png"); 
        g.drawImage(hazardPic, hazard.x, hazard.y, renderer);
        
        Image charPic = Toolkit.getDefaultToolkit().getImage("iceCube.png"); //player character
        g.drawImage(charPic, character.width + 20, character.y - 80, renderer);
       
        for (Rectangle rect : fireBall) //next 3 for loops apply images to ArrayList elements
        { 
            Image fireBallPic = Toolkit.getDefaultToolkit().getImage("fireBall.png");
            g.drawImage(fireBallPic, rect.x-100, rect.y-100, renderer);
        }
        
        for (Rectangle rect : gold)
        {
    		Image coinPic = Toolkit.getDefaultToolkit().getImage("goldCoin.png");
    		g.drawImage(coinPic, rect.x-100, rect.y-100, renderer);
        }

        for (Rectangle rect : healthDrop)
        {
    		Image healthPic = Toolkit.getDefaultToolkit().getImage("healthArt.png");
            g.drawImage(healthPic, rect.x-100, rect.y-100, renderer);
        }
        
        g.setColor(Color.cyan); //color of all font
        g.setFont(new Font("Agency FB", 1 ,75));
        
        if (!start) 
        { 
            g.drawString("Press Space to begin!", 100, HEIGHT / 2);
            g.setFont(new Font("Agency FB", 1 ,25)); //sizing
            g.drawString("Pass snow for health and coins for gold!", 240, HEIGHT/2+60);
        }
        else if (gameover) 
        { 
            g.drawString("Game Over!", 233, HEIGHT / 2);
        }
        
        DecimalFormat truncateSpeed = new DecimalFormat("#0.0"); //keeps hanging decimals off-screen as speed is increased
        //On-screen counters, in descending order
        g.setFont(new Font("Agency FB", 1, 22)); //for sizing
        g.drawString("Score:  " + score, 5, 35); //left side
        g.drawString("Jumps:  " + spacePressed, 5, 60);
        g.drawString("Gold: " + goldCounter, 5, 85);
        g.drawString("Speed: " + truncateSpeed.format(speed), 5, 110);
        g.drawString("Total Score: " + sessionScore, WIDTH-155, 35); //right side
        g.drawString("High Score: " + highScore, WIDTH-155, 60);
        g.drawString("Most Gold: " + bestGold, WIDTH-155, 85);
        g.drawString("Best Speed: " + truncateSpeed.format(bestSpeed), WIDTH-155, 110);
        g.drawString("Health: " + health, 340, 35); //top middle
    }

    public void addfireBall(boolean start) //populate route with obstacles
    { 
       int width = 350; //any higher is too punishing upon intersection
       if (start) //start = true, continuous
       {
    	   fireBall.add(new Rectangle(WIDTH + width + fireBall.size() * 300, rand.nextInt(HEIGHT-120), 80, 100));
       }
       else //off-screen
       {
    	   fireBall.add(new Rectangle(fireBall.get(fireBall.size() - 1).x + 300, rand.nextInt(HEIGHT-120), 80, 100));
       }
    } 
    
    public void addGold(boolean start) //mechanism for generating the gold
    {
    	int width = 500; //larger area, easier to collect
    	if (start)
    	{
    		gold.add(new Rectangle(WIDTH + width + gold.size() * 3, rand.nextInt(HEIGHT-120), 15, 20));
    	}
    	else
    	{
    		gold.add(new Rectangle(gold.get(gold.size() - 1).x + 300, rand.nextInt(HEIGHT-120), 15, 20));
    	}
    }
    
    public void addHealth(boolean start) //mechanism for spawning health pickups
    {
    	int width = 500; //larger area, easier to collect
    	if (start)
    	{
    		healthDrop.add(new Rectangle(WIDTH + width + gold.size() * 3, rand.nextInt(HEIGHT-120), 15, 20));
    	}
    	else
    	{
    		healthDrop.add(new Rectangle(gold.get(gold.size() - 1).x + 300, rand.nextInt(HEIGHT-120), 15, 20));
    	}
    }

    public void flap() //controls what happens when you press space
    {
        if (gameover) //if on Game Over! screen, resets game
        {
        	character = new Rectangle(220, 240, 100, 100);
            fireBall.clear();
            gold.clear();
            healthDrop.clear();
            
            for (int i = 0; i < 5; i++)
            {
            	addfireBall(true);
            }
            
            for (int i = 0; i < 2; i++)
            {
            	addGold(true); 
            }
            
            addHealth(true);
            
            gameover = false; //run setup procedure then restart
        }

        if (!start) //space starts game if not started
        {
            start = true;
        }
        else if (!gameover) //jump command
        {
            character.y -= 50;
        	speed += 0.1; //increases speed every jump
            tick = 0;
        }
    }

    @Override //implements ActionListener
    public void actionPerformed(ActionEvent e)
    {
        if (start) 
        {//Condensed for loops below into cleaner, nested loops
           for (int i = 0; i < fireBall.size(); i++) 
           {//This for loop and the following two function as fail-safes for spawning
               Rectangle rect = fireBall.get(i);
               rect.x -= speed;
               if (rect.x + rect.width < 0) //runs for each i
               {
            	   fireBall.remove(rect);
                   addfireBall(false);
               }
           } 
           
           for (int i = 0; i < gold.size(); i++) 
           {
               Rectangle rect = gold.get(i);
               rect.x -= speed;
               if (rect.x + rect.width < 0) //runs for each i
               {
            	   gold.remove(rect);
                   addGold(false);
               }
           }
           
           for (int i = 0; i < healthDrop.size(); i++) 
           {
               Rectangle rect = healthDrop.get(i);
               rect.x -= speed;
               if (rect.x + rect.width < 0) //runs for each i
               {
            	   healthDrop.remove(rect);
                   addHealth(false);
               }
           }
        	   
           for (Rectangle rect : fireBall)
           {//Game over condition besides going off-screen
               if (rect.intersects(character))
               {//Collisions are less punishing at higher speeds (less time intersecting)
            	   health -= 1;
               }
               if (health == 0) //when health is fully depleted
               {
            	   gameover = true;
            	   character.x -= speed;
            	   health = DEFAULT_HEALTH; //reset health for next run
               }
           }
           //Two for loops below, when player intersects with pickups
           for (Rectangle rect : gold)
           {
         	  if (rect.intersects(character))
         	  {//While intersecting, goldCounter goes up by 10
         		  goldCounter += 10; 
         	  }
           }
           
           for (Rectangle rect : healthDrop)
           {//Health increased by 1 while intersecting snow
        	   if (rect.intersects(character))
        	   {
        		   health += 1;
        	   }
           }
          
            tick++; //score continually increases as long as player is still alive
            
            if (!gameover) //keeps sessionScore from increasing on Game Over! screen
            {
            	   sessionScore += tick; //score that doesn't reset, tracks whole session
            	   score += tick; //must be score += tick and not score = tick because of flap() method
            }
            
            if (tick %2 == 0 && character.y < HEIGHT-100) 
            {     
            	character.y += tick;
            }
            
            if (gameover && character.y >= HEIGHT - 100) //lose without falling into hazard
            {
                character.x -= speed;
            }
            
            if (character.y >=  HEIGHT - 100 || character.y < 0) 
            { 
                gameover = true;
            }
//Long sequence of score keeping conditional statements found below
            if (gameover && score > highScore && speed > bestSpeed && goldCounter > bestGold)
            {//Generally speaking, if one of the > conditions is true, they will all be true but there are catches below
            	highScore = score; //All statements below log record scores and then reset them for new iteration
            	bestGold = goldCounter;
            	bestSpeed = speed;
            	score = 0;
            	goldCounter = 0;
            	spacePressed = 0;
            	speed = DEFAULT_SPEED; //speed and health always reset at the end of a run
            	health = DEFAULT_HEALTH;
            }
            else if (gameover && score > highScore)
            {
            	highScore = score;
            	score = 0;
            	goldCounter = 0;
            	spacePressed = 0;
            	speed = DEFAULT_SPEED;
            	health = DEFAULT_HEALTH;
            }
            else if (gameover && speed > bestSpeed)
            {
            	bestSpeed = speed;
            	score = 0;
            	goldCounter = 0;
            	spacePressed = 0;
            	speed = DEFAULT_SPEED;
            	health = DEFAULT_HEALTH;
            }
            else if (gameover && goldCounter > bestGold)
            {
            	bestGold = goldCounter;
            	score = 0;
            	goldCounter = 0;
            	spacePressed = 0;
            	speed = DEFAULT_SPEED; 
            	health = DEFAULT_HEALTH;
            }
            else if (gameover) //score !> highScore && speed !> highSpeed && pointCounter !> bestPoints
            {
            	spacePressed = 0;
            	score = 0;
            	goldCounter = 0;
            	speed = DEFAULT_SPEED;
            	health = DEFAULT_HEALTH;
            }
//Conditional statements for updating the record at gameover state end here
        }//end of if(start) on line 226
        renderer.repaint();
    } 

    public static void main(String args[]) 
    {
       main = new Main(); //begins Main constructor
    }
  
    @Override //implements KeyListener
    public void keyReleased(KeyEvent e) 
    {	
        if (e.getKeyCode() == KeyEvent.VK_SPACE) 
        {
        	spacePressed++; //increment jump counter
            flap();
        } 
    }
     
    @Override 
    public void keyTyped(KeyEvent e) 
    { }

    @Override
    public void keyPressed(KeyEvent e) 
    { }
}
