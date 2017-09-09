/**
 * 
 */
package com.test;

import java.math.BigInteger;

/**
 * @author Vidya
 *
 */
public class ResultObj {
	private String videoId;
	private String title;
	private BigInteger viewCount;
	/**
	 * @return the videoId
	 */
	public String getVideoId() {
		return videoId;
	}
	/**
	 * @param videoId the videoId to set
	 */
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the viewCount
	 */
	public BigInteger getViewCount() {
		return viewCount;
	}
	/**
	 * @param viewCount the viewCount to set
	 */
	public void setViewCount(BigInteger viewCount) {
		this.viewCount = viewCount;
	}
	
	
}
