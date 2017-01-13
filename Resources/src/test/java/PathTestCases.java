import org.junit.Assert;
import org.junit.Test;

import static resources.PathParsing.pathChecker;
import static resources.PathParsing.pathsMatch;

public class PathTestCases {
    private static final String singleLevelWildcard = "home/+/temperature";
    private static final String multiLevelWildcard = "home/kitchen/#";
    private static final String noWildcard = "home/kitchen/temperature";

    @Test
    public void noWildcard_matches1() {
        Assert.assertEquals(true, pathsMatch("home/kitchen/temperature", noWildcard));
    }

    @Test
    public void noWildcard_matches2() {
        Assert.assertEquals(true, pathsMatch("home", "#"));
    }

    @Test
    public void noWildcard_matches3() {
        Assert.assertEquals(true, pathsMatch("home", "+"));
    }

    @Test
    public void noWildcard_doesntMatchLastLevel() {
        Assert.assertEquals(false, pathsMatch("home/kitchen/status", noWildcard));
    }

    @Test
    public void noWildcard_doesntMatchMidLevel() {
        Assert.assertEquals(false, pathsMatch("home/bedroom/temperature", noWildcard));
    }

    @Test
    public void noWildcard_doesntMatchTooShort() {
        Assert.assertEquals(false, pathsMatch("home/kitchen", noWildcard));
    }

    @Test
    public void singleLevelWildcard_matches1() {
        Assert.assertEquals(true, pathsMatch("home/refrigerator/temperature", singleLevelWildcard));
    }

    @Test
    public void singleLevelWildcard_matches2() {
        Assert.assertEquals(true, pathsMatch("home/bedroom/temperature", singleLevelWildcard));
    }

    @Test
    public void singleLevelWildcard_doesntMatch1() {
        Assert.assertEquals(false, pathsMatch("home/bathroom/water-boiler/temperature", singleLevelWildcard));
    }

    @Test
    public void singleLevelWildcard_doesntMatch2() {
        Assert.assertEquals(false, pathsMatch("home/bedroom/temperature/extra", singleLevelWildcard));
    }

    @Test
    public void singleLevelWildcard_doesntMatch3() {
        Assert.assertEquals(false, pathsMatch("home/bedroom", singleLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_matches1() {
        Assert.assertEquals(true, pathsMatch("home/kitchen/temperature", multiLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_matches2() {
        Assert.assertEquals(true, pathsMatch("home/kitchen/humidity", multiLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_matches3() {
        Assert.assertEquals(true, pathsMatch("home/kitchen/refrigerator/temperature", multiLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_matches4() {
        Assert.assertEquals(true, pathsMatch("home/kitchen/freezer/temperature", multiLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_matches5() {
        Assert.assertEquals(true, pathsMatch("home/kitchen/gasdetector-status", multiLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_doesNotMatch() {
        Assert.assertEquals(false, pathsMatch("home/refrigerator/temperature", multiLevelWildcard));
    }

    @Test
    public void multipleWildcards__matches1() {
        Assert.assertEquals(true, pathsMatch("a/+/c", "a/b/+"));
    }

    @Test
    public void multipleWildcards_matches2() {
        Assert.assertEquals(true, pathsMatch("a/+/c", "a/b/#"));
    }

    @Test
    public void multipleWildcards_matches3() {
        Assert.assertEquals(true, pathsMatch("a/+/#", "a/b/#"));
    }

    @Test
    public void multipleWildcards_matches4() {
        Assert.assertEquals(true, pathsMatch("a/+/a", "a/#"));
    }

    @Test
    public void multipleWildcards_matches5() {
        Assert.assertEquals(true, pathsMatch("a/#", "a/+/a"));
    }

    @Test
    public void multipleWildcards_matches6() {
        Assert.assertEquals(true, pathsMatch("a/+/#", "a/#"));
    }

    @Test
    public void multipleWildcards_matches7() {
        Assert.assertEquals(true, pathsMatch("a/+/+", "a/#"));
    }

    @Test
    public void multipleWildcards_doesntMatch1() {
        Assert.assertEquals(false, pathsMatch("a/+/#", "a/+"));
    }

    @Test
    public void multipleWildcards_doesntMatch3() {
        Assert.assertEquals(true, pathsMatch("a/#", "a/+/+"));
    }

    @Test
    public void invalidPath1() {
        Assert.assertEquals(false, pathChecker(""));
    }

    @Test
    public void invalidPath2() {
        Assert.assertEquals(false, pathChecker("/"));
    }

    @Test
    public void invalidPath3() {
        Assert.assertEquals(false, pathChecker("/a"));
    }

    @Test
    public void invalidPath4() {
        Assert.assertEquals(false, pathChecker("a/b#"));
    }

    @Test
    public void invalidPath5() {
        Assert.assertEquals(false, pathChecker("a/#/b"));
    }

    @Test
    public void invalidPath6() {
        Assert.assertEquals(false, pathChecker("a/+b"));
    }

    @Test
    public void invalidPath7() {
        Assert.assertEquals(false, pathChecker("a//b"));
    }

    @Test
    public void invalidPath8() {
        Assert.assertEquals(false, pathChecker("a/b/"));
    }

    @Test
    public void invalidPath9() {
        Assert.assertEquals(false, pathChecker("a/ /c"));
    }

    @Test
    public void invalidPath10() {
        Assert.assertEquals(false, pathChecker("a/a /c"));
    }

    @Test
    public void invalidPath11() {
        Assert.assertEquals(false, pathChecker("a/##/c"));
    }

    @Test
    public void invalidPath12() {
        Assert.assertEquals(false, pathChecker("a/++/c"));
    }

    @Test
    public void invalidPath13() {
        Assert.assertEquals(false, pathChecker("a/+#/c"));
    }

    @Test
    public void validPath1() {
        Assert.assertEquals(true, pathChecker("a"));
    }

    @Test
    public void validPath2() {
        Assert.assertEquals(true, pathChecker("a/b"));
    }

    @Test
    public void validPath3() {
        Assert.assertEquals(true, pathChecker("a/b/#"));
    }

    @Test
    public void validPath4() {
        Assert.assertEquals(true, pathChecker("a/+/c"));
    }

    @Test
    public void validPath5() {
        Assert.assertEquals(true, pathChecker("a/+"));
    }

    @Test
    public void validPath6() {
        Assert.assertEquals(true, pathChecker("a/+/#"));
    }

    @Test
    public void validPath7() {
        Assert.assertEquals(true, pathChecker("#"));
    }

    @Test
    public void validPath8() {
        Assert.assertEquals(true, pathChecker("+"));
    }
}
