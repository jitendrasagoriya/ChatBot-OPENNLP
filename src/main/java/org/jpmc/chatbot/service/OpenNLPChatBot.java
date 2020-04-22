package org.jpmc.chatbot.service;

import opennlp.tools.doccat.*;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.*;
import opennlp.tools.util.model.ModelUtil;
import org.jpmc.chatbot.config.MissingTranscationConfig;
import org.jpmc.chatbot.helper.MissingTranscationHelper;
import org.jpmc.chatbot.model.TransactionStatusModel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Custom chat bot or chat agent for automated chat replies for FAQs. It uses
 * different features of Apache OpenNLP for understanding what user is asking
 * for. NLP is natural language processing.
 * 
 * @author Jitendra Sagoriya
 *
 */
public class OpenNLPChatBot {

	private static Map<String, String> questionAnswer = new HashMap<>();

	private static SentenceDetectorME sentenceDetectorME;
	private static TokenizerME tokenizerME;
	private static POSTaggerME posTaggerME;
	private static LemmatizerME lemmatizerME;
	private DoccatModel model;

	private void loadSentenceDetectorME()  throws FileNotFoundException, IOException {
		try (InputStream modelIn =  getClass()
				.getClassLoader().getResourceAsStream("en-sent.bin")) {
			sentenceDetectorME = new SentenceDetectorME(new SentenceModel(modelIn));
		}
	}

	private void loadTokenizerME() throws FileNotFoundException,IOException {
		try (InputStream modelIn =  getClass()
				.getClassLoader().getResourceAsStream("en-token.bin")) {
			// Initialize tokenizer tool
			tokenizerME = new TokenizerME(new TokenizerModel(modelIn));
		}
	}

	private void loadPOSTaggerME() throws FileNotFoundException,IOException {
		try (InputStream modelIn =  getClass()
				.getClassLoader().getResourceAsStream("en-pos-maxent.bin")) {
			// Initialize POS tagger tool
			posTaggerME = new POSTaggerME(new POSModel(modelIn));
		}
	}

	private void loadLemmatizerME() throws FileNotFoundException,IOException {
		try (InputStream modelIn =  getClass()
				.getClassLoader().getResourceAsStream("en-lemmatizer.bin")) {
			// Tag sentence.
			lemmatizerME = new LemmatizerME(new LemmatizerModel(modelIn));
		}
	}


	/*
	 * Define answers for each given category.
	 */
	static {
		questionAnswer.put("greeting", "Hello, how can I help you?");
		questionAnswer.put("name-inquiry","My name is ALICE. and i am a bot.");
		questionAnswer.put("age-inquiry", "My age is 12 Days. I am still growing.");
		questionAnswer.put("conversation-continue", "What else can I help you with?");
		questionAnswer.put("missing-transaction","What is bank id?");
		questionAnswer.put("bank-continue","What is Branch id?");
		questionAnswer.put("branch-continue","What is account id?");
		questionAnswer.put("account-continue","What is transaction as of date?");
		questionAnswer.put("date-of-transaction","Please wait we are checking.....");
		questionAnswer.put("conversation-complete", "Nice chatting with you. Bye.");

	}


