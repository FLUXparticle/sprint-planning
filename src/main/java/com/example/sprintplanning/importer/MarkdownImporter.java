package com.example.sprintplanning.importer;

import com.example.sprintplanning.model.*;
import jakarta.xml.bind.*;

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class MarkdownImporter {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: MarkdownImporter <input.md> <output.xml>");
            System.exit(1);
        }
        Path mdFile  = Path.of(args[0]);
        Path xmlFile = Path.of(args[1]);

        List<String> lines = Files.readAllLines(mdFile);
        List<Task> rootTasks = parseMarkdown(lines);

        // JAXB marshalling
        Tasks tasks = new Tasks();
        tasks.setTasks(rootTasks);
        JAXBContext ctx = JAXBContext.newInstance(Tasks.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(tasks, xmlFile.toFile());

        System.out.println("Imported " + rootTasks.size() + " top-level tasks into " + xmlFile);
    }

    private static List<Task> parseMarkdown(List<String> lines) {
        List<Task> roots = new ArrayList<>();
        Deque<Frame> stack = new ArrayDeque<>();
        // Frame holds (task, level)
        stack.push(new Frame(null, -1)); // sentinel

        Pattern heading = Pattern.compile("^(#{2,})\\s*(.+)$");
        Pattern checkbox = Pattern.compile("^(\\s*)- \\[([ xX])\\]\\s*(.+)$");
        int lastHeadlineLevel = 0;
        for (String raw : lines) {
            String line = raw.strip();
            if (line.isEmpty()) continue;
            Matcher mH = heading.matcher(raw);
            Matcher mC = checkbox.matcher(raw);

            int level;
            String text;
            boolean done = false;
            boolean important = false;

            if (mH.matches()) {
                int hashes = mH.group(1).length();
                level = hashes - 2;  // "##" → 0, "###" → 1, …
                lastHeadlineLevel = level;
                text = mH.group(2).trim();
                if (text.isBlank()) { continue; }
            }
            else if (mC.matches()) {
                int spaces = mC.group(1).length();
                level = lastHeadlineLevel + 1 + spaces / 4;  // 4 spaces = one deeper level
                done = mC.group(2).equalsIgnoreCase("x");
                text = mC.group(3).trim();
            } else {
                // sonst überspringen
                continue;
            }

            // fett (**text**) → wichtig
            if (text.startsWith("**") && text.endsWith("**")) {
                important = true;
                text = text.substring(2, text.length() - 2).trim();
            }

            text = stripMarkdown(text);

            Task task = new Task(text);
            task.setDone(done);
            task.setImportant(important);

            // find correct parent for this level
            while (stack.peek().level >= level) {
                stack.pop();
            }
            Frame parentFrame = stack.peek();
            if (parentFrame.task == null) {
                roots.add(task);
            } else {
                parentFrame.task.getChildren().add(task);
            }
            stack.push(new Frame(task, level));
        }
        return roots;
    }

    private static String stripMarkdown(String s) {
        // Entfernt einfache HTML-Tags und Markdown-Links, …
        // hier nur rudimentär: <ins>…</ins> raus und fett-Markup
        return s
            .replaceAll("<[^>]+>", "")
            .replaceAll("\\*\\*", "")
            .trim();
    }

    private record Frame(Task task, int level) {}
}