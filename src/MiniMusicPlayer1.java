import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;


public class MiniMusicPlayer1 {

    static JFrame frame = new JFrame("My music");
    static MyDrawPanel ml;

    class MyDrawPanel extends JPanel implements ControllerEventListener{

        boolean msg = false;

        @Override
        public void controlChange(ShortMessage event) {
            msg = true;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (msg){
                Graphics2D g2d = (Graphics2D) g;
                Color randomColor = new Color((int) (Math.random() * 220), (int) (Math.random() * 255), (int) (Math.random() * 255));
                g.setColor(randomColor);
                int y = (int) ((Math.random() * 60) + 10);
                int width = (int) ((Math.random() * 120) + 10);
                int height = (int) ((Math.random() * 120) + 10);
                int x = 20;
                g.fillRect(x,y,width, height);
                msg = false;
            }
        }
    }

    public static MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd,chan,one,two);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return event;

    }

    public void play(){
        setupGUI();
        try {

            Sequencer player = MidiSystem.getSequencer();
            player.open();

            int[] eventsIWant = {127};
            player.addControllerEventListener(ml,eventsIWant);

            Sequence seq = new Sequence(Sequence.PPQ,4);
            Track track = seq.createTrack();

            int r = 0;

            for (int i=0; i<60;i+=4) {
                r = (int) (Math.random() * 50) + 1;
                track.add(makeEvent(144,1,r,100,i));
                track.add(makeEvent(176,1,127,0,i));
                track.add(makeEvent(128,1,r,100,i+2));
            }

            player.setSequence(seq);
            player.setTempoInBPM(120);
            player.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setupGUI() {
        ml = new MyDrawPanel();
        frame.setContentPane(ml);
        frame.setBounds(30,30,300,300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        MiniMusicPlayer1 mini = new MiniMusicPlayer1();
        mini.play();

    }

}
