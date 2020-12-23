import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class server_frame extends javax.swing.JFrame {
    //global variables
    ArrayList clientOutputStreams;
    ArrayList<String> users;
    Socket sock;
    ServerSocket serverSocket;
    char type;
    String username;

    public class ClientHandler implements Runnable
    {
        BufferedReader reader;
        PrintWriter client;
        public ClientHandler(Socket sock1, PrintWriter client) {
            this.client = client;
            try {
                sock = sock1;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
            } catch (Exception ex) {
                ta_chat.append("Unexpected error occurred... \n");
            }
        }
        @Override
        public void run()
        {
            String message, connect = "Connect", disconnect = "Disconnect", chat = "Chat", send = "Send", receive = "Receive";
            String[] data;
            try {
                while ((message = reader.readLine()) != null) {
                    ta_chat.append("Received- " + message + "\n");
                    data = message.split("-");
                    type = data[0].charAt(0);
                    username = data[0];
                    for (String token : data) {
                        ta_chat.append(token + "\n");
                    }
                    if (data[2].equals(connect)) {
                        tellEveryone((data[0] + "-" + data[1] + "-" + chat));
                        userAdd(data[0]);
                    } else if (data[2].equals(disconnect)) {
                        tellEveryone((data[0] + "-has disconnected." + "-" + chat));
                        userRemove(data[0]);
                    } else if (data[2].equals(chat)) {
                        tellEveryone(message);
                    } else if (data[2].equals(send)) {
                        tellEveryone((data[0] + "-" + data[1] + "-" + send));
                        receiveFile();
                    } else if (data[2].equals(receive)) {
                        tellEveryone((data[0] + "-" + data[1] + "-" + receive));
                        sendFile(data[1]);
                    } else {
                        ta_chat.append("No Conditions were met. \n");
                    }
                }
            } catch (IOException ex) {
                ta_chat.append("Lost a connection. \n");
                clientOutputStreams.remove(client);
            }
        }
    }

    //function to receive file from client/
    public void receiveFile()
    {
        try {
            int bytesRead;
            DataInputStream clientData = new DataInputStream(sock.getInputStream());
            //receiving file name and file size from client
            String fileName = clientData.readUTF();
            //System.out.println("Received file"+fileName);
            OutputStream output = new FileOutputStream(fileName);
            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            output.flush();

            //copying content to a new file


            File old_fileName = new File(fileName);

            File new_fileName = new File(type+fileName);
            new_fileName.createNewFile();

            FileInputStream ins = null;
            FileOutputStream outs = null;

            ins = new FileInputStream(old_fileName);
            outs = new FileOutputStream(new_fileName);
            byte[] buffer1 = new byte[1024];
            int length;

            while ((length = ins.read(buffer)) > 0) {
                outs.write(buffer, 0, length);
            }
            ins.close();
            outs.close();

            //System.out.println("File "+fileName+" received from client.");
        }catch (Exception ex)
        {
            System.err.println("Client error. Connection closed.");
        }
    }

    //function to send file to client/
    public void sendFile(String fileName) throws IOException {
        String copy = fileName;
        //System.out.println(fileName);
        try {
            if(type=='S'){
                fileName ='T'+fileName;
            }

            File myFile = new File(fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];
            if(!myFile.exists()) {
//                    System.out.println("File not uploaded by teacher");
                    throw new Exception();
            }
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);
            //System.out.println(Arrays.toString(mybytearray));
            OutputStream os = sock.getOutputStream();

            //sending file name and file size to client
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            dos.close();
            //System.out.println("File "+fileName+" sent to client.");
        } catch (Exception e) {
//            System.err.println("File does not exist!");
            tellEveryone((username + "-" + copy + "-" + "CANNOT SEND THE FILE"));
            File original_fileName =new File(copy);
            byte[] mybytearray = new byte[(int) original_fileName.length()];
            FileInputStream fis = new FileInputStream(original_fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(mybytearray, 0, mybytearray.length);
            //System.out.println(Arrays.toString(mybytearray));
            OutputStream os = sock.getOutputStream();

            //sending file name and file size to client
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(original_fileName.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.close();

        }
    }

    public server_frame()
    {
        initComponents();
    }

    //user interface design/
    private void initComponents()
    {
        jScrollPane1 = new javax.swing.JScrollPane();
        ta_chat = new javax.swing.JTextArea();
        b_start = new javax.swing.JButton();
        b_end = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Virtual Classroom Server's frame");
        setName("server");
        setResizable(false);
        ta_chat.setColumns(20);
        ta_chat.setRows(5);
        jScrollPane1.setViewportView(ta_chat);
        b_start.setText("START CLASSROOM SERVER");
        b_start.setBackground(Color.green);
        b_start.addActionListener(evt -> b_startActionPerformed(evt));
        b_end.setText("STOP CLASSROOM SERVER");
        b_end.setBackground(Color.orange);
        b_end.addActionListener(evt -> b_endActionPerformed(evt));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(b_end, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(b_start, javax.swing.GroupLayout.DEFAULT_SIZE, 500,
                                                                Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18,
                                                        Short.MAX_VALUE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false))))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 340,
                                        Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(b_start))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(b_end))
                                .addGap(18, 18, 18))
        );
        pack();
    }
    private void b_endActionPerformed(java.awt.event.ActionEvent evt) {
        tellEveryone("Virtual Classroom Server:is stopping and all users will be disconnected.\n:Chat");
        ta_chat.append("Virtual Classroom Server stopping... \n");
        System.exit(0);
    }
    private void b_startActionPerformed(java.awt.event.ActionEvent evt) {
        Thread starter = new Thread(new ServerStart());
        starter.start();
        ta_chat.append("Virtual Classroom Server started...\n");
    }
    /*private void b_usersActionPerformed(java.awt.event.ActionEvent evt) {
        ta_chat.append("\n Online users are : \n");
        for (String current_user : users) {
            ta_chat.append(current_user);
            ta_chat.append("\n");
        }
    }*/
    private void b_clearActionPerformed(java.awt.event.ActionEvent evt) {
        ta_chat.setText("");
    }
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                server_frame sf = new server_frame();
                sf.setVisible(true);
                sf.setBackground(Color.orange);
            }
        });
    }
    public class ServerStart implements Runnable {
        @Override
        public void run() {
            clientOutputStreams = new ArrayList();
            users = new ArrayList();
            try {
                ServerSocket serverSock = new ServerSocket(0007);
                serverSocket = serverSock;
                while (true) {
                    Socket clientSock = serverSock.accept();
                    PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
                    clientOutputStreams.add(writer);

                    Thread listener = new Thread(new ClientHandler(clientSock, writer));
                    listener.start();
                    ta_chat.append("Yes we got a connection!! \n");
                }
            } catch (Exception ex) {
                ta_chat.append("Error making a connection.. \n");
            }
        }
    }
    public void userAdd(String data) {
        String message, add = "- -Connect", done = "Server- -Done", name = data;
        ta_chat.append("Before " + name + " added. \n");
        users.add(name);
        ta_chat.append("After " + name + " added. \n");
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);
        for (String token : tempList) {
            message = (token + add);
            tellEveryone(message);
        }
        tellEveryone(done);
    }
    public void userRemove(String data) {
        String message, add = "- -Connect", done = "Server- -Done", name = data;
        users.remove(name);
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);
        for (String token : tempList) {
            message = (token + add);
            tellEveryone(message);
        }
        tellEveryone(done);
    }
    public void tellEveryone(String message) {
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            try
            {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                ta_chat.append("Sending- " + message + "\n");
                writer.flush();
                ta_chat.setCaretPosition(ta_chat.getDocument().getLength());
            }
            catch(Exception ex)
            {
                ta_chat.append("Error telling everyone. \n");
            }
        }
    }

    //variables declaration
    private javax.swing.JButton b_end;
    private javax.swing.JButton b_start;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea ta_chat;
}