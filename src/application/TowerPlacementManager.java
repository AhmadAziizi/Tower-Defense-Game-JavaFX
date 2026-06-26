package application;

import javafx.scene.input.*;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.geometry.Bounds;

/**
 * Surukle bırak yaparak kule yerlestirmeyi, sag tık yaparak kuleleri iade
 * etmeyi, bir kuleyi iade edince bir sure beklemeyi (cooldown), kulelerin
 * alınıp alınamayacıgını (money) kontrol ettigimiz toplu sınıf burası.
 */
public class TowerPlacementManager {
	// Kodun devamında da kullanabilmek adına bazı sabitler kullanıyoruz.
	private static final DataFormat TOWER_TYPE = new DataFormat("tower-type");
	private static final DataFormat TOWER_OBJ = new DataFormat("tower-obj");

	// Sürükleme esnasında gözükecek kesikli çember
	private static Circle dragRangeCircle = null;

	// Cooldown'da kullanabilmek adına kulelerin ne zaman kaldırıldıgını tutuyoruz.
	// Yine cooldown'da kullanabilmek adına bir sure sabiti belirledik. 3 saniye
	// gayet yeterli bir sure ancak ms olarak belirlememiz gerekti.
	// Bu 3 saniye: Kulenin kaldırılma suresine bakıyor.
	// Yani kule son 3 saniye icerisinde mi kaldırılmıs ona bakmak icin boyle bir
	// sabit belirledik.
	private static final Map<String, Long> lastRemovalTime = new HashMap<>();
	private static final long COOLDOWN_MS = 3000; // 3 saniye

	private static final List<Object> activeTowers = new ArrayList<>();

	/**
	 * Burada "suruklenebilir icon" yapma methoduna basladık. Yaptıgımız sey, VBox
	 * ile sag tarafa alt alta koydugumuz kule iconlarını oyuncunun tutarak harita
	 * uzerinde bir lokasyona bırakabilmesini saglamak. Kodda ilk olarak bir
	 * cooldown kontrolu yapıyoruz. Bu kontrolun amacı, oyuncu bir kuleyi iade etme
	 * kararı verdikten hemen sonra yerlestiremesin diye.
	 * 
	 */
	public static void makeDraggableIcon(VBox towerBox, String type, int cost) {
		towerBox.setOnDragDetected(evt -> {
			// Cooldown kontrolü
			Long last = lastRemovalTime.get(type); // O kule tipinin en son kaldırıldıgı zamanı alıyoruz
			long now = System.currentTimeMillis();
			if (last != null && now - last < COOLDOWN_MS) {
				// Eger kule son 3 saniye icinde iade edilmemisse o kuleyi secip
				// yerlestirebiliyoruz.
				double x = evt.getSceneX(), y = evt.getSceneY();
				long wait = (COOLDOWN_MS - (now - last)) / 1000;
				showWarning((Pane) towerBox.getScene().getRoot(), x, y,
						"Please wait for " + wait + " secs \nto use this tower!");
				// Eger kule son 3 saniye icinde iade edilmisse o kuleyi tekrar secmek icin
				// kalan sureyi gosteren bir yazı beliriyor ekranda
				evt.consume();
				return;
			}
			// Burası kuleyi oldugu gibi alıp baska bir yere tasımak icin yazdıgımız kod.
			// Aslında bu kod basta yazılırken "herhangi bir cisim tasınıyormus" gibi
			// yazmıstık.
			// Ancak sonradan hemen altındaki bolumu (Group dragViewGroup ile baslayan
			// kısım) de yazdık.
			Dragboard db = towerBox.startDragAndDrop(TransferMode.COPY);
			ClipboardContent content = new ClipboardContent();
			content.put(TOWER_TYPE, type);
			content.put(TOWER_OBJ, cost);
			db.setContent(content);

			// Bu bolumun amacı: Kuleleri sag taraftan tutup haritanın uzerine suruklerken
			// mouse iconunda kulenin goruntusunun olması
			// Bunu da her bir kule tipi icin ayrı ayrı olusturdugumuz senaryolarla ve
			// yine her bir kule tipi icin ayrı ayrı olusturdugumuz "goruntu olusturucularla
			// (create...towerview") sagladık.
			Group dragViewGroup;
			int viewSize = 40; // surukleme esnasında kullanılacak icon boyutunu burada belirledik
			switch (type) {
			case "single":
				dragViewGroup = TowerViewFactory.createSingleShotTowerView(viewSize);
				break;
			case "laser":
				dragViewGroup = TowerViewFactory.createLaserTowerView(viewSize);
				break;
			case "triple":
				dragViewGroup = TowerViewFactory.createTripleShotTowerView(viewSize);
				break;
			case "missile":
				dragViewGroup = TowerViewFactory.createMissileLauncherTowerView(viewSize);
				break;
			default:
				dragViewGroup = new Group();
			}
			db.setDragView(dragViewGroup.snapshot(null, null));
			evt.consume();
		});
	}

