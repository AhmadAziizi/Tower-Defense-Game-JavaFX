package application;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class GameManagerImpl implements GameManager {
	private final Pane gamePane;
	private final Text livesText;
	private final Text moneyText;
	private int lives;
	private int money;
	private int killedEnemies;
	private int successfullEnemies;

	public GameManagerImpl(Pane gamePane, Text livesText, Text moneyText, int initialLives, int initialMoney) {
		this.gamePane = gamePane;
		this.livesText = livesText;
		this.moneyText = moneyText;
		this.lives = initialLives;
		this.money = initialMoney;
		updateUI();
	}

	// Allow the Game class to inject its GameOver() method as a callback

	private void updateUI() {
		Platform.runLater(() -> {
			livesText.setText("Lives: " + lives);
			moneyText.setText("Money: " + money);
		});
	}

	@Override
	public void addMoney(int amount) {
		money += amount;
		updateUI();
	}

	@Override
	public void loseLife() {
		lives--;
		successfullEnemies++;
		updateUI();
	}

	@Override
	public void removeEnemy(Enemy enemy) {
		Platform.runLater(() -> gamePane.getChildren().remove(enemy.getView()));
	}

	@Override
	public void addExplosion(Group explosion) {

		Platform.runLater(() -> {
			gamePane.getChildren().add(explosion);

		});
	}

	public int getLives() {
		return lives;
	}

	public int getMoney() {
		return money;
	}

	public int getKilledEnemies() {
		return killedEnemies;
	}

	public void setKilledEnemies(int killedEnemies) {
		this.killedEnemies = killedEnemies;
	}

	public int getSuccessfullEnemies() {
		return successfullEnemies;
	}
}