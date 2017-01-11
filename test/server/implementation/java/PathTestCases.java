import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTestCases {
    private static final String singleLevelWildcard = "home/+/temperature";
    private static final String multiLevelWildcard = "home/kitchen/#";
    private static final String noWildcard = "home/kitchen/temperature";

    @Test
    public void noWildcard_matches1(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("home/kitchen/temperature",noWildcard));
    }

    @Test
    public void noWildcard_matches2(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("home","#"));
    }

    @Test
    public void noWildcard_matches3(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("home","+"));
    }

    @Test
    public void noWildcard_doesntMatchLastLevel(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathsMatch("home/kitchen/status",noWildcard));
    }

    @Test
    public void noWildcard_doesntMatchMidLevel(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathsMatch("home/bedroom/temperature",noWildcard));
    }

    @Test
    public void noWildcard_doesntMatchTooShort(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathsMatch("home/kitchen",noWildcard));
    }

    @Test
    public void singleLevelWildcard_matches1(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("home/refrigerator/temperature",singleLevelWildcard));
    }

    @Test
    public void singleLevelWildcard_matches2(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("home/bedroom/temperature",singleLevelWildcard));
    }

    @Test
    public void singleLevelWildcard_doesntMatch1(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathsMatch("home/bathroom/water-boiler/temperature",singleLevelWildcard));
    }

    @Test
    public void singleLevelWildcard_doesntMatch2(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathsMatch("home/bedroom/temperature/extra",singleLevelWildcard));
    }

    @Test
    public void singleLevelWildcard_doesntMatch3(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathsMatch("home/bedroom",singleLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_matches1(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("home/kitchen/temperature",multiLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_matches2(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("home/kitchen/humidity",multiLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_matches3(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("home/kitchen/refrigerator/temperature",multiLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_matches4(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("home/kitchen/freezer/temperature",multiLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_matches5(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("home/kitchen/gasdetector-status",multiLevelWildcard));
    }

    @Test
    public void multiLevelWildcard_doesNotMatch(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathsMatch("home/refrigerator/temperature",multiLevelWildcard));
    }

    @Test
    public void multipleWildcards__matches1(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("a/+/c","a/b/+"));
    }

    @Test
    public void multipleWildcards_matches2(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("a/+/c","a/b/#"));
    }

    @Test
    public void multipleWildcards_matches3(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("a/+/#","a/b/#"));
    }

    @Test
    public void multipleWildcards_matches4(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("a/+/a","a/#"));
    }

    @Test
    public void multipleWildcards_matches5(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("a/#","a/+/a"));
    }

    @Test
    public void multipleWildcards_matches6(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("a/+/#","a/#"));
    }

    @Test
    public void multipleWildcards_matches7(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("a/+/+","a/#"));
    }

    @Test
    public void multipleWildcards_doesntMatch1(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathsMatch("a/+/#","a/+"));
    }

    @Test
    public void multipleWildcards_doesntMatch3(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathsMatch("a/#","a/+/+"));
    }
    
    @Test
    public void invalidPath1(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker(""));
    }

    @Test
    public void invalidPath2(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("/"));
    }

    @Test
    public void invalidPath3(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("/a"));
    }

    @Test
    public void invalidPath4(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("a/b#"));
    }

    @Test
    public void invalidPath5(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("a/#/b"));
    }

    @Test
    public void invalidPath6(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("a/+b"));
    }

    @Test
    public void invalidPath7(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("a//b"));
    }

    @Test
    public void invalidPath8(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("a/b/"));
    }

    @Test
    public void invalidPath9(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("a/ /c"));
    }

    @Test
    public void invalidPath10(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("a/a /c"));
    }

    @Test
    public void invalidPath11(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("a/##/c"));
    }

    @Test
    public void invalidPath12(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("a/++/c"));
    }

    @Test
    public void invalidPath13(){
        final Server server = new Server(false);

        Assert.assertEquals(false, server.pathChecker("a/+#/c"));
    }

    @Test
    public void validPath1(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathChecker("a"));
    }

    @Test
    public void validPath2(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathChecker("a/b"));
    }

    @Test
    public void validPath3(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathChecker("a/b/#"));
    }

    @Test
    public void validPath4(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathChecker("a/+/c"));
    }

    @Test
    public void validPath5(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathChecker("a/+"));
    }

    @Test
    public void validPath6(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathChecker("a/+/#"));
    }

    @Test
    public void validPath7(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathChecker("#"));
    }

    @Test
    public void validPath8(){
        final Server server = new Server(false);

        Assert.assertEquals(true, server.pathChecker("+"));
    }
}
