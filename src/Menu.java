import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class Menu extends JFrame {

    private JButton record;
    private JButton stopRecord;
    private JButton loadRecording;
    private JPanel buttonPanel;
    private JPanel clockPanel;
    private JPanel recordings;
    private JPanel mainPanel;

    static boolean shouldRecord;
    private static long startTime;  // start time and stop time used for duration of a recording
    static long stopTime;
    static List<Recording> allUserRecordings;  // keep user recordings here
    static List<Movement> singleRecording;

    private Robot robot;

    Menu(Robot robot) {

        super("AUTOMATION TOOL");
        this.robot = robot;

    }

    void initialize() {   // code style

        new Thread(new TimeCheck()).start();
        allUserRecordings = Collections.synchronizedList(new LinkedList<>()); //needs to be synced for thread safety
        shouldRecord = false;

        mainPanel = new JPanel();
        setContentPane(mainPanel);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("BUTTON", Font.BOLD, 25);

        record = new JButton("RECORD");
        record.setFont(buttonFont);
        stopRecord = new JButton("STOP RECORDING");
        stopRecord.setFont(buttonFont);
        loadRecording = new JButton("LOAD RECORDING");
        loadRecording.setFont(buttonFont);

        buttonPanel = new JPanel();
        buttonPanel.add(record);
        buttonPanel.add(stopRecord);
        buttonPanel.add(loadRecording);

        recordings = new JPanel();
        recordings.setLayout(new GridLayout(1, 5));

        clockPanel = new JPanel();
        ClockLabel clock = new ClockLabel();


        clockPanel.add(clock);

        record.addActionListener(e -> {     //use of lambdas

            singleRecording = new LinkedList<>();
            startTime = System.nanoTime();
            shouldRecord = true;

            setExtendedState(JFrame.ICONIFIED);
            record.setEnabled(false);
            loadRecording.setEnabled(false);
        });

        stopRecord.addActionListener(e -> { // use of lambdas

            if (singleRecording != null) {   // prevent user from not having a recording to stop

                shouldRecord = false;

                singleRecording.remove(singleRecording.size() - 1); // the last click on "Stop Record" is not needed during playback

                stopTime = System.nanoTime() - startTime;  // accurately duration
                record.setEnabled(true);
                loadRecording.setEnabled(true);

                new RecordingStatsFrame(this).initialize();

            } else JOptionPane.showMessageDialog(this, "YOU HAVEN'T STARTED A RECORDING!"); // informing the user

        });

        loadRecording.addActionListener(e -> loadFile());  // separated in a dedicated method for code clarity

        mainPanel.add(buttonPanel);
        mainPanel.add(clockPanel);
        mainPanel.add(recordings);

    }

    private void loadFile() {   // used to load a serialized recording

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory()); //default to home dir
        jfc.setDialogTitle("Choose a recording file to load");

        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setMultiSelectionEnabled(false);

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Desktop Bot Recordings", "desktopbotFile"); // custom extension
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.addChoosableFileFilter(filter);

        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {

            File selectedFile = jfc.getSelectedFile();

            try {
                // deserialize and store
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile));
                singleRecording = (List<Movement>) in.readObject();
                in.close();
                new RecordingStatsFrame(this).initialize();

            } catch (IOException | ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void executeMovements(Recording var) {   // when it is playback time this method goes through each movement and executes it

        boolean holdButton = false;  // for mouse dragging; left mouse button hold

        for (Movement movement : var.getMovements()) {

            switch (movement.getType()) {

                case "MouseMove": {

                    Point point = (Point) movement.getMovement();
                    robot.mouseMove((int) point.getX(), (int) point.getY());

                    holdButton = false;

                    robot.delay(3);

                    break;
                }
                case "WheelMove":

                    robot.mouseWheel((int) movement.getMovement());

                    holdButton = false;

                    robot.delay(300);

                    break;

                case "MouseButton":

                    try {

                        if ((int) movement.getMovement() == 1) {

                            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                            holdButton = false;

                            robot.delay(500);

                        } else if ((int) movement.getMovement() == 3) {

                            robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                            robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);

                            holdButton = false;

                            robot.delay(500);

                        } else {

                            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);

                            holdButton = false;

                            robot.delay(500);
                        }

                    } catch (Exception exc) {
                        System.err.println("Mouse button not recognised!");
                    }

                    break;

                case "MouseDrag": {
                    if (!holdButton) {   // in order to achieve the mouse drag effect during playback
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        holdButton = true;
                    }

                    Point point = (Point) movement.getMovement();

                    robot.mouseMove((int) point.getX(), (int) point.getY());

                    robot.delay(3);

                    break;
                }

                case "KeyPress":

                    try {

                        robot.keyPress(Main.keyboard.get(movement.getMovement()));
                        robot.keyRelease(Main.keyboard.get(movement.getMovement()));

                        holdButton = false;

                        robot.delay(500);
                    } catch (Exception exc) {
                        System.err.println("Keyboard button not recognised!");
                    }
                    break;

                case "KeyCombo":

                    LinkedList<Integer> whichModifiers = (LinkedList<Integer>) movement.getMovement();

                    try {

                        for (Integer whichModifier : whichModifiers) {
                            robot.keyPress(Main.keyboard.get(whichModifier));
                        }

                        for (int i = whichModifiers.size() - 1; i >= 0; i--) {
                            robot.keyRelease(Main.keyboard.get(whichModifiers.get(i)));
                        }

                        holdButton = false;

                        robot.delay(500);

                    } catch (Exception exc) {
                        System.err.println("Keyboard button not recognised!");
                    }
                    break;
            }
        }
    }

    private class TimeCheck implements Runnable {

        @Override
        public void run() {   //constantly checks if a recording should be executed

            while (true) {

                LocalTime time = LocalTime.now(); //getting current time

                if (!allUserRecordings.isEmpty()) {

                    synchronized (allUserRecordings) {

                        for (Iterator<Recording> listIT = allUserRecordings.iterator(); listIT.hasNext(); ) {

                            Recording forCheck = listIT.next();

                            if (forCheck.getHour() == time.getHour() && forCheck.getMinute() == time.getMinute()) {

                                record.setEnabled(false);
                                stopRecord.setEnabled(false);
                                loadRecording.setEnabled(false);


                                setExtendedState(JFrame.ICONIFIED);

                                executeMovements(forCheck);

                                record.setEnabled(true);
                                stopRecord.setEnabled(true);
                                loadRecording.setEnabled(true);

                                listIT.remove();

                                recordings.remove(forCheck.getPanelGUI());  // updating the GUI when the playback is done
                                recordings.revalidate();
                            }
                        }
                    }
                }
            }
        }
    }

    JButton getRecord() {
        return record;
    }

    JButton getStopRecord() {
        return stopRecord;
    }

    JButton getLoadRecording() {
        return loadRecording;
    }

    JPanel getRecordings() {
        return recordings;
    }
}
