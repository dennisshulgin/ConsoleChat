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
		Client client = new Client("localhost", 5555, "Denis");
		client.start();

		try (Scanner scanner = new Scanner(System.in)){
			while(true) {
				System.out.print("Message: ");
				String message = scanner.nextLine();
				client.sendMessage(message);
			}
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
					display(message);
				} catch(ClassNotFoundException | IOException e) {
					display(e.getMessage());
				}
			}
		}
	}
}
