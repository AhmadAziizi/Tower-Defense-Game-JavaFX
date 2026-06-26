package application;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class TowerViewFactory {

	/**
	 * Single-Shot Tower görünüşünü oluşturur.
	 */
	public static Group createSingleShotTowerView(double size) {
		// Ana gövde
		Rectangle body = new Rectangle(size, size);
		body.setFill(Color.BURLYWOOD);

		// Kapı
		double doorW = size * 0.2;
		double doorH = size * 0.33;
		double doorX = (size - doorW) / 2.0;
		Rectangle door = new Rectangle(doorW, doorH);
		door.setFill(Color.SADDLEBROWN);
		door.setX(doorX);
		door.setY(size - doorH);

		// Tepe süsleri (battlements) – sadece sol ve sağ
		double bSize = size * 0.2;
		Rectangle battlementLeft = new Rectangle(bSize, bSize);
		battlementLeft.setFill(Color.SADDLEBROWN);
		battlementLeft.setX(0);
		battlementLeft.setY(0);

		Rectangle battlementRight = new Rectangle(bSize, bSize);
		battlementRight.setFill(Color.SADDLEBROWN);
		battlementRight.setX(size - bSize);
		battlementRight.setY(0);

		// Pencereler (sol ve sağ, gray renkte)
		double wW = size * 0.15;
		double wH = size * 0.25;
		double windowY = size * 0.3;

		Rectangle windowLeft = new Rectangle(wW, wH);
		windowLeft.setFill(Color.GRAY);
		windowLeft.setX(bSize);
		windowLeft.setY(windowY);

		Rectangle windowRight = new Rectangle(wW, wH);
		windowRight.setFill(Color.GRAY);
		windowRight.setX((size - bSize) - wW);
		windowRight.setY(windowY);

		return new Group(body, door, battlementLeft, battlementRight, windowLeft, windowRight);
	}

	/**
	 * Laser-Shot Tower görünüşünü oluşturdugumuz yer.
	 * 2 tane Single-Shot Tower'ın ust uste konulmasıyla olusuyor tower goruntusu.
	 */
	public static Group createLaserTowerView(double size) {
		double smallSize = size / 3.0;

		Group base = createSingleShotTowerView(size);
		base.setTranslateY(smallSize);

		Group top = createSingleShotTowerView(smallSize);
		top.setTranslateX((size - smallSize) / 2.0);
		top.setTranslateY(0);

		return new Group(base, top);
	}

	/**
	 * Triple-Shot Tower görünüşünü oluşturdugumuz bölüm. Uzun dikdörtgen gövde, üstte süsler,
	 * ortada 4 pencere ve alt ortada kapı bulunuyor.
	 * Hepsi Rectangle class aracılıgıyla hazırlandı.
	 */
	public static Group createTripleShotTowerView(double size) {
		double bodyW = size;
		double bodyH = size * 2;

		//Gövdesi
		Rectangle body = new Rectangle(bodyW, bodyH);
		body.setFill(Color.BURLYWOOD);

		//Kapı (alt ortada)
		double doorW = bodyW * 0.2;
		double doorH = bodyH * 0.15;
		double doorX = (bodyW - doorW) / 2.0;
		double doorY = bodyH - doorH;
		Rectangle door = new Rectangle(doorW, doorH);
		door.setFill(Color.SADDLEBROWN);
		door.setX(doorX);
		door.setY(doorY);

		//Tepe süsleri (battlements) – sol, orta, sağ
		double bSize = size * 0.2;
		double leftX = 0;
		double centerX = (bodyW - bSize) / 2.0;
		double rightX = bodyW - bSize;

		Rectangle battlementLeft = new Rectangle(bSize, bSize);
		battlementLeft.setFill(Color.SADDLEBROWN);
		battlementLeft.setX(leftX);
		battlementLeft.setY(0);

		Rectangle battlementCenter = new Rectangle(bSize, bSize);
		battlementCenter.setFill(Color.SADDLEBROWN);
		battlementCenter.setX(centerX);
		battlementCenter.setY(0);

		Rectangle battlementRight = new Rectangle(bSize, bSize);
		battlementRight.setFill(Color.SADDLEBROWN);
		battlementRight.setX(rightX);
		battlementRight.setY(0);

		//Beyaz dekorasyon kareleri battlement’ler arası
		double t1X = (leftX + centerX) / 2.0;
		double t2X = (centerX + rightX) / 2.0;
		Rectangle decor1 = new Rectangle(bSize, bSize);
		decor1.setFill(Color.WHITE);
		decor1.setX(t1X);
		decor1.setY(0);
		Rectangle decor2 = new Rectangle(bSize, bSize);
		decor2.setFill(Color.WHITE);
		decor2.setX(t2X);
		decor2.setY(0);

		//Ortada 4 pencere var (iki satır, iki sütun)
		double wW = bodyW * 0.2;
		double wH = bodyH * 0.15;
		double row1Y = bodyH * 0.3;
		double row2Y = bodyH * 0.6;
		double col1X = bodyW * 0.2;
		double col2X = bodyW * 0.6;

		Rectangle win1 = new Rectangle(wW, wH);
		win1.setFill(Color.GRAY);
		win1.setX(col1X);
		win1.setY(row1Y);

		Rectangle win2 = new Rectangle(wW, wH);
		win2.setFill(Color.GRAY);
		win2.setX(col2X);
		win2.setY(row1Y);

		Rectangle win3 = new Rectangle(wW, wH);
		win3.setFill(Color.GRAY);
		win3.setX(col1X);
		win3.setY(row2Y);

		Rectangle win4 = new Rectangle(wW, wH);
		win4.setFill(Color.GRAY);
		win4.setX(col2X);
		win4.setY(row2Y);

		return new Group(body, door, battlementLeft, battlementCenter, battlementRight, decor1, decor2, win1, win2,
				win3, win4);
	}

	/**
	 * Dördüncü kule görünüşünü oluşturdugumuz yer. Solda TripleShot, ortada SingleShot,
	 * sağda TripleShot. Hepsi aynı zemin üzerine hizalanır.
	 */
	public static Group createMissileLauncherTowerView(double size) {
		// 1) Üç kuleyi oluştur
		size *=0.85;
		Group leftTriple = createTripleShotTowerView(size);
		Group centerSingle = createSingleShotTowerView(size);
		Group rightTriple = createTripleShotTowerView(size);

		// 2) Y hizalaması: single tower'ı one layer lower
		centerSingle.setTranslateY(size);

		// 3) X pozisyonları için boşluk
		leftTriple.setTranslateX(0);
		centerSingle.setTranslateX(size);
		rightTriple.setTranslateX(size * 2);

		// 4) Son grubu return et
		return new Group(leftTriple, centerSingle, rightTriple);
	}
}