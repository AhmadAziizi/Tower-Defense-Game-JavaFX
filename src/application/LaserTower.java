package application;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;

public class LaserTower {
	private static final double VIEW_SIZE = 40; // px cinsinden sabit kule görsel boyutu

	private final double x, y, range, fireRate;
	private final Pane gamePane;
	private final GameManager gameManager;
	private final List<Enemy> enemies;
	private final Group viewGroup;
	private Timeline shootTimeline;
	private Timeline laserLoop;
	private static final double DAMAGE_PER_SECOND = 20;
	private java.util.Map<Enemy, Line> activeBeams = new java.util.HashMap<>();

	public LaserTower(double x, double y, double range, double fireRate, Pane gamePane, GameManager gameManager,
			List<Enemy> enemies) {
		this.x = x;
		this.y = y;
		this.range = range;
		this.fireRate = fireRate;
		this.gamePane = gamePane;
		this.gameManager = gameManager;
		this.enemies = enemies;

		// Sabit görsel boyut kullandık
		this.viewGroup = TowerViewFactory.createLaserTowerView(VIEW_SIZE);
		viewGroup.setTranslateX(x - VIEW_SIZE / 2);
		viewGroup.setTranslateY(y - VIEW_SIZE / 2);
		viewGroup.setUserData(this);
		gamePane.getChildren().add(viewGroup);

		startShooting();
	}

	private void startShooting() {
		laserLoop = new Timeline(new KeyFrame(Duration.seconds(0.1), e -> updateLaser(0.1)));
		laserLoop.setCycleCount(Animation.INDEFINITE);
		laserLoop.play();
	}

	public void stopShooting() {
		//Dusmanlar ölduyse kalan atıs efektlerini siliyoruz.
        if (laserLoop != null) laserLoop.stop();
        activeBeams.values().forEach(b -> gamePane.getChildren().remove(b));
        activeBeams.clear();
	}

	private void updateLaser(double deltaTime) {
		// Sıradaki hedefleri yakala ve ışın oluştur
		for (Enemy e : enemies) {
			if (!e.isAlive())
				continue;
			double dist = Math.hypot(e.getPixelX() - x, e.getPixelY() - y);
			if (dist <= range) {
				// eğer daha önce yoksa ışın oluşturur
				activeBeams.computeIfAbsent(e, enemy -> {
					Line beam = new Line(x, y, enemy.getPixelX(), enemy.getPixelY());
					beam.setStroke(Color.RED);
					beam.setStrokeWidth(2);
					gamePane.getChildren().add(beam);
					return beam;
				});
			}
		}
		// Mevcut ışınları güncelle veya sil
		java.util.Iterator<java.util.Map.Entry<Enemy, Line>> it = activeBeams.entrySet().iterator();
		while (it.hasNext()) {
			java.util.Map.Entry<Enemy, Line> ent = it.next();
			Enemy e = ent.getKey();
			Line beam = ent.getValue();

			if (!e.isAlive() || Math.hypot(e.getPixelX() - x, e.getPixelY() - y) > range) {
				// menzil dışında veya ölüyse: ışını kaldır
				gamePane.getChildren().remove(beam);
				it.remove();
			} else {
				// hala hedefteyse ışını güncelle
				beam.setEndX(e.getPixelX());
				beam.setEndY(e.getPixelY());
				// Hasar ver
				double damage = DAMAGE_PER_SECOND * deltaTime;
				e.takeDamage((int) damage);
			}
		}
	}
}
