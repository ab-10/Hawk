/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package indexation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.DefinitionTokenizerImpl;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeFactory;

import static org.apache.lucene.analysis.standard.StandardTokenizer.MAX_TOKEN_LENGTH_LIMIT;

/**
 * A modification of Lucene's StandardTokenizer,
 * that tokenizes based on space and underscore characters
 */


public class DefinitionTokenizer extends Tokenizer {
    private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;
    // stores whether the reader has been read
    private boolean notRead;

    /* Lucene uses attributes to store information about a single token. For
     * this tokenizer, the only attribute that we are going to use is the
     * CharTermAttribute, which can store the text for the token that is
     * generated. Other types of attributes exist (see interfaces and classes
     * derived from org.apache.lucene.util.Attribute); we will use some of
     * these other attributes when we build our custom token filter. It is
     * important that you register attributes, whatever their type, using the
     * addAttribute() function.
     */
    protected CharTermAttribute charTermAttribute =
            addAttribute(CharTermAttribute.class);

    public DefinitionTokenizer() {
        System.out.println("DefinitionTokenizer() called");
    }

    /**
     * Reads the <code>input</code> if it hasn't been read before and find the next token.
     * <p>
     * Creates a token from <code>position</code> up to the closest underscore or space character
     * if neither of those characters are remaining creates a token up to the end of the <code>input</code>.
     * </p>
     *
     * @return <code>true</code> iff the token was successfully incremented <code>false</code> otherwise.
     * @throws IOException
     */
    @Override
    public boolean incrementToken() throws IOException {
        if (notRead) {
            int numChars;
            char[] buffer = new char[1024];
            StringBuilder stringBuilder = new StringBuilder();
            try {
                while ((numChars =
                        input.read(buffer, 0, buffer.length)) != -1) {
                    stringBuilder.append(buffer, 0, numChars);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            notRead = false;
            this.stringToTokenize = stringBuilder.toString();
        }

        System.out.println("incrementToken() called");
        this.charTermAttribute.setEmpty();
        clearAttributes();

        int nextSpaceIndex = this.stringToTokenize.indexOf(' ', this.position);
        int nextUnderscoreIndex = this.stringToTokenize.indexOf('_', this.position);
        /* finds the closest symbol at which to tokenize
           by finding the smallest index which is not -1
           sets nextIndex to -1 in case nextSpaceIndex and nextUnderscoreIndex are equal,
           which can only occur if both of them are -1
         */
        int nextIndex = -1;
        if(nextSpaceIndex < nextUnderscoreIndex){
           nextIndex = nextSpaceIndex != -1 ? nextSpaceIndex : nextUnderscoreIndex;
        }
        if(nextUnderscoreIndex < nextSpaceIndex){
            nextIndex = nextUnderscoreIndex != -1 ? nextUnderscoreIndex : nextSpaceIndex;
        }


        // Execute this block if a plus symbol was found. Save the token
        // and the position to start at when incrementToken() is next
        // called.
        if (nextIndex != -1) {
            String nextToken = this.stringToTokenize.substring(
                    this.position, nextIndex);
            this.position = nextIndex + 1;
            this.charTermAttribute.append(nextToken);
            System.out.println(nextToken);
            return true;
        }

        // Execute this block if no more + signs are found, but there is
        // still some text remaining in the string. For example, this saves
        // “text" in "This+is++some+text".
        else if (this.position < this.stringToTokenize.length()) {
            String nextToken =
                    this.stringToTokenize.substring(this.position);
            this.charTermAttribute.append(nextToken);
            this.position = this.stringToTokenize.length();
            System.out.println(nextToken);
            return true;
        }

        // Execute this block if no more tokens exist in the string.
        else {
            return false;
        }
    }


    public void setMaxTokenLength(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("maxTokenLength must be greater than zero");
        } else if (length > MAX_TOKEN_LENGTH_LIMIT) {
            throw new IllegalArgumentException("maxTokenLength may not exceed " + MAX_TOKEN_LENGTH_LIMIT);
        }
        if (length != maxTokenLength) {
            maxTokenLength = length;
        }
    }

    /**
     * Sets the <code>tokenizer</code> to state where it's ready to create new tokens.
     *
     * <p>
     * Although unread <code>input</code> hasn't been given, <code>notRead</code> is set to <code>true</code>
     * because this is supposed to be called before giving new <code>input</code>.
     * </p>
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        notRead = true;
        this.position = 0;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }


    /* This object stores the string that we are turning into tokens. We will
     * process its content as we call the incrementToken() function.
     */
    protected String stringToTokenize;

    /* This stores the current position in this.stringToTokenize. We will
     * increment its value as we call the incrementToken() function.
     */
    protected int position = 0;
}
