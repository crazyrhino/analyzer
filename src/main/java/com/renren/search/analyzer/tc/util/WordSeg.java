package com.renren.search.analyzer.tc.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.util.CloseableThreadLocal;

import com.tencent.research.nlp.SWIGTYPE_p_void;
import com.tencent.research.nlp.TCWordSeg;

public class WordSeg {
	private static final Logger logger = Logger.getLogger(WordSeg.class);
	private static boolean isInit = false;
	private static String Dic_Path = null;
	public static final String SystemPropertyKey = "tc.analyzer.path";
	private static CloseableThreadLocal<SWIGTYPE_p_void> handles = new CloseableThreadLocal<SWIGTYPE_p_void>();

	public static SWIGTYPE_p_void getHandle(int mode) {
		SWIGTYPE_p_void newHandle = null;
		try {
			newHandle = handles.get();
			if (newHandle != null) {
				return newHandle;
			} else {
				newHandle = TCWordSeg.TCCreateSegHandle(mode);
				handles.set(newHandle);
			}
		} catch (NullPointerException npe) {
			if (handles == null) {
				throw new AlreadyClosedException("this Analyzer is closed");
			} else {
				throw npe;
			}
		}
		return newHandle;
	}

	public static void init() {
		if (!isInit) {
			initLib();
			initDic();
			isInit = true;
		}
	}

	public static void destroy() {
		SWIGTYPE_p_void handle;
		try {
			while (handles != null && (handle = handles.get()) != null) {
				TCWordSeg.TCCloseSegHandle(handle);
			}
		} catch (NullPointerException npe) {
			logger.error(npe);
		}
		handles.close();
		TCWordSeg.TCUnInitSeg();
		isInit = false;
	}

	private static void initDic() {
		try {
			TCWordSeg.TCInitSeg(to_cstr_bytes(getDefaultPath() + "/data"));
		} catch (IOException e) {
			logger.error("dictionary init failed.", e);
		}
	}

	private static void initLib() {
		System.load(getDefaultPath() + "/libTCWordSeg.so");
	}

	private static byte[] to_cstr_bytes(String str) throws IOException {
		String s = str + '\0';
		return s.getBytes("GBK");
	}

	public static String getDefaultPath() {
		if (Dic_Path == null) {
			synchronized (WordSeg.class) {
				Dic_Path = System.getProperty(SystemPropertyKey);
				logger.info("look up in "+SystemPropertyKey+" = " + Dic_Path);
				if (SystemPropertyKey == null) {
					URL url = WordSeg.class.getClassLoader().getResource("tc");
					if (url != null) {
						Dic_Path = url.getFile();
						logger.info("look up in classpath=" + Dic_Path);
					} else {
						Dic_Path = System.getProperty("user.dir") + "/tc";
						logger.info("look up in user.dir=" + Dic_Path);
					}

				}
				if (Dic_Path == null) {
					logger.warn("tc path is null");
				} else {
					File defalutPathFile = new File(Dic_Path);
					if (!defalutPathFile.exists()) {
						logger.warn("defalut dic path=" + defalutPathFile
								+ " not exist");
					}
				}
			}
		}
		return Dic_Path;
	}
}
