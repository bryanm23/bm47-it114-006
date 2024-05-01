package MM.Client.Views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import MM.Client.CardView;
import MM.Client.Client;
import MM.Client.ClientUtils;
import MM.Client.ICardControls;
import MM.Server.Room;

import java.awt.event.*;

public class ChatPanel extends JPanel {
    private static Logger logger = Logger.getLogger(ChatPanel.class.getName());
    private JPanel chatArea = null;
    private UserListPanel userListPanel;
    private JPanel userListArea;

    public ChatPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        // wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        // no need to add content specifically because scroll wraps it
        wrapper.add(scroll);
        this.add(wrapper, BorderLayout.CENTER);

        JPanel input = new JPanel();
        input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
        JTextField textValue = new JTextField();
        input.add(textValue);
        JButton sendButton = new JButton("Send");

        // Adding export button
        //bm47 BRYAN MADEWELL it114 spring 2024
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener((event) -> {
            exportChatHistory();
            logger.log(Level.INFO, "Exporting chat messages");
        });

        // lets us submit with the enter key instead of just the button click
        textValue.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButton.doClick();
                }
            }
        });

        sendButton.addActionListener((event) -> {
            try {
                String text = textValue.getText().trim();
                if (text.length() > 0) {
                    Client.INSTANCE.sendMessage(text);
                    textValue.setText(""); // clear the original text

                    // debugging
                    logger.log(Level.FINEST, "Content: " + content.getSize());
                    logger.log(Level.FINEST, "Parent: " + this.getSize());
                }
            } catch (NullPointerException e) {
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        input.add(sendButton);
        input.add(exportButton); // Adding export button to the input panel
        //bm47 BRYAN MADEWELL it114 spring 2024

        chatArea = content;
        userListPanel = new UserListPanel();
        this.add(userListPanel, BorderLayout.EAST);
        this.add(input, BorderLayout.SOUTH);
        this.setName(CardView.CHAT.name());
        controls.addPanel(CardView.CHAT.name(), this);

        chatArea.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                if (chatArea.isVisible()) {
                    chatArea.revalidate();
                    chatArea.repaint();
                }
            }

            

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (chatArea.isVisible()) {
                    chatArea.revalidate();
                    chatArea.repaint();
                }
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // System.out.println("Resized to " + e.getComponent().getSize());
                // rough concepts for handling resize
                // set the dimensions based on the frame size
                Dimension frameSize = wrapper.getParent().getParent().getSize();
                int w = (int) Math.ceil(frameSize.getWidth() * .3f);

                userListPanel.setPreferredSize(new Dimension(w, (int) frameSize.getHeight()));
                userListPanel.revalidate();
                userListPanel.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // System.out.println("Moved to " + e.getComponent().getLocation());
            }
        });
    }

    List<String> mutedUsers = new ArrayList<>();

    //bryan madewell bm47 IT114 SPRING 2024 Milestone 4
    
    public void addUserListItem(long clientId, String clientName) {
        String formattedClientName = "<span style='color: red;'>" + clientName + "</span>";
        userListPanel.addUserListItem(clientId, formattedClientName);
    }
    
    

    public void removeUserListItem(long clientId) {
        userListPanel.removeUserListItem(clientId);
    }

    public void clearUserList() {
        userListPanel.clearUserList();
    }

    protected void highlightCurrentTurn(long clientId) {
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            if (c instanceof UserListItem) {
                UserListItem uli = (UserListItem) c;
                uli.setCurrentTurn(clientId);
            }
        }
    }

    public void addText(String text) {
        JPanel content = chatArea;
        // add message
        JEditorPane textContainer = new JEditorPane("text/html", text);
        // sizes the panel to attempt to take up the width of the container
        // and expand in height based on word wrapping
        textContainer.setLayout(null);
        textContainer.setPreferredSize(new Dimension(content.getWidth(), ClientUtils.calcHeightForText(this, text, content.getWidth())));
        textContainer.setMaximumSize(textContainer.getPreferredSize());
        textContainer.setEditable(false);
        ClientUtils.clearBackground(textContainer);
        // add to container and tell the layout to revalidate
        content.add(textContainer);
        // scroll down on new message
        JScrollBar vertical = ((JScrollPane) chatArea.getParent().getParent()).getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    void exportChatHistory() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy_HHms");
            String timestamp = dateFormat.format(new Date());
    
            File file = new File("chat_logs_" + timestamp + ".txt");
            FileWriter writer = new FileWriter(file);
    
            dateFormat.applyPattern("MM-dd-yyyy HH:mm:ss");
    
            for (Component component : chatArea.getComponents()) {
                if (component instanceof JEditorPane) {
                    JEditorPane messagePane = (JEditorPane) component;
                    String messageText = messagePane.getText().trim();
                    if (!messageText.isEmpty()) {
                        String finalMessage = "[" + dateFormat.format(new Date()) + "] " + messageText; // Add the timestamp to the message
    
                        // Makes the chat logs easier to read by getting rid of unnecessary spaces and adding a new line after each message
                        //bm47 BRYAN MADEWELL it114 spring 2024
                        writer.write(finalMessage.trim());
                        writer.write(System.lineSeparator());
                    }
                }
            }
    
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}