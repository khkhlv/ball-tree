package benchmark;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import ru.khkhlv.balltree.BallTree;
import ru.khkhlv.balltree.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
public class BallTreeBenchmark {

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(BallTreeBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
    private List<RealVector> vectors;
    private RealVector target;
    private BallTree ballTree;
    private Node root;

    @Setup
    public void setUp() {
        String fileName = "src/test/resources/dataset.txt";
        List<List<Double>> twoDList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\s+");
                List<Double> row = new ArrayList<>();
                for (String value : values) {
                    row.add(Double.parseDouble(value));
                }
                twoDList.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.vectors = twoDList.stream().map(doubleList -> new ArrayRealVector(doubleList.stream().mapToDouble(Double::doubleValue).toArray())).collect(Collectors.toList());
        target = vectors.get(ThreadLocalRandom.current().nextInt(0, vectors.size()));

        ballTree = new BallTree();
        root = ballTree.buildTree(vectors);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void testBallTree() {
       ballTree.searchTree(root, target, 3);
    }
}