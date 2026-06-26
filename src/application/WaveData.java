package application;

public class WaveData {
	private int enemies;
	private double interval;
	private int delay;

	public WaveData(int enemies, double interval, int delay) {
		this.enemies = enemies;
		this.interval = interval;
		this.delay = delay;
	}

	public int getEnemies() {
		return enemies;
	}

	public double getInterval() {
		return interval;
	}

	public int getDelay() {
		return delay;
	}

}