	// sınıf içinde, enablePaneDrop metodundan önce helper metod:
	private static double getRangeForType(String type) {
		switch (type) {
		case "single":
			return 100;
		case "laser":
			return 120;
		case "triple":
			return 150;
		case "missile":
			return 150;
		default:
			return 0;
		}
	}

	/**
	 * Kulelerin, sadece oyunun map kısmına bırakabilinmesi icin bu bolumu yazdık.
	 */
	public static void enablePaneDrop(Pane gamePane, GameManagerImpl gameManager, List<Enemy> enemies) {
		gamePane.setOnDragOver(evt -> {
			if (evt.getDragboard().hasContent(TOWER_TYPE)) {
				evt.acceptTransferModes(TransferMode.COPY);
				// Çemberi oluştur veya güncelle
				String type = (String) evt.getDragboard().getContent(TOWER_TYPE);
				double range = getRangeForType(type);
				if (dragRangeCircle == null) {
					dragRangeCircle = new Circle(range);
					dragRangeCircle.setStroke(Color.BLUE);
					dragRangeCircle.setStrokeWidth(2);
					dragRangeCircle.getStrokeDashArray().addAll(10.0, 5.0);
					dragRangeCircle.setFill(null);
					gamePane.getChildren().add(dragRangeCircle);
				}
				dragRangeCircle.setCenterX(evt.getX());
				dragRangeCircle.setCenterY(evt.getY());
			}
			evt.consume();
		});

		gamePane.setOnDragDropped(evt -> {
			final int CELL = 50;
			final int PAD = 2;
			final double STEP = CELL + PAD;

			Dragboard db = evt.getDragboard();
			boolean success = false;
			if (!db.hasContent(TOWER_TYPE)) {
				evt.setDropCompleted(false);
				evt.consume();
				return;
			}

			String type = (String) db.getContent(TOWER_TYPE);
			int cost = (Integer) db.getContent(TOWER_OBJ);
			double x = evt.getX(), y = evt.getY();

			int col = (int) (x / STEP);
			int row = (int) (y / STEP);

			double snappedX = col * STEP + CELL / 2;
			double snappedY = row * STEP + CELL / 2;

			// Kulelerin, dusmanların gectigi yol uzerine yerlestirilememesi lazım. O yuzden
			// burada kontrol ediyoruz.
			// Kontrolu ise "isPathCell" yardımcı methodu aracılıgıyla yapıyoruz.
			GridPane grid = findGrid(gamePane);
			if (grid != null && isPathCell(grid, x, y)) {
				showWarning(gamePane, x, y, "Towers can't be placed on the path!");
				evt.setDropCompleted(false);
				evt.consume();
				return;
			}

			// Burası da map dısına tower koymamak yaptıgımız method.
			// Zaten isPathCell methoduyla neredeyse aynı imzayı tasıyor.
			// Birisi hem gridi hem de path i kontrol ederken digeri sadece grid i kontrol
			// etmis oluyor.

			if (isOutsideMap(grid, evt.getX(), evt.getY())) {
				showWarning(gamePane, evt.getX(), evt.getY(), "Towers can't be placed outside the map!");
				evt.setDropCompleted(false);
				evt.consume();
				return;
			}
			// Kulelerin path uzerine konulmaya calısılmadıgından emin olduktan sonra sıra
			// cooldown kontrolune geliyor.
			// Burada da eger cooldown bitmeden aynı kule tipi yazılmaya calısılırsa ekrana
			// uyarı veriyoruz.
			// Uyarı kalan sureyi de gosteriyor (3,2,1,0 gibi)
			Long last = lastRemovalTime.get(type);
			long now = System.currentTimeMillis();
			if (last != null && now - last < COOLDOWN_MS) {
				long wait = (COOLDOWN_MS - (now - last)) / 1000;
				showWarning(gamePane, x, y, "Please wait for " + wait + " secs to construct this tower again!");
				evt.setDropCompleted(false);
				evt.consume();
				return;
			}

			// Path ve cooldown kontrolunden sonra sıra para kontrolune geliyor.
			// Eger her sey kule insa etmeye uygunsa son olarak kuleyi koyabilmek adına
			// yeterli paramız
			// olup olmadıgına bakılıyor.
			// Eger yeterli para yoksa ekranda paramızın yetersiz olduguna dair uyarı
			// veriliyor.
			if (gameManager.getMoney() < cost) {
				showWarning(gamePane, x, y, "Not enough money!");
				evt.setDropCompleted(false);
				evt.consume();
				return;
			}

			// Butun kontrollerden sonra kule insa ettigimiz kısıma geldik.
			gameManager.addMoney(-cost); // Kule eklendiginde paramızdan kesiliyor. addMoney komutuna kulenin maliyetini
											// eksi olarak verirsek paramız azalır.
			int before = gamePane.getChildren().size();
			Object towerInst = createTowerInstance(type, snappedX, snappedY, gamePane, gameManager, enemies);
			if (towerInst == null) {
				evt.setDropCompleted(false);
				evt.consume();
				return;
			}
			Node newNode = gamePane.getChildren().get(before);
			if (newNode instanceof Group) {
				Group view = (Group) newNode;
				// Kulelere bir iade mantıgı eklemek istedik.
				// Bunun icin en dogru yontem sag tıklamak olarak geldi
				// Cunku tower defence oyunlarında bir kule yerlestirildigi yerden kaldırılamaz
				// normalde.
				// Ancak kullanıcı kazara yanlıs yere koymus olabilir.
				// Buna cozum olması acısından yerlestirilen bir kulenin kaldırılabilmesi icin
				// sag tıklamak yeterli oluyor

				view.setOnMouseClicked(ev -> {
					if (ev.getButton() == MouseButton.SECONDARY) {
						// Ateşi durdur
						Object inst = towerInst;
						if (inst instanceof SingleShotTower)
							((SingleShotTower) inst).stopShooting();
						else if (inst instanceof LaserTower)
							((LaserTower) inst).stopShooting();
						else if (inst instanceof TripleShotTower)
							((TripleShotTower) inst).stopShooting();
						else if (inst instanceof MissileTower)
							((MissileTower) inst).stopShooting();
						// Görünümü kaldır
						gamePane.getChildren().remove(view);
						// Paranın iadesi
						gameManager.addMoney(cost);
						lastRemovalTime.put(type, System.currentTimeMillis());
					}
					// Burada dikkat edilmesi gereken sey, kule kaldırıldıktan sonra cooldown a
					// girecegimiz.
					// Dikkat: Cooldown sadece AYNI KULE TURU TEKRAR İNSA EDİLMEK İSTENİRSE devreye
					// girecek.
					// Yani kullanıcı singleshot kaldırıp lazershot eklemek isterse bir sorun yok.
				});

				success = true;
			}

			evt.setDropCompleted(success);
			evt.consume();
		});
		gamePane.setOnDragExited(evt -> {
			if (dragRangeCircle != null) {
				gamePane.getChildren().remove(dragRangeCircle);
				dragRangeCircle = null;
			}
			evt.consume();
		});
	}

