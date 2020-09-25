import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.UnexpectedException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jakub on 07.02.2017.
 * Pattern p = Pattern.compile("Text otázky.*?:([\\w\\W]*?(?=Odpov))[\\w\\W]*?(^Odpov.*?:{1}[^:]*?A$)*", Pattern.MULTILINE);
 */
public class Main {
    public static final String WORKING_DIRECTORY = "c:/ivet/";
    public static final String INPUT_FILE_NAME = "ivet.txt";

    public static final String QUESTION_LABEL = "Číslo otázky";
    public static final String PS_LABEL = "Případová\\nstudie\\s*";//"Zadání PS";

    public static void main(String[] args) throws IOException {
        Map<QuestionTypes, List<String>> linesQuestionMap = new EnumMap<>(QuestionTypes.class);
        String text = new String(Files.readAllBytes(Paths.get(WORKING_DIRECTORY + INPUT_FILE_NAME)));

        Pattern instancePattern = Pattern.compile(QUESTION_LABEL + ".*?([\\w\\W]*?(?=" + QUESTION_LABEL + "))", Pattern.MULTILINE);
        Pattern typePattern = Pattern.compile("Typ otázky:.*?([\\w\\W]*?(?=Kategorie:))", Pattern.MULTILINE);
        Pattern psPattern = Pattern.compile(PS_LABEL + "(.*?(?=Text ot|Odůvodnění ot))", Pattern.MULTILINE | Pattern.DOTALL);
        Pattern questionAndAnswerPattern = Pattern.compile("Text otázky(.*?(?=Odpověď))(.*(?=Odůvodnění|\\r\\n))", Pattern.MULTILINE | Pattern.DOTALL);
        Pattern correctAnswerPattern = Pattern.compile("Odpověď.{3}(?=✓).(.*?(?=Odpověď|\\r\\n))", Pattern.MULTILINE);
        Pattern reasonPattern = Pattern.compile("Odůvodnění (.*)", Pattern.MULTILINE);
//        Pattern correctAnswerPattern = Pattern.compile("(^Odpov.*?:{1}[^:]*?(?=A$))", Pattern.MULTILINE);

        Matcher matcher = instancePattern.matcher(text);

        while (matcher.find()) {
            StringBuilder stringBuilder = new StringBuilder();
            String originalText = matcher.group();

            /**
             * Type part
             */
            QuestionTypes type = QuestionTypes.NA;
            Matcher typeMatcher = typePattern.matcher(originalText);

            if (typeMatcher.find()) {
                type = Arrays.stream(QuestionTypes.values())
                        .filter(x -> typeMatcher.group(1).contains(x.getValue()))
                        .findFirst()
                        .get();
            }

            /**
             * Pripadova studie - part
             */
            Matcher psMatcher = psPattern.matcher(originalText);

            if (psMatcher.find()) {
                stringBuilder.append("PS:").append(psMatcher.group(1).replace(System.lineSeparator(), " ")).append(System.lineSeparator());
            }

            /**
             * Q&A part
             */
            Matcher questionAndAnswersMatcher = questionAndAnswerPattern.matcher(originalText);

            while (questionAndAnswersMatcher.find()) {
                stringBuilder.append(questionAndAnswersMatcher.group(1).replace(System.lineSeparator(), "")).append(System.lineSeparator());
                Matcher answerMatcher = correctAnswerPattern.matcher(questionAndAnswersMatcher.group(2));

                StringBuilder answers = new StringBuilder();
                while (answerMatcher.find()) {
                    answers.append("-").append(answerMatcher.group(1).replace(System.lineSeparator(), "").replace("Odpověď ", "")).append(System.lineSeparator());
                }

                if (answers.length() == 0)
                    throw new UnexpectedException("No answer matches in text: " + originalText);
                else
                    stringBuilder.append(answers);
            }

            /**
             * Reason - part
             */
            Matcher reasonMatcher = reasonPattern.matcher(originalText);
            if (reasonMatcher.find()) {
                //oddovodnenie vynechane
                //stringBuilder.append("+").append(reasonMatcher.group(1));
            }

            /**
             * Save
             */
            linesQuestionMap.computeIfAbsent(type, k -> new ArrayList<>());
            List<String> stringList = linesQuestionMap.get(type);

            if (stringBuilder.length() == 0)
                throw new UnexpectedException("no pattern matched in text: " + originalText);

            if (stringBuilder.length() > 0) {
                stringList.add(stringBuilder.toString());
            }
        }

        for (Map.Entry<QuestionTypes, List<String>> entry : linesQuestionMap.entrySet()) {
            List<String> lines = entry.getValue();
            Collections.sort(lines);
            Path file = Paths.get(WORKING_DIRECTORY + entry.getKey() + ".txt");
            Files.write(file, lines, StandardCharsets.UTF_8);
        }
    }
}
