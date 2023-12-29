package nl.ou.debm.devtools.explorer;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;

/*
This class is a JButton that is its own MouseListener. Example usage:
ListeningButton button = new ListeningButton("click here") {
 	public void mouseReleased(MouseEvent e) {
 		System.out.println("MouseReleased has taken place.");
 	}
 };
*/
public class ListeningButton extends JButton implements MouseListener {
    public ListeningButton(String text) {
        super(text);
        this.addMouseListener(this);
    }
    public void mouseReleased(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
