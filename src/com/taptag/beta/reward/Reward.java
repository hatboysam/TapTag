package com.taptag.beta.reward;

public class Reward {

	private String title;
	private int progress;
	private int max;

	public Reward(String title, int progress, int max) {
		this.title = title;
		this.max = max;
		this.progress = progress;
	}

	public String getTitle() {
		return title;
	}

	public int getProgress() {
		return progress;
	}

	public int getMax() {
		return max;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void setMax(int max) {
		this.max = max;
	}

}
