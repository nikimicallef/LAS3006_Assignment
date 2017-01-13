package resources;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PathParsing {
    public static boolean pathChecker(String path) {
        if(path.contains("\\")){
            path = path.replace("\\","/");
        }
        final String[] pathLevels = path.split("/");

        if (Arrays.stream(pathLevels).filter(level -> level.length() == 0).collect(Collectors.toList()).size() > 0) {
            System.out.println("Path " + path + " invalid since once or more levels is empty");
            return false;
        } else if (path.charAt(0) == '/') {
            System.out.println("Path " + path + " invalid since it starts with a /");
            return false;
        } else if (path.charAt(path.length() - 1) == '/') {
            System.out.println("Path " + path + " invalid since it ends with a /.");
            return false;
        } else if (path.contains(" ")) {
            System.out.println("Path " + path + " invalid since it contains a space.");
            return false;
        } else if (Arrays.stream(pathLevels).filter(level -> level.length() > 1 && (level.contains("+") || level.contains("#"))).collect(Collectors.toList()).size() > 0) {
            System.out.println("Path " + path + " invalid since it contains a + or a # within a level");
            return false;
        } else if (Arrays.stream(pathLevels).filter(level -> !(pathLevels[pathLevels.length - 1].equals(level))).filter(item -> item.contains("#")).collect(Collectors.toList()).size() > 0) {
            System.out.println("Path " + path + " invalid since the wildcard # can only be used at the end.");
            return false;
        }
        return true;
    }

    public static boolean pathsMatch(final String inputtedPath, final String pathInHashMap) {
        final String[] inputtedPathSplit = inputtedPath.split("/");
        final String[] pathInHashMapSplit = pathInHashMap.split("/");

        for (int counter = 0; counter < pathInHashMapSplit.length; counter++) {
            if (pathInHashMapSplit[counter].equals("#") || (inputtedPathSplit.length > counter && inputtedPathSplit[counter].equals("#"))) {
                return true;
            } else if (pathInHashMapSplit[counter].equals("+") || ((inputtedPathSplit.length > counter && inputtedPathSplit[counter].equals("+")))) {
                continue;
            } else if (inputtedPathSplit.length > counter && !pathInHashMapSplit[counter].equals(inputtedPathSplit[counter])) {
                return false;
            }
        }

        return inputtedPathSplit.length == pathInHashMapSplit.length;
    }
}