	// Her tower türü için kuleleri oluşturdugumuz yerleri hazırladık.
	// Kulelere dair fieldları burada kullanıyoruz.
	private static Object createTowerInstance(String type, double x, double y, Pane pane, GameManagerImpl mgr,
			List<Enemy> enemies) {
		Object tower = null;
		switch (type) {
		case "single":
			tower = new SingleShotTower(x, y, 100, 1.5, pane, mgr, enemies);
			break;
		case "laser":
			tower = new LaserTower(x, y, 120, 1.0, pane, mgr, enemies);
			break;
		case "triple":
			tower = new TripleShotTower(x, y, 150, 2.0, pane, mgr, enemies);
			break;
		case "missile":
			tower = new MissileTower(x, y, 150, 3.0, pane, mgr, enemies);
			break;
		}
		if (tower != null) {
			activeTowers.add(tower);
		}
		return tower;
	}

	// GridPane bulma
	private static GridPane findGrid(Pane pane) {
		for (Node n : pane.getChildren())
			if (n instanceof GridPane)
				return (GridPane) n;
		return null;
	}

	// Path hucresi olup olmadıgını kontrol etmek adına tasarlanan method.
	// Burada yaptıgımız sey aslında basit bir bantıga dayanıyor.
	// Temelde yapılan kontrol path renginin farklı olmasından kaynaklanıyor.
	// PDF'de de gosterilen path renginde kareler uzerine denk gelinirse, tower'ın
	// yerlestirilmesine izin verilmiyor.
	private static boolean isPathCell(GridPane grid, double x, double y) {
		final int CELL = 50, PAD = 2;
		int col = (int) (x / (CELL + PAD)), row = (int) (y / (CELL + PAD));
		for (Node cell : grid.getChildren()) {
			Integer r = GridPane.getRowIndex(cell), c = GridPane.getColumnIndex(cell);
			if (r != null && c != null && r == row && c == col && cell instanceof Rectangle) {
				Rectangle rect = (Rectangle) cell;
				String style = rect.getStyle();
				if (style != null && style.contains("#F3DEC3"))
					return true;
			}
		}
		return false;
	}

