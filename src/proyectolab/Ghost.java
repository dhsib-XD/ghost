/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectolab;

import java.io.*;
import java.util.*;
import java.util.Scanner;

/**
 *
 * @author omarr
 */
class Ghost {
    private Player currentUser;
    private char[][] board;
    private int difficulty;
    private int mode;
    private final String FILE_PATH = "players.txt";
    private final int[] ghostCount = {8, 4, 2};
    private final Random random = new Random();
    private static final char EMPTY = '.';
    private int player1Good = 0, player1Bad = 0, player2Good = 0, player2Bad = 0;
    public static Scanner scanner = new Scanner(System.in);

    private Player[] players; // Arreglo de jugadores
    private int playerCount;  // Número actual de jugadores

    public Ghost() {
        this.board = new char[6][6];
        this.difficulty = 0; // Normal
        this.mode = 0; // Aleatorio
        this.players = new Player[100]; // Capacidad máxima inicial de 100 jugadores
        this.playerCount = 0;
        initializeBoard();
        loadPlayers();
    }

    private void initializeBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    public void registerPlayer(String username, String password) {
        if (isUserExists(username)) {
            System.out.println("El username ya existe. Intente con otro.");
            return;
        }
        if (playerCount >= players.length) {
            System.out.println("No se pueden registrar más jugadores. Capacidad máxima alcanzada.");
            return;
        }
        players[playerCount++] = new Player(username, password);
        savePlayers();
        System.out.println("Jugador registrado exitosamente.");
    }

    public Player login(String username, String password) {
        for (int i = 0; i < playerCount; i++) {
            if (players[i].getUsername().equals(username) && players[i].getPassword().equals(password)) {
                currentUser = players[i];
                System.out.println("Inicio de sesion exitoso.");
                return currentUser;
            }
        }
        System.out.println("Credenciales incorrectas.");
        return null;
    }

    public void logout() {
        currentUser = null;
        System.out.println("Sesion cerrada.");
    }

    public Player getCurrentUser() {
        return currentUser;
    }

    public void deleteUser() {
        if (currentUser != null) {
            for (int i = 0; i < playerCount; i++) {
                if (players[i] == currentUser) {
                    // Eliminar el jugador desplazando elementos
                    for (int j = i; j < playerCount - 1; j++) {
                        players[j] = players[j + 1];
                    }
                    players[--playerCount] = null; // Reducir el contador y limpiar la posición
                    savePlayers();
                    System.out.println("Cuenta eliminada exitosamente.");
                    currentUser = null;
                    return;
                }
            }
        } else {
            System.out.println("No hay un usuario logueado.");
        }
    }

    public void showLast10Games() {
        if (currentUser != null) {
            System.out.println("Últimos 10 juegos de " + currentUser.getUsername() + ":");
            for (String log : currentUser.getGameLogs()) {
                System.out.println("- " + log);
            }
        } else {
            System.out.println("No hay un usuario logueado.");
        }
    }

    public void showRanking() {
        Player[] sortedPlayers = Arrays.copyOf(players, playerCount);
        Arrays.sort(sortedPlayers, 0, playerCount, (p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()));

        System.out.println("Ranking de jugadores:");
        for (int i = 0; i < playerCount; i++) {
            System.out.println(sortedPlayers[i].getUsername() + " - Puntos: " + sortedPlayers[i].getPoints());
        }
    }

    public void playGame(String opponentUsername) {
        Player opponent = getPlayerByUsername(opponentUsername);
        if (opponent == null) {
            System.out.println("El jugador oponente no existe.");
            return;
        }

        // Configurar fantasmas
        setupGhosts();

        // Turnos de juego
        boolean gameActive = true;
        int turn = 0; // Alterna entre jugadores: 0 - currentUser, 1 - opponent
        printBoard(turn == 0);
        while (gameActive) {
            askCoordinates(scanner, "Ingrese fila y columna del fantasma a mover (-1 -1 para salir): "); // Mostrar el tablero según el turno
            Player currentPlayer = (turn == 0) ? currentUser : opponent;
            System.out.println("Turno de: " + currentPlayer.getUsername());
            gameActive = takeTurn(currentPlayer, turn == 0);
            turn = 1 - turn;
        }
    }

