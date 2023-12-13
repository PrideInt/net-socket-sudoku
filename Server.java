import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Written by Pride
 */

public class Server {
    public static final List<ClientThread> CLIENTS = new ArrayList<>();
    private static final Set<String> PLAYERS = new HashSet<>();
    private static final Sudoku SUDOKU = new Sudoku();
    private static final Map<Pair, String> SPOTS = new HashMap<>();

    private static final List<Integer> DIGITS = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    private static final List<Character> ALPHABET_LOWER = List.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');
    private static final List<Character> ALPHABET_UPPER = ALPHABET_LOWER.stream().map(Character::toUpperCase).collect(Collectors.toList());

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
        int portNumber = Integer.parseInt(args[0]);
        SUDOKU.fillValues();

        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            // PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            // BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Server started. Connect to begin playing!");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                StringBuilder uuidBuilder = new StringBuilder();

                for (int i = 0; i < 6; i++) {
                    if (i % 2 == 0) {
                        boolean either = ThreadLocalRandom.current().nextBoolean();
                        uuidBuilder.append(either ? ALPHABET_LOWER.get(ThreadLocalRandom.current().nextInt(ALPHABET_LOWER.size())) : ALPHABET_UPPER.get(ThreadLocalRandom.current().nextInt(ALPHABET_UPPER.size())));
                    } else {
                        uuidBuilder.append(DIGITS.get(ThreadLocalRandom.current().nextInt(DIGITS.size())));
                    }
                }
                PLAYERS.add(uuidBuilder.toString());

                System.out.println("Client connected with unique ID " + uuidBuilder + "!");

                ClientThread clientThread = new ClientThread(clientSocket, SUDOKU, uuidBuilder.toString(), SPOTS, PLAYERS);
                clientThread.start();
                CLIENTS.add(clientThread);
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
        if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }
    }
}

class ClientThread extends Thread {
    private Socket socket;
    private Sudoku sudoku;
    private String uuid;
    private Map<Pair, String> spots;
    private Set<String> players;

    private BufferedReader in;
    private PrintWriter out;

    public ClientThread(Socket socket, Sudoku sudoku, String uuid, Map<Pair, String> spots, Set<String> players) throws IOException {
        this.socket = socket;
        this.sudoku = sudoku;
        this.uuid = uuid;
        this.spots = spots;
        this.players = players;

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            String inputLine;
            while ((inputLine = this.in.readLine()) != null) {
                boolean update = false;
                String[] split = inputLine.split(" ");
                if (split.length == 1) {
                    if (split[0].equals("show")) {
                        this.out.println(this.sudoku.getSudokuString());
                    }
                } else if (split.length > 1 && split.length < 5 && split[0].equals("update")) {
                    int i, j , num;
                    try {
                        i = Integer.parseInt(split[1]);
                        j = Integer.parseInt(split[2]);
                        num = Integer.parseInt(split[3]);

                        if (i < 0 || i > 8 || j < 0 || j > 8 || num < 1 || num > 9) {
                            this.out.println("Out of bounds.");
                            continue;
                        }
                    } catch (Exception e) {
                        this.out.println("Invalid command.");
                        continue;
                    }
                    if (this.sudoku.enterNumber(i, j, num)) {
                        this.spots.put(Pair.of(i, j), this.uuid);
                        update = true;
                    } else {
                        this.out.println("Invalid update.");
                    }

                    if (this.sudoku.isBoardFull()) {
                        for (ClientThread client : Server.CLIENTS) {
                            Set<Pair<String, Integer>> scores = new HashSet<>();
                            for (String player : this.players) {
                                int count = 0;

                                for (Map.Entry<Pair, String> entry : this.spots.entrySet()) {
                                    if (entry.getValue().equals(player)) {
                                        count++;
                                    }
                                }
                                scores.add(Pair.of(player, count));
                            }
                            int max = Integer.MIN_VALUE;
                            String winner = "";

                            for (Pair<String, Integer> score : scores) {
                                if (score.getRight() > max) {
                                    max = score.getRight();
                                    winner = score.getLeft();
                                }
                            }
                            client.out.println("The board is complete.");
                            client.out.println(this.sudoku.getSudokuString());
                            client.out.println("The winner is client " + winner + "!");
                        }
                        System.exit(1);
                    }
                }
                for (ClientThread client : Server.CLIENTS) {
                    if (update) {
                        client.out.println("A client updated the board.");
                        client.out.println(this.sudoku.getSudokuString());
                    }
                }
            }
        } catch (IOException e) {
            System.exit(1);
        }
    }
}

class Pair<L, R> {
    private L left;
    private R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return this.left;
    }
    public L getKey() { return this.left; }

    public R getRight() {
        return this.right;
    }
    public R getValue() { return this.right; }

    public void setLeft(L left) {
        this.left = left;
    }
    public void setRight(R right) {
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }
}