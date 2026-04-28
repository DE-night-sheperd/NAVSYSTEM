import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatPanel {

    public static JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);

        // Header with Call Buttons
        JPanel headerPanel = UIUtils.pageHeader("Nearby Chat", "Connect with other students and staff around campus");
        JPanel callActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        callActions.setOpaque(false);
        callActions.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 20));
        
        JButton voiceCallBtn = UIUtils.secondaryBtn("📞 Voice Call");
        JButton videoCallBtn = UIUtils.primaryBtn("📹 Video Call");
        
        callActions.add(voiceCallBtn);
        callActions.add(videoCallBtn);
        headerPanel.add(callActions, BorderLayout.SOUTH);

        root.add(headerPanel, BorderLayout.NORTH);

        // Chat History Area
        JPanel chatBox = new JPanel();
        chatBox.setLayout(new BoxLayout(chatBox, BoxLayout.Y_AXIS));
        chatBox.setBackground(Color.WHITE);
        chatBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatBox);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);

        // Input Area
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.BORDER),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JTextField messageField = UIUtils.styledField("Type a message...");
        messageField.setPreferredSize(new Dimension(0, 45));

        JButton sendBtn = UIUtils.primaryBtn("Send");
        JButton micBtn = UIUtils.secondaryBtn("🎤");
        micBtn.setToolTipText("Hold to Record Voice Note");

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(micBtn);
        actionsPanel.add(sendBtn);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(actionsPanel, BorderLayout.EAST);
        root.add(inputPanel, BorderLayout.SOUTH);

        Runnable loadMessages = () -> {
            chatBox.removeAll();
            for (ChatMessage msg : Database.chatMessages) {
                boolean isMe = Database.currentUser != null && msg.sender.equals(Database.currentUser.name);
                JPanel msgPanel = new JPanel(new BorderLayout());
                msgPanel.setBackground(Color.WHITE);
                msgPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                // Bubble
                JPanel bubble = new JPanel(new BorderLayout());
                bubble.setBackground(isMe ? UIUtils.ACCENT : new Color(241, 245, 249));
                bubble.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
                bubble.setOpaque(false);

                // Custom painting for rounded bubble
                JPanel bubbleWrapper = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(isMe ? UIUtils.ACCENT : new Color(241, 245, 249));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                        super.paintComponent(g);
                    }
                };
                bubbleWrapper.setOpaque(false);
                bubbleWrapper.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

                JLabel senderLabel = new JLabel(msg.sender + "  •  " + msg.timestamp);
                senderLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
                senderLabel.setForeground(isMe ? new Color(255, 255, 255, 200) : UIUtils.TEXT2);

                JComponent contentComponent;

                if (msg.text.startsWith("[CALL]")) {
                    String[] parts = msg.text.split(":", 2);
                    String callType = parts[1];
                    JButton joinBtn = UIUtils.primaryBtn("Join " + callType);
                    joinBtn.setBackground(new Color(34, 197, 94)); // Green for join
                    joinBtn.addActionListener(e -> {
                        JOptionPane.showMessageDialog(null, "Connecting to " + callType + "...", "Call", JOptionPane.INFORMATION_MESSAGE);
                    });
                    
                    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                    p.setOpaque(false);
                    JLabel l = new JLabel("Started a " + callType);
                    l.setForeground(isMe ? Color.WHITE : UIUtils.TEXT1);
                    p.add(l);
                    p.add(joinBtn);
                    contentComponent = p;
                } else if (msg.text.startsWith("[VOICENOTE]")) {
                    String duration = msg.text.substring(11);
                    JButton playBtn = UIUtils.secondaryBtn("▶ Play " + duration);
                    playBtn.addActionListener(e -> {
                        playBtn.setText("⏸ Playing...");
                        Timer t = new Timer(2000, evt -> playBtn.setText("▶ Play " + duration));
                        t.setRepeats(false);
                        t.start();
                        VoiceGuide.speak("Voice note playback simulated");
                    });
                    contentComponent = playBtn;
                } else {
                    JTextArea textLabel = new JTextArea(msg.text);
                    textLabel.setWrapStyleWord(true);
                    textLabel.setLineWrap(true);
                    textLabel.setEditable(false);
                    textLabel.setFocusable(false);
                    textLabel.setOpaque(false);
                    textLabel.setFont(UIUtils.fontNormal);
                    textLabel.setForeground(isMe ? Color.WHITE : UIUtils.TEXT1);
                    textLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
                    contentComponent = textLabel;
                }

                bubbleWrapper.add(senderLabel, BorderLayout.NORTH);
                bubbleWrapper.add(contentComponent, BorderLayout.CENTER);

                // Align left or right
                JPanel alignPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
                alignPanel.setBackground(Color.WHITE);
                
                // Set max width to make text wrap properly
                bubbleWrapper.setPreferredSize(new Dimension(
                    Math.min(400, contentComponent.getPreferredSize().width + 40),
                    contentComponent.getPreferredSize().height + 40
                ));
                
                alignPanel.add(bubbleWrapper);
                msgPanel.add(alignPanel, BorderLayout.CENTER);
                chatBox.add(msgPanel);
            }
            chatBox.revalidate();
            chatBox.repaint();
            // Scroll to bottom
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        };

        // Send logic
        Runnable sendMessage = () -> {
            String text = messageField.getText().trim();
            if (text.isEmpty() || text.equals("Type a message...")) return;
            
            String sender = Database.currentUser != null ? Database.currentUser.name : "Guest";
            String time = new SimpleDateFormat("HH:mm").format(new Date());
            
            ChatMessage newMsg = new ChatMessage(sender, text, time);
            Database.addChatMessage(newMsg);
            
            messageField.setText("");
            loadMessages.run();
        };

        sendBtn.addActionListener(e -> sendMessage.run());
        messageField.addActionListener(e -> sendMessage.run());

        voiceCallBtn.addActionListener(e -> {
            String sender = Database.currentUser != null ? Database.currentUser.name : "Guest";
            String time = new SimpleDateFormat("HH:mm").format(new Date());
            Database.addChatMessage(new ChatMessage(sender, "[CALL]:Voice Call", time));
            loadMessages.run();
        });

        videoCallBtn.addActionListener(e -> {
            String sender = Database.currentUser != null ? Database.currentUser.name : "Guest";
            String time = new SimpleDateFormat("HH:mm").format(new Date());
            Database.addChatMessage(new ChatMessage(sender, "[CALL]:Video Call", time));
            loadMessages.run();
        });

        micBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            private Timer recordTimer;
            private int seconds = 0;
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                micBtn.setText("🔴 0:00");
                micBtn.setForeground(Color.RED);
                seconds = 0;
                recordTimer = new Timer(1000, evt -> {
                    seconds++;
                    micBtn.setText("🔴 0:0" + seconds);
                });
                recordTimer.start();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (recordTimer != null) recordTimer.stop();
                micBtn.setText("🎤");
                micBtn.setForeground(Color.WHITE);
                if (seconds > 0) {
                    String sender = Database.currentUser != null ? Database.currentUser.name : "Guest";
                    String time = new SimpleDateFormat("HH:mm").format(new Date());
                    Database.addChatMessage(new ChatMessage(sender, "[VOICENOTE]0:0" + seconds, time));
                    loadMessages.run();
                }
            }
        });

        // Initial load
        loadMessages.run();
        
        // Auto refresh timer (every 3 seconds to get updates from file if modified externally or by other instances)
        Timer refreshTimer = new Timer(3000, e -> {
            int oldSize = Database.chatMessages.size();
            Database.chatMessages.clear();
            Database.loadChat();
            if (Database.chatMessages.size() != oldSize) {
                loadMessages.run();
            }
        });
        refreshTimer.start();

        return root;
    }
}