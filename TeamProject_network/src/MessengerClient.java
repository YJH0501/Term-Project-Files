import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class MessengerClient {
	private String serverAddress;
	private int portNum;
	private Socket socket;
	
	UserInfo myInfo;
	
	Scanner inputStream;
	PrintWriter outputStream;
	
	LoginFrame loginFrame;//로그인 창
	RegisterFrame registerFrame;//회원가입 창
	ForgotFrame forgotFrame;//아이디,비번 찾기 창
	MainFrame mainFrame;//메인 창
	InfoFrame infoFrame;//정보 창
	Hashtable<String,ChatFrame>chatRoomList=new Hashtable<String,ChatFrame>();//대화방 창
	
	public MessengerClient(String serverAddress, int portNum)
	{
		this.serverAddress=serverAddress;
		this.portNum=portNum;
	}
	
	private void connectServer()
	{
		try
		{
			socket=new Socket(serverAddress,portNum);
			inputStream=new Scanner(socket.getInputStream());
			outputStream=new PrintWriter(socket.getOutputStream(),true);
		}
		catch(IOException ioe)
		{
			System.out.println(ioe.getMessage());
			System.exit(0);
		}
	}
	
	//login page processing
	private void runLogin()
	{
		//open login frame
		loginFrame=new LoginFrame();
		loginFrame.setMain(this);
		loginFrame.setVisible(true);	
				
		registerFrame=new RegisterFrame();
		registerFrame.setMain(this);
				
		forgotFrame=new ForgotFrame();
		forgotFrame.setMain(this);
		
		//loop until login clear
		while(true)
		{
			String getMessage;
			
			//get protocol(message) from server
			getMessage=inputStream.nextLine();
			
			//get login result
			if(getMessage.equals("LOGIN_RESULT"))
			{
				String result=inputStream.nextLine();
				
				//login success
				if(result.equals("LOGIN_SUCCESS"))
				{
					JOptionPane.showMessageDialog(loginFrame, "SUCCESS","LOGIN RESULT",JOptionPane.DEFAULT_OPTION);
					
					//close frames
					registerFrame.dispose();
					forgotFrame.dispose();
					loginFrame.dispose();
					
					//end login method
					break;
				}
				
				//아이디 중복 접속 불가, 아이디 없음, 비밀번호 오류 나누기
				//duplicate login, not register ID, invalid password
				else if(result.equals("LOGIN_FAIL"))
					JOptionPane.showMessageDialog(loginFrame, "FAIL","LOGIN RESULT",JOptionPane.DEFAULT_OPTION);
			}
			
			//get checking ID result when register
			else if(getMessage.equals("REGISTER_ID_CHECK_RESULT"))
			{
				boolean isOK=inputStream.nextBoolean(); inputStream.nextLine();
				
				//useful ID
				if(isOK==true)
				{
					int result=JOptionPane.showConfirmDialog(registerFrame, "You can use this ID\nDo you want to use this ID?","ID check",JOptionPane.YES_NO_OPTION);
					
					if(result==JOptionPane.YES_OPTION)
					{
						registerFrame.idTxtField.setEditable(false);
						registerFrame.idCheckBtn.setEnabled(false);
					}
				}
					
				//ID that has already been registered
				else
				{
					JOptionPane.showMessageDialog(registerFrame,"You can't use this ID","ID check",JOptionPane.DEFAULT_OPTION);
					registerFrame.idTxtField.setText("");
				}
			}
			
			//get register result
			else if(getMessage.equals("REGISTER_RESULT"))
			{
				//success
				if(inputStream.nextLine().equals("REGISTER_SUCCESS"))
				{
					JOptionPane.showMessageDialog(registerFrame, "SUCCESS", "Register result", JOptionPane.DEFAULT_OPTION);
					
					//Empty all the input spaces in the frame
					registerFrame.idTxtField.setText("");
					registerFrame.idTxtField.setEditable(true);
					registerFrame.idCheckBtn.setEnabled(true);
					registerFrame.pswTxtField.setText("");
					registerFrame.rePswTxtField.setText("");
					registerFrame.nameTxtField.setText("");
					registerFrame.nickNameTxtField.setText("");
					registerFrame.yearComboBox.setSelectedIndex(0);
					registerFrame.monthComboBox.setSelectedIndex(0);
					registerFrame.dayComboBox.setSelectedIndex(0);
					registerFrame.emailTxtField.setText("");
					registerFrame.addressTxtField.setText("");
					registerFrame.phoneTxtField.setText("");
					registerFrame.answerTxtField.setText("");
					registerFrame.addressComboBox.setSelectedIndex(0);
					registerFrame.questionComboBox.setSelectedIndex(0);
					
					//close register frame & open login frame
					registerFrame.setVisible(false);
					loginFrame.setVisible(true);
				}
				
				//Failed to enter information into database
				else if(inputStream.nextLine().equals("REGISTER_FAIL"))
				{
					JOptionPane.showMessageDialog(registerFrame, "FAiL", "Register result", JOptionPane.DEFAULT_OPTION);
				}		
			}
			
			//get result for finding ID/password
			else if(getMessage.equals("FORGOT_ID_RESULT")||getMessage.equals("FORGOT_PSW_RESULT"))
				forgotFrame.lblResult.setText(inputStream.nextLine());
		}
	}

	//main page processing
	public void runMain()
	{	
		//user's information
		String id;
		String name;
		String nickname;
		String birth;
		String email;
		String phone;
		String feel;
		String friend;
		
		//get user information from the server
		if(inputStream.nextLine().equals("SEND_INFO"))
		{
			id=inputStream.nextLine();
			name=inputStream.nextLine();
			nickname=inputStream.nextLine();
			birth=inputStream.nextLine();
			email=inputStream.nextLine();
			phone=inputStream.nextLine();
			feel=inputStream.nextLine();
			
			//save received information to user object
			myInfo=new UserInfo(id,name,nickname,birth,email,phone,feel);	
		}
		
		//received user's friend list from server
		if(inputStream.nextLine().equals("SEND_FRIEND"))
		{
			while(true)
			{
				friend=inputStream.nextLine();
				
				if(friend.equals("SEND_FRIEND_END"))
					break;
				
				else
					myInfo.addFriend(friend);
			}
		}
		
		//open main frame
		mainFrame=new MainFrame(myInfo);
		mainFrame.setMain(this);
		mainFrame.insertFriend();  //divide the list of friends online and offline
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
		
		//prepare information frame
		infoFrame=new InfoFrame();
		infoFrame.setMain(this);
		
		//
		while(true)
		{
			String getMessage;
			getMessage=inputStream.nextLine();
			
			if(getMessage.equals("JOIN_USER"))
			{
				mainFrame.gotoOnline(inputStream.nextLine());
			}
			
			else if(getMessage.equals("LEFT_USER"))
			{
				mainFrame.gotoOffline(inputStream.nextLine());
			}
			
			else if(getMessage.startsWith("MESSAGE"))
			{
				String sender=getMessage.substring(7);
				String message=inputStream.nextLine();
				
				if(message.equals("LEFT_CHATROOM"))
				{
					chatRoomList.get(sender).messageArea.setEnabled(false);
					chatRoomList.get(sender).typeButton.setEnabled(false);
					chatRoomList.get(sender).chatArea.append(sender+" left room");
					chatRoomList.remove(sender);
				}
				
				else if(message.equals("JOIN_CHATROOM"))
				{
					chatRoomList.get(sender).messageArea.setEnabled(true);
					chatRoomList.get(sender).typeButton.setEnabled(true);
					chatRoomList.get(sender).chatArea.append(sender+" joined room\n");
				}
				
				else
				{
					chatRoomList.get(sender).chatArea.append(sender+" -> "+message+"\n");
				}
			}
			
			else if(getMessage.equals("SEARCH_INFORMATION_RESULT"))
			{
				String infoId=inputStream.nextLine();
				String infoName=inputStream.nextLine();
				String infoNickname=inputStream.nextLine();
				String infoBirth=inputStream.nextLine();
				String infoEmail=inputStream.nextLine();
				String infoPhone=inputStream.nextLine();
				String infoFeel=inputStream.nextLine();
				boolean isFriend=false;				
				for(String u:myInfo.getFriend())
				{
					if(infoId.equals(u))
					{
						isFriend=true;
						break;
					}
				}
				boolean isChat=chatRoomList.containsKey(infoId);
				
				infoFrame.setFrame(infoId,infoName,infoNickname,infoBirth,infoEmail,infoPhone,infoFeel,isFriend,isChat);
				infoFrame.setLocation(mainFrame.getX()-460, mainFrame.getY());
			}
			
			else if(getMessage.equals("SEARCH_USER_ID_RESULT"))
			{
				int index=0;
				ArrayList<String> userIds=new ArrayList<String>();
				String tempId;
				while(true)
				{
					tempId=inputStream.nextLine();
					if(tempId.equals("SEARCH_USER_ID_RESULT_END"))
						break;
					
					else
						userIds.add(tempId);
				}
				
				
				mainFrame.searchUserId(userIds);
			}
			
			else if(getMessage.equals("REQUEST_CHATROOM_RESULT"))
			{
				String result=inputStream.nextLine();
				
				if(result.equals("REQUEST_CHATROOM_CLEAR"))
				{
					String otherUser=inputStream.nextLine();
					infoFrame.setVisible(false);
					
					if(!(chatRoomList.containsKey(otherUser)))
					{
						chatRoomList.put(otherUser, new ChatFrame(otherUser,mainFrame.getX()+410,mainFrame.getY()));	
						chatRoomList.get(otherUser).setMain(this);
						
						if(myInfo.getFriend().contains(otherUser))
							chatRoomList.get(otherUser).addFriendButton.setEnabled(false);
					}	
				}
				
				else if(result.equals("USER_OFFLINE"))
				{
					JOptionPane.showMessageDialog(mainFrame,"The user is currently offline.","CHAT_REQUEST",JOptionPane.DEFAULT_OPTION);
					infoFrame.chatButton.setEnabled(true);
				}
			}
			
			else if(getMessage.equals("REQUEST_CHATROOM"))
			{
				String otherUser=inputStream.nextLine();
				
				if(chatRoomList.containsKey(otherUser))
					continue;
				
				else
				{
					int result=JOptionPane.showConfirmDialog(mainFrame,"You have been invited to chat room by "+otherUser+"\nWill you accept it?", "INVITE_CHAT", JOptionPane.YES_NO_OPTION);
					
					if(result==JOptionPane.YES_OPTION)
					{
						chatRoomList.put(otherUser, new ChatFrame(otherUser,mainFrame.getX()+410,mainFrame.getY()));
						chatRoomList.get(otherUser).setMain(this);
						
						chatRoomList.get(otherUser).messageArea.setEnabled(true);
						chatRoomList.get(otherUser).typeButton.setEnabled(true);	
						if(myInfo.getFriend().contains(otherUser))
							chatRoomList.get(otherUser).addFriendButton.setEnabled(false);
						
						outputStream.println("REQUEST_CHAT_ACCEPT");
						outputStream.println(otherUser);
					}
					else
					{
						outputStream.println("REQUEST_CHAT_REFUSE");
						outputStream.println(otherUser);
					}
				}				
			}
			
			else if(getMessage.equals("INSERT_NEW_FRIEND_RESULT"))
			{
				String result=inputStream.nextLine();
				
				if(result.equals("SUCCESS"))
				{
					JOptionPane.showMessageDialog(mainFrame,"Success to add new friend.","INSERT_RESULT",JOptionPane.DEFAULT_OPTION);
					
					String newFriend=inputStream.nextLine();
					boolean isOnline=inputStream.nextBoolean(); inputStream.nextLine();
					myInfo.addFriend(newFriend);
					mainFrame.insertNewFriend(newFriend, isOnline);
					infoFrame.addButton.setEnabled(false);
					infoFrame.chatButton.setEnabled(true);
				}
				
				else if(result.equals("FAIL"))
				{
					JOptionPane.showMessageDialog(mainFrame,"Fail to add new friend.","INSERT_RESULT",JOptionPane.DEFAULT_OPTION);
				}
			}
			
			else if(getMessage.equals("REQUEST_INSERT_FRIEND"))
			{
				String requestId=inputStream.nextLine();
				
				int result=JOptionPane.showConfirmDialog(mainFrame,"You got a friend request from "+requestId+".\nDo you want to accept it?", "REQUEST_FRIEND", JOptionPane.YES_NO_OPTION);
				
				if(result==JOptionPane.YES_OPTION)
				{
					outputStream.println("INSERT_NEW_FRIEND");
		  			outputStream.println(requestId);
				}
			}
			
			else if(getMessage.equals("DELETE_FRIEND_RESULT"))
			{
				String result=inputStream.nextLine();
				
				if(result.equals("SUCCESS"))
				{
					JOptionPane.showMessageDialog(mainFrame,"Success to delete friend.","DELETE_RESULT",JOptionPane.DEFAULT_OPTION);
					
					String removeId=inputStream.nextLine();
					myInfo.getFriend().remove(removeId);					
				}
				
				else if(result.equals("FAIL"))
				{
					JOptionPane.showMessageDialog(mainFrame,"Fail to delete friend.","DELETE_RESULT",JOptionPane.DEFAULT_OPTION);
				}
			}	
		}
	}
	
	public static void main(String[] args) {		
		String inputServerAddress="14.47.236.108";
		int inputPortNum=35859;
		
		//client object / connect server
		MessengerClient client=new MessengerClient(inputServerAddress,inputPortNum);
		client.connectServer();
		
		client.runLogin();		
		client.runMain();
	}

}