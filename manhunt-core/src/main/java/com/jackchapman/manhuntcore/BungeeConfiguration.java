package com.jackchapman.manhuntcore;

import java.util.HashMap;

public class BungeeConfiguration {
	private final HashMap<String, Object> configMap;

	public BungeeConfiguration() {
		this.configMap = new HashMap<>();
	}

	public void setValue(String key, Object value) {
		configMap.put(key, value);
	}

	public String getString(String key) {
		return String.valueOf(configMap.get(key));
	}

	public int getInt(String key) {
		return (int) configMap.get(key);
	}
}
