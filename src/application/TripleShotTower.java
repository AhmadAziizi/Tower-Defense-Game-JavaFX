package application;

import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

public class TripleShotTower {
	private static final double VIEW_SIZE = 40; // px cinsinden sabit kule görsel boyutu
	private static final int DAMAGE = 25; // üçlü atış hasarı
	private static final double BULLET_RADIUS = 4;

	private final double x, y, range, fireRate;
	private final Pane gamePane;
	private final GameManager gameManager;
	private final List<Enemy> enemies;
	private final Group viewGroup;
	private Timeline shootTimeline;

	public TripleShotTower(double x, double y, double range, double fireRate, Pane gamePane, GameManager gameManager,
			List<Enemy> enemies) {
		this.x = x;
		this.y = y;
		this.range = range;
		this.fireRate = fireRate;
		this.gamePane = gamePane;
		this.gameManager = gameManager;
		this.enemies = enemies;

		// Görsel
		viewGroup = TowerViewFactory.createTripleShotTowerView(VIEW_SIZE);
		viewGroup.setTranslateX(x - VIEW_SIZE / 2);
		viewGroup.setTranslateY(y - VIEW_SIZE / 2);
		viewGroup.setUserData(this);
		gamePane.getChildren().add(viewGroup);

		startShooting();
	}

	private void startShooting() {
		shootTimeline = new Timeline(new KeyFrame(Duration.seconds(fireRate), e -> shoot()));
		shootTimeline.setCycleCount(Timeline.INDEFINITE);
		shootTimeline.play();
	}

	public void stopShooting() {
		shootTimeline.stop();
	}

	private void shoot() {
		// Menzil içindeki en yakın 3 düşmanı bul
		List<Enemy> targets = enemies.stream().filter(Enemy::isAlive).filter(e -> dist(e) <= range)
				.sorted((e1, e2) -> Double.compare(dist(e1), dist(e2))).limit(3).collect(Collectors.toList());

		for (Enemy target : targets) {
			// Mermi oluştur
			Circle bullet = new Circle(BULLET_RADIUS);
			bullet.setTranslateX(x);
			bullet.setTranslateY(y);
			gamePane.getChildren().add(bullet);

			// Yol ve animasyon
			Path path = new Path(new MoveTo(x, y), new LineTo(target.getPixelX(), target.getPixelY()));
			PathTransition pt = new PathTransition(Duration.seconds(dist(target) / 300.0), path, bullet);
			pt.setOnFinished(ev -> {
				Platform.runLater(() -> {
					target.takeDamage(DAMAGE);
					gamePane.getChildren().remove(bullet);
				});
			});
			pt.play();
		}
	}

	private double dist(Enemy e) {
		double dx = e.getPixelX() - x, dy = e.getPixelY() - y;
		return Math.hypot(dx, dy);
	}
}