	private static boolean isOutsideMap(GridPane grid, double x, double y) {
		// GridPane'in sınırlarını aldıgımız yer
		Bounds b = grid.localToParent(grid.getBoundsInLocal());
		// Eğer tıklanan nokta bu sınırlar içinde değilse, dısarıdayız
		return !b.contains(x, y);
	}

	// Burası da sık kullandıgımız uyarıları daha duzenli hale getirmek icin
	// yazdıgımız method.
	// Birden fazla yerde uyarı vermemiz gerektigi icin bunları standartize etmek ve
	// tek bir catı aldında toplamak daha kolay ve uyumlu geldi.
	// Zira her bir hata mesajı farklı renkte sekilde olsaydı daha tuhaf olurdu.
	// Ancak bu sayede belirli bir hata mantıgımız oldu: Kırmızı renkli ve ufak bir
	// yazı tipiyle 4 saniye icinde yok olan bir yazı.
	// Dikkat: Oyun hızlı ilerleyen bir yapıya sahip oldugu icin uyarıların ekranda
	// kalma suresini daha kısa yaptım.
	// Bana kalırsa direkt ekranın ust orta kısmında uyarı cıkmalıydı. (Ben oyle
	// tasarlamaya calıstım basta)
	// Ancak oyunun dinamigi buna musade etmedigi icin daha basit bir cozume giderek
	// sadece ufak bir yazı yaptık.

	private static void showWarning(Pane pane, double x, double y, String msg) {
		Text warning = new Text(msg);
		warning.setFill(Color.RED);
		// Kırmızı renk zaten genelde hata ve uyarılara ozdeslestigi icin secildi
		warning.setX(x);
		warning.setY(y);
		pane.getChildren().add(warning);
		FadeTransition ft = new FadeTransition(Duration.seconds(4), warning);
		// 4 saniye secilme sebebi de oyun dinamigi hızlı oldugu icin ekranı fazla
		// mesgul etmemek.
		ft.setFromValue(1.0);
		ft.setToValue(0.0);
		ft.setOnFinished(e -> pane.getChildren().remove(warning));
		ft.play();
	}

	// Oyun bittiğinde veya restart’ta tüm tower’ları kapat
	public static void stopAllTowers() {
		for (Object inst : activeTowers) {
			if (inst instanceof SingleShotTower)
				((SingleShotTower) inst).stopShooting();

			else if (inst instanceof LaserTower)
				((LaserTower) inst).stopShooting();

			else if (inst instanceof TripleShotTower)
				((TripleShotTower) inst).stopShooting();

			else if (inst instanceof MissileTower)
				((MissileTower) inst).stopShooting();
		}
		activeTowers.clear();
	}
}