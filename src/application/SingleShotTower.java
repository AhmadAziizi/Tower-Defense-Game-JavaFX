package application;

import java.util.List;
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

public class SingleShotTower {
	private static final double VIEW_SIZE = 40; // görsel boyut

	private final double x, y, range, fireRate;
	private final int damage = 25;
	private final Pane gamePane;
	private final GameManager gameManager;
	private final List<Enemy> enemies;
	private final Group viewGroup;
	private Timeline shootTimeline;

	public SingleShotTower(double x, double y, double range, double fireRate, Pane gamePane, GameManager gameManager,
			List<Enemy> enemies) {
		this.x = x;
		this.y = y;
		this.range = range;
		this.fireRate = fireRate;
		this.gamePane = gamePane;
		this.gameManager = gameManager;
		this.enemies = enemies;

		// Sabit görsel boyut kullandık
		this.viewGroup = TowerViewFactory.createSingleShotTowerView(VIEW_SIZE);
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
		if (shootTimeline != null) 
			shootTimeline.stop(); 
		}

	private void shoot() {
		Enemy target = enemies.stream().filter(Enemy::isAlive).min((e1, e2) -> Double.compare(dist(e1), dist(e2)))
				.orElse(null);

		if (target == null || dist(target) > range)
			return;

		Circle bullet = new Circle(5);
		bullet.setTranslateX(x);
		bullet.setTranslateY(y);
		gamePane.getChildren().add(bullet);

		Path path = new Path(new MoveTo(x, y), new LineTo(target.getPixelX(), target.getPixelY()));
		PathTransition pt = new PathTransition(Duration.seconds(dist(target) / 300.0), path, bullet);
		pt.setOnFinished(ev -> {
			Platform.runLater(() -> {
				target.takeDamage(damage);
				gamePane.getChildren().remove(bullet);
			});
		});
		pt.play();
	}

	private double dist(Enemy e) {
		double dx = e.getPixelX() - x, dy = e.getPixelY() - y;
		return Math.hypot(dx, dy);
	}
}
