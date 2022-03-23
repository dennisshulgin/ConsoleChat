import java.util.*;
import java.net.*;
import java.io.*;

public class Client {

	private int port;

	private String name;

	private String address;

	private Socket socket;

	private ObjectOutputStream os;
	private ObjectInputStream is;
	
	public Client(String address, int port, String name) {
		this.address = address;
		this.port = port;
		this.name = name;
	}

	public void start() {
		try {
			socket = new Socket(address, port);
		} catch(UnknownHostException e) {
			display(e.getMessage());
		} catch(IOException e) {
			display(e.getMessage());
		}
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);

		try {
			is = new ObjectInputStream(socket.getInputStream());
			os = new ObjectOutputStream(socket.getOutputStream());
		} catch(IOException e) {
			display(e.getMessage());
		}

		new Thread(new ListenFromServer()).start();
		
		sendMessage(name);

	}

	public void sendMessage(String message) {
		try {
			os.writeObject(message);
		} catch (IOException e) {
			display(e.getMessage());
		}
	}

	public void display(String message) {
		System.out.println(message);
	}

	public static void main(String... args) {
		String username = "Anonymous";
		if(args.length > 0) 
			username = args[0];
		Client client = new Client("localhost", 5555, username);
		client.start();

		try (Scanner scanner = new Scanner(System.in)){
			while(true) {
				System.out.print("> ");
				String message = scanner.nextLine();
				client.sendMessage(message);
				if(message.equals("exit")) {
					break;
				}
			}
		}
		client.disconnect();

	}

	public void disconnect() {
		try {
			if(is != null) 
				is.close();
			if(os != null)
				os.close();
			if(socket != null)
				socket.close();
		} catch(Exception e) {
		}
	}

	public static void console(String param) {
		System.out.print("Input " + param + ":");
	}

	class ListenFromServer implements Runnable {
		@Override
		public void run() {
			while(true) {
				try{
					String message = (String) is.readObject();
					if(message != null) {
						System.out.println(message);
						System.out.print("> ");
					}
				} catch(ClassNotFoundException | IOException e) {
					display(e.getMessage());
					break;
				}
			}
		}
	}
}
