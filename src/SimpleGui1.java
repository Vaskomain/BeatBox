import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimpleGui1{

    JFrame frame;
    JLabel label;
    MyPanel panel;

    public class MyPanel extends JPanel{

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            Color startColor = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
            Color endColor = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));

            g2d.setPaint(new GradientPaint(70,70,startColor,150,150,endColor));
            g2d.fillOval(70,70,100,100);

        }
    }

    public void go(){

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton labelbutton = new JButton("Change Label");
        labelbutton.addActionListener(new LabelListener());
        JButton colorbutton = new JButton("Change Circle");
        colorbutton.addActionListener(new ColorListener());
        label = new JLabel("I'm a label");
        frame.getContentPane().add(BorderLayout.SOUTH,colorbutton);
        frame.getContentPane().add(BorderLayout.EAST,labelbutton);
        frame.getContentPane().add(BorderLayout.WEST,label);
        frame.getContentPane().add(BorderLayout.CENTER,new MyPanel());

        frame.setSize(300,300);
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        SimpleGui1 gui = new SimpleGui1();
        gui.go();

    }

    private class LabelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            label.setText("Yes");
        }
    }

    private class ColorListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            frame.repaint();
        }
    }
}
