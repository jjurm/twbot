package com.jjurm.twbot.tribalwars;

import org.apache.commons.configuration.Configuration;

public class World {

	String name;
	String host;
	String player;
	String password;
	
	/**
	 * @param config Tribal wars configuration
	 */
	public World(Configuration config) {
		this.name = config.getString("name");
		this.host = config.getString("host");
		this.player = config.getString("player");
		this.password = config.getString("password");
	}
	
	public String getName() {
		return this.name;
	}

	public String getHost() {
		return this.host;
	}

	public String getPlayer() {
		return this.player;
	}

	public String getPassword() {
		return this.password;
	}

}
