package com.renren.search.analyzer.standard.lucene;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;

public final class StandardTokenizer extends Tokenizer {
	private final StandardTokenizerImpl scanner;

	public static final int ALPHANUM = 0;
	public static final int APOSTROPHE = 1;
	public static final int ACRONYM = 2;
	public static final int COMPANY = 3;
	public static final int EMAIL = 4;
	public static final int HOST = 5;
	public static final int NUM = 6;
	public static final int CJ = 7;

	public static final int ACRONYM_DEP = 8;

	public static final String[] TOKEN_TYPES = new String[] { "<ALPHANUM>",
			"<APOSTROPHE>", "<ACRONYM>", "<COMPANY>", "<EMAIL>", "<HOST>",
			"<NUM>", "<CJ>", "<ACRONYM_DEP>" };

	private boolean replaceInvalidAcronym;

	private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

	public void setMaxTokenLength(int length) {
		this.maxTokenLength = length;
	}

	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	public StandardTokenizer(Version matchVersion, Reader input) {
		super();
		this.scanner = new StandardTokenizerImpl(input);
		init(input, matchVersion);
	}

	public StandardTokenizer(Version matchVersion, AttributeSource source,
			Reader input) {
		super(source);
		this.scanner = new StandardTokenizerImpl(input);
		init(input, matchVersion);
	}

	public StandardTokenizer(Version matchVersion, AttributeFactory factory,
			Reader input) {
		super(factory);
		this.scanner = new StandardTokenizerImpl(input);
		init(input, matchVersion);
	}

	private void init(Reader input, Version matchVersion) {
		if (matchVersion.onOrAfter(Version.LUCENE_24)) {
			replaceInvalidAcronym = true;
		} else {
			replaceInvalidAcronym = false;
		}
		this.input = input;
		termAtt = addAttribute(TermAttribute.class);
		offsetAtt = addAttribute(OffsetAttribute.class);
		posIncrAtt = addAttribute(PositionIncrementAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
	}

	private TermAttribute termAtt;
	private OffsetAttribute offsetAtt;
	private PositionIncrementAttribute posIncrAtt;
	private TypeAttribute typeAtt;

	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();
		int posIncr = 1;

		while (true) {
			int tokenType = scanner.getNextToken();

			if (tokenType == StandardTokenizerImpl.YYEOF) {
				return false;
			}

			if (scanner.yylength() <= maxTokenLength) {
				posIncrAtt.setPositionIncrement(posIncr);
				scanner.getText(termAtt);
				final int start = scanner.yychar();
				offsetAtt.setOffset(correctOffset(start), correctOffset(start
						+ termAtt.termLength()));
				if (tokenType == StandardTokenizerImpl.ACRONYM_DEP) {
					if (replaceInvalidAcronym) {
						typeAtt.setType(StandardTokenizerImpl.TOKEN_TYPES[StandardTokenizerImpl.HOST]);
						termAtt.setTermLength(termAtt.termLength() - 1);
					} else {
						typeAtt.setType(StandardTokenizerImpl.TOKEN_TYPES[StandardTokenizerImpl.ACRONYM]);
					}
				} else {
					typeAtt.setType(StandardTokenizerImpl.TOKEN_TYPES[tokenType]);
				}
				return true;
			} else
				posIncr++;
		}
	}

	@Override
	public final void end() {
		int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		scanner.yyreset(input);
	}

	@Override
	public void reset(Reader reader) throws IOException {
		super.reset(reader);
		reset();
	}

	public boolean isReplaceInvalidAcronym() {
		return replaceInvalidAcronym;
	}

	public void setReplaceInvalidAcronym(boolean replaceInvalidAcronym) {
		this.replaceInvalidAcronym = replaceInvalidAcronym;
	}
}
