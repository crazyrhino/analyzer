package com.renren.search.analyzer.standard.lucene;

import org.apache.lucene.analysis.*;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

/**
 * 本类是从lucene拷贝过来的，只对public TokenStream tokenStream(String fieldName, Reader
 * reader) 和public TokenStream tokenStream(String fieldName, Reader
 * reader)方法进行了修改
 * 
 * @author chunfei.yang
 * 
 */
public class StandardAnalyzer extends Analyzer {
	private Set<?> stopSet;

	private final boolean replaceInvalidAcronym, enableStopPositionIncrements;

	public static final Set<?> STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	private final Version matchVersion;

	public StandardAnalyzer(Version matchVersion) {
		this(matchVersion, STOP_WORDS_SET);
	}

	public StandardAnalyzer(Version matchVersion, Set<?> stopWords) {
		stopSet = stopWords;
		setOverridesTokenStreamMethod(StandardAnalyzer.class);
		enableStopPositionIncrements = StopFilter
				.getEnablePositionIncrementsVersionDefault(matchVersion);
		replaceInvalidAcronym = matchVersion.onOrAfter(Version.LUCENE_24);
		this.matchVersion = matchVersion;
	}

	public StandardAnalyzer(Version matchVersion, File stopwords)
			throws IOException {
		this(matchVersion, WordlistLoader.getWordSet(stopwords));
	}

	public StandardAnalyzer(Version matchVersion, Reader stopwords)
			throws IOException {
		this(matchVersion, WordlistLoader.getWordSet(stopwords));
	}

	/**
	 * 将lucene的停用词（StopFilter）和词干分析器（StandardFilter）禁用
	 */
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		StandardTokenizer tokenStream = new StandardTokenizer(matchVersion,
				reader);
		tokenStream.setMaxTokenLength(maxTokenLength);
		TokenStream result = new LowerCaseFilter(tokenStream);
		return result;
	}

	private static final class SavedStreams {
		StandardTokenizer tokenStream;
		TokenStream filteredTokenStream;
	}

	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	public void setMaxTokenLength(int length) {
		maxTokenLength = length;
	}

	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	/**
	 * 将lucene的停用词（StopFilter）和词干分析器（StandardFilter）禁用
	 */
	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader reader)
			throws IOException {
		if (overridesTokenStreamMethod) {
			return tokenStream(fieldName, reader);
		}
		SavedStreams streams = (SavedStreams) getPreviousTokenStream();
		if (streams == null) {
			streams = new SavedStreams();
			setPreviousTokenStream(streams);
			streams.tokenStream = new StandardTokenizer(matchVersion, reader);
			streams.filteredTokenStream = new LowerCaseFilter(
					streams.tokenStream);
		} else {
			streams.tokenStream.reset(reader);
		}
		streams.tokenStream.setMaxTokenLength(maxTokenLength);

		streams.tokenStream.setReplaceInvalidAcronym(replaceInvalidAcronym);

		return streams.filteredTokenStream;
	}
}
