package ru.vafilonov;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Program for parsing .class files
 *
 * @author Vsevolod Filonov
 */
public class signatureParser {

    static String usage = "Usage: java signatureParser <flags> dir_name";

    static Path location;



    static Path outdir;

    static boolean isOutdir = false;
    static boolean isDifferent = true;

    static String separator = ";";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Invalid number of args.");
            System.out.println(usage);
            return;
        }

        parserState state = parserState.READ;

        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (state != parserState.READ) {
                    System.out.println("Unexpected flag.");
                    return;
                }

                if (arg.equals("-o")) {
                    isOutdir = true;
                    state = parserState.OUTDIR;
                }
                if (arg.equals("-s")) {
                    state = parserState.SEPARATOR;
                }
            } else {
                switch (state) {
                    case READ:
                        try {
                            location = Paths.get(arg);
                        } catch (InvalidPathException pathEx) {
                            System.out.println("Invalid project path: " + arg);
                            return;
                        }
                        break;
                    case SEPARATOR:
                        separator = arg;
                        state = parserState.READ;
                        break;
                    case OUTDIR:
                        try {
                            outdir = Path.of(arg);
                            state = parserState.READ;
                        } catch (InvalidPathException pathEx) {
                            System.out.println("Invalid outdir path: " + arg);
                            return;
                        }
                        break;
                }
            }
        }

        String classCsv;
        if (isOutdir) {
            classCsv = outdir.toString() + "/" +"classes.csv";
        } else {
            outdir = Path.of(System.getProperty("user.dir"));

            classCsv = "classes.csv";
        }

        try (PrintWriter CSV = new PrintWriter(classCsv, StandardCharsets.UTF_8)) {
            CSV.println("Класс" + separator + "Назначение");
            Stream<Path> files = Files.walk(location);
            URLClassLoader loader = URLClassLoader.newInstance(new URL[]{location.toUri().toURL()});
            files.filter(signatureParser::filterClassfiles).forEach(path -> processClassfile(path, loader, CSV));
        }

    }

    static boolean filterClassfiles(Path path) {
        return !Files.isDirectory(path) && path.getFileName().toString().endsWith(".class");
    }

    static void processClassfile(Path classfile, URLClassLoader loader, PrintWriter classCSV) {
        // resolve classpath to dot form
        String path = classfile.subpath(location.getNameCount(), classfile.getNameCount()).toString();
        String className = path.substring(0, path.length()-6).replace('/', '.'); // without ABC.class
        int classDot = className.lastIndexOf('.');

        String csvName;
        if (classDot == -1) {
            csvName = className + ".csv";
        } else {
            csvName = className.substring(classDot + 1) + ".csv";
        }

        try (PrintWriter localCSV = new PrintWriter(outdir.toString() + "/" + csvName, StandardCharsets.UTF_8)) {
            Class clazz = loader.loadClass(className);
            // class info
            classCSV.println(clazz.getSimpleName() + separator);

            writeFieldsInfo(localCSV, clazz);
            writeMethodsInfo(localCSV, clazz);
        } catch (IOException | ClassNotFoundException io) {
            RuntimeException re = new RuntimeException();
            re.setStackTrace(io.getStackTrace());
            throw re;
        }

    }

    static void writeFieldsInfo(PrintWriter writer, Class cl) {
        writer.println("Поля");
        writer.println("Имя" + separator + "Модификатор доступа" + separator + "Тип" + separator + "Назначение");
        Field[] fields = cl.getDeclaredFields();

        for (var field : fields) {
            writer.println(field.getName() + separator + decodeModifiers(field.getModifiers()) + separator +
                    field.getType().getSimpleName() + separator);
        }
    }

    static void writeMethodsInfo(PrintWriter writer, Class cl) {
        writer.println("Методы");
        writer.println("Имя" + separator + "Модификатор доступа" + separator + "Тип" + separator + "Аргументы" + separator + "Назначение");
        Constructor[] constrs = cl.getConstructors();
        Method[] methods = cl.getDeclaredMethods();

        for (var con : constrs) {
            writer.println(cl.getSimpleName() + separator + decodeModifiers(con.getModifiers()) + separator +
                    "конструктор" + separator + getParameters(con) + separator);
        }

        for (var method : methods) {
            writer.println(method.getName() + separator + decodeModifiers(method.getModifiers()) + separator +
                    method.getReturnType().getSimpleName() + separator + getParameters(method) + separator);
        }
    }

    static String decodeModifiers(int mods) {
        StringBuilder modStr = new StringBuilder();
        if (Modifier.isPublic(mods))
            return "public";
        if (Modifier.isProtected(mods))
            return "protected";
        if (Modifier.isPrivate(mods))
            return "private";

        return "";
    }

    static String getParameters(Executable m) {
        Parameter[] params = m.getParameters();
        if (params.length == 0)
            return "-";
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        for (int i = 0; i < params.length; i++) {
            builder.append(params[i].getType().getSimpleName());
            builder.append(' ');
            builder.append(params[i].getName());
            if (i != params.length - 1)
                builder.append(',');
        }
        builder.append("\"");
        return builder.toString();
    }

    enum parserState {
        READ,
        SEPARATOR,
        OUTDIR
    }
}
