package com.jjurm.twbot.bot;

import net.sourceforge.htmlunit.corejs.javascript.NativeObject;

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Class for holding page and some data with it.
 * 
 * @author JJurM
 */
public class PageData {

	/**
	 * Page that this object belongs to.
	 */
	HtmlPage page;
	
	/**
	 * Javascript variable <tt>game_data</tt>
	 */
	NativeObject game_data;
	
	/**
	 * Basic constructor
	 * 
	 * @param page
	 */
	public PageData(HtmlPage page) {
		this.page = page;
		
		ScriptResult res = page.executeJavaScript("game_data");
		this.game_data = (NativeObject) res.getJavaScriptResult();
	}
	
	public void cleanUp() {
		page.cleanUp();
		//game_data.clear();
	}
	
	/**
	 * @return HtmlPage <tt>page</tt>
	 */
	public HtmlPage getPage() {
		return this.page;
	}
	
	/**
	 * @return NativeObject <tt>game_data</tt>
	 */
	public NativeObject getGameData() {
		return this.game_data;
	}

}
