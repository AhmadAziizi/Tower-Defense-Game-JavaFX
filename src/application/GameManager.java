package application;

import javafx.scene.Group;

public interface GameManager {
	void addMoney(int amount);

	void loseLife();

	void removeEnemy(Enemy enemy);

	void addExplosion(Group explosion);

	int getMoney();

	int getLives();
}