	public OpenNLPChatBot(){
		try {
			loadLemmatizerME();
			loadPOSTaggerME();
			loadSentenceDetectorME();
			loadTokenizerME();


			// Train categorizer model to the training data we created.
			model = trainCategorizerModel();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public static void main(String[] args) throws IOException {

		OpenNLPChatBot chatBot = new OpenNLPChatBot();
		// Take chat inputs from console (user) in a loop.
		Scanner scanner = new Scanner(System.in);
		while (true) {
			// Get chat input from user.
			System.out.println("##### You:");
			String userInput = scanner.nextLine();

			chatBot.chat(userInput);
		}
	}

	public String chat(String userInput) throws IOException {


		String answer = "";
		boolean conversationComplete = false;

		// Break users chat input into sentences using sentence detection.
		String[] sentences = breakSentences(userInput);

		// Loop through sentences.
		for (String sentence : sentences) {

			// Separate words from each sentence using tokenizer.
			String[] tokens = tokenizeSentence(sentence);

			// Tag separated words with POS tags to understand their grammatically structure.
			String[] posTags = detectPOSTags(tokens);

			// Lemmatize each word so that its easy to categorize.
			String[] lemmas = lemmatizeTokens(tokens, posTags);

			// Determine BEST category using lemmatized tokens used a mode that we trained
			// at start.
			String category = detectCategory(model, lemmas);


			/*MissingTranscationHelper.checkResponse(category);
			System.out.println( MissingTranscationConfig.getInstance().isStarted());

			if( MissingTranscationConfig.getInstance().isStarted() ) {
				MissingTranscationHelper.setValues(category,lemmas[lemmas.length-1]);
			}

			System.out.println( "ALl Data Done :"+ TransactionStatusModel.getInstance().checkedAllDataDone() );


			if(!TransactionStatusModel.getInstance().checkedAllDataDone()
					&& MissingTranscationConfig.getInstance().isStarted()) {
				String notDone = MissingTranscationConfig.getInstance().allParameterEntered();
				System.out.println( MissingTranscationHelper.getCategory(notDone));
				category = MissingTranscationHelper.getCategory(notDone);
			}

			if(TransactionStatusModel.getInstance().checkedAllDataDone()) {
				category = "date-of-transaction";
			}*/

			// Get predefined answer from given category & add to answer.
			answer = answer + " " + questionAnswer.get(category);

			// If category conversation-complete, we will end chat conversation.
			if ("conversation-complete".equals(category)) {
				conversationComplete = true;
			}

		}

		// Print answer back to user. If conversation is marked as complete, then end
		// loop & program.
		System.out.println("##### Chat Bot: " + answer);

		return answer;
	}

	/**
	 * Train categorizer model as per the category sample training data we created.
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static DoccatModel trainCategorizerModel() throws FileNotFoundException, IOException {
		// faq-categorizer.txt is a custom training data with categories as per our chat
		// requirements.

		File file = new File(
				OpenNLPChatBot.class.getClassLoader().getResource("faq-categorizer.txt").getFile()
		);

		InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(file);
		ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
		ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

		DoccatFactory factory = new DoccatFactory(new FeatureGenerator[] { new BagOfWordsFeatureGenerator() });

		TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
		params.put(TrainingParameters.CUTOFF_PARAM, 0);

		// Train a model with classifications from above file.
		DoccatModel model = DocumentCategorizerME.train("en", sampleStream, params, factory);
		return model;
	}

	/**
	 * Detect category using given token. Use categorizer feature of Apache OpenNLP.
	 * 
	 * @param model
	 * @param finalTokens
	 * @return
	 * @throws IOException
	 */
	private static String detectCategory(DoccatModel model, String[] finalTokens) throws IOException {

		// Initialize document categorizer tool
		DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);

		// Get best possible category.
		double[] probabilitiesOfOutcomes = myCategorizer.categorize(finalTokens);
		String category = myCategorizer.getBestCategory(probabilitiesOfOutcomes);
		System.out.println("Category: " + category);

		return category;

	}

	/**
	 * Break data into sentences using sentence detection feature of Apache OpenNLP.
	 * 
	 * @param data
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static String[] breakSentences(String data) {
		String[] sentences = sentenceDetectorME.sentDetect(data);
		System.out.println("Sentence Detection: " + Arrays.stream(sentences).collect(Collectors.joining(" | ")));
		return sentences;
	}

	/**
	 * Break sentence into words & punctuation marks using tokenizer feature of
	 * Apache OpenNLP.
	 * 
	 * @param sentence
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static String[] tokenizeSentence(String sentence) {
		// Tokenize sentence.
		String[] tokens = tokenizerME.tokenize(sentence);
		System.out.println("Tokenizer : " + Arrays.stream(tokens).collect(Collectors.joining(" | ")));
		return tokens;
	}

	/**
	 * Find part-of-speech or POS tags of all tokens using POS tagger feature of
	 * Apache OpenNLP.
	 * 
	 * @param tokens
	 * @return
	 * @throws IOException
	 */
	private static String[] detectPOSTags(String[] tokens){
		// Tag sentence.
		String[] posTokens = posTaggerME.tag(tokens);
		System.out.println("POS Tags : " + Arrays.stream(posTokens).collect(Collectors.joining(" | ")));
		return posTokens;
	}

	/**
	 * Find lemma of tokens using lemmatizer feature of Apache OpenNLP.
	 * 
	 * @param tokens
	 * @param posTags
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	private static String[] lemmatizeTokens(String[] tokens, String[] posTags){
		String[] lemmaTokens = lemmatizerME.lemmatize(tokens, posTags);
		System.out.println("Lemmatizer : " + Arrays.stream(lemmaTokens).collect(Collectors.joining(" | ")));
		return lemmaTokens;
	}



}
