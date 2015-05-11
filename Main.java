import java.util.Scanner;

public class Main extends Thread{

	public static Node[] nodes;
	public static int startingPort;
	
	public static void main(String[] args) throws Exception {
		
		int numNodes = 10;
		startingPort = 200;
		nodes = new Node[numNodes];

		Scanner scanner = new Scanner(System.in);
		
		/* initialise each node and assign it a unique port */
		for(int i = 0; i < numNodes; i++){
			nodes[i] = new Node(i, startingPort + i);
		}
		
		/* create the window */
		Canvas canvas = new Canvas();
		canvas.setVisible(true);
		
		/* redraw the window every 1/60th of a second */
		(new Main()).start();
		
		for(int i = 0; i < numNodes; i++){
			new Thread(nodes[i]).start();
		}
		
		nodes[0].hasToken = true;
		
		String input = "";
		
		do{
			
			System.out.println(("To send data from one node to another, enter the information in the following format:\n1,2 or enter -1 to exit"));
			input = scanner.nextLine();
			
			int[] values = checkFormat(input);
			
			if(values != null){
				
				nodes[values[0]].toSend = true;
				nodes[values[0]].destNode = values[1];
				
			}else if(!input.equals("-1")){
				System.out.println("Error in parsing input");
			}
			
		}while(!input.equals("-1"));
		
		scanner.close();
		System.exit(1);
		
	}
	
	public static int[] checkFormat(String input){
		
		int[] parsedValues = new int[2];
		
		if(!input.contains(",")){
			return null;
		}
		
		String[] splits = input.split(",");
		
		if(splits.length != 2){
			return null;
		}
		
		try{
			parsedValues[0] = Integer.parseInt(splits[0]);
			parsedValues[1] = Integer.parseInt(splits[1]);
		} catch(Exception e){
			System.out.println("Error parsing input");
		}
		
		return parsedValues;
		
	}
	
	public void run(){
		
		while(true){
			
			Canvas.window.repaint();
			
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				System.out.println("Crashed when trying to wake the repaint thread");
			}
			
		}
		
	}

}
