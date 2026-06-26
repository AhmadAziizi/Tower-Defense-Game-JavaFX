package application;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Wave {
	protected ArrayList<WaveData> waveDataList = new ArrayList<>();
	private String line;
	private int totalEnemies;

	public Wave(String filename) {
		loadWave(filename);
	}

	private void loadWave(String filename) {
		try (BufferedReader file = new BufferedReader(new FileReader(filename))) {
			outerloop: while ((line = file.readLine()) != null) {

				if (line.equals("WAVE_DATA:")) {
					while (true) {
						line = file.readLine();

						if (line == null)
							break outerloop;

						String[] parts = line.split(",");
						int enemyInfo = Integer.parseInt(parts[0].trim());
						double tbe = Double.parseDouble(parts[1].trim());
						int delay = Integer.parseInt(parts[2].trim());

						totalEnemies += enemyInfo;

						waveDataList.add(new WaveData(enemyInfo, tbe, delay));
					}
				}
			}
		}

		catch (FileNotFoundException e) {
			System.out.println("File is not exist.");
		}

		catch (IOException e) {
			System.out.println("IO Exception has occured.");
		}
	}

	public ArrayList<WaveData> getWaveDataList() {
		return waveDataList;
	}

	public int getTotalEnemies() {
		return totalEnemies;
	}
}