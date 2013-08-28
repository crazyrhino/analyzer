package com.renren.search.util;

public class WordSeg {
	private final int OperationTypeNone = 0;
	private final int OperationTypeTopMark = 1;
	private final int OperationTypeSecondaryMark = 2;
	private final int OutputFormatStdPku = 0;
	private final int OutputFormatStd973 = 1;
	private final int OutputFormatXML = 2;
	public boolean isInit = false;
	private long WSHandle;
	private native long initNative(long WSDict);
	private native String SegStringNative(long WSHandle, String line, int operationType, int outputFormat);
	private native void destroy(long WSHandle);
	
	public void init(WordSegDict WSDict){
		if (!isInit){
			WSHandle = initNative(WSDict.pDict);
			isInit = true;
		}
	}
	public String SegString(String line){
		return SegStringNative(WSHandle, line, OperationTypeSecondaryMark, OutputFormatStdPku);
	}
	public WordSeg(){
		WSHandle = 0;
	}
	public void destroy(){
		if (WSHandle != 0) destroy(WSHandle);
		isInit = false;
	}
}

