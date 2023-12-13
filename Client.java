import java.io.*;
import java.net.*;

/**
 * Written by Pride
 */

public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Client <host name> <port number>");
            System.exit(1);
        }
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        // System.out.println(SUDOKU.getSudokuString());
 
        try (
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            ServerThread connection = new ServerThread(echoSocket);
            connection.start();

            String userInput;

            // System.out.println("CLIENT TEST");

            while ((userInput = stdIn.readLine()) != null) {
                String[] split = userInput.split(" ");
                if (split.length == 1) {
                    if (split[0].equals("quit")) {
                        out.println("Client disconnected.");
                        stdIn.close();
                        in.close();
                        out.close();
                        echoSocket.close();
                    } else if (split[0].equals("show")) {
                        out.println(userInput);
                        // System.out.println(Server.SUDOKU.getSudokuString());
                    }
                } else if (split.length > 1 && split.length < 5) {
                    if (split[0].equals("update")) {
                        out.println(userInput);
                        /*
                        int i = Integer.parseInt(split[1]);
                        int j = Integer.parseInt(split[2]);
                        int num = Integer.parseInt(split[3]);
                        Server.SUDOKU.enterNumber(i, j, num);

                         */
                    }
                }
                /*
                SUDOKU.enterNumber(0, 0, 100);
                out.println(userInput);
                out.println(SUDOKU.getSudokuString());
                 */
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        } 
    }
}

class ServerThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println(this.in.readLine());
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        }
    }
}