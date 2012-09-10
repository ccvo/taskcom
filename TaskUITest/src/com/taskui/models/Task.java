package com.taskui.models;

public final class Task {
	public String id;
	public String title;//for example "Locate person on map"
	public String[] keywords;// locate, call, email, send...
	public float matchingScore = 1;

	public Task(String id, String title, String description) {
		this.id = id;
		this.title = title;
		this.keywords = description.toLowerCase().split("\\s+");
	}

	public float getMatchScore(String phrase) {
		float result = 0;
		if (phrase == null || phrase.length() == 0) {
			result = 1;
		} else {
			String[] words = phrase.toLowerCase().split("\\s+");
			for (int i = words.length - 1; i >= 0; i--) {
				for (int j = this.keywords.length - 1; j >= 0; j--) {
					if (this.keywords[j].startsWith(words[i])) {
						result += 1;
						break;
					}
				}
			}
		}
		this.matchingScore = result;
		return result;
	}

	public String toString() {
		return title;
	}

	public int compareTo(Task t2) {
		//return (this.matchingScore == t2.matchingScore ? this.title.compareTo(t2.title) : this.matchingScore > t2.matchingScore ? -1 : 1);
		return (this.matchingScore == t2.matchingScore ? 0 : this.matchingScore > t2.matchingScore ? -1 : 1);
	}
}
