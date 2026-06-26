package application;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.animation.Timeline;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class Game extends Application {

	private BorderPane root;
	private Map map;
	private Wave wave;
	private int currentLevel;
	private Pane gamePane = new Pane();
	private Text livesText, moneyText, waveTimerText;
	private GameManager gameManager;
	private List<Enemy> enemies = new ArrayList<>();
	private int lives = 5;
	private int money = 500;
	private int currentWaveIndex = 0;
	private Timeline waveCountdownTimeline;
	private VBox uielements;
	private VBox tower1;
	private VBox tower2;
	private VBox tower3;
	private VBox tower4;
	private Group singleTower;
	private Group laserTower;
	private Group tripleTower;
	private Group missileTower;
	private AnimationTimer lW;
	private Timeline spawnTimeline;
	private int totalEnemies;
	private int killedEnemies;

	@Override
	public void start(Stage primaryStage) {
		try {

			// Ekrandaki Stage'in özellikleri belirleniyor.
			root = new BorderPane();
			root.setStyle("-fx-background-color: #FAF1DC;");

			Scene scene = new Scene(root, 500, 500);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setTitle("Tower Defense Game");
			primaryStage.setScene(scene);
			primaryStage.setFullScreen(true);
			primaryStage.show();
			mainMenu();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Oyun başlatıldığında ekrana gelecek metod.
	private void mainMenu() {

		root.getChildren().clear();

		// Her menü ekranı geldiğinde değerler sıfırlanıyor.
		currentLevel = 1;
		currentWaveIndex = 0;
		lives = 5;
		money = 500;

		// Oyunu başlatacak buton
		Button start = new Button("Start Game");
		root.setCenter(start);

		start.setStyle(
				"-fx-background-color: #F1CE8E; -fx-text-fill: #4E2C1D; -fx-background-radius: 20; -fx-font-size: 40px;"
						+ "-fx-padding: 30 60; -fx-font-family: 'Georgia';");

		// Butona basıldığında bir sonraki seviyeden başlatıyoruz.
		start.setOnAction(e -> {
			try {
				currentLevel = 1;
				levelPane("level" + currentLevel + ".txt");

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
	}

	// Son seviye hariç tüm seviyeler başarıyla tamamlanınca bu metod çağrılacak.
	private void showWinScreen() {
		TowerPlacementManager.stopAllTowers();
		enemies.clear();
		root.getChildren().clear();

		// Tekrardan bazı değerleri sıfırlıyoruz.
		currentWaveIndex = 0;
		money = 500;

		VBox winPane = new VBox(20);
		winPane.setStyle("-fx-background-color: #FAF1DC; -fx-alignment: center;");

		Label winLabel = new Label("You Won!");
		winLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #4E2C1D; -fx-font-family: 'Georgia';");

		// Bir sonraki seviyeye geçmemizi sağlayan buton ve bu butona basılınca bir
		// sonraki level'a geçiyoruz.
		Button continueButton = new Button("Continue to Next Level");
		continueButton.setStyle(
				"-fx-background-color: #F1CE8E; -fx-text-fill: #4E2C1D; -fx-background-radius: 20; -fx-font-size: 40px;"
						+ "-fx-padding: 20 40; -fx-font-family: 'Georgia';");
		continueButton.setOnAction(e -> {
			try {
				loadNextLevel();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});

		winPane.getChildren().addAll(winLabel, continueButton);
		root.setCenter(winPane);
	}

	// Eğer can 0 olursa GameOver() metodu çağrılıyor.
	public void GameOver() {

		// lw, waveCountdownTimeline ve spawnTimeline data field'larının içindeki
		// referans yok ediliyor.
		if (lW != null)
			lW.stop();

		if (waveCountdownTimeline != null) {
			waveCountdownTimeline.stop();
			waveCountdownTimeline = null;
		}

		if (spawnTimeline != null) {
			spawnTimeline.stop();
			spawnTimeline = null;
		}

		TowerPlacementManager.stopAllTowers();
		root.getChildren().clear();

		enemies.clear();

//		currentLevel = 1;
//		currentWaveIndex = 0;
//		lives = 5;
//		money = 500;

		VBox endPane = new VBox(20);
		endPane.setStyle("-fx-background-color: #FAF1DC; -fx-alignment: center;");

		Label endLabel = new Label("Game Over!");
		endLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #4E2C1D; -fx-font-family: 'Georgia';");

		Button exitButton = new Button("Back to Main Menu");
		exitButton.setStyle(
				"-fx-background-color: #F1CE8E; -fx-text-fill: #4E2C1D; -fx-background-radius: 20; -fx-font-size: 30px;"
						+ "-fx-padding: 20 40; -fx-font-family: 'Georgia';");

		exitButton.setOnAction(e -> {
			mainMenu();
		});

		endPane.getChildren().addAll(endLabel, exitButton);
		root.setCenter(endPane);
	}

	// Eğer level 5'i geçersek bu metod çağıracağız.
	private void gameCompeleted() {
		TowerPlacementManager.stopAllTowers();
		enemies.clear();
		root.getChildren().clear();

		VBox winPane = new VBox(20);
		winPane.setStyle("-fx-background-color: #FAF1DC; -fx-alignment: center;");

		Label winLabel = new Label("Congratulations! You Have Completed The Game!");
		winLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #4E2C1D; -fx-font-family: 'Georgia';");

		Button continueButton = new Button("Back to Main Menu");
		continueButton.setStyle(
				"-fx-background-color: #F1CE8E; -fx-text-fill: #4E2C1D; -fx-background-radius: 20; -fx-font-size: 40px;"
						+ "-fx-padding: 20 40; -fx-font-family: 'Georgia';");
		continueButton.setOnAction(e -> {
			mainMenu();
		});

		winPane.getChildren().addAll(winLabel, continueButton);
		root.setCenter(winPane);
	}

	// Her seviye bu metod altında dizayn ediliyor.
	public void levelPane(String filename) throws FileNotFoundException, IOException {
		root.getChildren().clear();

		enemies.clear();
		map = new Map(filename);
		wave = new Wave(filename);
		uielements = new VBox(20);

		totalEnemies = wave.getTotalEnemies();

		gamePane = new Pane();
		gamePane.getChildren().add(map.getMap());

		// Map ve düşmanların birleştiği pane'in boyutlarını map'e göre ayarlıyoruz.
		gamePane.setMaxSize((map.getCellsize() * map.getWidth() + (map.getWidth() - 2) * map.getCellpadding()),
				(map.getCellsize() * map.getHeight() + (map.getHeight() - 2) * map.getCellpadding()));

		// UI parçaları oluşturuluyor ve hepsi bir araya getiriliyor.
		livesText = new Text("Lives: " + lives);
		livesText.setStyle("-fx-font-family: 'Georgia'; -fx-fill: #4E2C1D; -fx-font-size: 20px;");
		moneyText = new Text("Money: " + money);
		moneyText.setStyle("-fx-font-family: 'Georgia'; -fx-fill: #4E2C1D; -fx-font-size: 20px;");
		waveTimerText = new Text("Next Wave: --");
		waveTimerText.setStyle("-fx-font-family: 'Georgia'; -fx-fill: #4E2C1D; -fx-font-size: 20px;");
		currentWaveIndex = 0;

		tower1 = new VBox(5);
		tower1.setStyle("-fx-background-color: #F1D79C;-fx-background-radius: 10; -fx-padding: 10;");
		singleTower = TowerViewFactory.createSingleShotTowerView(50);
		Text txt1 = new Text("Single Shot Tower - 50$");
		tower1.getChildren().addAll(singleTower, txt1);

		tower2 = new VBox(5);
		tower2.setStyle("-fx-background-color: #F1D79C; -fx-background-radius: 10; -fx-padding: 10;");
		laserTower = TowerViewFactory.createLaserTowerView(40);
		Text txt2 = new Text("Laser Tower - 120$");
		tower2.getChildren().addAll(laserTower, txt2);

		tower3 = new VBox(5);
		tower3.setStyle("-fx-background-color: #F1D79C; -fx-background-radius: 10; -fx-padding: 10;");
		tripleTower = TowerViewFactory.createTripleShotTowerView(40);
		Text txt3 = new Text("Triple Tower - 150$");
		tower3.getChildren().addAll(tripleTower, txt3);

		tower4 = new VBox(5);
		tower4.setStyle("-fx-background-color: #F1D79C; -fx-background-radius: 10; -fx-padding: 10;");
		missileTower = TowerViewFactory.createMissileLauncherTowerView(30);
		Text txt4 = new Text("Missile Launcher Tower - 200$");
		tower4.getChildren().addAll(missileTower, txt4);

		tower1.setAlignment(Pos.CENTER);
		tower2.setAlignment(Pos.CENTER);
		tower3.setAlignment(Pos.CENTER);
		tower4.setAlignment(Pos.CENTER);

		uielements.getChildren().addAll(livesText, moneyText, waveTimerText, tower1, tower2, tower3, tower4);
		uielements.setSpacing(15);
		// All panes is added to root stage.
		root.setRight(uielements);
		root.setCenter(gamePane);
		uielements.setAlignment(Pos.CENTER);
		BorderPane.setMargin(uielements, new Insets(0, 10, 0, 0));

		gameManager = new GameManagerImpl(gamePane, livesText, moneyText, lives, money);

		// Oyun boyunca kontrol yapması için AnimationTimer sınıfından bir tane instance
		// oluşturuluyor.
		lW = new AnimationTimer() {
			@Override
			public void handle(long now) {

				killedEnemies = ((GameManagerImpl) gameManager).getKilledEnemies();

				// Eğer tüm düşman sayısı öldürülen ve sona ulaşan düşman sayısına eşitse bu
				// kısım devreye giriyor.
				// Eğer devreye girmeseydi oyun bir bölümün içinde sıkışıp kalacaktı.
				if (totalEnemies == killedEnemies + ((GameManagerImpl) gameManager).getSuccessfullEnemies()) {
					if (currentLevel < 5) {
						// bir kere tetiklesin, sonra dursun
						this.stop();
						Platform.runLater(() -> showWinScreen());
					}

					else {
						// bir kere tetiklesin, sonra dursun
						this.stop();
						Platform.runLater(() -> gameCompeleted());
					}
				}
				if (gameManager.getLives() == 0) {
					// bir kere tetiklesin, sonra dursun
					this.stop();
					Platform.runLater(() -> GameOver());
				}
			}
		};
		lW.start();

		new Timeline(new KeyFrame(Duration.millis(1), e -> startNextWave())).play();
		// sag paneldeki kuleleri suruklenebilir yapmak icin bu kısmı kullandık.
		// her bir tower icin ayrı ayrı olusturdugumuzdan dolayı her bir towerı ayrı
		// ayrı suruklenebilir yaptık
		TowerPlacementManager.makeDraggableIcon(tower1, "single", 50);
		TowerPlacementManager.makeDraggableIcon(tower2, "laser", 120);
		TowerPlacementManager.makeDraggableIcon(tower3, "triple", 150);
		TowerPlacementManager.makeDraggableIcon(tower4, "missile", 200);

		// oyundaki map ekranını da kuleleri bırakacagımız bolum olarak sectik burada
		TowerPlacementManager.enablePaneDrop(gamePane, (GameManagerImpl) gameManager, enemies);
	}

	private void loadNextLevel() throws FileNotFoundException, IOException {
		// Her bir sonraki seviyede değerleri ilk haline getiriyoruz, bu sayede önceki
		// seviyeden bir şey kalmıyor.
		TowerPlacementManager.stopAllTowers();
		enemies.clear();
		currentLevel++;
		currentWaveIndex = 0;
		lives = 5;
		if (currentLevel <= 5) {
			root.getChildren().clear();
			levelPane("level" + currentLevel + ".txt");
			root.setCenter(gamePane);
		}
	}

	private void startNextWave() {

		if (currentWaveIndex >= wave.getWaveDataList().size()) {
			// Eğer GameOver ekranı zaten gösterildiyse burada hiç bir şey yapma
			return;
		}

		WaveData waveData = wave.getWaveDataList().get(currentWaveIndex);
		startWaveCountdown(waveData.getDelay());

		spawnTimeline = new Timeline();
		for (int i = 0; i < waveData.getEnemies(); i++) {
			spawnTimeline.getKeyFrames().add(new KeyFrame(
					Duration.seconds(waveData.getDelay() + i * waveData.getInterval()), e -> spawnEnemy()));
		}

		spawnTimeline.setOnFinished(e -> {
			currentWaveIndex++;
			startNextWave();
		});
		spawnTimeline.play();

	}

	private void startWaveCountdown(int delaySeconds) {
		if (waveCountdownTimeline != null)
			waveCountdownTimeline.stop();

		final IntegerProperty remainingTime = new SimpleIntegerProperty(delaySeconds);
		waveCountdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
			remainingTime.set(remainingTime.get() - 1);
			Platform.runLater(() -> waveTimerText.setText("Next Wave: " + remainingTime.get() + "s"));
		}));
		waveCountdownTimeline.setCycleCount(delaySeconds);
		waveCountdownTimeline.play();
	}

	private void spawnEnemy() {
		Enemy enemy = new Enemy(map, gameManager);
		enemies.add(enemy);
		StackPane.setAlignment(enemy.getView(), Pos.CENTER);
		gamePane.getChildren().add(enemy.getView());
	}

	public static void main(String[] args) {
		launch(args);
	}
}