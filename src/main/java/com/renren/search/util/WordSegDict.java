package com.renren.search.util;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class WordSegDict {
	public native long initNative(String path);
	public native void destroy(long dictHandle);
	
	private static final Logger logger = Logger.getLogger(WordSegDict.class);
	private static final ConcurrentHashMap<String, WordSegDict> dics = new ConcurrentHashMap<String, WordSegDict>();
	private static String defaultPath = null;
	public static final String SystemPropertyKey="ares.analyzer.path";
	
	public static WordSegDict getInstance(){
		String path = getDefaultPath();
		return getInstance(path);
	}
	
	public static WordSegDict getInstance(String path){
		WordSegDict dic  = dics.get(path);
		if(dic==null){
			WordSegDict newDic = new  WordSegDict(path);
			dic = dics.putIfAbsent(path, newDic);
			if(dic==null){
				dic = newDic;
			}
		}
		return dic;
	}
	
	private WordSegDict(String path){
		this.load(path);
		this.init(path+File.separator+"data"+File.separator);
	}
	
	public static String getDefaultPath(){
		if(defaultPath == null) {
			synchronized(WordSegDict.class){
				defaultPath = System.getProperty(SystemPropertyKey);
				logger.info("look up in ares.analyzer.path="+defaultPath);
				if(defaultPath == null) {
					URL url = WordSegDict.class.getClassLoader().getResource("data");
					if(url != null) {
						defaultPath = url.getFile();
						logger.info("look up in classpath="+defaultPath);
					} else {
						defaultPath = System.getProperty("user.dir")+"/data";
						logger.info("look up in user.dir="+defaultPath);
					}
				
				}
				if(defaultPath==null){
					logger.warn("dic path is null");
				}else{
					File defalutPathFile = new File(defaultPath);
					if(!defalutPathFile.exists()) {
						logger.warn("defalut dic path="+defalutPathFile+" not exist");
					}
				}
			}
		}
		return defaultPath;
	}
	
	public long pDict;
	public static boolean isLoad = false;
	public void load(String path){
		if (!isLoad){
			synchronized(WordSegDict.class){
				if(!isLoad){
					logger.info("load library from path:"+path);
					System.load(path + "/libWordSegJni.so");
					isLoad = true;
				}
			}
		}
	}
	public void init(String path){
		pDict = initNative(path);
	}
	public void destroy(){
		if (pDict != 0) destroy(pDict);
		isLoad = false;
	}
}
