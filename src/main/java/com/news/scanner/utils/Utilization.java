package com.news.scanner.utils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilization {


    public static List<String> getPunctuationForLanguage() {
        List<String> punctuation = new LinkedList<>();
        punctuation.add(".");
        punctuation.add("?");
        punctuation.add("!");
        punctuation.add(";");
        punctuation.add("...");
        return punctuation;
    }

    /**
     * Split text by the specified sign.
     *
     * @param text the input text to be split
     * @param sign the delimiter used for splitting
     * @return an array of strings split by the sign, or null if no splitting occurs
     */
    public static List<String> splitTextBySign(String text, String sign) {
        return new ArrayList<>(Arrays.asList(text.split(Pattern.quote(sign))));
    }


    public static String addSignToText(String text, String sign) {
        return text.concat(sign);
    }

    /**
     * Split text with a specific sign while considering certain conditions.
     *
     * @param text the input text to be processed
     * @param sign the delimiter used for splitting
     * @return the processed text as a string
     */
    public static String splitWithSign(String text, String sign) {
        text = text.trim();  // Remove leading and trailing spaces

        int lastIndex = 0;
        int length = text.length();
        StringBuilder listText = new StringBuilder();
        int nextIndex = 0;

        for (int index = 0; index < length; index++) {
            if ((index + 1) < length) {
                nextIndex = index + 1;
                char preChar = index > 0 ? text.charAt(index - 1) : ' ';
                char currentChar = text.charAt(index);

                if (currentChar == sign.charAt(0)) {
                    if (text.charAt(nextIndex) != sign.charAt(0)) {
                        String addText = text.substring(lastIndex, index).trim();

                        if (!addText.isEmpty()) {
                            // Regex for specific Unicode ranges
                            boolean isSpecialChar = Pattern.compile("[\\u0E80-\\u0EFF]").matcher(String.valueOf(preChar)).find() ||
                                    (Character.isLetter(preChar) && Character.isLowerCase(preChar)) ||
                                    Pattern.compile("[\\u4E00-\\u9FFF]").matcher(String.valueOf(preChar)).find();

                            if (isSpecialChar) {
                                if (!Character.isDigit(text.charAt(nextIndex)) && !Character.isLetter(text.charAt(nextIndex))) {
                                    listText.append(addText).append(currentChar).append("\n");
                                    lastIndex = nextIndex;
                                    continue;
                                }

                                if (Pattern.compile("[\\u4E00-\\u9FFF]").matcher(String.valueOf(text.charAt(nextIndex))).find()) {
                                    listText.append(addText).append(currentChar);
                                    lastIndex = nextIndex;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Handle the last segment after the last delimiter
        if (lastIndex < length) {
            listText.append(text.substring(lastIndex));
        }

        return listText.toString();
    }


    // Method to add a sign to each element in the list
    public static List<String> addSignToList(List<String> listText, String sign) {
        List<String> newList = new ArrayList<>();
        for (String text : listText) {
            newList.add(text + sign);
        }
        return newList;
    }

    /**
     * Adds a sign to each string in the list based on certain conditions.
     *
     * @param listText the list of strings to process
     * @param sign     the sign to add
     * @param listSign the list of signs to check against
     * @return a new list with signs added as appropriate
     */
    public static List<String> addSignToList(List<String> listText, String sign, List<Character> listSign) {
        if (listText.isEmpty()) {
            return new ArrayList<>();
        }

        // Remove trailing empty strings
        while (!listText.isEmpty() && listText.get(listText.size() - 1).isEmpty()) {
            listText.remove(listText.size() - 1);
        }

        List<String> newList = new ArrayList<>();
        String oldText = "";
        int lastIndex = 0;

        for (String text : listText) {
            if (text.isEmpty()) {
                oldText = addSignToText(oldText, sign);
                if (lastIndex > 0) {
                    newList.set(lastIndex - 1, oldText);
                }
                continue;
            }

            boolean isSign = false;
            char lastCharacter = text.charAt(text.length() - 1);

            // Check if the last character is in the list of signs
            for (char endSign : listSign) {
                if (lastCharacter == endSign) {
                    isSign = true;
                    break;
                }
            }

            if (!isSign) {
                oldText = addSignToText(text, sign);
            } else {
                oldText = text;
            }

            oldText = oldText.trim();
            newList.add(oldText);
            lastIndex++;
        }

        return newList;
    }

    /**
     * Recreates a list of text based on raw text and additional text to add.
     *
     * @param rawText  the original list of text
     * @param addList  a list containing the position and the text to add
     * @param sign     the sign to append to each string being added
     * @param listSign the list of signs to check against
     * @return the new list with the additional text inserted
     */
    public static List<String> remakeListText(List<String> rawText, List<Object> addList, String sign, List<Character> listSign) {
        // Make a copy of the original list
        List<String> newList = new ArrayList<>(rawText);

        // Extract position and list of texts to add
        int position = (int) addList.get(0);
        List<String> listText = (List<String>) addList.get(1);

        // Process the list of texts to add
        listText = addSignToList(listText, sign, listSign);

        for (String text : listText) {
            if (text.isEmpty()) {
                continue;
            }
            newList.add(position, text); // Insert the text at the specified position
            position++; // Move to the next position
        }

        // Remove the last added position since it's out of bounds
        if (position > 0) {
            newList.remove(position - 1); // Adjust to remove the last added text
        }

        return newList;
    }

    // Example formatDocumentWithComma implementation
    public static String deleteEmoji(String text) {
        // Regex pattern to match emojis and symbols
        String regexPattern = "[\uD83C\uDF00-\uD83C\uDFFF" // symbols & pictographs
                + "\uD83D\uDE00-\uD83D\uDE4F" // emoticons
                + "\uD83D\uDE80-\uD83D\uDEFF" // transport & map symbols
                + "\uD83C\uDDE0\uD83C\uDDFF" // flags (iOS)
                + "]";

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll("");
    }

    // Method to format string by removing emojis and applying sentence formatting
    public static String formatString(String text) {
        text = deleteEmoji(text);
        text = formatSentence(text);
        return text;
    }

    // Method to remove non-breaking spaces from text
    public static String removeNBSP(String text) {
        return text.replace("&nbsp", "");
    }

    /**
     * Splits the text based on the provided list of signs.
     *
     * @param text     the original text to split
     * @param listSign a list of signs to use for splitting the text
     * @return the formatted text after splitting
     */
    public static String splitText(String text, List<String> listSign) {
        for (String sign : listSign) {
            text = splitWithSign(text, sign);
        }

        // Format the document with the first sign in the list
        text = formatDocumentWithComma(text, listSign.get(0));

        return text;
    }

    public static String slipWithEndLine(String text) {
        List<String> list = new ArrayList<>();
        int length = text.length() - 1;
        int next = 0;
        int start = 0;
        int cropAt = 0;

        while (start < length) {
            if (next >= length) {
                break;
            }

            // Skip non-newline characters
            if (text.charAt(start) != '\n') {
                start++;
                next = start;
                continue;
            }

            // If next character is not a newline, extract the line
            if (text.charAt(next) != '\n') {
                String line = text.substring(cropAt, start + 1);
                line = formatSentence(line); // Format the line

                list.add(line);
                cropAt = next;
                start = next;
                continue;
            }

            next++;
        }

        // Handle the remaining text after the last newline
        if (cropAt != next) {
            list.add(formatSentence(text.substring(cropAt, start + 1)));
        }

        // Construct the new string from the list
        StringBuilder newString = new StringBuilder();
        for (String line : list) {
            if (line.isEmpty()) {
                continue; // Skip empty lines
            }
            newString.append(line); // Append formatted line to the new string
        }

        return newString.toString();
    }

    public static String formatSentence(String text) {
        // Convert to UTF-8 encoding and handle decoding errors
        text = new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        // Replace specific characters and sequences
        text = text.replace("\u0007", " ");  // \a is not a valid escape in Java, so use unicode \u0007
        text = text.replace("\r", " ");
        // Use regular expressions to replace unwanted characters
        text = text.replaceAll("\u00A0", " ");  // \xa0 is non-breaking space in unicode
        text = text.replaceAll("&nbsp", " ");
        text = text.replaceAll("\u200B", " ");  // zero-width space
        text = text.replaceAll("\\./", " ");
        text = text.replaceAll("•", "");
        text = text.replaceAll("”", "");
        text = text.replaceAll("\t+", " ");  // Replaces multiple tabs with one space
        text = text.replaceAll(" +", " ");   // Replaces multiple spaces with one space
        text = text.replaceAll("\n +\n", "\n");  // Matches newlines with spaces between
        text = text.replaceAll("\n +", "\n");    // Matches newlines followed by spaces
        text = text.replaceAll("\n+", "\n");     // Matches multiple newlines

        return text;
    }

    public static String formatDocumentWithComma(String text, String comma) {
        // Replace multiple newlines and spaces between newlines
        text = text.replaceAll("[\n]+?[ ]+?[\n]+", "\n");
        // Replaces newlines followed by spaces
        text = text.replaceAll("[\n]+?[ ]+", "\n");

        // Call the slipWithEndLine method (to be defined separately)
        text = slipWithEndLine(text);

        // Replace the ellipsis character with three commas
        text = text.replace("…", comma + comma + comma);

        return text;
    }

/*    public static void main(String[] args) {
        // Example usage
        String text = "How are you...How are you.";

        String splitResult = splitText(text, List.of("..."));
        System.out.println(splitResult);

        // Other functions can be tested similarly
    }*/
}
