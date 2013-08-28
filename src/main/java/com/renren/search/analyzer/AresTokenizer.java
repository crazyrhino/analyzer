package com.renren.search.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

import com.renren.search.util.WordSeg;

public class AresTokenizer extends Tokenizer {
	private static final Logger logger = Logger.getLogger(AresTokenizer.class);

	public native static int init(String conf);

	public native static String SegString(String name);

	private WordSeg wordseg;

	private TermAttribute termAttr;
	private OffsetAttribute offsetAttr;
	private TypeAttribute typeAttr;
	private int index = 0, len = 0, offset = 0, origLen = 0;
	private String[] resultArray;
	private StringBuffer original;

	private static class HTMLTag {
		private String startTag;
		private String[] endTags;

		HTMLTag(String startTag, String[] endTags) {
			this.startTag = startTag;
			this.endTags = endTags;
		}
	}

	private static HTMLTag[] HTML_TAGS = {
			new HTMLTag("<a", new String[] { ">" }),
			new HTMLTag("<img", new String[] { ">" }),
			new HTMLTag("<", new String[] { "/a>", "/img>" }) };

	public AresTokenizer(Reader in) {
		super(in);
		init();
	}

	public AresTokenizer(Reader in, WordSeg wordseg) {
		super(in);
		init();
		this.wordseg = wordseg;
	}

	public AresTokenizer(AttributeSource source, Reader in) {
		super(source, in);
		init();
	}

	public AresTokenizer(AttributeFactory factory, Reader in) {
		super(factory, in);
		init();

	}

	public void init() {
		this.termAttr = (TermAttribute) addAttribute(TermAttribute.class);
		this.offsetAttr = (OffsetAttribute) addAttribute(OffsetAttribute.class);
		this.typeAttr = (TypeAttribute) addAttribute(TypeAttribute.class);
	}

	public void seg() {
		try {
			char[] buffer = new char[1024];
			original = new StringBuffer();// 存放每一行内容
			int len = 0;
			while ((len = input.read(buffer)) > -1) {
				original.append(buffer, 0, len);
			}
			// 全角空格以及回车换行等空白字符的处理
			String sentence = original.toString().replaceAll("\\s", " ")
					.replaceAll("　", " ");
			original = new StringBuffer(sentence);
			// 空白字符处理以及大写转换
			String result = wordseg.SegString(sentence.trim().toUpperCase());
			// System.out.println("调用词典之后的字符:$$" + result + "$$");
			// logger.debug("调用词典之后的字符:$$" + result + "$$");
			result = result.replaceAll("\\s+", " ");
			resultArray = result.split(" ");
		} catch (Exception e) {
			logger.error("seg error ", e);
		}
	}

