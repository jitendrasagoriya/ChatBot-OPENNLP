package org.jpmc.chatbot.config;

import java.util.HashMap;
import java.util.Map;


/**
 * Custom chat bot or chat agent for automated chat replies for FAQs. It uses
 * different features of Apache OpenNLP for understanding what user is asking
 * for. NLP is natural language processing.
 *
 * @author Jitendra Sagoriya
 *
 */
public class MissingTranscationConfig {

    private boolean isStarted;
    private boolean isFinished;
    private boolean isAllInputParamDone;

    private Map<String, Boolean> inputDoneMap = new HashMap<>();

    private static MissingTranscationConfig SINGLE_INSTANCE = null;

    private MissingTranscationConfig() {
        this.isStarted = false;
        this.isFinished = false;
        this.isAllInputParamDone = false;

        inputDoneMap.put("bank", Boolean.FALSE);
        inputDoneMap.put("branch", Boolean.FALSE);
        inputDoneMap.put("account", Boolean.FALSE);
        inputDoneMap.put("date", Boolean.FALSE);


    }

    public String allParameterEntered() {
        for (Map.Entry<String, Boolean> entry : inputDoneMap.entrySet())  {
            if(!entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void setInputParamMapValues(String key) {
        if(inputDoneMap.containsKey(key)) {
            inputDoneMap.put(key, Boolean.TRUE);
        }
    }

    public static MissingTranscationConfig getInstance() {
        if (SINGLE_INSTANCE == null) {
            synchronized(MissingTranscationConfig.class) {
                SINGLE_INSTANCE = new MissingTranscationConfig();
            }
        }
        return SINGLE_INSTANCE;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public boolean isAllInputParamDone() {
        return isAllInputParamDone;
    }

    public void setAllInputParamDone(boolean allInputParamDone) {
        isAllInputParamDone = allInputParamDone;
    }
}
