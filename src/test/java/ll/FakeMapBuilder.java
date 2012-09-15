package ll;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;

/**
 * Builder for creating a {@link FakeMap}
 *
 * @author Kohsuke Kawaguchi
 */
public class FakeMapBuilder implements TestRule {
    private File dir;

    public FakeMapBuilder() {
    }

    public FakeMapBuilder add(int n, String id) throws IOException {
        verifyId(id);
        File build = new File(dir,id);
        build.mkdir();
        FileUtils.write(new File(build, "n"), Integer.toString(n));
        FileUtils.write(new File(build,"id"),id);
        return this;
    }

    /**
     * Adds a symlink from n to build id.
     *
     * (in test we should ideally create a symlink, but we fake the test
     * by actually making it a directory and staging the same data.)
     */
    public FakeMapBuilder addCache(int n, String id) throws IOException {
        return addBogusCache(n,n,id);
    }

    public FakeMapBuilder addBogusCache(int label, int actual, String id) throws IOException {
        verifyId(id);
        File build = new File(dir,Integer.toString(label));
        build.mkdir();
        FileUtils.write(new File(build, "n"), Integer.toString(actual));
        FileUtils.write(new File(build,"id"),id);
        return this;
    }

    public FakeMapBuilder addBoth(int n, String id) throws IOException {
        return add(n,id).addCache(n,id);
    }

    private void verifyId(String id) {
        try {
            Integer.parseInt(id);
            throw new IllegalMonitorStateException("ID cannot be a number");
        } catch (NumberFormatException e) {
            // OK
        }
    }

    /**
     * Adds a build record under the givn ID but make it unloadable,
     * which will cause a failure when a load is attempted on this build ID.
     */
    public FakeMapBuilder addUnloadable(String id) throws IOException {
        File build = new File(dir,id);
        build.mkdir();
        return this;
    }

    public FakeMapBuilder addUnloadableCache(int n) throws IOException {
        File build = new File(dir,String.valueOf(n));
        build.mkdir();
        return this;
    }

    public FakeMap make() {
        return new FakeMap(dir);
    }

    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                dir = File.createTempFile("lazyload","test");
                dir.delete();
                dir.mkdirs();
                try {
                    base.evaluate();
                } finally {
                    FileUtils.deleteDirectory(dir);
                }
            }
        };
    }
}