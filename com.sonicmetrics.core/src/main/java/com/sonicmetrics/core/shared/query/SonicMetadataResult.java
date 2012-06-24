package com.sonicmetrics.core.shared.query;

import java.io.Serializable;

import org.xydra.annotations.RunsInGWT;


/**
 * @author xamde
 * 
 */
@RunsInGWT(true)
public class SonicMetadataResult implements Serializable {
	
	private static final long serialVersionUID = 2348096707115482457L;
	
	public final PropertyMetadataResult<String> subject = new PropertyMetadataResult<String>(300);
	
	public final PropertyMetadataResult<String> category = new PropertyMetadataResult<String>(200);
	
	public final PropertyMetadataResult<String> action = new PropertyMetadataResult<String>(50);
	
	public final PropertyMetadataResult<String> label = new PropertyMetadataResult<String>(100);
	
	public final PropertyMetadataResult<String> source = new PropertyMetadataResult<String>(300);
	
}
