package application;

import java.util.List;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Enemy {
	private double pixelX, pixelY;
	private double targetPixelX, targetPixelY;
	private List<int[]> pathCells;
	private int currentPathIndex = 0;
	private double speed = 0.65;
	private Map map;
	private Rectangle healthBarBackground;
	private Rectangle healthBarForeground;
	private Group visualGroup;
	private AnimationTimer timer;
	ImageView imageV;
	private int health = 100;
	private final int maxHealth = 100;
	private boolean isAlive = true;
	private GameManager gameManager;

	public Enemy(Map map, GameManager gameManager) {
		this.map = map;
		this.pathCells = map.getPathCoordinatesList();
		this.gameManager = gameManager;

		if (pathCells.isEmpty()) {
			System.err.println("No path cells found!");
		}

		initializeEnemy(); // dusmani insa etmek
		startMoving(); // motion baslatmak
	}

	private void initializeEnemy() {
		// fotografi src folderden almasini sagliyor
		Image image = new Image(getClass().getResourceAsStream("/application/D_Walk.png"));
		imageV = new ImageView(image);
		imageV.setTranslateX(-25);
		imageV.setTranslateY(-25);

		// sağlıkÇubuğu Arkaplanı

		healthBarBackground = new Rectangle(30, 5, Color.rgb(50, 50, 50));
		healthBarBackground.setX(1);
		healthBarBackground.setY(1);
		healthBarBackground.setArcHeight(5);
		healthBarBackground.setArcWidth(5);

		// sağlık çubuğu ön plan
		healthBarForeground = new Rectangle(30, 5, Color.FORESTGREEN);
		healthBarForeground.setX(1);
		healthBarForeground.setY(1);
		healthBarForeground.setArcHeight(5);
		healthBarForeground.setArcWidth(5);
		healthBarBackground.setTranslateY(-20);
		healthBarBackground.setTranslateX(-15);
		healthBarForeground.setTranslateY(-20);
		healthBarForeground.setTranslateX(-15);
		healthBarForeground.setWidth(30);

		// tek grubta toplamak
		visualGroup = new Group(imageV, healthBarBackground, healthBarForeground);
		// baslangic noktayi almak
		int[] startCell = pathCells.get(0);

		int gridX = startCell[0];
		int gridY = startCell[1];
		// pathcells arry grid satir sutun den pixel konumunu bulmak
		pixelX = gridX * (map.getCellsize() + map.getCellpadding()) + map.getCellsize() / 2;
		pixelY = gridY * (map.getCellsize() + map.getCellpadding()) + map.getCellsize() / 2;

		updateVisualPosition();
		updateTargetPosition();
	}

	private void updateVisualPosition() {
		// oyun ekraninda konumu guncellemek
		visualGroup.setTranslateX(pixelX);
		visualGroup.setTranslateY(pixelY);
	}

	private void updateTargetPosition() {
		// sonraki noktayi hesablamak
		if (currentPathIndex + 1 < pathCells.size()) {
			int[] nextCell = pathCells.get(currentPathIndex + 1);

			int targetGridX = nextCell[0];
			int targetGridY = nextCell[1];
			targetPixelX = targetGridX * (map.getCellsize() + map.getCellpadding()) + map.getCellsize() / 2;
			targetPixelY = targetGridY * (map.getCellsize() + map.getCellpadding()) + map.getCellsize() / 2;

		}
	}

	private void startMoving() {
		// hareketin baslamasi
		timer = new AnimationTimer() {

			@Override
			public void handle(long now) {
				moveTarget();
			}
		};
		timer.start();
	}

	private void moveTarget() {
		// son noktaya ulastiysa fade effect le dusmani kaldirmak

		if (hasReachedEnd()) {
			gameManager.loseLife();
			timer.stop();
			isAlive = false;
			FadeTransition fadeOut = new FadeTransition(Duration.millis(500), visualGroup);
			fadeOut.setFromValue(1.0);
			fadeOut.setToValue(0.0);

			fadeOut.setOnFinished(e -> {
				Platform.runLater(() -> {
					gameManager.removeEnemy(Enemy.this);
				});
			});

			fadeOut.play();

			return;
		}
		// aradaki mesafeyi hesaplayip mesafe hizidan azsa direct sonraki noktaya
		// konumunu gunceller
		double dx = targetPixelX - pixelX;
		double dy = targetPixelY - pixelY;
		double distance = Math.sqrt(dx * dx + dy * dy);

		double directionX = dx / distance;
		double directionY = dy / distance;

		double moveX = directionX * speed;
		double moveY = directionY * speed;

		// düşmanın sonraki noktaya mesafesi hizdan azsa direkt sonraki noktaya guncelle
		if (distance < speed) {
			pixelX = targetPixelX;
			pixelY = targetPixelY;
			currentPathIndex++;

			if (!hasReachedEnd()) {

				updateTargetPosition();
			}

		} // degilse hizla bagli harekete edecek
		else {
			pixelX += moveX;
			pixelY += moveY;

			updateVisualPosition();
		}

	}

	public void takeDamage(int damage) {
		if (!isAlive)
			return;

		health = Math.max(0, health - damage);
		updateHealthBar();

		if (health <= 0) {
			die();
		}
	}

	private void die() {
		isAlive = false;
		timer.stop();

		// effect çalıştırılıyor
		playExplosionEffect();

		// Öldürülen düşman sayısı güncelleniyor.
		((GameManagerImpl) gameManager).setKilledEnemies(((GameManagerImpl) gameManager).getKilledEnemies() + 1);

		Platform.runLater(() -> {
			// Düşman kaldırılıyor ve para ekleniyor.
			visualGroup.setVisible(false);
			gameManager.removeEnemy(Enemy.this);
		});
		gameManager.addMoney(10);

	}

	private void playExplosionEffect() {
		if (hasReachedEnd())
			return;

		// Düşmanın konumunu al
		Point2D sceneOrigin = visualGroup.localToScene(0, 0);

		// Düşmanın koordinatlarını gamePane’in lokaline çevir
		// (gamePane, GameManagerImpl içinde tutuluyor)
		Pane gamePane = (Pane) map.getMap().getParent();
		Point2D paneOrigin = gamePane.sceneToLocal(sceneOrigin);

		// Her bir parçacığı doğrudan gamePane'e ekle ve hareket ettir
		for (int i = 0; i < 20; i++) {
			Circle particle = new Circle(3, Color.ORANGERED);
			particle.setCenterX(paneOrigin.getX());
			particle.setCenterY(paneOrigin.getY());
			gamePane.getChildren().add(particle);

			double angle = Math.random() * 2 * Math.PI;
			double dist = 50 + Math.random() * 50;
			double dx = dist * Math.cos(angle);
			double dy = dist * Math.sin(angle);

			PathTransition pt = new PathTransition(Duration.millis(500),
					new Path(new MoveTo(paneOrigin.getX(), paneOrigin.getY()),
							new LineTo(paneOrigin.getX() + dx, paneOrigin.getY() + dy)),
					particle);
			pt.setOnFinished(e -> gamePane.getChildren().remove(particle));
			pt.play();
		}
	}

	private void updateHealthBar() {
		double healthPercentage = (double) health / maxHealth;
		healthBarForeground.setWidth(30 * healthPercentage);

		// dusmanin cani azaltinda can bari renkini disitiriyor
		if (healthPercentage < 0.4) {
			healthBarForeground.setFill(Color.RED);
		} else if (healthPercentage < 0.7) {
			healthBarForeground.setFill(Color.YELLOW);
		}
	}

	// getter ve setter

	public Group getView() {
		return visualGroup;
	}

	public boolean hasReachedEnd() {
		return currentPathIndex >= pathCells.size() - 1;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public double getPixelX() {
		return pixelX;
	}

	public double getPixelY() {
		return pixelY;
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public void setPixelX(double pixelX) {
		this.pixelX = pixelX;
	}

	public void setPixelY(double pixelY) {
		this.pixelY = pixelY;
	}

	public double getTargetPixelX() {
		return targetPixelX;
	}

	public void setTargetPixelX(double targetPixelX) {
		this.targetPixelX = targetPixelX;
	}

	public double getTargetPixelY() {
		return targetPixelY;
	}

	public void setTargetPixelY(double targetPixelY) {
		this.targetPixelY = targetPixelY;
	}

	public List<int[]> getPathCells() {
		return pathCells;
	}

	public void setPathCells(List<int[]> pathCells) {
		this.pathCells = pathCells;
	}

	public int getCurrentPathIndex() {
		return currentPathIndex;
	}

	public void setCurrentPathIndex(int currentPathIndex) {
		this.currentPathIndex = currentPathIndex;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public Rectangle getHealthBarBackground() {
		return healthBarBackground;
	}

	public void setHealthBarBackground(Rectangle healthBarBackground) {
		this.healthBarBackground = healthBarBackground;
	}

	public Rectangle getHealthBarForeground() {
		return healthBarForeground;
	}

	public void setHealthBarForeground(Rectangle healthBarForeground) {
		this.healthBarForeground = healthBarForeground;
	}

	public Group getVisualGroup() {
		return visualGroup;
	}

	public void setVisualGroup(Group visualGroup) {
		this.visualGroup = visualGroup;
	}

	public ImageView getImageV() {
		return imageV;
	}

	public void setImageV(ImageView imageV) {
		this.imageV = imageV;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public GameManager getGameManager() {
		return gameManager;
	}

	public void setGameManager(GameManager gameManager) {
		this.gameManager = gameManager;
	}
}