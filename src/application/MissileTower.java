	package application;

import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

public class MissileTower {
	private static final double VIEW_SIZE = 40; // px cinsinden sabit kule görsel boyutu
	private static final double MISSILE_RADIUS = 6; // mermi boyutu
	private static final double EXPLOSION_RADIUS = 50; // patlama yarıçapı (px)
	private static final int DAMAGE = 45; // hasarı

	private final double x, y, range, fireRate;
	private final Pane gamePane;
	private final GameManager gameManager;
	private final List<Enemy> enemies;
	private final Group viewGroup;
	private Timeline shootTimeline;


	public MissileTower(double x, double y, double range, double fireRate, Pane gamePane, GameManager gameManager,
			List<Enemy> enemies) {
		this.x = x;
		this.y = y;
		this.range = range;
		this.fireRate = fireRate;
		this.gamePane = gamePane;
		this.gameManager = gameManager;
		this.enemies = enemies;

		// Sabit görsel boyutlu kule görünümü
		this.viewGroup = TowerViewFactory.createMissileLauncherTowerView(VIEW_SIZE);
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
		if (shootTimeline != null) {
	        shootTimeline.stop();
	    }
	}

	private void shoot() {
		// En yakın düşmanı bul
		Enemy target = enemies.stream().filter(Enemy::isAlive).min((e1, e2) -> Double.compare(dist(e1), dist(e2)))
				.orElse(null);

		if (target == null || dist(target) > range)
			return;

		// Mermi oluştur
		Circle missile = new Circle(MISSILE_RADIUS);
		missile.setTranslateX(x);
		missile.setTranslateY(y);
		gamePane.getChildren().add(missile);

		// Merminin hedefe yolculuk
		Path path = new Path(new MoveTo(x, y), new LineTo(target.getPixelX(), target.getPixelY()));
		double travelTime = dist(target) / 200.0; // 200 px/s hız
		PathTransition pt = new PathTransition(Duration.seconds(travelTime), path, missile);
		pt.setOnFinished(ev -> {
			// Patlama efekti ve hasarı
			Platform.runLater(() -> {
				// Patlama 
				Circle explosion = new Circle(EXPLOSION_RADIUS);
				explosion.setLayoutX(target.getPixelX());
				explosion.setLayoutY(target.getPixelY());
				explosion.setOpacity(0.7);
				explosion.setFill(javafx.scene.paint.Color.ORANGE);
				// Haritanın üzerine ekliyoruz, böylece grid ile aynı koordinat sistemine girer
				gamePane.getChildren().add(explosion);
				// Yakındaki düşmanlara hasar
				enemies.stream().filter(Enemy::isAlive).filter(e -> {
					double dx = e.getPixelX() - target.getPixelX();
					double dy = e.getPixelY() - target.getPixelY();
					return Math.hypot(dx, dy) <= EXPLOSION_RADIUS;
				}).forEach(e -> {
					e.takeDamage(DAMAGE);
				});

				// Patlamayı kaldır
				FadeTransition ft = new FadeTransition(Duration.seconds(0.5), explosion);
				ft.setFromValue(0.7);
				ft.setToValue(0.0);
				ft.setOnFinished(ae -> gamePane.getChildren().remove(explosion));
				ft.play();

				// Mermiyi kaldır
				gamePane.getChildren().remove(missile);
			});
		});
		pt.play();
	}

	private double dist(Enemy e) {
		double dx = e.getPixelX() - x, dy = e.getPixelY() - y;
		return Math.hypot(dx, dy);
	}
}
