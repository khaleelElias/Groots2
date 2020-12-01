package com.example.groots;

public class MessageReader {

    /**
     * messageParser:
     * Messages may contain noise. Message is split into smaller
     * chunks who is further split and then searched for usable data.
     *
     * */

    protected static String[] messageParser(String message) throws MessageException  {
        String[] parsedMessage;
        String[] hashtagSplitedArray = message.split("#");

        if(hashtagSplitedArray.length >= 2 &&
                countChar(hashtagSplitedArray[hashtagSplitedArray.length-1], ',') < 4 &&
                countChar(hashtagSplitedArray[hashtagSplitedArray.length-2], ',') < 4)
            throw new MessageException("Invalid message received.");

        else if(countChar(hashtagSplitedArray[hashtagSplitedArray.length-1], ',') == 4) {
            parsedMessage = hashtagSplitedArray[hashtagSplitedArray.length - 1].split(",");
        }
        else if(hashtagSplitedArray.length >= 2){
            parsedMessage = hashtagSplitedArray[hashtagSplitedArray.length - 2].split(",");
        }   else
            throw new MessageException("Invalid message received.");

        return parsedMessage;
    }

    public static int countChar(String str, char c) {
        int count = 0;

        for(int i=0; i < str.length(); i++)
            if(str.charAt(i) == c)
                count++;
        return count;
    }
}