	// 用于得到下一个Token
	// 首先得到一个词，然后将该词放在termAttr中，其次将该词开始和结束处的偏移量放在offsetAttr中
	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		return next();
	}

	@Override
	public void end() throws IOException {
		final int finalOffset = offset;
		this.offsetAttr.setOffset(finalOffset, finalOffset);
	}

	// 使得此TokenStrean可以重新开始返回各个分词
	@Override
	public void reset() throws IOException {
		super.reset();
		offset = index = 0;
		len = origLen = 0;
	}

	@Override
	public void reset(Reader input) throws IOException {
		super.reset(input);
		reset();
	}

	private boolean next() {

		if (index == 0) {
			seg();
			index = 0;
			offset = 0;
			len = resultArray.length;
			origLen = original.length();
		}
		if (index > len - 1) { // 超过分词结果时退出
			index = 0;
			offset = 0;
			len = 0;
			origLen = 0;
			return false;
		}
		String word = resultArray[index];// 得到该索引的词

		// 这段逻辑其实可以删除，因为在前面有保证检查了
		if (word == null || original == null)
			return false;

		HTMLTag tag = null;
		// 检查word中是否含有HTML的标签
		for (int i = 0; i < HTML_TAGS.length; i++) {
			tag = HTML_TAGS[i];
			/**
			 * 判断word中是否包含HTML开始标签。index不能忽略大小写，以后需要对其进行改进。
			 */
			boolean isStartTag = isStartTag(tag.startTag, index);

			if (isStartTag) {
				for (int j = 0; j < tag.endTags.length; j++) {
					// 从原始文本中判断是否包含HTML结束标签
					int originalIndex = original
							.indexOf(tag.endTags[j], offset);
					if (originalIndex > -1) {// 如果包含HTML结束标签

						repositionOffset(tag.startTag);
						// 截取HTML标签
						String filted = original.substring(offset,
								originalIndex + tag.endTags[j].length());

						/**
						 * 1.对截取的文本进行分析，用以确定下次应该跳转到resultArray的下标。
						 * 关于确定下标的问题，理论上应该调用相关的分词程序，确定截取的字符串被分成的String数组。
						 * 
						 * 2.目前这种方式会导致反复调用wordseg，以后需要优化。
						 * 
						 * 3.filted不再调用toUpperCase()方法。这里用wordseg只是为了确定index。
						 */

						int step = wordseg.SegString(filted)
								.replaceAll("\\s+", " ").split(" ").length;

						return dealToken(filted, "html", index + step);

					}

				}
			}
		}

		int wordLen = 0;
		int biasIndex = word.lastIndexOf("/");
		if (biasIndex != -1) {
			termAttr.setTermBuffer(word.substring(0, biasIndex)); // 设置termAttr
			typeAttr.setType(word.substring(biasIndex + 1, word.length())); // 词的长度
			wordLen = word.substring(0, biasIndex).length();
		} else {
			termAttr.setTermBuffer(word);
			typeAttr.setType("xx");
			wordLen = word.length();
		}
		/**
		 * 如果是从word的开头第0个位置截取，那么需要考虑空格 否则offset记录的不是绝对位置，而会相差去除过相应空格数个位置。
		 */
		repositionOffset(getOriginalText(word));
		offsetAttr.setOffset(offset, offset + wordLen);// 设置位移
		offset += wordLen;
		index++;

		return true;

	}

	private boolean isStartTag(String tag, int start) {
		int i = start;
		int wordIndex = 0;
		int len = 0;
		int tagLen = tag.length();
		String orgTxt;
		while (wordIndex < tagLen) {
			orgTxt = getOriginalText(resultArray[i]);
			len = orgTxt.length();
			if ((wordIndex + len) > tagLen)
				return false;
			else {
				if (tag.substring(wordIndex, wordIndex + len).equalsIgnoreCase(
						orgTxt)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	// 需要实现，具体参考isStartTag
	private boolean isEndTag() {
		return false;
	}

	private boolean dealToken(String word, String wordType, int index) {
		termAttr.setTermBuffer(word);
		typeAttr.setType(wordType);
		/**
		 * 如果是从word的开头第0个位置截取，那么需要考虑空格 否则offset记录的不是绝对位置，而会相差去除过相应空格数个位置。
		 */
		repositionOffset(word);
		offsetAttr.setOffset(offset, offset + word.length());
		offset += word.length();
		// 设置位置信息，下次应该从有HTML标签的位置开始处理
		this.index = index;
		return true;
	}

	// 重新定位offset
	private void repositionOffset(String word) {
		/**
		 * 1.这种实现方式的优点是能够应对过滤的字符是各种类型的，缺点是不是最高效的
		 * ，另外，如果对字符做了转义处理（比如大小写转换）的话，会识别不了。
		 * 2.考虑到我们实现过程中只会过滤空格，因此可以采用更高效的方式。实现可参考如下： while
		 * (Character.isWhitespace(original.charAt(offset))) { offset++; }
		 */
		// offset = original.indexOf(word, offset);
		while (offset < origLen
				&& Character.isWhitespace(original.charAt(offset))) {
			offset++;
		}

	}

	private String getOriginalText(final String word) {

		if (word == null)
			return null;
		String wd;
		int ld = word.lastIndexOf("/");
		if (ld > -1) {
			wd = word.substring(0, ld);
		} else {
			wd = word;
		}

		return wd;

	}

}
