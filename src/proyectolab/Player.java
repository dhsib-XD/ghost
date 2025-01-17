/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectolab;

import java.util.LinkedList;

/**
 *
 * @author omarr
 */
class Player {
    private String username;
    private String password;
    private int points;
    private LinkedList<String> gameLogs; // Últimos 10 juegos

    public Player(String username, String password) {
        this.username = username;
        this.password = password;
        this.points = 0;
        this.gameLogs = new LinkedList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void addGameLog(String log) {
        if (gameLogs.size() == 10) {
            gameLogs.removeFirst(); // Remueve el log más antiguo
        }
        gameLogs.addLast(log);
    }

    public LinkedList<String> getGameLogs() {
        return new LinkedList<>(gameLogs);
    }

    @Override
    public String toString() {
        return username + "," + password + "," + points;
    }

    public static Player fromString(String data) {
        String[] parts = data.split(",");
        if (parts.length == 3) {
            Player player = new Player(parts[0], parts[1]);
            player.points = Integer.parseInt(parts[2]);
            return player;
        }
        return null;
    }
}
