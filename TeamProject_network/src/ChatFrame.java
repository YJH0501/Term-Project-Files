import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class ChatFrame extends JFrame {
	MessengerClient client;
   
   JPanel chatPanel;
   JPanel typePanel;
   JPanel chattingPanel;
   JPanel infoPanel;
   JPanel searchPanel;
   JLabel searchLabel;
   JLabel icon;
   JLabel friendName;
   JButton addFriendButton;
   JButton leaveChatButton;
   JButton typeButton;
   JButton searchButton;
   JTextArea messageArea;
   JTextArea chatArea;
   JScrollPane scrollPane;
   JScrollPane scrollPane_1;
   JTextField searchField;
   List resultList;
   
   public ChatFrame(String otherUser, int x,int y) {
      setTitle("Chat Room");
      
      setLocation(x,y);
      setResizable(false);
      chatPanel = new JPanel();
      chatPanel.setPreferredSize(new Dimension(400,600));
      chatPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(chatPanel);
      chatPanel.setLayout(null);
      
      
      //Panel of Chatting area
      
      chattingPanel = new JPanel();
      chattingPanel.setBackground(new Color(127, 255, 0));
      chattingPanel.setBounds(0, 80, 400, 420);
      chatPanel.add(chattingPanel);
      chattingPanel.setLayout(null);
      
      chatArea = new JTextArea();
      chatArea.setEditable(false);
      scrollPane_1 = new JScrollPane(chatArea);
      chatArea.setLineWrap(true);
      chatArea.setFont(new Font("Calibri", Font.PLAIN, 17));      
      scrollPane_1.setBounds(10, 10, 380, 400);
      
      chattingPanel.add(scrollPane_1);
      
      
      //Panel of information
      
      infoPanel = new JPanel();
      infoPanel.setBackground(new Color(173, 255, 47));
      infoPanel.setBounds(0, 0, 400, 80);
      chatPanel.add(infoPanel);
      infoPanel.setLayout(null);
      
      icon = new JLabel();
      icon.setIcon(changeImageSize((new ImageIcon(ChatFrame.class.getResource("/image/user_online.png"))),60,60));
      icon.setBounds(10, 10, 60, 60);
      infoPanel.add(icon);
      
      friendName = new JLabel(otherUser);
      friendName.setFont(new Font("Calibri", Font.PLAIN, 20));
      friendName.setBounds(80, 10, 150, 30);
      infoPanel.add(friendName);
      
      addFriendButton = new JButton("Add New Friend");
      addFriendButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
        	client.outputStream.println("INSERT_NEW_FRIEND");
   			client.outputStream.println(otherUser);
   			addFriendButton.setEnabled(false);
         }
      });
      addFriendButton.setFont(new Font("Calibri", Font.PLAIN, 16));
      addFriendButton.setBounds(80, 50, 150, 20);
      infoPanel.add(addFriendButton);
      
      leaveChatButton = new JButton("Leave The Chat");
      leaveChatButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
             int confirmed = JOptionPane.showConfirmDialog(chatPanel, 
                    "Are you sure you want to exit the chat?\n"
                    + "The message log will be deleted.", "Exit Chat Message Box",
                    JOptionPane.YES_NO_OPTION);

                if (confirmed == JOptionPane.YES_OPTION)
                {
                	client.chatRoomList.remove(otherUser);
                	
                	if(typeButton.isEnabled())
                	{
                		client.outputStream.println("LEFT_CHATROOM");
                    	client.outputStream.println(otherUser);
                    	
                	}
                	dispose();
                }
                  
                else
                   setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         }
      });
      leaveChatButton.setFont(new Font("Calibri", Font.PLAIN, 17));
      leaveChatButton.setBounds(240, 50, 150, 20);
      infoPanel.add(leaveChatButton);
      
      
      //Panel to type the message      
      typePanel = new JPanel();
      typePanel.setBackground(new Color(255, 250, 250));
      typePanel.setBounds(0, 500, 400, 100);
      chatPanel.add(typePanel);
      typePanel.setLayout(null);
      
      messageArea = new JTextArea();
      scrollPane = new JScrollPane(messageArea);
      messageArea.setFont(new Font("Calibri", Font.PLAIN, 17));
      scrollPane.setBounds(10, 10, 280, 50);
      messageArea.setEnabled(false);
      messageArea.setLineWrap(true);
      typePanel.add(scrollPane);
      
      typeButton = new JButton("Type");
      typeButton.addActionListener(new typeHandler());
      typeButton.setEnabled(false);
      typeButton.setFont(new Font("Calibri", Font.PLAIN, 17));
      typeButton.setBounds(310, 10, 80, 80);
      typePanel.add(typeButton);

     pack();
     setVisible(true);
     
     //윈도우 창 닫기를 눌렀을 때 동작
     addWindowListener(new WindowAdapter() {
    	 public void windowClosing(WindowEvent e)
    	 {
    		 int confirmed = JOptionPane.showConfirmDialog(chatPanel, "Are you sure you want to exit the chat?\nThe message log will be deleted.", "Exit Chat Message Box",JOptionPane.YES_NO_OPTION);
    		 
    		 if (confirmed == JOptionPane.YES_OPTION)
             {
             	client.chatRoomList.remove(otherUser);
             	
             	if(typeButton.isEnabled())
             	{
             		client.outputStream.println("LEFT_CHATROOM");
                 	client.outputStream.println(otherUser);
                 	
             	}
             	dispose();
             }
               
             else
                setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    	 }
     });
   }
   
   private class typeHandler implements ActionListener
   {
	   public void actionPerformed(ActionEvent e) {
		   String message=messageArea.getText();
		   chatArea.append(client.myInfo.getId()+" -> "+message+"\n");
		   
		   client.outputStream.println("MESSAGE_SEND"+friendName.getText());
		   client.outputStream.println(message);
		   
		   messageArea.setText("");		
	   }
   }
   
    //change image size
 	private ImageIcon changeImageSize(ImageIcon icon, int width, int height)
 	{
 		Image img=icon.getImage();
 		Image resultImage=img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
 		
 		ImageIcon resultIcon=new ImageIcon();

 		return new ImageIcon(resultImage);
 	}
 	
 	//connect with client
    public void setMain(MessengerClient c) {
  		client=c;	
    }
}