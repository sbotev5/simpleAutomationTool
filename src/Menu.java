import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.util.*;

public class Menu extends JFrame {

    private JButton record;
    private JButton stopRecord;
    private JPanel buttonPanel;
    private JPanel clockPanel;
    private JPanel recordings;
    private JPanel mainPanel;

    public static boolean shouldRecord = false;
    public static LinkedHashMap<String, Object> actions;
    public static ArrayList<LinkedHashMap<String, Object>> setsOfActions;
    public static HashMap<UUID, JPanel> updateGUI;
    public static int mpCounter = 0;
    public static int kpCounter = 0;
    public static int mmCounter = 0;
    private Robot robot;


    public Menu(Robot robot) {

        super("AUTOMATION TOOL");
        this.robot = robot;
    }


    public void init() {

        new Thread(new MyThread()).start();
        setsOfActions = new ArrayList<LinkedHashMap<String, Object>>();
        updateGUI = new HashMap<UUID, JPanel>();
        mainPanel = new JPanel();
        setContentPane(mainPanel);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        Font buttonFont = new Font("BUTTON", Font.BOLD, 25);
        record = new JButton("RECORD");
        record.setFont(buttonFont);
        stopRecord = new JButton("STOP RECORDING");
        stopRecord.setFont(buttonFont);

        buttonPanel = new JPanel();
        buttonPanel.add(record);
        buttonPanel.add(stopRecord);

        recordings = new JPanel();
        recordings.setLayout(new GridLayout(1, 5));
        recordings.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3)), "RECORDINGS",
                TitledBorder.CENTER, TitledBorder.TOP));

        clockPanel = new JPanel();
        ClockLabel clock = new ClockLabel();
        clock.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));

        clockPanel.add(clock);

        record.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actions = new LinkedHashMap<String, Object>();
                shouldRecord = true;
                setExtendedState(JFrame.ICONIFIED);
                record.setEnabled(false);
            }
        });

        stopRecord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (actions != null) {

                    String lastEntry = null;

                    for (String key : actions.keySet()) {

                        lastEntry = key;
                    }

                    actions.remove(lastEntry);

                    shouldRecord = false;
                    record.setEnabled(true);

                    RecordingStatsFrame statsFrame = new RecordingStatsFrame();
                    statsFrame.init();

                } else JOptionPane.showMessageDialog(null, "YOU HAVEN'T STARTED A RECORDING !");

            }
        });

        mainPanel.add(buttonPanel);
        mainPanel.add(clockPanel);
        mainPanel.add(recordings);

    }

    public int[] printActionStats() {

        int[] stats = new int[3];
        int howManyMovements = 0;
        int howManyMouseClicks = 0;
        int howManyKeyPresses = 0;

        for (Map.Entry<String, Object> entry : actions.entrySet()) {
            if (entry.getKey().contains("MouseMove")) {

                howManyMovements++;

            } else if (entry.getKey().contains("KeyBoard")) {

                howManyKeyPresses++;

            } else if (entry.getKey().contains("MouseButton")) {

                howManyMouseClicks++;
            }
        }

        stats[0] = howManyMovements;
        stats[1] = howManyMouseClicks;
        stats[2] = howManyKeyPresses;

        return stats;
    }

    public void executeActions(LinkedHashMap<String, Object> var) {

        for (Map.Entry<String, Object> entry : var.entrySet()) {

            if (entry.getKey().contains("MouseMove")) {

                Point point = (Point) entry.getValue();
                robot.mouseMove((int) point.getX(), (int) point.getY());

                try {
                    Thread.sleep(1);
                } catch (Exception exc) {
                    System.out.println("Sleep");
                }

            } else if (entry.getKey().contains("KeyBoard")) {
                try {

                    robot.keyPress(Main.keyboard.get(entry.getValue()));
                    robot.keyRelease(Main.keyboard.get(entry.getValue()));

                    try {
                        Thread.sleep(500);
                    } catch (Exception exc) {
                        System.out.println("Sleep");
                    }

                } catch (Exception exc) {
                    System.out.println("Keyboard button not recognised!");
                }

            } else if (entry.getKey().contains("MouseButton")) {

                try {

                    if ((int) entry.getValue() == 1) {

                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                        try {
                            Thread.sleep(500);
                        } catch (Exception exc) {
                            System.out.println("Sleep");
                        }

                    } else {

                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);

                        try {
                            Thread.sleep(500);
                        } catch (Exception exc) {
                            System.out.println("Sleep");
                        }
                    }

                } catch (Exception exc) {
                    System.out.println("Mouse button not recognised!");
                }
            }
        }
    }

    private class RecordingStatsFrame extends JFrame {

        private JSpinner hour;
        private JSpinner minute;
        private JButton setTime;
        private JTextField nameOfRec;
        private JLabel enterHour;
        private JLabel enterMinute;
        private JLabel enterName;


        public RecordingStatsFrame() {
            super("RECORDING DETAILS");
        }

        public void init() {

            record.setEnabled(false);
            stopRecord.setEnabled(false);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            setContentPane(panel);

            setTime = new JButton("SET TIME");
            hour = new JSpinner();
            minute = new JSpinner();
            nameOfRec = new JTextField();

            int[] stats = printActionStats();

            JFormattedTextField txt1 = ((JSpinner.NumberEditor) hour.getEditor()).getTextField();
            ((NumberFormatter) txt1.getFormatter()).setAllowsInvalid(false);

            JFormattedTextField txt2 = ((JSpinner.NumberEditor) minute.getEditor()).getTextField();
            ((NumberFormatter) txt2.getFormatter()).setAllowsInvalid(false);

            Font font = new Font("Panel", Font.BOLD, 20);

            Font font2 = new Font("Spinner", Font.BOLD, 50);

            nameOfRec.setFont(font);

            hour.setFont(font2);
            minute.setFont(font2);

            enterHour = new JLabel("ENTER HOUR FOR AUTOMATIC EXECUTION", SwingConstants.CENTER);
            enterHour.setFont(font);

            enterName = new JLabel("ENTER NAME OF RECORDING", SwingConstants.CENTER);
            enterName.setFont(font);

            enterMinute = new JLabel("ENTER MINUTE FOR AUTOMATIC EXECUTION", SwingConstants.CENTER);
            enterMinute.setFont(font);

            hour.setValue(0);
            hour.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    if ((int) hour.getValue() < 0 || (int) hour.getValue() > 23) {
                        JOptionPane.showMessageDialog(null, "INVALID HOUR");
                        hour.setValue(0);
                    }
                }
            });

            minute.setValue(0);

            minute.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {

                    if ((int) minute.getValue() < 0 || (int) minute.getValue() > 59) {
                        JOptionPane.showMessageDialog(null, "INVALID MINUTE");
                        minute.setValue(0);
                    }
                }
            });

            setTime.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    if (nameOfRec.getText().equals("")) {

                        JOptionPane.showMessageDialog(null, "PLEASE ENTER A NAME !");

                    } else {

                        actions.put("RecordingName", nameOfRec.getText());
                        actions.put("Hour", hour.getValue());
                        actions.put("Minute", minute.getValue());
                        UUID id = UUID.randomUUID();
                        actions.put("ID", id);

                        dispose();

                        JPanel singleRecording = new JPanel();
                        singleRecording.setLayout(new GridLayout(9, 1));
                        singleRecording.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

                        JLabel name = new JLabel("Recording Name : " + (String) actions.get("RecordingName"), SwingConstants.CENTER);
                        JLabel timeForExecution = new JLabel("Time for execution : " + Integer.toString((int) actions.get("Hour")) + ":" + Integer.toString((int) actions.get("Minute")), SwingConstants.CENTER);

                        Font font = new Font("Panel", Font.BOLD, 15);

                        name.setFont(font);

                        timeForExecution.setFont(font);

                        JLabel numKeyPress = new JLabel("Number Of Key Presses : " + String.valueOf(stats[2]), SwingConstants.CENTER);
                        numKeyPress.setFont(font);

                        JLabel numPointerMoves = new JLabel("Number Of Mouse Pointer Movements : " + String.valueOf(stats[0]), SwingConstants.CENTER);
                        numPointerMoves.setFont(font);

                        JLabel numMouseClicks = new JLabel("Number Of Mouse Clicks : " + String.valueOf(stats[1]), SwingConstants.CENTER);
                        numMouseClicks.setFont(font);

                        JLabel numTotalMoves = new JLabel("Number Of Total Movements : " + String.valueOf(actions.size() - 4), SwingConstants.CENTER);
                        numTotalMoves.setFont(font);

                        JLabel idNumber = new JLabel("ID : " + id, SwingConstants.CENTER);
                        idNumber.setFont(font);

                        JButton remove = new JButton("Remove Recording");

                        remove.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {

                                for (Iterator<LinkedHashMap<String, Object>> listIT = setsOfActions.iterator(); listIT.hasNext(); ) {

                                    if (listIT.next().get("ID").equals(id)) {
                                        listIT.remove();
                                    }
                                }

                                recordings.remove(singleRecording);
                                recordings.revalidate();
                            }
                        });

                        singleRecording.add(name);
                        singleRecording.add(timeForExecution);
                        singleRecording.add(numKeyPress);
                        singleRecording.add(numPointerMoves);
                        singleRecording.add(numMouseClicks);
                        singleRecording.add(numTotalMoves);
                        singleRecording.add(idNumber);
                        singleRecording.add(remove);

                        updateGUI.put(id, singleRecording);

                        recordings.add(singleRecording);

                        setsOfActions.add(actions);

                        actions = null;

                        record.setEnabled(true);
                        stopRecord.setEnabled(true);

                    }
                }
            });

            setTime.setFont(new Font("BUTTON", Font.BOLD, 40));

            panel.add(enterName);
            panel.add(nameOfRec);
            panel.add(enterHour);
            panel.add(hour);
            panel.add(enterMinute);
            panel.add(minute);
            panel.add(setTime);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);

                    dispose();

                    actions = null;

                    record.setEnabled(true);
                    stopRecord.setEnabled(true);

                }
            });

            setSize(500, 500);
            setVisible(true);
        }
    }

    private class MyThread implements Runnable {

        @Override
        public void run() {

            while (true) {

                LocalTime time = LocalTime.now();

                if (!setsOfActions.isEmpty()) {

                    for (Iterator<LinkedHashMap<String, Object>> listIT = setsOfActions.iterator(); listIT.hasNext(); ) {

                        LinkedHashMap<String, Object> forCheck = listIT.next();

                        if ((int) forCheck.get("Hour") == time.getHour() && (int) forCheck.get("Minute") == time.getMinute()) {

                            setExtendedState(JFrame.ICONIFIED);

                            executeActions(forCheck);

                            listIT.remove();

                            recordings.remove(updateGUI.get(forCheck.get("ID")));
                            recordings.revalidate();
                        }
                    }
                }
            }
        }
    }
}