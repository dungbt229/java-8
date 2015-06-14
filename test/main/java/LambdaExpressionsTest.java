import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.horstmann.java8.DoubleRunnable;
import com.horstmann.java8.LinkedList2;
import com.horstmann.java8.RunnableEx;
import com.horstmann.java8.Runner;

public class LambdaExpressionsTest {

    /**
     * Is the comparator code in the Arrays.sort method called in the same thread as the call to sort or a different thread?
     * 
     * <p>
     * Verifies the assumption that call to comparator is executed in the same thread.
     * </p>
     */
    @Test
    public void test1() {
	ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
	int beforeSort = thbean.getPeakThreadCount();
	Arrays.sort(Stream.iterate(1, x -> x + 1).limit(10000).toArray(Integer[]::new), Comparator.reverseOrder());
	int afterSort = thbean.getPeakThreadCount();

	// assert that number of threads did not increase because of sort
	Assert.assertThat(afterSort, Matchers.is(beforeSort));
    }

    /**
     * Using the listFiles( FileFilter) and isDirectory methods of the java.io.File class, write a method that returns all subdirectories of a given directory.
     * Use a lambda expression instead of a FileFilter object. Repeat with a method expression.
     * 
     * <p>
     * Asserts that filtering with FilterFilter object produces the same results as filtering with lambda expression or method reference.
     * </p>
     */
    @Test
    public void test2() {
	String root = System.getProperty("user.home");
	File[] subdirs1 = listSubdirsObject(root);
	File[] subdirs2 = listSubdirsLambda(root);
	File[] subdirs3 = listSubdirsMethodRef(root);

	Assert.assertThat(subdirs1, Matchers.is(subdirs2));
	Assert.assertThat(subdirs1, Matchers.is(subdirs3));
    }

    private static File[] listSubdirsObject(String root) {
	return new File(root).listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File pathname) {
		return pathname.isDirectory();
	    }
	});
    }

    private static File[] listSubdirsLambda(String root) {
	return new File(root).listFiles(x -> x.isDirectory());
    }

    private static File[] listSubdirsMethodRef(String root) {
	return new File(root).listFiles(File::isDirectory);
    }

    /**
     * Using the list( FilenameFilter) method of the java.io.File class, write a method that returns all files in a given directory with a given extension. Use
     * a lambda expression, not a FilenameFilter. Which variables from the enclosing scope does it capture?
     * 
     * <p>
     * Shows that lambda expression can access variables from the enclosing scope if they are effectively final.
     * </p>
     */
    @Test
    public void test3() {
	File root = Mockito.mock(File.class);
	Mockito.when(root.list()).thenReturn(new String[] { "file.txt", "game.exe", "scores.txt", "cv.doc" });
	Mockito.when(root.list(Mockito.any(FilenameFilter.class))).thenCallRealMethod();

	String extension = ".txt";
	String[] filtered = root.list((dir, name) -> name.endsWith(extension));
	Assert.assertThat(filtered, Matchers.arrayWithSize(2));
    }

    /**
     * Given an array of File objects, sort it so that the directories come before the files, and within each group, elements are sorted by path name. Use a
     * lambda expression, not a Comparator.
     */
    @Test
    public void test4() {
	File[] array = new File[10];
	char j = 'j';
	for (int i = 0; i < 10; i++) {
	    String name = String.valueOf(j--);
	    boolean isDir = i % 2 == 0;

	    array[i] = Mockito.mock(File.class);
	    Mockito.when(array[i].isDirectory()).thenReturn(isDir);
	    Mockito.when(array[i].getName()).thenReturn(name);
	    Mockito.when(array[i].toString()).thenReturn(name + " [" + (isDir ? "dir" : "file") + "]");
	}
	Arrays.sort(array, (a, b) -> {
	    if (a.isDirectory() && !b.isDirectory()) {
		return -1;
	    }
	    if (!a.isDirectory() && b.isDirectory()) {
		return 1;
	    }
	    return a.getName().compareTo(b.getName());
	});
	for (int i = 0; i < 10; i++) {
	    Assert.assertThat(array[i].isDirectory(), Matchers.is(i < 5));
	}
	for (int i = 1; i < 5; i++) {
	    Assert.assertThat(array[i], Matchers.greaterThan(array[i - 1]));
	}
	for (int i = 6; i < 10; i++) {
	    Assert.assertThat(array[i], Matchers.greaterThan(array[i - 1]));
	}
    }

    /**
     * Didn't you always hate it that you had to deal with checked exceptions in a Runnable? Write a method uncheck that catches all checked exceptions and
     * turns them into unchecked exceptions
     */
    @Test
    public void test6() {
	new Thread(RunnableEx.uncheck(() -> {
	    Thread.sleep(1000);
	})).start();
	// Look, no catch (InterruptedException)!
    }

    /**
     * Write a static method andThen that takes as parameters two Runnable instances and returns a Runnable that runs the first, then the second. In the main
     * method, pass two lambda expressions into a call to andThen, and run the returned instance.
     */
    @Test
    public void test7() {
	LongAdder sum = new LongAdder();
	Thread t = new Thread(DoubleRunnable.andThen(() -> sum.increment(), () -> sum.add(2l)));
	t.start();
	try {
	    t.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    Assert.fail();
	}
	Assert.assertThat(sum.longValue(), Matchers.is(3l));
    }

    /**
     * What happens when a lambda expression captures values in an enhanced for loop? Is it legal? Does each lambda expression capture a different value, or do
     * they all get the last value? What happens if you use a traditional loop?
     */
    @Test
    public void test8() {
	String[] names = { "Peter", "Paul", "Mary" };
	List<Runner> runners = new ArrayList<>();
	for (String name : names) {
	    runners.add(() -> name);
	}
	String[] runnersNames = runners.stream().map(Runner::name).toArray(String[]::new);
	Assert.assertThat(runnersNames, Matchers.is(names));
	
	// traditional loop: 
	// for (int i = 0; i < names.length; i++) {
	// runners.add(() -> names[i]);
	// }
	// does not compile: variable i is not effectively final
    }
    
    /**
     * Form a subclass Collection2 from Collection and add a default method void forEachIf(Consumer<T> action, Predicate<T> filter) that applies action to each
     * element for which filter returns true. How could you use it?
     */
    @Test
    public void test9() {
	LinkedList2<List<String>> mailingLists = new LinkedList2<>();
	mailingLists.add(Stream.of("Peter", "Paul", "Mary").collect(Collectors.toList()));
	mailingLists.add(Stream.of("John", "Victor", "Anna").collect(Collectors.toList()));
	mailingLists.add(Stream.of("Peter", "Mary", "Anna").collect(Collectors.toList()));
	mailingLists.add(Stream.of("Victor").collect(Collectors.toList()));
	
	// add new employee, April, to the same mailing lists as Mary:
	mailingLists.forEachIf(x -> x.add("April"), x -> x.contains("Mary"));
	
	Assert.assertThat(mailingLists.get(0), Matchers.hasItem("April"));
	Assert.assertThat(mailingLists.get(1), Matchers.not(Matchers.hasItem("April")));
	Assert.assertThat(mailingLists.get(2), Matchers.hasItem("April"));
	Assert.assertThat(mailingLists.get(3), Matchers.not(Matchers.hasItem("April")));
	
    }
}
