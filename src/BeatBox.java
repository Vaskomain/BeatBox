import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class BeatBox {
    JPanel mainPanel;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;
    JList incomingList;
    JTextField userMessage;
    int nextNum;
    Vector<String> listVector = new Vector<String>();
    String userName;
    ObjectOutputStream out;
    ObjectInputStream in;
    HashMap<String,boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();

    String[] instrumentNames = {"Bass Drum","Closed Hi-Hat","Open Hi-Hat","Acoustic Snare","Crash Cymbal","Hand Clap",
                                "High Tom","Hi Bongo","Maracas","Свисток","Low Conga","Колокольчик","Vibraslap",
                                "Low-mid Tom","High Agogo","Open Hi Conga"};

    int [] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

    public static void main(String[] args) {
        new BeatBox().startUp("Vasko");
    }

    private void startUp(String name) {
        userName = name;
        try {
            Socket sock = new Socket("127.0.0.1",4242);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        setupMidi();
        buildGUI();
    }

    private void buildGUI() {
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton serializeIt = new JButton("Serialize It!");
        serializeIt.addActionListener(new MySendListener());
        buttonBox.add(serializeIt);

        JButton restore = new JButton("Restore");
        restore.addActionListener(new MyReadListener());
        buttonBox.add(restore);

        JButton sendIt = new JButton("Send it");
        sendIt.addActionListener(new MyNetSendListener());
        buttonBox.add(sendIt);

        userMessage = new JTextField();
        buttonBox.add(userMessage);

        incomingList = new JList();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i=0; i<16;i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST,buttonBox);
        background.add(BorderLayout.WEST,nameBox);

        theFrame.getContentPane().add(background);
        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER,mainPanel);

        for (int i=0; i<256;i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        }

        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setVisible(true);

    }

    private void setupMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildTrackAndStart(){
        int [] trackList = null;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i =0;i<16;i++) {
            trackList = new int[16];

            int key = instruments[i];

            for (int j=0;j<16;j++) {
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16*i));
                if (jc.isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList [j] = 0;
                }

                makeTracks(trackList);
                track.add(makeEvent(176,1,127,0,15));

            }

            track.add(makeEvent(192,9,1,0,15));
            try{
                sequencer.setSequence(sequence);
                sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
                sequencer.start();
                sequencer.setTempoInBPM(120);
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    private void makeTracks(int[] list) {
        for (int i=0;i<16;i++){
            int key = list[i];
            if (key!=0){
                track.add(makeEvent(144,9,key,100,i));
                track.add(makeEvent(128,9,key,100,i+1));
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
    private class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndStart();
        }
    }

    private class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    private class MyUpTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    private class MyDownTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * .97));

        }
    }

    public class MySendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = new boolean[256];

            for (int i=0; i<256;i++){
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if (check.isSelected()){
                    checkboxState[i] = true;
                }
            }

            try {

                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File("d:\\javanew\\Learning\\BeatBox\\checkbox.ser"));
                fc.showSaveDialog(theFrame);
                FileOutputStream fileStream = new FileOutputStream(fc.getSelectedFile());
                ObjectOutputStream os = new ObjectOutputStream(fileStream);
                os.writeObject(checkboxState);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public class MyReadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = null;
            try {
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File("d:\\javanew\\Learning\\BeatBox\\checkbox.ser"));
                fc.showOpenDialog(theFrame);
                FileInputStream fileIn = new FileInputStream(fc.getSelectedFile());
                ObjectInputStream is = new ObjectInputStream(fileIn);
                checkboxState = (boolean[]) is.readObject();

            } catch (Exception e1) {
                e1.printStackTrace();
            }

            for (int i=0;i<256;i++){
                JCheckBox jc = (JCheckBox) checkboxList.get(i);
                jc.setSelected(checkboxState[i]);
            }

            sequencer.stop();
            buildTrackAndStart();
        }
    }

    private class MyNetSendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = new boolean[256];
            for (int i =0; i<256; i++) {
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if (check.isSelected()) checkboxState[i] = true;
            }

            try {
                out.writeObject(userName + nextNum++ + ": "+userMessage.getText());
                out.writeObject(checkboxState);

            } catch (Exception e1) {
                System.out.println("Could not send message");
            }
            userMessage.setText("");
        }
    }

    private class MyListSelectionListener implements javax.swing.event.ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                String selected = (String) incomingList.getSelectedValue();
                if (selected != null) {
                    boolean[] selectedState = (boolean[]) otherSeqsMap.get(selected);
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                }
            }
        }

    }

    private void changeSequence(boolean[] checkboxState) {
        for (int i=0; i<256; i++) {
            JCheckBox check = (JCheckBox) checkboxList.get(i);
            check.setSelected(checkboxState[i]);
        }
    }

    private class RemoteReader implements Runnable {
        boolean checkboxState[] = null;
        String nameToShow = null;
        Object obj = null;

        @Override
        public void run() {
                try {
                    while ((obj=in.readObject())!=null) {
                        System.out.println("object from server");
                        System.out.println(obj.getClass());
                        nameToShow = (String) obj;
                        checkboxState = (boolean[]) in.readObject();
                        otherSeqsMap.put(nameToShow,checkboxState);
                        listVector.add(nameToShow);
                        incomingList.setListData(listVector);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }
}
