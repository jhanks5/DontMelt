package javaGame;

import javax.swing.*;
import java.awt.*;

public class Renderer extends JPanel{

    private static final long serialVersionUID = 1L; //declare serial to clear warnings
    
    protected void paintComponent(Graphics g) 
    {
        Main.main.repaint(g); //continually repaints itself
    }

}