    private void setupGhosts() {
        int count = ghostCount[difficulty];
        System.out.println("Configurando fantasmas...");
        if (mode == 0) {
            randomSetup(count, true);
            randomSetup(count, false);
        } else {
            manualSetup(count, true);
            manualSetup(count, false);
        }
    }

    private void randomSetup(int count, boolean isPlayer1) {
        for (int i = 0; i < count; i++) {
            char type = (i < count / 2) ? (isPlayer1 ? 'B' : 'b') : (isPlayer1 ? 'M' : 'm');
            placeGhostRandomly(type, isPlayer1);
        }
    }

    private void placeGhostRandomly(char type, boolean isPlayer1) {
        int row, col;
        do {
            row = random.nextInt(2) + (isPlayer1 ? 0 : 4);
            col = random.nextInt(6);
        } while (board[row][col] != EMPTY || isExit(row, col));
        board[row][col] = type;
    }

    private boolean isExit(int row, int col) {
        return (row == 0 && (col == 0 || col == 5)) || (row == 5 && (col == 0 || col == 5));
    }

    private void manualSetup(int count, boolean isPlayer1) {
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < count; i++) {
            char type = (i < count / 2) ? (isPlayer1 ? 'B' : 'b') : (isPlayer1 ? 'M' : 'm');
            boolean placed = false;
            while (!placed) {
                System.out.print("Ingrese fila y columna para el fantasma " + (type == 'B' || type == 'b' ? "bueno" : "malo") + ": ");
                int row = scanner.nextInt();
                int col = scanner.nextInt();
                if (row >= (isPlayer1 ? 0 : 4) && row < (isPlayer1 ? 2 : 6) && col >= 0 && col < 6 && board[row][col] == EMPTY && !isExit(row, col)) {
                    board[row][col] = type;
                    placed = true;
                } else {
                    System.out.println("Posicion invalida. Intente de nuevo.");
                }
            }
        }
    }

    public void printBoard(boolean isPlayer1) {
        System.out.println("\nTablero:");
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                char cell = board[i][j];
                if ((cell == 'b' || cell == 'm') && isPlayer1) cell = 'X'; // Ocultar fantasmas del oponente
                if ((cell == 'B' || cell == 'M') && !isPlayer1) cell = 'X'; // Ocultar fantasmas del oponente
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    private void loadPlayers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Player player = Player.fromString(line);
                if (player != null) {
                    players[playerCount++] = player;
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar jugadores.");
        }
    }

    private void savePlayers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (int i = 0; i < playerCount; i++) {
                writer.write(players[i].toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar jugadores.");
        }
    }

    boolean isUserExists(String username) {
        for (int i = 0; i < playerCount; i++) {
            if (players[i].getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private Player getPlayerByUsername(String username) {
        for (int i = 0; i < playerCount; i++) {
            if (players[i].getUsername().equals(username)) {
                return players[i];
            }
        }
        return null;
    }
    
    private int[] askCoordinates(Scanner scanner, String prompt) {
        int[] coordinates = new int[2];
        boolean validInput = false;

        while (!validInput) {
            System.out.print(prompt);
            try {
                coordinates[0] = scanner.nextInt(); // Fila
                coordinates[1] = scanner.nextInt(); // Columna

                if (coordinates[0] >= 0 && coordinates[0] < 6 && coordinates[1] >= 0 && coordinates[1] < 6) {
                    validInput = true; // Las coordenadas están dentro del rango
                } else {
                    System.out.println("Coordenadas fuera del rango. Intente nuevamente (0-5).");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada no válida. Ingrese números enteros.");
                scanner.nextLine(); // Limpiar la entrada inválida
            }
        }
        takeTurn(currentUser, true);
        return coordinates;
    }

    private boolean takeTurn(Player currentPlayer, boolean isPlayer1) {
        Scanner scanner = new Scanner(System.in);

        // Pedir coordenadas del fantasma a mover
        int[] source = askCoordinates(scanner, "Ingrese fila y columna del fantasma a mover (-1 -1 para salir): ");

        // Verificar si el jugador quiere retirarse
        if (source[0] == -1 && source[1] == -1) {
            System.out.print("¿Está seguro que desea retirarse? (S/N): ");
            char confirm = scanner.next().toUpperCase().charAt(0);
            if (confirm == 'S') {
                System.out.println(currentPlayer.getUsername() + " se ha retirado del juego.");
                return false; // El otro jugador gana por retiro
            } else {
                return true; // Continuar el turno
            }
        }

        // Validar selección del fantasma
        if (!isValidMove(source[0], source[1], isPlayer1)) {
            System.out.println("La selección no es válida. Intente nuevamente.");
            return true; // Repetir turno
        }

        // Pedir coordenadas del destino
        int[] destination = askCoordinates(scanner, "Ingrese fila y columna destino: ");
        if (!processMove(source[0], source[1], destination[0], destination[1], isPlayer1)) {
            System.out.println("Movimiento no válido. Intente nuevamente.");
            return true; // Repetir turno
        }

        // Verificar condiciones de victoria
        return checkWinConditions();
    }
    
    private boolean isValidMove(int row, int col, boolean isPlayer1) {
        if (row < 0 || row >= 6 || col < 0 || col >= 6 || board[row][col] == EMPTY) {
            return false; // Fuera del rango o celda vacía
        }
        char ghost = board[row][col];
        return isPlayer1 ? ghost == 'B' || ghost == 'M' : ghost == 'b' || ghost == 'm';
    }
    
    private boolean processMove(int row, int col, int destRow, int destCol, boolean isPlayer1) {
        // Validar que el destino está en una casilla adyacente
        if (Math.abs(destRow - row) > 1 || Math.abs(destCol - col) > 1 || destRow < 0 || destRow >= 6 || destCol < 0 || destCol >= 6) {
            return false;
        }

        // Verificar si el destino está ocupado
        char targetGhost = board[destRow][destCol];
        if (targetGhost != EMPTY && (isPlayer1 ? targetGhost == 'B' || targetGhost == 'M' : targetGhost == 'b' || targetGhost == 'm')) {
            return false; // No se puede mover a una casilla ocupada por un fantasma propio
        }

        // Comer fantasma enemigo si corresponde
        if (targetGhost != EMPTY) {
            System.out.println("¡Te has comido un fantasma!");
            if (isPlayer1) {
                if (targetGhost == 'b') {
                    player2Good--;
                } else {
                    player2Bad--;
                }
            } else {
                if (targetGhost == 'B') {
                    player1Good--;
                } else {
                    player1Bad--;
                }
            }
        }

        // Mover el fantasma
        board[destRow][destCol] = board[row][col];
        board[row][col] = EMPTY;

        // Verificar si un fantasma bueno alcanza la salida
        if (isExit(destRow, destCol)) {
            char movedGhost = board[destRow][destCol];
            if ((movedGhost == 'B' && isPlayer1) || (movedGhost == 'b' && !isPlayer1)) {
                System.out.println("¡Un fantasma bueno ha escapado!");
                return false; // Fin del juego
            }
        }

        return true;
    }
    
    private boolean checkWinConditions() {
        if (player1Good == 0) {
            System.out.println("¡" + currentUser.getUsername() + " gana porque capturó todos los fantasmas buenos del oponente!");
            return false;
        }
        if (player2Good == 0) {
            System.out.println("¡El oponente gana porque capturó todos tus fantasmas buenos!");
            return false;
        }
        if (player1Bad == 0) {
            System.out.println("¡El oponente gana porque tú perdiste todos tus fantasmas malos!");
            return false;
        }
        if (player2Bad == 0) {
            System.out.println("¡" + currentUser.getUsername() + " gana porque el oponente perdió todos sus fantasmas malos!");
            return false;
        }
        return true; // El juego continúa
    }

    void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    void setMode(int mode) {
        this.mode = mode;
    }

    void changePassword(String newPassword) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}
