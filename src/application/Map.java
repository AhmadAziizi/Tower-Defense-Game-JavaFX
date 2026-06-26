package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Map {
	private int width;
	private int height;
	private String line;
	private GridPane map;
	private boolean[][] pathCoordinates;
	private int Cellpadding = 2;
	private String filename;
	private int size = 50;

	public Map(String filename) throws FileNotFoundException, IOException {
		this.filename = filename;
		makeMap(filename);
	}

	private void makeMap(String filename) throws FileNotFoundException, IOException {

		// Her satırın teker okunması için BufferedReader ve dosyanın otomatik
		// kapatılması için try kullandım.
		try (BufferedReader file = new BufferedReader(new FileReader(filename))) {

			// while'da her kontrol sırasında line'a bir sonraki satır atanıyor.
			while ((line = file.readLine()) != null) {

				if (line.startsWith("WIDTH:"))
					this.width = Integer.parseInt(line.substring(6));

				else if (line.startsWith("HEIGHT:")) {
					this.height = Integer.parseInt(line.substring(7));
					pathCoordinates = new boolean[width][height];
				}

				// Yol bilgilerini elde edip 2d array içine ekliyoruz.
				else if (line.matches("\\d+,\\d+")) {
					pathCoordinates[Integer.parseInt(line.substring(0, line.indexOf(",")))][Integer
							.parseInt(line.substring(line.indexOf(",") + 1) + "")] = true;
				}

				else if (line.startsWith("WAVE_DATA:"))
					break;
			}
		}

		catch (FileNotFoundException e) {
			System.out.println("File is not exist.");
		}

		catch (IOException e) {
			System.out.println("IO Exception has occured.");
		}

		map = new GridPane();

		// GridPane'i BorderPane'e eklerken tam ortalanmasını sağlıyor.
		map.setAlignment(Pos.CENTER);

		// Her kare arasındaki boşluklar ayarlanıyor.
		map.setHgap(2);
		map.setVgap(2);

		// Animasyonları sırayla yapmamızı sağlıyor.
		SequentialTransition sT = new SequentialTransition();
		// Başlangıçta 0.1 saniye gecikme
		PauseTransition initialDelay = new PauseTransition(Duration.millis(100));
		sT.getChildren().add(initialDelay);

		for (int i = 0; i < width; i++) {

			ParallelTransition rowTransition = new ParallelTransition();

			for (int j = 0; j < height; j++) {

				// Map'in her bir karesini oluşturuyoruz. Map'in kare sayısına göre karelerin
				// büyüklüğünü ayarlıyoruz.
				Rectangle rct = new Rectangle(size, size);
				rct.setScaleX(0);
				rct.setScaleX(0);

				// EKleyeceğimiz karenin rengi yol olup olamamasına göre belirleniyor.
				if (pathCoordinates[i][j])
					rct.setStyle("-fx-fill: #F3DEC3;");

				else
					rct.setStyle(Math.random() > 0.5 ? "-fx-fill: #FFC44F;" : "-fx-fill: #FFD30F;");

				map.add(rct, j, i);

				// Karelerin opaklık değerini animasyonla değiştirmemizi sağlıyor.
				// ScaleTransition ile animasyon
				ScaleTransition scale = new ScaleTransition(Duration.millis(200), rct);
				scale.setFromX(0.0);
				scale.setFromY(0.0);
				scale.setToX(1.0);
				scale.setToY(1.0);

				rowTransition.getChildren().add(scale);
			}
			// Satır animasyonu öncesi 50ms gecikme
			PauseTransition rowDelay = new PauseTransition(Duration.millis(50));
			sT.getChildren().add(rowDelay);
			sT.getChildren().add(rowTransition);
		}

		// Tüm kareler oluşturulduğunda Animasyonlar başlatılıyor.
		sT.play();
	}

	/**
	 * boolean cinsinden elde ettiğimiz yol koordinatları array içine alıp düşman
	 * sürülerinin doğru yolu takip etmesi sağlanıyor.
	 */
	public List<int[]> getPathCoordinatesList() {

		/*
		 * List bir arayüz, ArrayList bir class olduğu için List instance'ını ArrayList
		 * constructor'ı ile oluşturabiliyoruz.
		 */
		List<int[]> pathList = new ArrayList<>();

		// Sırayla path hücreleri bulunuyor.
		try (BufferedReader file = new BufferedReader(new FileReader(filename))) {
			String line;

			while ((line = file.readLine()) != null) {

				// WAVE_DATA'dan önceki bilgiler bizim için WAVE_DATA'ya geldiğimizde döngü sona
				// erdiliyor.
				if (line.equals("WAVE_DATA:")) {
					break;
				}

				/*
				 * Sırayla her satırdan y ve x koordinatlarını alıyoruz. \\d+ virgülden önce ve
				 * sonra sayı olup olmadığını kontrol ediyor.
				 */
				if (line.matches("\\d+,\\d+")) {
					String[] parts = line.split(",");
					int y = Integer.parseInt(parts[0].trim());
					int x = Integer.parseInt(parts[1].trim());

					// Eğer koordinatlar uyuşuyorsa pathList içine alıyoruz.
					if (pathCoordinates[y][x]) {
						pathList.add(new int[] { x, y });
					}
				}
			}
		} catch (IOException e) {
			System.err.println("IO Exception has occured.");
		}

		return pathList;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public GridPane getMap() {
		return map;
	}

	public boolean[][] getPathCoordinates() {
		return pathCoordinates;
	}

	public int getCellsize() {
		return size;
	}

	public int getCellpadding() {
		return Cellpadding;
	}
}