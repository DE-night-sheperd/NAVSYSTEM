import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class AIPanel {

    private static final List<ChatMessage> aiHistory = new ArrayList<>();
    private static JPanel chatBox;
    private static JScrollPane scrollPane;

    public static JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);

        // Header
        root.add(UIUtils.pageHeader("SCOSS AI Assistant", "Your smart campus guide. Ask me anything!"), BorderLayout.NORTH);

        // Chat History Area
        chatBox = new JPanel();
        chatBox.setLayout(new BoxLayout(chatBox, BoxLayout.Y_AXIS));
        chatBox.setBackground(Color.WHITE);
        chatBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(chatBox);
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

        JTextField messageField = UIUtils.styledField("Ask SCOSS AI...");
        messageField.setPreferredSize(new Dimension(0, 45));

        JButton sendBtn = UIUtils.primaryBtn("Ask AI");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        root.add(inputPanel, BorderLayout.SOUTH);

        // Initial Greeting
        if (aiHistory.isEmpty()) {
            aiHistory.add(new ChatMessage("SCOSS AI", "Hello! I am your smart campus assistant. How can I help you today?", getTime()));
        }
        renderMessages();

        Runnable sendMessage = () -> {
            String text = messageField.getText().trim();
            if (text.isEmpty() || text.equals("Ask SCOSS AI...")) return;
            
            String sender = Database.currentUser != null ? Database.currentUser.name : "You";
            aiHistory.add(new ChatMessage(sender, text, getTime()));
            messageField.setText("");
            renderMessages();
            
            // Simulate AI thinking
            Timer typingTimer = new Timer(1000, e -> {
                String response = generateAIResponse(text);
                aiHistory.add(new ChatMessage("SCOSS AI", response, getTime()));
                renderMessages();
                VoiceGuide.speak(response); // Read the response out loud
            });
            typingTimer.setRepeats(false);
            typingTimer.start();
        };

        sendBtn.addActionListener(e -> sendMessage.run());
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) sendMessage.run();
            }
        });

        return root;
    }

    private static String getTime() {
        return new SimpleDateFormat("HH:mm").format(new Date());
    }

    private static void renderMessages() {
        chatBox.removeAll();
        for (ChatMessage msg : aiHistory) {
            boolean isMe = !msg.sender.equals("SCOSS AI");
            JPanel msgPanel = new JPanel(new BorderLayout());
            msgPanel.setBackground(Color.WHITE);
            msgPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Bubble
            JPanel bubbleWrapper = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isMe) g2.setColor(UIUtils.ACCENT);
                    else g2.setColor(new Color(230, 235, 250));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    super.paintComponent(g);
                }
            };
            bubbleWrapper.setOpaque(false);
            bubbleWrapper.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            JLabel senderLabel = new JLabel(msg.sender + "  •  " + msg.timestamp);
            senderLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
            senderLabel.setForeground(isMe ? new Color(255, 255, 255, 200) : UIUtils.TEXT2);

            JTextArea textLabel = new JTextArea(msg.text);
            textLabel.setWrapStyleWord(true);
            textLabel.setLineWrap(true);
            textLabel.setEditable(false);
            textLabel.setFocusable(false);
            textLabel.setOpaque(false);
            textLabel.setFont(UIUtils.fontNormal);
            textLabel.setForeground(isMe ? Color.WHITE : UIUtils.TEXT1);
            textLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            int w = Math.min(500, Math.max(200, textLabel.getPreferredSize().width + 40));
            textLabel.setSize(w, Short.MAX_VALUE);

            bubbleWrapper.add(senderLabel, BorderLayout.NORTH);
            bubbleWrapper.add(textLabel, BorderLayout.CENTER);

            JPanel alignPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
            alignPanel.setBackground(Color.WHITE);
            
            bubbleWrapper.setPreferredSize(new Dimension(w, textLabel.getPreferredSize().height + 45));
            
            alignPanel.add(bubbleWrapper);
            msgPanel.add(alignPanel, BorderLayout.CENTER);
            chatBox.add(msgPanel);
        }
        chatBox.revalidate();
        chatBox.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private static String generateAIResponse(String query) {
        String lower = query.toLowerCase();

        // Location queries
        if (lower.contains("where is") || lower.contains("find") || lower.contains("locate")) {
            for (Location loc : Database.locations) {
                if (lower.contains(loc.locationName.toLowerCase()) || lower.contains(loc.category.toLowerCase())) {
                    return "You can find " + loc.locationName + " at the " + loc.building + " building. Check the Campus Map tab to start navigation!";
                }
            }
            return "I couldn't find that exact location. Try searching the Campus Map tab for more details.";
        }

        // Services & Help
        if (lower.contains("help") || lower.contains("support") || lower.contains("service")) {
            for (Service svc : Database.services) {
                if (lower.contains(svc.serviceName.toLowerCase()) || lower.contains(svc.department.toLowerCase())) {
                    return svc.serviceName + " is handled by the " + svc.department + " department. You can contact them at " + svc.contactInfo + ".";
                }
            }
            return "We have many services like IT Support, Counselling, and Academic Advising. Which one do you need help with?";
        }

        // Requests
        if (lower.contains("request") || lower.contains("broken") || lower.contains("fix")) {
            return "If you need to log a maintenance issue or request, please head over to the 'My Requests' tab to submit a formal ticket.";
        }
        
        // Friendly conversational
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hello there! I'm your AI guide. Need help finding a building or accessing campus services?";
        }
        
        if (lower.contains("thank")) {
            return "You're very welcome! Let me know if you need anything else.";
        }

        return "That's an interesting question. I'm a specialized AI for Sol Plaatje University. You can ask me about campus locations, services, or how to log a request!";
    }
}