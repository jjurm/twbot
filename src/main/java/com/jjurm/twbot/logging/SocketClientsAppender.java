package com.jjurm.twbot.logging;

import java.io.IOException;
import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.jjurm.twbot.control.SocketListener;
import com.jjurm.twbot.system.Main;

@Plugin(name = "SocketClients", category = "Core", elementType = "appender", printObject = true)
public final class SocketClientsAppender extends AbstractAppender {

	private SocketClientsAppender(String name, Layout<? extends Serializable> layout,
			Filter filter, boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
	}

	@PluginFactory
	public static SocketClientsAppender createAppender(@PluginAttribute("name") String name,
			@PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filters") Filter filter) {

		if (name == null) {
			LOGGER.error("No name provided for SocketClientsAppender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new SocketClientsAppender(name, layout, filter, ignoreExceptions);
	}

	@Override
	public void append(LogEvent event) {
		
		byte[] data = getLayout().toByteArray(event);
		try {
			SocketListener socketListener = Main.getSocketListener();
			if (socketListener != null) {
				socketListener.writeToAll(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
