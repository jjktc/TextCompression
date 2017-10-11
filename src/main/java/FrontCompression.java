import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Implement front compression.
 * <p>
 * Front compression (also called, strangely, back compression, and, less strangely, front coding)
 * is a compression algorithm used for reducing the size of certain kinds of textual structured
 * data. Instead of storing an entire string value, we use a prefix from the previous value in a
 * list.
 * <p>
 * Front compression is particularly useful when compressing lists of words where each successive
 * element has a great deal of similarity with the previous. One example is a search (or book)
 * index. Another example is a dictionary.
 * <p>
 * This starter code will help walk you through the process of implementing front compression.
 *
 * @see <a href="https://cs125.cs.illinois.edu/lab/6/">Lab 6 Description</a>
 * @see <a href="https://en.wikipedia.org/wiki/Incremental_encoding"> Incremental Encoding on
 *      Wikipedia </a>
 */

public class FrontCompression {

    /** My personal test on compression. */
    public static final boolean SORTED = true;

    /**
     * Compress a newline-separated list of words using simple front compression.
     *
     * @param corpus the newline-separated list of words to compress
     * @return the input compressed using front encoding
     */
    public static String compress(final String corpus) {
        /*
         * Defend against bad inputs.
         */
        String compressed = "";
        if (corpus == null) {
            return null;
        } else if (corpus.length() == 0) {
            return "";
        }
        if (SORTED) {
            String[] lines = corpus.split("\n");
            compressed += 0 + " " + lines[0] + "\n";
            for (int i = 1; i < lines.length; i++) {
                int shared = longestPrefix(lines[i - 1], lines[i]);
                compressed += shared + " " + lines[i].substring(shared, lines[i].length()) + "\n";
            }
        } else {
            String[] lines = corpus.split("\n");
            compressed += 0 + " " + 0 + " " + lines[0] + "\n";
            for (int i = 1; i < lines.length; i++) {
                int bestIndex = bestPrefix(lines, i);
                int shared = longestPrefix(lines[bestIndex], lines[i]);
                compressed += bestIndex + " " + shared + " "
                        + lines[i].substring(shared, lines[i].length()) + "\n";
            }
        }
        return compressed;
    }

    /**
     * Decompress a newline-separated list of words using simple front compression.
     *
     * @param corpus the newline-separated list of words to decompress
     * @return the input decompressed using front encoding
     */
    public static String decompress(final String corpus) {
        /*
         * Defend against bad inputs.
         */
        if (corpus == null) {
            return null;
        } else if (corpus.length() == 0) {
            return "";
        }

        if (SORTED) {
            String[] lines = corpus.split("\n");
            String lastLine = lines[0].split(" ")[1];
            String raw = lastLine + "\n";
            for (int i = 1; i < lines.length; i++) {
                int prefixLength = Integer.parseInt(lines[i].split(" ")[0]);
                String prefix = "";
                if (prefixLength >= 1) {
                    prefix = lastLine.substring(0, prefixLength);
                } else {
                    prefix = "";
                }
                String suffix = lines[i].split(" ")[1];

                String rawLine = prefix + suffix;
                raw += rawLine + "\n";
                lastLine = rawLine;
            }
            return raw;
        } else {
            String[] lines = corpus.split("\n");
            String lastLine = lines[0].split(" ")[2];
            String raw = lastLine + "\n";

            List<String> decompressed = new ArrayList<String>();
            decompressed.add(lastLine);

            for (int i = 1; i < lines.length; i++) {
                int bestIndex = i - Integer.parseInt(lines[i].split(" ")[0]);
                int prefixLength = Integer.parseInt(lines[i].split(" ")[1]);
                String prefix = "";
                if (prefixLength >= 1) {
                    if (bestIndex > 0) {
                        prefix = decompressed.get(bestIndex).substring(0, prefixLength);
                    } else {
                        prefix = decompressed.get(decompressed.size() - 1)
                                .substring(0, prefixLength);
                    }
                } else {
                    prefix = "";
                }
                String suffix = lines[i].split(" ")[2];

                String rawLine = prefix + suffix;
                raw += rawLine + "\n";
                decompressed.add(rawLine);
                lastLine = rawLine;
            }
            return raw;
        }
    }

    /**
     * Compute the length of the common prefix between two strings.
     *
     * @param firstString the first string
     * @param secondString the second string
     * @return the length of the common prefix between the two strings
     */
    private static int longestPrefix(final String firstString, final String secondString) {
        /*
         * Complete this function.
         */
        int longest = 0;
        for (int i = 0; i < firstString.length() && i < secondString.length(); i++) {
            if (firstString.charAt(i) == secondString.charAt(i)) {
                longest++;
            } else {
                return longest;
            }
        }
        return longest;
    }

    /**
     * Get the best prefix index.
     *
     * @param lines unused
     * @param thisString unused
     * @return the index
     */
    private static int bestPrefix(final String[] lines, final int thisString) {
        int longest = 0;
        int index = 0;
        for (int i = 0; i < thisString; i++) {
            int prefix = longestPrefix(lines[thisString], lines[i]);
            if (prefix >= longest) {
                longest = prefix;
                index = i;
            }
        }
        if (longest > 0) {
            return thisString - index;
        } else {
            return 0;
        }
    }

    /**
     * Test your compression and decompression algorithm.
     *
     * @param unused unused input arguments
     * @throws URISyntaxException thrown if the file URI is invalid
     * @throws FileNotFoundException thrown if the file cannot be found
     */
    public static void main(final String[] unused)
            throws URISyntaxException, FileNotFoundException {

        /*
         * The magic 6 lines that you need in Java to read stuff from a file.
         */
        String words = null;
        String wordsFilePath = FrontCompression.class.getClassLoader().getResource("words.txt")
                .getFile();
        wordsFilePath = new URI(wordsFilePath).getPath();
        File wordsFile = new File(wordsFilePath);
        Scanner wordsScanner = new Scanner(wordsFile, "UTF-8");
        words = wordsScanner.useDelimiter("\\A").next();
        wordsScanner.close();

        String originalWords = words;
        String compressedWords = compress(words);
        String decompressedWords = decompress(compressedWords);

        String[] lines = compressedWords.split("\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.println(lines[i]);
        }

        if (decompressedWords.equals(originalWords)) {
            System.out.println("Original length: " + originalWords.length());
            System.out.println("Compressed length: " + compressedWords.length());
        } else {
            System.out.println("Your compression or decompression is broken!");
            String[] originalWordsArray = originalWords.split("\\R");
            String[] decompressedWordsArray = decompressedWords.split("\\R");
            boolean foundMismatch = false;
            for (int stringIndex = 0; //
                    stringIndex < Math.min(originalWordsArray.length,
                            decompressedWordsArray.length); //
                    stringIndex++) {
                if (!(originalWordsArray[stringIndex]
                        .equals(decompressedWordsArray[stringIndex]))) {
                    System.out.println("Line " + stringIndex + ": " //
                            + originalWordsArray[stringIndex] //
                            + " != " + decompressedWordsArray[stringIndex]);
                    foundMismatch = true;
                    break;
                }
            }
            if (!foundMismatch) {
                if (originalWordsArray.length != decompressedWordsArray.length) {
                    System.out.println("Original and decompressed files have different lengths");
                } else {
                    System.out.println("Original and decompressed files " //
                            + "have different line endings.");
                }
            }
        }
    }
}
