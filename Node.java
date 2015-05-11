import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Node extends Thread{

	DatagramSocket socket;
	DatagramPacket outPacket;
	DatagramPacket inPacket;
	
	/* 1st byte is 1 for frame and 0 for token
	 * 2nd byte is the destination address
	 * 3rd byte is the source address
	 * if it is a token and not a frame,
	 * the 2nd and 3rd bytes will be 0
	 */
	
	byte[] outBuffer = new byte[3];
	byte[] inBuffer = new byte[3];
	
	int nodeNum;
	String status = "Idle";
	
	boolean hasToken = false;
	boolean tokenLock = false;
	boolean hasFrame = false;
	boolean hasSentFrame = false;
	
	boolean toSend = false;
	
	int port;
	int nextPort;
	
	int destNode;
	
	
	public Node(int i, int passedPort) throws Exception{
		
		nodeNum = i;
		port = passedPort;
		
		socket = new DatagramSocket(passedPort, InetAddress.getByName("127.0.0.1"));
		socket.setSoTimeout(1000);
		
		/* set the next port, need to allow node 9 to connect to node 0 */
		if(nodeNum != 9){
			nextPort = port + 1;
		}else{
			nextPort = port - nodeNum;
		}
		
	}
	
        public void runTokenLock() throws Exception{
            
                inPacket = new DatagramPacket(inBuffer, 3);
                socket.receive(inPacket);
                tokenLock = false;
                toSend = false;
                
        }
        
        public void runHasToken() throws Exception{
            
                /* sending frame */
		if(!tokenLock && toSend){
										
                        tokenLock = true;
                        outBuffer[0] = 1;
                        outBuffer[1] = (byte) (Main.startingPort + destNode);
                        outBuffer[2] = (byte) port;
				
                        outPacket = new DatagramPacket(outBuffer, 3, InetAddress.getByName("127.0.0.1"), nextPort);
                        socket.send(outPacket);	
                
		}else if(!tokenLock){ /* sending token as long as we're not waiting on a frame */
					
			hasToken = false;
			outBuffer[0] = outBuffer[1] = outBuffer[2] = 0;
					
			outPacket = new DatagramPacket(outBuffer, 3, InetAddress.getByName("127.0.0.1"), nextPort);
			socket.send(outPacket);
		}
                
        }
        
        public void runHasFrame() throws Exception{
            
                for(int i = 0; i < 3; i++){
			outBuffer[i] = inBuffer[i];
		}
				
		outPacket = new DatagramPacket(outBuffer, 3, InetAddress.getByName("127.0.0.1"), nextPort);
		socket.send(outPacket);
		hasSentFrame = true;
		hasFrame = false;
                
        }
        
        public void runIdle() throws Exception{
            
                hasSentFrame = false;
				
		inPacket = new DatagramPacket(inBuffer, 3);
		socket.receive(inPacket);
					
		if(inBuffer[0] == 1){
						
			if(inBuffer[2] == port){
				tokenLock = false;
			}else{
				hasFrame = true;
			}
						
		}else{
			hasToken = true;
		}

        }
	public void run(){
		
		/* prevent the threads from starting too fast */
		try {
			Thread.currentThread();
			Thread.sleep(500);
		} catch (Exception e) {
			System.out.println("Error when trying to wake node " + nodeNum);
			System.exit(-1);
		}
		
		while(true){
			
                        try{
                                if(tokenLock){ // token is held until frame comes back
					runTokenLock();
                                }else if(hasToken){ // node has token, will check if it should be seized
                                        runHasToken();
                                }else if(hasFrame){ // node has received a frame and will forward it
                                        runHasFrame();
                                }else{ // node is idle
                                        runIdle();
                                }
			
                                if(tokenLock){
                                	status = "Waiting";
                                }else if(hasToken){
                                	status = "Has token";
                                }else if(hasFrame){
                                	status = "Has frame";
                                }else{
                                	status = "Idle";
                                }
			
				Thread.currentThread();
				Thread.sleep(500);
			
                        } catch (Exception e){
                            
                        }
		}
		
		
	}
	
}
