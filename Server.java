import java.util.List;
import java.util.ArrayList;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private static int uniqueId;

	private int port;

	private boolean keepGoing;

	private List<ClientThread> clients;

	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

	public Server() {
		new Server(5555);
	}

	public Server(int port) {
		this.port = port;
		this.clients = new ArrayList<>();
	}

	public void start() {
		keepGoing = true;
		try {
			ServerSocket serverSocket = new ServerSocket(port);

			while(keepGoing) {
				Socket socket = serverSocket.accept();
				ClientThread client = new ClientThread(socket, ++uniqueId);
				client.start();
				clients.add(client);
			}

			try {
				serverSocket.close();
				for(ClientThread client : clients) {
					client.os.close();
					client.is.close();
					client.socket.close();
				}
			} catch(IOException e) {
				display(e.getMessage());
			}
		} catch(IOException e) {
			display(e.getMessage());
		}

	}

	public void stop() {
		keepGoing = false;
	}

	public synchronized void broadcast(String message) {
		LocalTime time = LocalTime.now();
		String strTime = time.format(dtf);
		for(int i = 0; i < clients.size(); i++) {
			ClientThread client = clients.get(i);
			if(!client.writeMessage(strTime + " " + client.getUsername() + " > " + message)) {
				display(client.getUsername() + " disconnected from server.");
				clients.remove(i);
			}
		}

	}

	public void display(String message) {
		LocalTime time = LocalTime.now();
		String str = time.format(dtf) + " > " + message;
		System.out.println(str);	
	}

	public static void main(String... args) {
		int port = Integer.parseInt(args[0]);
		Server server = new Server(port);
		server.start();
	}
	
	public class ClientThread extends Thread {
		
		Socket socket;
		
		int id;
		String name;

		ObjectOutputStream os;
		ObjectInputStream is;
		
		public ClientThread(Socket socket, int id) {
			this.id = id;
			this.socket = socket;

			try {
				os = new ObjectOutputStream(socket.getOutputStream());
				is = new ObjectInputStream(socket.getInputStream());
				
				name = (String) is.readObject();
				display(name + " connected to the chat.");

			} catch(IOException | ClassNotFoundException e) {
				display(e.getMessage());
			}
		}

		public String getUsername() {
			return this.name;
		}

		public void close() {
			try {
				if(os != null) os.close();
				if(is != null) is.close();
				if(socket != null) socket.close();
			} catch(IOException e) {
				display(e.getMessage());
			}
		}

		public boolean writeMessage(String message) {
			if(!socket.isConnected()) {
				close();
				return false;
			}

			try {
				os.writeObject(message);
			} catch(IOException e) {
				display(e.getMessage());
			}
			return true;
		}
		
		@Override
		public void run() {
			boolean keepGoing = true;
			while(keepGoing) {
				try {
					String message = (String) is.readObject();
					display(message);
					broadcast(message);

					if(message.equals("exit"))
						keepGoing = false;
				} catch(IOException | ClassNotFoundException e) {
					display(e.getMessage());
				}
			}
		}
	}
}
